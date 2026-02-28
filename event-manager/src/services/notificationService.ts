import { supabase } from '../lib/supabase';
import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from '../config/supabase';

type EdgeFunctionResult = {
  response: Response;
  payload: unknown;
  functionName: string;
};

const callNotificationFunction = async (
  functionName: string,
  accessToken: string,
  payload: { title: string; body: string; topic: string }
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
  sendNotification: async (title: string, body: string, topic: string) => {
    const {
      data: { session },
      error: sessionError,
    } = await supabase.auth.getSession();
    if (sessionError) {
      throw sessionError;
    }
    if (!session?.access_token) {
      throw new Error('Your admin session expired. Please sign in again.');
    }

    const requestPayload = {
      title,
      body,
      topic: topic || 'GENERAL',
    };

    const candidates = ['send-notification-v2', 'send-notification'];
    let result: EdgeFunctionResult | null = null;

    for (const functionName of candidates) {
      const attempted = await callNotificationFunction(functionName, session.access_token, requestPayload);
      if (attempted.response.status !== 404) {
        result = attempted;
        break;
      }
    }

    if (!result) {
      throw new Error('Notification function is not deployed (tried send-notification-v2 and send-notification).');
    }

    if (!result.response.ok) {
      const backendMessage =
        result.payload && typeof result.payload === 'object' && 'error' in result.payload
          ? String((result.payload as { error: unknown }).error)
          : `Edge Function ${result.functionName} returned ${result.response.status}`;
      throw new Error(backendMessage);
    }

    return result.payload;
  },
};
