export const IMAGE_FALLBACK_SRC = `data:image/svg+xml;utf8,${encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360" viewBox="0 0 640 360"><rect width="640" height="360" fill="#f3f4f6"/><g fill="#6b7280" font-family="Arial, sans-serif" text-anchor="middle"><text x="320" y="170" font-size="22">Image unavailable</text><text x="320" y="200" font-size="14">The provided URL could not be loaded.</text></g></svg>',
)}`;

export const handleImageLoadError = (target: HTMLImageElement) => {
  // Prevent repeated fallback attempts when the source cannot be loaded.
  target.onerror = null;
  target.src = IMAGE_FALLBACK_SRC;
};
