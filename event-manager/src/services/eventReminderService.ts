import { supabase } from '../lib/supabase';

export type EventReminderConfig = {
  id?: number;
  eventId: number;
  isEnabled: boolean;
  leadMinutes: number;
  channel: string;
  bodyTemplate: string;
  sortOrder: number;
};

type EventReminderRow = {
  id: number;
  event_id: number;
  is_enabled: boolean | null;
  lead_minutes: number | null;
  channel: string | null;
  body_template: string | null;
  sort_order: number | null;
};

const DEFAULT_LEAD_MINUTES = 15;
const MAX_LEAD_MINUTES = 24 * 60;

const normalizeLeadMinutes = (value: unknown): number => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    const parsed = Math.floor(value);
    if (parsed < 0) return 0;
    if (parsed > MAX_LEAD_MINUTES) return MAX_LEAD_MINUTES;
    return parsed;
  }

  if (typeof value === 'string') {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed)) {
      if (parsed < 0) return 0;
      if (parsed > MAX_LEAD_MINUTES) return MAX_LEAD_MINUTES;
      return parsed;
    }
  }

  return DEFAULT_LEAD_MINUTES;
};

const toReminderConfig = (row: EventReminderRow): EventReminderConfig => ({
  id: row.id,
  eventId: row.event_id,
  isEnabled: row.is_enabled !== false,
  leadMinutes: normalizeLeadMinutes(row.lead_minutes),
  channel: row.channel?.trim() || '',
  bodyTemplate: row.body_template?.trim() || '',
  sortOrder: typeof row.sort_order === 'number' ? row.sort_order : 0,
});

export const eventReminderService = {
  getEventReminders: async (eventId: number): Promise<EventReminderConfig[]> => {
    const { data, error } = await supabase
      .from('event_notification_reminders')
      .select('id, event_id, is_enabled, lead_minutes, channel, body_template, sort_order')
      .eq('event_id', eventId)
      .order('sort_order', { ascending: true })
      .order('id', { ascending: true });

    if (error) {
      throw error;
    }

    return (data || []).map((row) => toReminderConfig(row as EventReminderRow));
  },

  replaceEventReminders: async (eventId: number, reminders: EventReminderConfig[]): Promise<EventReminderConfig[]> => {
    const { error: deleteError } = await supabase.from('event_notification_reminders').delete().eq('event_id', eventId);

    if (deleteError) {
      throw deleteError;
    }

    if (!reminders.length) {
      return [];
    }

    const payload = reminders.map((reminder, index) => ({
      event_id: eventId,
      is_enabled: reminder.isEnabled !== false,
      lead_minutes: normalizeLeadMinutes(reminder.leadMinutes),
      channel: reminder.channel.trim() || null,
      body_template: reminder.bodyTemplate.trim() || null,
      sort_order: index,
    }));

    const { data, error } = await supabase
      .from('event_notification_reminders')
      .insert(payload)
      .select('id, event_id, is_enabled, lead_minutes, channel, body_template, sort_order')
      .order('sort_order', { ascending: true })
      .order('id', { ascending: true });

    if (error) {
      throw error;
    }

    return (data || []).map((row) => toReminderConfig(row as EventReminderRow));
  },
};
