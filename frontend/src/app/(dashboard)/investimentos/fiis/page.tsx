'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Building2, Target, TrendingUp, ChevronDown, ChevronRight, Calendar, DollarSign, FileText } from 'lucide-react';

interface CompraItem {
  id: number;
  dataCompra: string;
  quantidade: number;
  precoUnitario: number;
  taxasB3: number;
  precoMedioAjustadoOperacao: number;
  valorTotalPago: number;
  origemOuNota: string;
}

interface ProjecaoFiiItem {
  fiiId: number;
  ticker: string;
  nome: string;
  cotasAtuais: number;
  precoMedio: number;
  precoAtual: number;
  valorTotalInvestido: number;
  valorAtualTotal: number;
  dividendoMedioPorCota: number;
  rendimentoMensalAtual: number;
  cotasFaltantesParaMetaTotal: number;
  investimentoEstimadoNecessario: number;
  compras?: CompraItem[];
}

export default function FiisPage() {
  const [metaMensalInput, setMetaMensalInput] = useState('1000');
  const [expandedTicker, setExpandedTicker] = useState<string | null>(null);

  const { data: projecao, isLoading } = useQuery({
    queryKey: ['projecaoMeta', metaMensalInput],
    queryFn: async () => {
      const resp = await api.get(`/api/fiis/meta/projecao?metaMensal=${metaMensalInput}`);
      return resp.data;
    },
  });

  const resumo = projecao?.resumoMeta;

  const toggleAccordion = (ticker: string) => {
    setExpandedTicker((prev) => (prev === ticker ? null : ticker));
  };

  return (
    <>
      <Header title="Módulo Renda Variável — Dividendos & Projeção de Metas" />
      <main className="p-8 space-y-8 max-w-7xl mx-auto">
        {/* Painel da Meta de Dividendos */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6">
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-slate-100 pb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
                <Target className="w-5 h-5 text-sky-600" />
                Meta de Renda Passiva Mensal em Renda Variável
              </h3>
              <p className="text-xs text-slate-500">Defina o objetivo financeiro de dividendos e proventos mensais com FIIs e Ações</p>
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
                R$ {(resumo?.mediaRendaMensalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </h4>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Falta Para Concluir</p>
              <h4 className="text-2xl font-bold text-amber-600 mt-1">
                R$ {(resumo?.faltaParaMetaMensal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </h4>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Aporte Estimado Faltante</p>
              <h4 className="text-2xl font-bold text-slate-800 mt-1">
                R$ {(projecao?.aporteTotalEstimadoNecessario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
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

        {/* Tabela de Projeções Renda Variável por Ativo */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-sky-600" />
              <h3 className="font-bold text-slate-800 text-base">Carteira de Renda Variável (FIIs & Ações) — Compras & Preço Médio</h3>
            </div>
            <span className="text-xs text-slate-500">Clique na linha do ativo para expandir o histórico de compras</span>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
              <tr>
                <th className="w-8 px-3 py-3"></th>
                <th className="px-4 py-3">Ticker</th>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3 text-right">Qtd Cotas</th>
                <th className="px-4 py-3 text-right">Preço Médio</th>
                <th className="px-4 py-3 text-right">Preço Atual</th>
                <th className="px-4 py-3 text-right">Total Investido</th>
                <th className="px-4 py-3 text-right">Rend./Cota</th>
                <th className="px-4 py-3 text-right">Renda Mensal</th>
                <th className="px-4 py-3 text-right">Cotas Faltantes</th>
                <th className="px-4 py-3 text-right">Aporte Estimado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={11} className="px-6 py-8 text-center text-slate-400">
                    Carregando carteira de Renda Variável...
                  </td>
                </tr>
              ) : projecao?.projecoesPorFii?.length === 0 || !projecao ? (
                <tr>
                  <td colSpan={11} className="px-6 py-8 text-center text-slate-400">
                    Nenhum ativo de Renda Variável cadastrado para cálculo de projeção. Importe Notas de Corretagem B3.
                  </td>
                </tr>
              ) : (
                projecao.projecoesPorFii.map((item: ProjecaoFiiItem) => {
                  const isExpanded = expandedTicker === item.ticker;
                  const temCompras = item.compras && item.compras.length > 0;

                  return (
                    <>
                      <tr
                        key={item.ticker}
                        onClick={() => toggleAccordion(item.ticker)}
                        className={`cursor-pointer transition-colors ${
                          isExpanded ? 'bg-sky-50/50 hover:bg-sky-50' : 'hover:bg-slate-50/70'
                        }`}
                      >
                        <td className="px-3 py-4 text-slate-400">
                          {isExpanded ? (
                            <ChevronDown className="w-4 h-4 text-sky-600" />
                          ) : (
                            <ChevronRight className="w-4 h-4 text-slate-400" />
                          )}
                        </td>
                        <td className="px-4 py-4 font-bold text-sky-700 font-mono flex items-center gap-1.5">
                          {item.ticker}
                        </td>
                        <td className="px-4 py-4 font-medium text-slate-800">{item.nome}</td>
                        <td className="px-4 py-4 text-right font-mono font-semibold text-slate-900">{item.cotasAtuais}</td>
                        <td className="px-4 py-4 text-right font-mono text-slate-700 font-semibold">
                          R$ {(item.precoMedio || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-4 py-4 text-right font-mono text-slate-600">
                          R$ {(item.precoAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-4 py-4 text-right font-mono font-bold text-slate-900">
                          R$ {(item.valorTotalInvestido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-4 py-4 text-right font-mono text-slate-600">
                          R$ {(item.dividendoMedioPorCota || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-4 py-4 text-right font-mono font-bold text-emerald-600">
                          R$ {(item.rendimentoMensalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-4 py-4 text-right font-mono text-amber-600 font-semibold">{item.cotasFaltantesParaMetaTotal}</td>
                        <td className="px-4 py-4 text-right font-mono font-bold text-slate-900">
                          R$ {(item.investimentoEstimadoNecessario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                      </tr>

                      {/* Accordion - Detalhamento de Compras do Ticker */}
                      {isExpanded && (
                        <tr key={`${item.ticker}-expanded`} className="bg-slate-50/80">
                          <td colSpan={11} className="px-6 py-4 border-t border-b border-sky-100">
                            <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-xs space-y-3">
                              <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                                <h4 className="font-bold text-slate-800 text-sm flex items-center gap-2">
                                  <FileText className="w-4 h-4 text-sky-600" />
                                  Histórico Detalhado de Compras e Operações — {item.ticker}
                                </h4>
                                <span className="text-xs font-semibold text-slate-500">
                                  Preço Médio Ponderado: R$ {(item.precoMedio || 0).toFixed(2)}
                                </span>
                              </div>

                              {temCompras ? (
                                <div className="overflow-x-auto">
                                  <table className="w-full text-xs">
                                    <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                                      <tr>
                                        <th className="px-3 py-2 text-left">Data da Compra</th>
                                        <th className="px-3 py-2 text-right">Qtd Cotas</th>
                                        <th className="px-3 py-2 text-right">Preço Unitário</th>
                                        <th className="px-3 py-2 text-right">Taxas B3</th>
                                        <th className="px-3 py-2 text-right">Preço Médio Ajustado</th>
                                        <th className="px-3 py-2 text-right">Total Pago</th>
                                        <th className="px-3 py-2 text-left">Origem / Nota</th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-100 font-mono">
                                      {item.compras!.map((compra: CompraItem, idx: number) => (
                                        <tr key={compra.id || idx} className="hover:bg-slate-50">
                                          <td className="px-3 py-2 text-slate-700 font-sans">
                                            {compra.dataCompra
                                              ? new Date(compra.dataCompra + 'T00:00:00').toLocaleDateString('pt-BR')
                                              : '-'}
                                          </td>
                                          <td className="px-3 py-2 text-right font-bold text-slate-900">{compra.quantidade}</td>
                                          <td className="px-3 py-2 text-right text-slate-600">
                                            R$ {(compra.precoUnitario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                          </td>
                                          <td className="px-3 py-2 text-right text-slate-500">
                                            R$ {(compra.taxasB3 || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                          </td>
                                          <td className="px-3 py-2 text-right font-semibold text-sky-700">
                                            R$ {(compra.precoMedioAjustadoOperacao || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                          </td>
                                          <td className="px-3 py-2 text-right font-bold text-slate-900">
                                            R$ {(compra.valorTotalPago || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                          </td>
                                          <td className="px-3 py-2 text-slate-500 font-sans text-xs">
                                            {compra.origemOuNota || 'Nota de Corretagem'}
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              ) : (
                                <p className="text-xs text-slate-400 py-2">
                                  Nenhum registro individual de compra localizado para este ativo.
                                </p>
                              )}
                            </div>
                          </td>
                        </tr>
                      )}
                    </>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </main>
    </>
  );
}

