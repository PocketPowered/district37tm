export interface TimeRange {
  startTime: number;
  endTime: number;
}

export interface AgendaItem {
  title: string;
  description: string;
  time: TimeRange;
  locationInfo: string;
}

export interface ExternalLink {
  displayName: string;
  url: string;
}

export interface Event {
  id: number;
  title: string;
  description: string;
  images: string[];
  time: TimeRange;
  locationInfo: string;
  agenda: AgendaItem[];
  additionalLinks: ExternalLink[];
  dateKey: string;
}

export interface EventPreview {
  id: number;
  title: string;
  image?: string;
  time: string;
  locationInfo: string;
}

// Helper function to format epoch timestamp to local time string
export const formatEventTime = (timestamp: number): string => {
  const date = new Date(timestamp);
  return date.toLocaleString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone: 'America/New_York'
  });
};

// Helper function to format time range
export const formatTimeRange = (startTime: number, endTime: number): string => {
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  const startStr = start.toLocaleString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone: 'America/New_York'
  });
  
  const endStr = end.toLocaleString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone: 'America/New_York'
  });
  
  return `${startStr} - ${endStr}`;
}; 