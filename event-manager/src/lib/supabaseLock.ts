type LockOperation = <R>(name: string, acquireTimeout: number, fn: () => Promise<R>) => Promise<R>;

const lockQueues = new Map<string, Promise<void>>();

/**
 * Supabase Auth lock implementation that serializes auth operations within this tab.
 * This avoids navigator.locks "steal" recovery aborts surfacing in the UI.
 */
export const inMemoryAuthLock: LockOperation = async <R>(name: string, _acquireTimeout: number, fn: () => Promise<R>) => {
  const previous = lockQueues.get(name) ?? Promise.resolve();

  let release: () => void = () => undefined;
  const current = new Promise<void>((resolve) => {
    release = resolve;
  });

  lockQueues.set(name, previous.then(() => current, () => current));

  await previous.catch(() => undefined);

  try {
    return await fn();
  } finally {
    release();
    if (lockQueues.get(name) === current) {
      lockQueues.delete(name);
    }
  }
};
