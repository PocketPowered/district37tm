type DateKeyInput = number | string;

const toDateKeyNumber = (dateKey: DateKeyInput): number => {
  const parsed = Number(dateKey);
  return Number.isFinite(parsed) ? parsed : Date.now();
};

export const getUtcDateKeyParts = (dateKey: DateKeyInput) => {
  const parsedDate = new Date(toDateKeyNumber(dateKey));

  return {
    year: parsedDate.getUTCFullYear(),
    month: parsedDate.getUTCMonth(),
    day: parsedDate.getUTCDate(),
  };
};

export const formatDateKey = (
  dateKey: DateKeyInput,
  options: Intl.DateTimeFormatOptions = {},
): string => {
  return new Intl.DateTimeFormat('en-US', {
    timeZone: 'UTC',
    ...options,
  }).format(new Date(toDateKeyNumber(dateKey)));
};

export const mergeDateKeyWithLocalTime = (
  dateKey: DateKeyInput,
  sourceTimestamp: number,
): number => {
  const sourceTime = new Date(sourceTimestamp);
  const { year, month, day } = getUtcDateKeyParts(dateKey);

  return new Date(
    year,
    month,
    day,
    sourceTime.getHours(),
    sourceTime.getMinutes(),
    sourceTime.getSeconds(),
    sourceTime.getMilliseconds(),
  ).getTime();
};

export const createUtcDateKeyFromDate = (date: Date): number => {
  return Date.UTC(date.getFullYear(), date.getMonth(), date.getDate());
};
