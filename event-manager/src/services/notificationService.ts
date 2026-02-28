import { supabase } from '../lib/supabase';
import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from '../config/supabase';

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

    const response = await fetch(`${SUPABASE_URL}/functions/v1/send-notification-v2`, {
      method: 'POST',
      headers: {
        apikey: SUPABASE_PUBLISHABLE_KEY,
        'Content-Type': 'application/json',
        Authorization: `Bearer ${session.access_token}`,
      },
      body: JSON.stringify({
        title,
        body,
        topic: topic || 'GENERAL',
      }),
    });

    let payload: unknown = null;
    try {
      payload = await response.json();
    } catch {
      payload = null;
    }

    if (!response.ok) {
      const backendMessage =
        payload && typeof payload === 'object' && 'error' in payload
          ? String((payload as { error: unknown }).error)
          : `Edge Function returned ${response.status}`;
      throw new Error(backendMessage);
    }

    return payload;
  },
};
