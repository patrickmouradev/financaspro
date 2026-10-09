import { create } from 'zustand';

interface AuthState {
  token: string | null;
  email: string | null;
  nome: string | null;
  lastActivity: number;
  setAuth: (token: string, email: string, nome: string) => void;
  updateActivity: () => void;
  logout: () => void;
}

const getInitialActivity = () => {
  if (typeof window === 'undefined') return Date.now();
  const stored = localStorage.getItem('financaspro_last_activity');
  return stored ? parseInt(stored, 10) : Date.now();
};

export const useAuthStore = create<AuthState>((set) => ({
  token: typeof window !== 'undefined' ? localStorage.getItem('financaspro_token') : null,
  email: typeof window !== 'undefined' ? localStorage.getItem('financaspro_email') : null,
  nome: typeof window !== 'undefined' ? localStorage.getItem('financaspro_nome') : null,
  lastActivity: getInitialActivity(),

  setAuth: (token: string, email: string, nome: string) => {
    const now = Date.now();
    if (typeof window !== 'undefined') {
      localStorage.setItem('financaspro_token', token);
      localStorage.setItem('financaspro_email', email);
      localStorage.setItem('financaspro_nome', nome);
      localStorage.setItem('financaspro_last_activity', now.toString());
    }
    set({ token, email, nome, lastActivity: now });
  },

  updateActivity: () => {
    const now = Date.now();
    if (typeof window !== 'undefined') {
      localStorage.setItem('financaspro_last_activity', now.toString());
    }
    set({ lastActivity: now });
  },

  logout: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('financaspro_token');
      localStorage.removeItem('financaspro_email');
      localStorage.removeItem('financaspro_nome');
      localStorage.removeItem('financaspro_last_activity');
    }
    set({ token: null, email: null, nome: null, lastActivity: 0 });
  },
}));
