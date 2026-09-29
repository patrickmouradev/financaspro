import { create } from 'zustand';

interface AuthState {
  token: string | null;
  email: string | null;
  nome: string | null;
  setAuth: (token: string, email: string, nome: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: typeof window !== 'undefined' ? localStorage.getItem('financaspro_token') : null,
  email: typeof window !== 'undefined' ? localStorage.getItem('financaspro_email') : null,
  nome: typeof window !== 'undefined' ? localStorage.getItem('financaspro_nome') : null,

  setAuth: (token: string, email: string, nome: string) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('financaspro_token', token);
      localStorage.setItem('financaspro_email', email);
      localStorage.setItem('financaspro_nome', nome);
    }
    set({ token, email, nome });
  },

  logout: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('financaspro_token');
      localStorage.removeItem('financaspro_email');
      localStorage.removeItem('financaspro_nome');
    }
    set({ token: null, email: null, nome: null });
  },
}));
