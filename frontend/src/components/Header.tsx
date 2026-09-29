'use client';

import { useAuthStore } from '@/store/auth.store';
import { User, Bell } from 'lucide-react';

export function Header({ title }: { title: string }) {
  const nome = useAuthStore((state) => state.nome);

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between shadow-xs">
      <h2 className="text-xl font-bold text-slate-800 tracking-tight">{title}</h2>

      <div className="flex items-center gap-4">
        <button className="p-2 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition-colors">
          <Bell className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 pl-4 border-l border-slate-200">
          <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600">
            <User className="w-4 h-4" />
          </div>
          <div className="text-sm">
            <p className="font-semibold text-slate-700 leading-none">{nome || 'Usuário'}</p>
            <p className="text-xs text-slate-400">Admin</p>
          </div>
        </div>
      </div>
    </header>
  );
}
