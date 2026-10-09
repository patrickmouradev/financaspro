'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  LayoutDashboard,
  Wallet,
  FileSpreadsheet,
  Tags,
  TrendingUp,
  Building2,
  Settings,
  LogOut,
  Receipt,
  ShieldCheck,
  CreditCard,
} from 'lucide-react';
import { useAuthStore } from '@/store/auth.store';

export function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const logout = useAuthStore((state) => state.logout);

  const menuSections = [
    {
      title: 'Principal',
      items: [
        { label: 'Dashboard Geral', href: '/', icon: LayoutDashboard },
        { label: 'Importar Arquivos', href: '/extrato/importar', icon: FileSpreadsheet },
      ],
    },
    {
      title: 'Conta Corrente',
      items: [
        { label: 'Extrato da Conta Corrente', href: '/extrato', icon: Wallet },
        { label: 'Fatura Cartão de Crédito', href: '/cartao', icon: CreditCard },
      ],
    },
    {
      title: 'Investimentos',
      items: [
        { label: 'Visão Geral & Custódia', href: '/investimentos', icon: TrendingUp },
        { label: 'Extrato da Conta Investimento', href: '/investimentos/extrato', icon: Receipt },
        { label: 'Renda Variável & Dividendos', href: '/investimentos/fiis', icon: Building2 },
        { label: 'Renda Fixa & Benchmarks', href: '/renda-fixa', icon: ShieldCheck },
      ],
    },
    {
      title: 'Administração',
      items: [
        { label: 'Categorias & Regras', href: '/extrato/categorias', icon: Tags },
        { label: 'Configurações', href: '/configuracoes', icon: Settings },
      ],
    },
  ];

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col min-h-screen border-r border-slate-800 shrink-0">
      <div className="p-6 border-b border-slate-800 flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg bg-sky-500 flex items-center justify-center font-bold text-white shadow-lg shadow-sky-500/30">
          F
        </div>
        <div>
          <h1 className="font-bold text-lg leading-tight tracking-tight">FinançasPro</h1>
          <p className="text-xs text-slate-400">Gestão & Mercado Financeiro</p>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-6 overflow-y-auto">
        {menuSections.map((section) => (
          <div key={section.title} className="space-y-2">
            <h2 className="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              {section.title}
            </h2>
            <div className="space-y-1">
              {section.items.map((item) => {
                const Icon = item.icon;
                const isActive = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
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
            </div>
          </div>
        ))}
      </nav>

      <div className="p-4 border-t border-slate-800">
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-500/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair da Conta
        </button>
      </div>
    </aside>
  );
}
