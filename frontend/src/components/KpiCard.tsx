import { LucideIcon } from 'lucide-react';

interface KpiCardProps {
  title: string;
  value: string;
  subtitle?: string;
  trend?: string;
  isPositive?: boolean;
  icon: LucideIcon;
}

export function KpiCard({ title, value, subtitle, trend, isPositive, icon: Icon }: KpiCardProps) {
  return (
    <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs flex items-start justify-between">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">{title}</p>
        <h3 className="text-2xl font-bold text-slate-800 mt-1">{value}</h3>
        {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        {trend && (
          <p className={`text-xs font-semibold mt-2 ${isPositive ? 'text-emerald-600' : 'text-rose-600'}`}>
            {trend}
          </p>
        )}
      </div>
      <div className="p-3 bg-slate-50 border border-slate-100 rounded-lg text-slate-600">
        <Icon className="w-5 h-5" />
      </div>
    </div>
  );
}
