'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Building2, Target, DollarSign, ArrowUpRight } from 'lucide-react';

export default function FiisPage() {
  const [metaMensalInput, setMetaMensalInput] = useState('1000');

  const { data: projecao } = useQuery({
    queryKey: ['projecaoMeta', metaMensalInput],
    queryFn: async () => {
      const resp = await api.get(`/api/fiis/meta/projecao?metaMensal=${metaMensalInput}`);
      return resp.data;
    },
  });

  const { data: fiis = [] } = useQuery({
    queryKey: ['fiis'],
    queryFn: async () => {
      const resp = await api.get('/api/fiis');
      return resp.data;
    },
  });

  const resumo = projecao?.resumoMeta;

  return (
    <>
      <Header title="Módulo FIIs — Dividendos & Projeção de Metas" />
      <main className="p-8 space-y-8">
        {/* Painel da Meta de Dividendos */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-slate-100 pb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
                <Target className="w-5 h-5 text-sky-600" />
                Meta de Rendimento Mensal em Dividendos
              </h3>
              <p className="text-xs text-slate-500">Defina o objetivo financeiro de renda passiva mensal com FIIs</p>
            </div>
            <div className="flex items-center gap-3">
              <label className="text-xs font-semibold text-slate-600 uppercase">Sua Meta (R$):</label>
              <input
                type="number"
                value={metaMensalInput}
                onChange={(e) => setMetaMensalInput(e.target.value)}
                className="w-32 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm font-bold text-slate-800"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Média Mensal Atual</p>
              <h4 className="text-2xl font-bold text-sky-700 mt-1">
                R$ {(resumo?.mediaRendaMensalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </h4>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Falta Para Concluir</p>
              <h4 className="text-2xl font-bold text-amber-600 mt-1">
                R$ {(resumo?.faltaParaMetaMensal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </h4>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Aporte Estimado Faltante</p>
              <h4 className="text-2xl font-bold text-slate-800 mt-1">
                R$ {(projecao?.aporteTotalEstimadoNecessario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </h4>
            </div>
          </div>

          {/* Barra de Progresso */}
          <div>
            <div className="flex justify-between text-xs font-semibold text-slate-600 mb-2">
              <span>Progresso da Meta: {(resumo?.percentualConcluido || 0).toFixed(1)}%</span>
              <span>Meta: R$ {Number(metaMensalInput).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}/mês</span>
            </div>
            <div className="w-full bg-slate-100 h-3 rounded-full overflow-hidden">
              <div
                className="bg-sky-600 h-full rounded-full transition-all duration-500 shadow-xs"
                style={{ width: `${Math.min(resumo?.percentualConcluido || 0, 100)}%` }}
              />
            </div>
          </div>
        </div>

        {/* Tabela de Projeções FII por FII */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center gap-2">
            <Building2 className="w-5 h-5 text-sky-600" />
            <h3 className="font-bold text-slate-800 text-base">Cotas Faltantes e Projeção por FII</h3>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3">Ticker</th>
                <th className="px-6 py-3">Nome</th>
                <th className="px-6 py-3 text-right">Cotas Atuais</th>
                <th className="px-6 py-3 text-right">Rend. Médio/Cota</th>
                <th className="px-6 py-3 text-right">Renda Mensal</th>
                <th className="px-6 py-3 text-right">Cotas Faltantes</th>
                <th className="px-6 py-3 text-right">Aporte Estimado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {projecao?.projecoesPorFii?.length === 0 || !projecao ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-slate-400">
                    Nenhum FII cadastrado para cálculo de projeção.
                  </td>
                </tr>
              ) : (
                projecao.projecoesPorFii.map((item: any) => (
                  <tr key={item.ticker} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 font-bold text-sky-700">{item.ticker}</td>
                    <td className="px-6 py-4 font-medium text-slate-800">{item.nome}</td>
                    <td className="px-6 py-4 text-right font-mono">{item.cotasAtuais}</td>
                    <td className="px-6 py-4 text-right font-mono text-slate-600">R$ {item.dividendoMedioPorCota.toFixed(2)}</td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-emerald-600">R$ {item.rendimentoMensalAtual.toFixed(2)}</td>
                    <td className="px-6 py-4 text-right font-mono text-amber-600 font-semibold">{item.cotasFaltantesParaMetaTotal}</td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-slate-900">R$ {item.investimentoEstimadoNecessario.toFixed(2)}</td>
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
