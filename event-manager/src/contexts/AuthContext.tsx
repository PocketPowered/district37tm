import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '@supabase/supabase-js';
import { supabase } from '../lib/supabase';
import { isUserAuthorized } from '../services/authService';

interface AuthContextType {
  currentUser: User | null;
  loading: boolean;
  isAuthorized: boolean;
  signInWithGoogle: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);
const AUTH_CHECK_TIMEOUT_MS = 8000;

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

  const checkAuthorization = async (user: User | null) => {
    if (user?.email) {
      const authorized = await Promise.race<boolean>([
        isUserAuthorized(user.email),
        new Promise<boolean>((resolve) => {
          window.setTimeout(() => resolve(false), AUTH_CHECK_TIMEOUT_MS);
        }),
      ]);
      setIsAuthorized(authorized);
    } else {
      setIsAuthorized(false);
    }
  };

  const signInWithGoogle = async () => {
    try {
      const redirectTo = `${window.location.origin}${window.location.pathname}`;
      const { error } = await supabase.auth.signInWithOAuth({
        provider: 'google',
        options: { redirectTo },
      });
      if (error) throw error;
    } catch (error) {
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

    const initializeAuth = async () => {
      try {
        setLoading(true);
        const { data } = await supabase.auth.getSession();
        if (!mounted) return;
        const user = data.session?.user || null;
        setCurrentUser(user);
        await checkAuthorization(user);
      } catch (error) {
        console.error('Error initializing auth:', error);
        if (!mounted) return;
        setCurrentUser(null);
        setIsAuthorized(false);
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    initializeAuth();

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(async (_event, session) => {
      try {
        setLoading(true);
        const user = session?.user || null;
        setCurrentUser(user);
        await checkAuthorization(user);
      } catch (error) {
        console.error('Error handling auth state change:', error);
        setCurrentUser(null);
        setIsAuthorized(false);
      } finally {
        setLoading(false);
      }
    });

    return () => {
      mounted = false;
      subscription.unsubscribe();
    };
  }, []);

  const value = {
    currentUser,
    loading,
    isAuthorized,
    signInWithGoogle,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 
