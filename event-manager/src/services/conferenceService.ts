import { supabase } from '../lib/supabase';
import { Conference, ConferenceUpsertInput } from '../types/Conference';

type ConferenceRow = {
  id: number;
  slug: string | null;
  name: string | null;
  schedule_title: string | null;
  app_header_title: string | null;
  start_date: string | null;
  end_date: string | null;
  is_active: boolean | null;
  created_at: string | null;
  updated_at: string | null;
};

const conferenceFields =
  'id, slug, name, schedule_title, app_header_title, start_date, end_date, is_active, created_at, updated_at';

const slugPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

const sanitizeSlug = (value: string): string => {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .replace(/-+/g, '-');
};

const normalizeDateField = (value: string | null | undefined): string | null => {
  if (!value) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
};

const normalizeTextField = (value: string | null | undefined): string | null => {
  if (!value) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
};

const toConference = (row: ConferenceRow): Conference => {
  return {
    id: row.id,
    slug: row.slug?.trim() || '',
    name: row.name?.trim() || '',
    scheduleTitle: row.schedule_title?.trim() || row.name?.trim() || '',
    appHeaderTitle: row.app_header_title?.trim() || null,
    startDate: row.start_date,
    endDate: row.end_date,
    isActive: row.is_active === true,
    createdAt: row.created_at || '',
    updatedAt: row.updated_at || '',
  };
};

const toConferenceInsert = (input: ConferenceUpsertInput) => {
  const slug = sanitizeSlug(input.slug);
  const name = input.name.trim();
  const scheduleTitle = input.scheduleTitle?.trim() || name;
  const appHeaderTitle = normalizeTextField(input.appHeaderTitle);
  const startDate = normalizeDateField(input.startDate);
  const endDate = normalizeDateField(input.endDate);

  if (!slugPattern.test(slug)) {
    throw new Error('Conference slug is invalid. Use letters, numbers, and hyphens.');
  }

  if (!name) {
    throw new Error('Conference name is required.');
  }

  if (startDate && endDate && startDate > endDate) {
    throw new Error('Conference end date must be on or after the start date.');
  }

  return {
    slug,
    name,
    schedule_title: scheduleTitle,
    app_header_title: appHeaderTitle,
    start_date: startDate,
    end_date: endDate,
  };
};

export const conferenceService = {
  sanitizeSlug,

  async getConferences(): Promise<Conference[]> {
    const { data, error } = await supabase
      .from('conferences')
      .select(conferenceFields)
      .order('start_date', { ascending: false, nullsFirst: false })
      .order('name', { ascending: true })
      .order('id', { ascending: true });

    if (error) throw error;
    return (data || []).map((row) => toConference(row as ConferenceRow));
  },

  async createConference(input: ConferenceUpsertInput): Promise<Conference> {
    const payload = toConferenceInsert(input);
    const { data, error } = await supabase
      .from('conferences')
      .insert(payload)
      .select(conferenceFields)
      .single();

    if (error) throw error;
    return toConference(data as ConferenceRow);
  },

  async updateConference(id: number, input: ConferenceUpsertInput): Promise<Conference> {
    const payload = toConferenceInsert(input);
    const { data, error } = await supabase
      .from('conferences')
      .update(payload)
      .eq('id', id)
      .select(conferenceFields)
      .single();

    if (error) throw error;
    return toConference(data as ConferenceRow);
  },

  async setActiveConference(id: number): Promise<void> {
    const now = new Date().toISOString();

    const { error: deactivateError } = await supabase
      .from('conferences')
      .update({ is_active: false, updated_at: now })
      .neq('id', id)
      .eq('is_active', true);

    if (deactivateError) throw deactivateError;

    const { error: activateError } = await supabase
      .from('conferences')
      .update({ is_active: true, updated_at: now })
      .eq('id', id);

    if (activateError) throw activateError;
  },
};
