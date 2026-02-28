import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { User } from '@supabase/supabase-js';
import { supabase } from '../lib/supabase';
import { isSupabaseLockStealAbortError } from '../lib/authErrors';
import { isUserAuthorized } from '../services/authService';

interface AuthContextType {
  currentUser: User | null;
  loading: boolean;
  isAuthorized: boolean;
  signInWithGoogle: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);
const AUTH_INIT_FAIL_SAFE_MS = 15000;
const AUTH_REDIRECT_URL_OVERRIDE = process.env.REACT_APP_AUTH_REDIRECT_URL?.trim();

const ensureTrailingSlash = (value: string) => (value.endsWith('/') ? value : `${value}/`);

const getAuthRedirectUrl = (): string => {
  if (AUTH_REDIRECT_URL_OVERRIDE) {
    return ensureTrailingSlash(AUTH_REDIRECT_URL_OVERRIDE);
  }

  const publicUrl = process.env.PUBLIC_URL?.trim();
  if (publicUrl) {
    try {
      return ensureTrailingSlash(new URL(publicUrl, window.location.origin).toString());
    } catch {
      // Fall through to runtime origin/path detection below.
    }
  }

  const currentPath = window.location.pathname || '/';
  return ensureTrailingSlash(`${window.location.origin}${currentPath}`);
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [isAuthorized, setIsAuthorized] = useState(false);
  const authorizationCheckRef = useRef<{ email: string; promise: Promise<boolean> } | null>(null);

  const runAuthorizationCheck = useCallback((email: string): Promise<boolean> => {
    if (authorizationCheckRef.current?.email === email) {
      return authorizationCheckRef.current.promise;
    }

    const promise = (async () => {
      try {
        return await isUserAuthorized(email);
      } catch (error) {
        console.error('Error checking user authorization:', error);
        return false;
      }
    })();

    authorizationCheckRef.current = { email, promise };
    promise.finally(() => {
      if (authorizationCheckRef.current?.promise === promise) {
        authorizationCheckRef.current = null;
      }
    });

    return promise;
  }, []);

  const checkAuthorization = useCallback(async (user: User | null) => {
    if (!user?.email) {
      setIsAuthorized(false);
      return;
    }

    const authorized = await runAuthorizationCheck(user.email);
    setIsAuthorized(authorized);
  }, [runAuthorizationCheck]);

  const signInWithGoogle = async () => {
    try {
      const redirectTo = getAuthRedirectUrl();
      const { error } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: { redirectTo },
      });
      if (error) throw error;
    } catch (error) {
      if (isSupabaseLockStealAbortError(error)) {
        return;
      }
      console.error('Error signing in with Google:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      const { error } = await supabase.auth.signOut();
      if (error) throw error;
      setIsAuthorized(false);
    } catch (error) {
      console.error('Error signing out:', error);
      throw error;
    }
  };

  useEffect(() => {
    let mounted = true;
    const loadingFailSafe = window.setTimeout(() => {
      if (mounted) {
        setLoading(false);
      }
    }, AUTH_INIT_FAIL_SAFE_MS);

    const initializeAuth = async () => {
      try {
        setLoading(true);
        const { data } = await supabase.auth.getSession();
        if (!mounted) return;
        const user = data.session?.user || null;
        setCurrentUser(user);
        if (!user) {
          setIsAuthorized(false);
          setLoading(false);
        }
      } catch (error) {
        console.error('Error initializing auth:', error);
        if (!mounted) return;
        setCurrentUser(null);
        setIsAuthorized(false);
        setLoading(false);
      }
    };

    initializeAuth();

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(async (event, session) => {
      const user = session?.user || null;
      const shouldCheckAuthorization =
        event === 'SIGNED_IN' || event === 'INITIAL_SESSION' || event === 'TOKEN_REFRESHED';

      try {
        setCurrentUser(user);
        if (!user) {
          setIsAuthorized(false);
          setLoading(false);
          return;
        }

        if (!shouldCheckAuthorization) {
          return;
        }

        setLoading(true);
        await checkAuthorization(user);
      } catch (error) {
        console.error('Error handling auth state change:', error);
        setCurrentUser(null);
        setIsAuthorized(false);
        setLoading(false);
      } finally {
        if (mounted && user && shouldCheckAuthorization) {
          setLoading(false);
        }
      }
    });

    return () => {
      mounted = false;
      window.clearTimeout(loadingFailSafe);
      subscription.unsubscribe();
    };
  }, [checkAuthorization]);

  const value = {
    currentUser,
    loading,
    isAuthorized,
    signInWithGoogle,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
