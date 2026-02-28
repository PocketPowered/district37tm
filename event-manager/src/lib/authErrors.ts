const LOCK_STEAL_ERROR_FRAGMENT = "Lock broken by another request with the 'steal' option";

const extractErrorMessage = (value: unknown): string | null => {
  if (typeof value === 'string') {
    return value;
  }

  if (!value || typeof value !== 'object') {
    return null;
  }

  const maybeMessage = (value as { message?: unknown }).message;
  if (typeof maybeMessage === 'string') {
    return maybeMessage;
  }

  return null;
};

export const isSupabaseLockStealAbortError = (error: unknown): boolean => {
  const directMessage = extractErrorMessage(error);
  if (directMessage?.includes(LOCK_STEAL_ERROR_FRAGMENT)) {
    return true;
  }

  if (!error || typeof error !== 'object') {
    return false;
  }

  const reasonMessage = extractErrorMessage((error as { reason?: unknown }).reason);
  return reasonMessage?.includes(LOCK_STEAL_ERROR_FRAGMENT) ?? false;
};
