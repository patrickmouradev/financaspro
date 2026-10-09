'use client';

import { useQuery } from '@tanstack/react-query';
import api from '@/lib/axios';
import { Calendar } from 'lucide-react';

interface MonthSelectorProps {
  selectedMonth: string;
  onChange: (month: string) => void;
}

const formatarMesAno = (ym: string) => {
  if (!ym || !ym.includes('-')) return ym;
  const [ano, mes] = ym.split('-');
  const nomesMeses: { [key: string]: string } = {
    '01': 'Janeiro',
    '02': 'Fevereiro',
    '03': 'Março',
    '04': 'Abril',
    '05': 'Maio',
    '06': 'Junho',
    '07': 'Julho',
    '08': 'Agosto',
    '09': 'Setembro',
    '10': 'Outubro',
    '11': 'Novembro',
    '12': 'Dezembro',
  };
  const nomeMes = nomesMeses[mes] || mes;
  return `${nomeMes}/${ano}`;
};

export function MonthSelector({ selectedMonth, onChange }: MonthSelectorProps) {
  const { data: meses = [] } = useQuery({
    queryKey: ['mesesDisponiveis'],
    queryFn: async () => {
      const resp = await api.get('/api/extrato/meses-disponiveis');
      return resp.data as string[];
    },
  });

  return (
    <div className="flex items-center gap-2 bg-white border border-slate-200 px-3 py-2 rounded-lg shadow-xs hover:border-slate-300 transition-colors">
      <Calendar className="w-4 h-4 text-sky-600 shrink-0" />
      <span className="text-xs font-medium text-slate-400">Mês:</span>
      <select
        value={selectedMonth}
        onChange={(e) => onChange(e.target.value)}
        className="bg-transparent text-sm font-semibold text-slate-700 outline-hidden cursor-pointer"
      >
        {meses.map((m: string) => (
          <option key={m} value={m}>
            {formatarMesAno(m)}
          </option>
        ))}
      </select>
    </div>
  );
}
