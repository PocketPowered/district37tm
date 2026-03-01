import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { conferenceService } from '../services/conferenceService';
import { Conference, ConferenceUpsertInput } from '../types/Conference';

interface ConferenceContextType {
  conferences: Conference[];
  selectedConference: Conference | null;
  selectedConferenceId: number | null;
  activeConference: Conference | null;
  loading: boolean;
  error: string | null;
  setSelectedConferenceId: (id: number | null) => void;
  refreshConferences: () => Promise<void>;
  createConference: (input: ConferenceUpsertInput) => Promise<Conference>;
  updateConference: (id: number, input: ConferenceUpsertInput) => Promise<Conference>;
  setActiveConference: (id: number) => Promise<void>;
}

const SELECTED_CONFERENCE_ID_STORAGE_KEY = 'event_manager_selected_conference_id';

const ConferenceContext = createContext<ConferenceContextType | null>(null);

const readPersistedConferenceId = (): number | null => {
  const stored = window.localStorage.getItem(SELECTED_CONFERENCE_ID_STORAGE_KEY);
  if (!stored) {
    return null;
  }

  const parsed = Number.parseInt(stored, 10);
  return Number.isFinite(parsed) ? parsed : null;
};

const persistConferenceId = (id: number | null) => {
  if (id === null) {
    window.localStorage.removeItem(SELECTED_CONFERENCE_ID_STORAGE_KEY);
    return;
  }
  window.localStorage.setItem(SELECTED_CONFERENCE_ID_STORAGE_KEY, id.toString());
};

export const useConference = (): ConferenceContextType => {
  const context = useContext(ConferenceContext);
  if (!context) {
    throw new Error('useConference must be used within a ConferenceProvider');
  }
  return context;
};

export const ConferenceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [conferences, setConferences] = useState<Conference[]>([]);
  const [selectedConferenceId, setSelectedConferenceIdState] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const setSelectedConferenceId = useCallback((id: number | null) => {
    setSelectedConferenceIdState(id);
    persistConferenceId(id);
  }, []);

  const refreshConferences = useCallback(async () => {
    try {
      setLoading(true);
      const fetched = await conferenceService.getConferences();
      const active = fetched.find((conference) => conference.isActive) || null;
      const persistedId = readPersistedConferenceId();

      setConferences(fetched);
      setSelectedConferenceIdState((previousId) => {
        const preferredId = previousId ?? persistedId;
        const selectedExists = preferredId !== null && fetched.some((conference) => conference.id === preferredId);
        const fallbackId = active?.id ?? fetched[0]?.id ?? null;
        const resolvedId = selectedExists ? preferredId : fallbackId;
        persistConferenceId(resolvedId);
        return resolvedId;
      });
      setError(null);
    } catch (refreshError) {
      console.error('Error loading conferences:', refreshError);
      setError('Failed to load conferences.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refreshConferences();
  }, [refreshConferences]);

  const createConference = useCallback(
    async (input: ConferenceUpsertInput): Promise<Conference> => {
      const created = await conferenceService.createConference(input);
      await refreshConferences();
      setSelectedConferenceId(created.id);
      return created;
    },
    [refreshConferences, setSelectedConferenceId]
  );

  const updateConference = useCallback(
    async (id: number, input: ConferenceUpsertInput): Promise<Conference> => {
      const updated = await conferenceService.updateConference(id, input);
      await refreshConferences();
      return updated;
    },
    [refreshConferences]
  );

  const setActiveConference = useCallback(
    async (id: number) => {
      await conferenceService.setActiveConference(id);
      await refreshConferences();
      setSelectedConferenceId(id);
    },
    [refreshConferences, setSelectedConferenceId]
  );

  const selectedConference = useMemo(() => {
    if (selectedConferenceId === null) {
      return null;
    }
    return conferences.find((conference) => conference.id === selectedConferenceId) || null;
  }, [conferences, selectedConferenceId]);

  const activeConference = useMemo(
    () => conferences.find((conference) => conference.isActive) || null,
    [conferences]
  );

  const value: ConferenceContextType = {
    conferences,
    selectedConference,
    selectedConferenceId,
    activeConference,
    loading,
    error,
    setSelectedConferenceId,
    refreshConferences,
    createConference,
    updateConference,
    setActiveConference,
  };

  return <ConferenceContext.Provider value={value}>{children}</ConferenceContext.Provider>;
};
