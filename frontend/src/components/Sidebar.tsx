'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Receipt,
  FileSpreadsheet,
  Tags,
  TrendingUp,
  Building2,
  Calculator,
  Settings,
  LogOut,
} from 'lucide-react';
import { useAuthStore } from '@/store/auth.store';

export function Sidebar() {
  const pathname = usePathname();
  const logout = useAuthStore((state) => state.logout);

  const menuItems = [
    { label: 'Dashboard', href: '/', icon: LayoutDashboard },
    { label: 'Extrato & Gastos', href: '/extrato', icon: Receipt },
    { label: 'Importar Arquivo', href: '/extrato/importar', icon: FileSpreadsheet },
    { label: 'Categorias & Regras', href: '/extrato/categorias', icon: Tags },
    { label: 'Investimentos', href: '/investimentos', icon: TrendingUp },
    { label: 'FIIs & Metas', href: '/investimentos/fiis', icon: Building2 },
    { label: 'Renda Fixa', href: '/renda-fixa', icon: Calculator },
    { label: 'Configurações', href: '/configuracoes', icon: Settings },
  ];

  return (
    <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col min-h-screen border-r border-slate-800">
      <div className="p-6 border-b border-slate-800 flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg bg-sky-500 flex items-center justify-center font-bold text-white shadow-lg shadow-sky-500/30">
          F
        </div>
        <div>
          <h1 className="font-bold text-lg leading-tight tracking-tight">FinançasPro</h1>
          <p className="text-xs text-slate-400">Gestão & Investimentos</p>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-sky-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              <Icon className="w-4 h-4" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-800">
        <button
          onClick={logout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-500/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair da Conta
        </button>
      </div>
    </aside>
  );
}
