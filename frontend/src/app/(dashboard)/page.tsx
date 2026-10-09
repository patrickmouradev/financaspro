'use client';

import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import { KpiCard } from '@/components/KpiCard';
import { MonthSelector } from '@/components/MonthSelector';
import api from '@/lib/axios';
import { Wallet, TrendingUp, ArrowDownRight, Building2 } from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

const COLORS = ['#0284c7', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

export default function DashboardPage() {
  const [selectedMonth, setSelectedMonth] = useState<string>('');

  const { data: resumo } = useQuery({
    queryKey: ['resumoMensal', selectedMonth],
    queryFn: async () => {
      const url = selectedMonth ? `/api/extrato/resumo-mensal?anoMes=${selectedMonth}` : '/api/extrato/resumo-mensal';
      const resp = await api.get(url);
      return resp.data;
    },
  });

  useEffect(() => {
    if (resumo?.anoMes && !selectedMonth) {
      setSelectedMonth(resumo.anoMes);
    }
  }, [resumo, selectedMonth]);

  const { data: carteira } = useQuery({
    queryKey: ['carteiraInvestimentos'],
    queryFn: async () => {
      try {
        const resp = await api.get('/api/investimentos/carteira');
        return resp.data;
      } catch {
        return null;
      }
    },
  });

  const { data: projecaoMeta } = useQuery({
    queryKey: ['projecaoMeta'],
    queryFn: async () => {
      try {
        const resp = await api.get('/api/fiis/meta/projecao');
        return resp.data;
      } catch {
        return null;
      }
    },
  });

  const totalEntradas = resumo?.totalReceita ?? resumo?.totalEntradas ?? 0;
  const totalSaidas = resumo?.totalGasto ?? resumo?.totalSaidas ?? 0;
  const totalInvestido = carteira?.valorTotalAtual || 0;
  const categoriasData = resumo?.porCategoria || resumo?.gastosPorCategoria || [];

  const metaMensalVal = projecaoMeta?.resumoMeta?.metaMensal ?? 1000;
  const percentualConcluidoVal = projecaoMeta?.resumoMeta?.percentualConcluido ?? 0;
  const rendaAtualVal = projecaoMeta?.resumoMeta?.mediaRendaMensalAtual ?? 0;

  return (
    <>
      <Header title="Visão Geral — Dashboard" />
      <main className="p-8 space-y-8">
        <div className="flex items-center justify-between bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-sm font-bold text-slate-800">Filtrar Período</h2>
            <p className="text-xs text-slate-500">Selecione o mês para atualizar o painel de indicadores</p>
          </div>
          <MonthSelector
            selectedMonth={selectedMonth || resumo?.anoMes || ''}
            onChange={(m) => setSelectedMonth(m)}
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <KpiCard
            title="Receita do Mês"
            value={`R$ ${totalEntradas.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
            subtitle="Entradas acumuladas"
            trend="Período selecionado"
            isPositive={true}
            icon={Wallet}
          />
          <KpiCard
            title="Despesas do Mês"
            value={`R$ ${totalSaidas.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
            subtitle="Saídas e compras no cartão"
            trend="Período selecionado"
            isPositive={false}
            icon={ArrowDownRight}
          />
          <KpiCard
            title="Carteira de Investimentos"
            value={`R$ ${totalInvestido.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
            subtitle="Ações + FIIs + Renda Fixa"
            trend={carteira?.itens?.length ? `${carteira.itens.length} ativo(s) cadastrado(s)` : 'Sem ativos cadastrados'}
            isPositive={totalInvestido > 0}
            icon={TrendingUp}
          />
          <KpiCard
            title="Dividendos FIIs (Meta)"
            value={`R$ ${Number(metaMensalVal).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
            subtitle={`Renda atual: R$ ${Number(rendaAtualVal).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}/mês`}
            trend={`${Number(percentualConcluidoVal).toFixed(1)}% concluído`}
            isPositive={percentualConcluidoVal > 0}
            icon={Building2}
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs">
            <h3 className="text-base font-bold text-slate-800 mb-4">Despesas por Categoria</h3>
            {categoriasData.length > 0 ? (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={categoriasData}
                      dataKey="valorTotal"
                      nameKey="categoriaNome"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    >
                      {categoriasData.map((entry: any, index: number) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value: number) => `R$ ${value.toFixed(2)}`} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="h-64 flex items-center justify-center text-slate-400 text-sm">
                Nenhum lançamento importado no mês selecionado.
              </div>
            )}
          </div>

          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs">
            <h3 className="text-base font-bold text-slate-800 mb-4">Distribuição de Investimentos</h3>
            {carteira?.itens?.length > 0 ? (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={carteira.itens}>
                    <XAxis dataKey="ticker" />
                    <YAxis />
                    <Tooltip formatter={(value: number) => `R$ ${value.toFixed(2)}`} />
                    <Bar dataKey="valorAtual" fill="#0284c7" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="h-64 flex items-center justify-center text-slate-400 text-sm">
                Nenhum ativo cadastrado na carteira.
              </div>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
