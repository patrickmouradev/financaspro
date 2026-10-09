'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import { MonthSelector } from '@/components/MonthSelector';
import api from '@/lib/axios';
import { Download, FileSpreadsheet, TrendingUp, DollarSign, Tag, ShieldCheck, ArrowUpRight, ArrowDownRight } from 'lucide-react';

export default function ExtratoInvestimentoPage() {
  const [selectedMonth, setSelectedMonth] = useState<string>('');

  const { data: lancamentosAll = [], isLoading } = useQuery({
    queryKey: ['lancamentos', selectedMonth],
    queryFn: async () => {
      const url = selectedMonth ? `/api/extrato/lancamentos?anoMes=${selectedMonth}` : '/api/extrato/lancamentos';
      const resp = await api.get(url);
      return resp.data;
    },
  });

  // Filtra rigorosamente APENAS lançamentos de Conta Investimento e Corretagem
  const lancamentosInvestimento = lancamentosAll.filter((l: any) => {
    const origem = (l.origem || '').toUpperCase();
    const tipo = (l.tipo || '').toUpperCase();
    const desc = (l.descricao || '').toUpperCase();
    const cat = (l.categoriaNome || '').toUpperCase();

    return (
      origem.includes('INVESTIMENTO') ||
      origem.includes('CORRETAGEM') ||
      tipo === 'COMPRA_ATIVO' ||
      tipo === 'VENDA_ATIVO' ||
      tipo === 'DIVIDENDO' ||
      tipo === 'RENDIMENTO' ||
      cat.includes('INVESTIMENTO') ||
      cat.includes('FUNDOS IMOBILIÁRIOS') ||
      cat.includes('AÇÕES') ||
      cat.includes('RENDA FIXA') ||
      cat.includes('PROVENTOS') ||
      desc.includes('COMPRA') ||
      desc.includes('VENDA') ||
      desc.includes('DIVIDENDO') ||
      desc.includes('RENDIMENTO')
    );
  });

  const totalProventos = lancamentosInvestimento
    .filter((l: any) => l.valor > 0)
    .reduce((acc: number, l: any) => acc + Number(l.valor), 0);

  const totalAportes = lancamentosInvestimento
    .filter((l: any) => l.valor < 0)
    .reduce((acc: number, l: any) => acc + Math.abs(Number(l.valor)), 0);

  return (
    <>
      <Header title="Extrato da Conta Investimento & Corretagem" />
      <main className="p-8 space-y-6 max-w-7xl mx-auto">
        {/* Top Bar & Filters */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-sky-600" />
              Extrato da Conta Investimento
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Movimentações exclusivas de investimentos, notas de corretagem B3, compras de ativos e proventos recebidos.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <MonthSelector
              selectedMonth={selectedMonth}
              onChange={(m) => setSelectedMonth(m)}
            />
          </div>
        </div>

        {/* KPIs Conta Investimento */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Total de Aportes / Compras</p>
            <h3 className="text-2xl font-bold text-slate-800 mt-1 flex items-center gap-1">
              <ArrowDownRight className="w-5 h-5 text-rose-500" />
              R$ {totalAportes.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-slate-500 mt-1">Capital alocado em ativos</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Proventos & Dividendos Creditados</p>
            <h3 className="text-2xl font-bold text-emerald-600 mt-1 flex items-center gap-1">
              <ArrowUpRight className="w-5 h-5 text-emerald-500" />
              R$ {totalProventos.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-emerald-700 font-semibold mt-1">Renda passiva caindo na conta</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Total de Operações Lidas</p>
            <h3 className="text-2xl font-bold text-sky-700 mt-1">
              {lancamentosInvestimento.length} movimentações
            </h3>
            <p className="text-xs text-slate-500 mt-1">Registros isolados de investimento</p>
          </div>
        </div>

        {/* Tabela Exclusiva do Extrato de Investimentos */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between">
            <h3 className="font-bold text-slate-800 text-base">Histórico da Conta Investimento</h3>
            <span className="text-xs text-slate-500 font-medium">{lancamentosInvestimento.length} lançamentos encontrados</span>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3.5">Data Pregão / Movimento</th>
                <th className="px-6 py-3.5">Categoria</th>
                <th className="px-6 py-3.5">Origem</th>
                <th className="px-6 py-3.5">Descrição do Operação</th>
                <th className="px-6 py-3.5 text-right">Valor Líquido</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                    Carregando extrato de investimentos...
                  </td>
                </tr>
              ) : lancamentosInvestimento.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                    Nenhuma movimentação de investimento encontrada no período. Importe uma Nota de Corretagem B3 ou Extrato de Conta Investimento.
                  </td>
                </tr>
              ) : (
                lancamentosInvestimento.map((l: any) => (
                  <tr key={l.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 font-mono text-xs text-slate-600">
                      {new Date(l.dataLancamento).toLocaleDateString('pt-BR')}
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 bg-sky-50 text-sky-700 rounded-md text-xs font-bold border border-sky-200 inline-flex items-center gap-1.5">
                        <Tag className="w-3 h-3 text-sky-500" />
                        {l.categoriaNome || 'Investimentos'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs font-semibold text-slate-600">
                      {l.origem || 'NOTA_CORRETAGEM'}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-800">{l.descricao}</td>
                    <td className={`px-6 py-4 text-right font-bold font-mono whitespace-nowrap ${l.valor < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                      {l.valor < 0
                        ? `- R$ ${Math.abs(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                        : `+ R$ ${Number(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                      }
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </>
  );
}
