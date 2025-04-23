export interface Event {
  id: number;
  title: string;
  images?: string[];
  time: string;
  locationInfo: string;
  description?: string;
  agenda?: string;
  additionalLinks?: string[];
  dateKey?: string;
}

export interface EventPreview {
  id: number;
  title: string;
  image?: string;
  time: string;
  locationInfo: string;
} 