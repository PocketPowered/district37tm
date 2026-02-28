import { Event, EventTag, ExternalLink } from '../types/Event';
import { BackendExternalLink } from '../types/BackendExternalLink';
import { Location } from '../types/Location';

type EventRow = {
  id: number;
  title: string | null;
  description: string | null;
  images: string[] | null;
  start_time: number | null;
  end_time: number | null;
  location_info: string | null;
  notify_before: boolean | null;
  notification_lead_minutes: number | null;
  additional_links: ExternalLink[] | null;
  date_key: number | null;
  tag: EventTag | null;
};

const DEFAULT_NOTIFICATION_LEAD_MINUTES = 15;
const MAX_NOTIFICATION_LEAD_MINUTES = 24 * 60;

const toPositiveInteger = (value: unknown, fallback: number): number => {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return Math.floor(value);
  }

  if (typeof value === 'string') {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed) && parsed > 0) {
      return parsed;
    }
  }

  return fallback;
};

const normalizeNotificationLeadMinutes = (value: unknown): number => {
  const parsed = toPositiveInteger(value, DEFAULT_NOTIFICATION_LEAD_MINUTES);
  if (parsed > MAX_NOTIFICATION_LEAD_MINUTES) {
    return DEFAULT_NOTIFICATION_LEAD_MINUTES;
  }
  return parsed;
};

type ResourceRow = {
  id: string;
  display_name: string | null;
  url: string | null;
  description: string | null;
  resource_type: string;
};

type LocationRow = {
  id: string;
  location_name: string;
  location_images: string[] | null;
};

export const toEvent = (row: EventRow): Event => ({
  id: row.id,
  title: row.title || '',
  description: row.description || '',
  images: row.images || [],
  time: {
    startTime: row.start_time || Date.now(),
    endTime: row.end_time || Date.now(),
  },
  locationInfo: row.location_info || '',
  notifyBefore: row.notify_before === true,
  notificationLeadMinutes: normalizeNotificationLeadMinutes(row.notification_lead_minutes),
  additionalLinks: row.additional_links || [],
  dateKey: (row.date_key || 0).toString(),
  tag: row.tag || EventTag.NORMAL,
});

export const toEventInsert = (event: Event) => ({
  id: event.id > 0 ? event.id : undefined,
  title: event.title,
  description: event.description,
  images: event.images || [],
  start_time: event.time?.startTime || null,
  end_time: event.time?.endTime || null,
  location_info: event.locationInfo,
  notify_before: event.notifyBefore === true,
  notification_lead_minutes: normalizeNotificationLeadMinutes(event.notificationLeadMinutes),
  additional_links: event.additionalLinks || [],
  date_key: event.dateKey ? Number(event.dateKey) : null,
  tag: event.tag || EventTag.NORMAL,
});

export const toLocation = (row: LocationRow): Location => ({
  id: row.id,
  locationName: row.location_name,
  locationImages: row.location_images || [],
});

export const toLocationInsert = (location: Location) => ({
  location_name: location.locationName,
  location_images: location.locationImages || [],
});

export const toResource = (row: ResourceRow): BackendExternalLink => ({
  id: row.id,
  displayName: row.display_name || '',
  url: row.url || '',
  description: row.description,
  resourceType: row.resource_type,
});

export const toResourceInsert = (resource: BackendExternalLink, resourceType: string) => ({
  display_name: resource.displayName || '',
  url: resource.url || '',
  description: resource.description || null,
  resource_type: resourceType,
});
