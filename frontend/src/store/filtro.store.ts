import { create } from 'zustand';

interface FiltroState {
  selectedMonth: string;
  setSelectedMonth: (month: string) => void;
}

export const useFiltroStore = create<FiltroState>((set) => ({
  selectedMonth: '',
  setSelectedMonth: (month: string) => set({ selectedMonth: month }),
}));
