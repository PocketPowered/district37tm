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
const CALLBACK_SESSION_POLL_MAX_ATTEMPTS = 20;
const CALLBACK_SESSION_POLL_DELAY_MS = 150;

const ensureTrailingSlash = (value: string) => (value.endsWith('/') ? value : `${value}/`);
const wait = (ms: number) => new Promise<void>((resolve) => window.setTimeout(resolve, ms));

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
  const authRequestRef = useRef(0);
  const initializedRef = useRef(false);
  const initializedUserIdRef = useRef<string | null>(null);

  const getSessionUserWithCallbackFallback = useCallback(async (): Promise<User | null> => {
    const {
      data: { session },
    } = await supabase.auth.getSession();

    if (session?.user) {
      return session.user;
    }

    const hasOAuthCode = new URLSearchParams(window.location.search).has('code');
    if (!hasOAuthCode) {
      return null;
    }

    for (let attempt = 0; attempt < CALLBACK_SESSION_POLL_MAX_ATTEMPTS; attempt += 1) {
      await wait(CALLBACK_SESSION_POLL_DELAY_MS);
      const {
        data: { session: polledSession },
      } = await supabase.auth.getSession();

      if (polledSession?.user) {
        return polledSession.user;
      }
    }

    return null;
  }, []);

  const applyAuthState = useCallback(async (user: User | null) => {
    const requestId = authRequestRef.current + 1;
    authRequestRef.current = requestId;

    setCurrentUser(user);

    if (!user?.email) {
      setIsAuthorized(false);
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const authorized = await isUserAuthorized(user.email);
      if (authRequestRef.current !== requestId) {
        return;
      }
      setIsAuthorized(authorized);
    } catch (error) {
      if (authRequestRef.current !== requestId) {
        return;
      }
      console.error('Error checking user authorization:', error);
      setIsAuthorized(false);
    } finally {
      if (authRequestRef.current === requestId) {
        setLoading(false);
      }
    }
  }, []);

  const handleAuthSessionChange = useCallback(
    (event: string, user: User | null) => {
      if (event === 'SIGNED_OUT') {
        window.setTimeout(() => {
          void applyAuthState(null);
        }, 0);
        return;
      }

      if (event !== 'SIGNED_IN' && event !== 'INITIAL_SESSION') {
        return;
      }

      if (
        event === 'INITIAL_SESSION' &&
        initializedRef.current &&
        initializedUserIdRef.current === (user?.id ?? null)
      ) {
        return;
      }

      // Supabase recommends avoiding awaited Supabase calls directly inside this callback.
      window.setTimeout(() => {
        void applyAuthState(user);
      }, 0);
    },
    [applyAuthState]
  );

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
        if (!mounted) return;
        const initialUser = await getSessionUserWithCallbackFallback();
        initializedRef.current = true;
        initializedUserIdRef.current = initialUser?.id ?? null;
        await applyAuthState(initialUser);
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
    } = supabase.auth.onAuthStateChange((event, session) => {
      handleAuthSessionChange(event, session?.user || null);
    });

    return () => {
      mounted = false;
      window.clearTimeout(loadingFailSafe);
      subscription.unsubscribe();
    };
  }, [applyAuthState, getSessionUserWithCallbackFallback, handleAuthSessionChange]);

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
