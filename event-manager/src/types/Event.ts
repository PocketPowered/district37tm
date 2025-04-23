export interface AgendaItem {
  title?: string;
  description?: string;
  time?: string;
  locationInfo?: string;
}

export interface ExternalLink {
  displayName?: string;
  url?: string;
}

export interface Event {
  id?: number;
  title: string;
  images?: string[];
  time: string;
  locationInfo: string;
  description?: string;
  agenda?: AgendaItem[];
  additionalLinks?: ExternalLink[];
  dateKey?: string;
}

export interface EventPreview {
  id: number;
  title: string;
  image?: string;
  time: string;
  locationInfo: string;
} 