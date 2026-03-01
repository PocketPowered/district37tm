export type NotificationTarget = 'GENERAL' | 'APP_ENV_DEBUG' | 'APP_ENV_PROD' | 'APP_VERSION' | 'CUSTOM';

export interface NotificationTargetOption {
  value: NotificationTarget;
  label: string;
}

export interface NotificationTopicSelection {
  target: NotificationTarget;
  version: string;
  customTopic: string;
}

const PRESET_TOPIC_TARGETS: NotificationTarget[] = ['GENERAL', 'APP_ENV_DEBUG', 'APP_ENV_PROD'];

export const NOTIFICATION_TARGET_OPTIONS: NotificationTargetOption[] = [
  { value: 'GENERAL', label: 'All users (GENERAL)' },
  { value: 'APP_ENV_DEBUG', label: 'Debug builds (APP_ENV_DEBUG)' },
  { value: 'APP_ENV_PROD', label: 'Production builds (APP_ENV_PROD)' },
  { value: 'APP_VERSION', label: 'Specific app version' },
  { value: 'CUSTOM', label: 'Custom topic' },
];

export const NOTIFICATION_TARGET_HELPER_TEXT =
  'Clients subscribe to GENERAL + environment + version topics.';

const normalizeTopicSegment = (raw: string): string => {
  const trimmed = raw.trim();
  if (!trimmed) {
    return '';
  }

  return trimmed
    .replace(/\./g, '_')
    .replace(/[^A-Za-z0-9_-]/g, '_');
};

export const resolveNotificationTopic = ({ target, version, customTopic }: NotificationTopicSelection): string => {
  if (target === 'APP_VERSION') {
    const normalizedVersion = normalizeTopicSegment(version);
    return normalizedVersion ? `APP_VERSION_${normalizedVersion}` : '';
  }

  if (target === 'CUSTOM') {
    return customTopic.trim().toUpperCase();
  }

  return target;
};

export const parseNotificationTopic = (topic: string): NotificationTopicSelection => {
  const trimmed = topic.trim();
  const upperTopic = trimmed.toUpperCase();

  if (!trimmed || upperTopic === 'GENERAL') {
    return {
      target: 'GENERAL',
      version: '',
      customTopic: '',
    };
  }

  if (PRESET_TOPIC_TARGETS.includes(upperTopic as NotificationTarget)) {
    return {
      target: upperTopic as NotificationTarget,
      version: '',
      customTopic: '',
    };
  }

  if (upperTopic === 'APP_VERSION') {
    return {
      target: 'APP_VERSION',
      version: '',
      customTopic: '',
    };
  }

  if (upperTopic.startsWith('APP_VERSION_')) {
    return {
      target: 'APP_VERSION',
      version: upperTopic.slice('APP_VERSION_'.length).replace(/_/g, '.'),
      customTopic: '',
    };
  }

  return {
    target: 'CUSTOM',
    version: '',
    customTopic: trimmed,
  };
};
