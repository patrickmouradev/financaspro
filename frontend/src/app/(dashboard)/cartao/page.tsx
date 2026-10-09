'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import { MonthSelector } from '@/components/MonthSelector';
import api from '@/lib/axios';
import { CreditCard, Tag, FileSpreadsheet, ArrowDownRight } from 'lucide-react';

export default function CartaoPage() {
  const [selectedMonth, setSelectedMonth] = useState<string>('');

  const { data: lancamentosAll = [], isLoading } = useQuery({
    queryKey: ['lancamentos', selectedMonth],
    queryFn: async () => {
      const url = selectedMonth ? `/api/extrato/lancamentos?anoMes=${selectedMonth}` : '/api/extrato/lancamentos';
      const resp = await api.get(url);
      return resp.data;
    },
  });

  // Filtra lançamentos de Fatura de Cartão de Crédito
  const lancamentosCartao = lancamentosAll.filter((l: any) => {
    const origem = (l.origem || '').toUpperCase();
    const obs = (l.observacao || '').toUpperCase();
    const desc = (l.descricao || '').toUpperCase();
    return origem.includes('FATURA') || origem.includes('CARTAO') || obs.includes('FATURA') || desc.includes('FATURA');
  });

  const totalFatura = lancamentosCartao.reduce((acc: number, l: any) => acc + Math.abs(Number(l.valor)), 0);

  return (
    <>
      <Header title="Extrato & Fatura Cartão de Crédito BTG" />
      <main className="p-8 space-y-6 max-w-7xl mx-auto">
        {/* Top Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <CreditCard className="w-5 h-5 text-sky-600" />
              Fatura & Compras de Cartão de Crédito
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Detalhamento de faturas, parcelas e compras no Cartão de Crédito BTG Pactual.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <MonthSelector
              selectedMonth={selectedMonth}
              onChange={(m) => setSelectedMonth(m)}
            />
          </div>
        </div>

        {/* KPIs Fatura Cartão */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Total da Fatura no Mês</p>
            <h3 className="text-2xl font-bold text-rose-600 mt-1 flex items-center gap-1">
              <ArrowDownRight className="w-5 h-5 text-rose-500" />
              R$ {totalFatura.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-slate-500 mt-1">Gastos consolidados do cartão</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Total de Compras na Fatura</p>
            <h3 className="text-2xl font-bold text-slate-800 mt-1">
              {lancamentosCartao.length} compras
            </h3>
            <p className="text-xs text-slate-500 mt-1">Itens importados da fatura BTG</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Ticket Médio por Compra</p>
            <h3 className="text-2xl font-bold text-sky-700 mt-1">
              R$ {(lancamentosCartao.length > 0 ? totalFatura / lancamentosCartao.length : 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-slate-500 mt-1">Valor médio por transação</p>
          </div>
        </div>

        {/* Tabela de Lançamentos de Fatura */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between">
            <h3 className="font-bold text-slate-800 text-base">Itens da Fatura de Cartão</h3>
            <span className="text-xs text-slate-500 font-medium">{lancamentosCartao.length} compras</span>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3.5">Data da Compra</th>
                <th className="px-6 py-3.5">Categoria</th>
                <th className="px-6 py-3.5">Estabelecimento / Descrição</th>
                <th className="px-6 py-3.5">Parcela</th>
                <th className="px-6 py-3.5 text-right">Valor R$</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                    Carregando itens da fatura...
                  </td>
                </tr>
              ) : lancamentosCartao.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                    Nenhuma compra de cartão de crédito encontrada no período. Importe a Fatura do Cartão BTG.
                  </td>
                </tr>
              ) : (
                lancamentosCartao.map((l: any) => (
                  <tr key={l.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 font-mono text-xs text-slate-600">
                      {new Date(l.dataLancamento).toLocaleDateString('pt-BR')}
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 bg-amber-50 text-amber-700 rounded-md text-xs font-bold border border-amber-200 inline-flex items-center gap-1.5">
                        <Tag className="w-3 h-3 text-amber-500" />
                        {l.categoriaNome || 'Cartão de Crédito'}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-800">{l.descricao}</td>
                    <td className="px-6 py-4 text-xs font-mono text-slate-500">
                      {l.parcelaAtual && l.totalParcelas ? `${l.parcelaAtual}/${l.totalParcelas}` : 'À vista'}
                    </td>
                    <td className="px-6 py-4 text-right font-bold font-mono text-rose-600 whitespace-nowrap">
                      - R$ {Math.abs(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
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
