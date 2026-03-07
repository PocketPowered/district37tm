import { supabase } from '../lib/supabase';

export const ADMIN_IMAGE_BUCKET = 'admin-images';
export const MAX_IMAGE_UPLOAD_BYTES = 5 * 1024 * 1024;
export const ACCEPTED_IMAGE_MIME_TYPES = ['image/png', 'image/jpeg', 'image/webp'] as const;
export const ACCEPTED_IMAGE_FILE_INPUT = ACCEPTED_IMAGE_MIME_TYPES.join(',');

type UploadImageCategory = 'events' | 'locations';

type UploadAdminImageParams = {
  file: File;
  conferenceId: number;
  category: UploadImageCategory;
};

const randomId = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().slice(0, 12);
  }
  return Math.random().toString(36).slice(2, 14);
};

const sanitizeFileName = (name: string): string => {
  return name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9._-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^[-_.]+|[-_.]+$/g, '');
};

const validateFile = (file: File): void => {
  if (!ACCEPTED_IMAGE_MIME_TYPES.includes(file.type as (typeof ACCEPTED_IMAGE_MIME_TYPES)[number])) {
    throw new Error('Only PNG, JPEG, and WebP images are supported.');
  }

  if (file.size > MAX_IMAGE_UPLOAD_BYTES) {
    throw new Error('Image must be 5 MB or smaller.');
  }
};

const buildPath = (file: File, conferenceId: number, category: UploadImageCategory): string => {
  const sanitizedName = sanitizeFileName(file.name) || 'upload-image';
  return `${conferenceId}/${category}/${Date.now()}-${randomId()}-${sanitizedName}`;
};

export const imageUploadService = {
  async uploadAdminImage({ file, conferenceId, category }: UploadAdminImageParams): Promise<string> {
    validateFile(file);
    const path = buildPath(file, conferenceId, category);

    const { error } = await supabase.storage.from(ADMIN_IMAGE_BUCKET).upload(path, file, {
      cacheControl: '3600',
      contentType: file.type,
      upsert: false,
    });

    if (error) {
      throw new Error(`Failed to upload image: ${error.message}`);
    }

    const { data } = supabase.storage.from(ADMIN_IMAGE_BUCKET).getPublicUrl(path);
    const publicUrl = data.publicUrl?.trim();
    if (!publicUrl) {
      throw new Error('Image uploaded, but public URL could not be generated.');
    }

    return publicUrl;
  },
};
