import { supabase } from '../lib/supabase';
import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from '../config/supabase';

type EdgeFunctionResult = {
  response: Response;
  payload: unknown;
  functionName: string;
};

export type NotificationSendResponse = {
  ok: boolean;
  scheduled?: boolean;
  notificationId?: string;
  topic?: string;
  scheduledFor?: string;
};

export type NotificationHistoryItem = {
  id: string;
  title: string;
  body: string;
  topic: string;
  status: string;
  createdBy: string;
  createdAt: string;
  scheduledFor: string | null;
  sentAt: string | null;
  source: string | null;
};

export type NotificationHistoryPage = {
  items: NotificationHistoryItem[];
  totalCount: number;
};

const getValidAccessToken = async (): Promise<string> => {
  const {
    data: { session },
    error: sessionError,
  } = await supabase.auth.getSession();
  if (sessionError) {
    throw sessionError;
  }

  let resolvedSession = session;
  const nowSeconds = Math.floor(Date.now() / 1000);
  const shouldRefresh =
    !resolvedSession?.access_token ||
    !resolvedSession.expires_at ||
    resolvedSession.expires_at <= nowSeconds + 30;

  if (shouldRefresh) {
    const { data: refreshed, error: refreshError } = await supabase.auth.refreshSession();
    if (refreshError) {
      throw new Error('Your admin session expired. Please sign in again.');
    }
    resolvedSession = refreshed.session;
  }

  if (!resolvedSession?.access_token) {
    throw new Error('Your admin session expired. Please sign in again.');
  }

  return resolvedSession.access_token;
};

const callNotificationFunction = async (
  functionName: string,
  accessToken: string,
  payload: { title: string; body: string; topic: string; scheduledFor?: string }
): Promise<EdgeFunctionResult> => {
  const response = await fetch(`${SUPABASE_URL}/functions/v1/${functionName}`, {
    method: 'POST',
    headers: {
      apikey: SUPABASE_PUBLISHABLE_KEY,
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(payload),
  });

  let parsedPayload: unknown = null;
  try {
    parsedPayload = await response.json();
  } catch {
    parsedPayload = null;
  }

  return { response, payload: parsedPayload, functionName };
};

export const notificationService = {
  sendNotification: async (
    title: string,
    body: string,
    topic: string,
    scheduledFor?: string
  ): Promise<NotificationSendResponse> => {
    const requestPayload = {
      title,
      body,
      topic: topic || 'GENERAL',
      ...(scheduledFor ? { scheduledFor } : {}),
    };

    const candidates = ['send-notification', 'send-notification-v2'];
    const tryCandidates = async (accessToken: string): Promise<EdgeFunctionResult | null> => {
      let result: EdgeFunctionResult | null = null;
      for (let i = 0; i < candidates.length; i += 1) {
        const functionName = candidates[i];
        const attempted = await callNotificationFunction(functionName, accessToken, requestPayload);
        const canTryNext =
          i < candidates.length - 1 &&
          (attempted.response.status === 404 ||
            attempted.response.status === 401 ||
            attempted.response.status === 403);

        if (canTryNext) {
          continue;
        }

        result = attempted;
        break;
      }
      return result;
    };

    let accessToken = await getValidAccessToken();
    let result = await tryCandidates(accessToken);

    // Handle stale token races by forcing one refresh and retrying once.
    if (result?.response.status === 401) {
      const { data: refreshed, error: refreshError } = await supabase.auth.refreshSession();
      if (!refreshError && refreshed.session?.access_token) {
        accessToken = refreshed.session.access_token;
        result = await tryCandidates(accessToken);
      }
    }

    if (!result) {
      throw new Error(
        'Notification function is not deployed (tried send-notification and send-notification-v2).',
      );
    }

    if (!result.response.ok) {
      const backendMessage =
        result.payload && typeof result.payload === 'object' && 'error' in result.payload
          ? String((result.payload as { error: unknown }).error)
          : `Edge Function ${result.functionName} returned ${result.response.status}`;
      throw new Error(`[${result.functionName}] ${backendMessage}`);
    }

    return (result.payload || { ok: true }) as NotificationSendResponse;
  },

  getNotificationHistory: async (page: number, pageSize: number): Promise<NotificationHistoryPage> => {
    const safePage = Number.isFinite(page) && page >= 0 ? Math.floor(page) : 0;
    const safePageSize = Number.isFinite(pageSize) && pageSize > 0 ? Math.floor(pageSize) : 10;
    const from = safePage * safePageSize;
    const to = from + safePageSize - 1;

    const { data, count, error } = await supabase
      .from('notifications')
      .select('id, title, body, topic, status, created_by, created_at, scheduled_for, sent_at, source', { count: 'exact' })
      .or('status.eq.sent,status.eq.queued')
      .order('created_at', { ascending: false })
      .range(from, to);

    if (error) {
      throw error;
    }

    const items: NotificationHistoryItem[] = (data || []).map((row) => ({
      id: row.id,
      title: row.title,
      body: row.body,
      topic: row.topic,
      status: row.status,
      createdBy: row.created_by || 'unknown',
      createdAt: row.created_at,
      scheduledFor: row.scheduled_for || null,
      sentAt: row.sent_at || null,
      source: row.source || null,
    }));

    return {
      items,
      totalCount: count || 0,
    };
  },
};
