export interface Conference {
  id: number;
  slug: string;
  name: string;
  scheduleTitle: string;
  appHeaderTitle: string | null;
  startDate: string | null;
  endDate: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ConferenceUpsertInput {
  slug: string;
  name: string;
  scheduleTitle?: string;
  appHeaderTitle?: string | null;
  startDate?: string | null;
  endDate?: string | null;
}

export interface ConferenceDateRange {
  minDateKey: number;
  maxDateKey: number;
}

const toUtcDateKey = (dateIso: string): number | null => {
  const parsed = new Date(`${dateIso}T00:00:00.000Z`);
  const timestamp = parsed.getTime();
  return Number.isFinite(timestamp) ? timestamp : null;
};

export const getConferenceDateRange = (conference: Conference | null): ConferenceDateRange | null => {
  if (!conference?.startDate || !conference.endDate) {
    return null;
  }

  const minDateKey = toUtcDateKey(conference.startDate);
  const maxDateKey = toUtcDateKey(conference.endDate);
  if (minDateKey === null || maxDateKey === null || minDateKey > maxDateKey) {
    return null;
  }

  return {
    minDateKey,
    maxDateKey,
  };
};
