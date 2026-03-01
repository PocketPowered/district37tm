import { supabase } from '../lib/supabase';

export const isUserAuthorized = async (email: string): Promise<boolean> => {
  const normalizedEmail = email.trim().toLowerCase();

  try {
    const { data: isAuthorizedEmail, error: emailRpcError } = await supabase.rpc('is_authorized_email', {
      p_email: normalizedEmail,
    });
    if (!emailRpcError && typeof isAuthorizedEmail === 'boolean') {
      return isAuthorizedEmail;
    }

    const { data: isAuthorizedAdmin, error: rpcError } = await supabase.rpc('is_authorized_admin');
    if (!rpcError && typeof isAuthorizedAdmin === 'boolean') {
      return isAuthorizedAdmin;
    }

    const { data, error } = await supabase
      .from('authorized_users')
      .select('email')
      .eq('email', normalizedEmail)
      .maybeSingle();
    if (error) {
      console.error('Error checking user authorization:', error);
      return false;
    }
    return Boolean(data?.email);
  } catch (error) {
    console.error('Error checking user authorization:', error);
    return false;
  }
};
