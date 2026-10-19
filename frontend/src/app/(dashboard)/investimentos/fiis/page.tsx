'use client';

import { useState, useMemo, Fragment } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import {
  Building2,
  Target,
  TrendingUp,
  ChevronDown,
  ChevronRight,
  FileText,
  Sparkles,
  Award,
  Zap,
  HelpCircle,
  CheckCircle2,
  ArrowUpDown,
  Radio
} from 'lucide-react';

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
  dividendYieldMensalAtual?: number;
  dividendYieldAnualizado?: number;
  custoParaUmRealRenda?: number;
  rankingOportunidade?: number;
  classificacaoCompra?: string;
  recomendacaoCompra?: string;
  cotacaoEmTempoReal?: boolean;
}

export default function FiisPage() {
  const [metaMensalInput, setMetaMensalInput] = useState('1000');
  const [expandedTicker, setExpandedTicker] = useState<string | null>(null);
  const [ordenarPorMelhorCompra, setOrdenarPorMelhorCompra] = useState(true);

  const { data: projecao, isLoading, refetch } = useQuery({
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

  const listaOrdenada = useMemo(() => {
    if (!projecao?.projecoesPorFii) return [];
    const lista = [...projecao.projecoesPorFii];
    if (ordenarPorMelhorCompra) {
      lista.sort((a: ProjecaoFiiItem, b: ProjecaoFiiItem) => {
        const rankA = a.rankingOportunidade ?? 999;
        const rankB = b.rankingOportunidade ?? 999;
        return rankA - rankB;
      });
    }
    return lista;
  }, [projecao?.projecoesPorFii, ordenarPorMelhorCompra]);

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
              <p className="text-[10px] text-slate-500 mt-0.5">Renda gerada pela carteira atual</p>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Falta Para Concluir</p>
              <h4 className="text-2xl font-bold text-amber-600 mt-1">
                R$ {(resumo?.faltaParaMetaMensal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </h4>
              <p className="text-[10px] text-slate-500 mt-0.5">Diferença para atingir a meta</p>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <p className="text-xs font-semibold text-slate-400 uppercase">Menor Aporte p/ Meta (Melhor Opção)</p>
              <h4 className="text-2xl font-bold text-slate-800 mt-1">
                R$ {(projecao?.aporteTotalEstimadoNecessario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </h4>
              <p className="text-[10px] text-slate-500 mt-0.5">Menor valor necessário focando no ativo mais eficiente</p>
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

        {/* Card Explicativo do Especialista de Mercado */}
        <div className="p-4 bg-gradient-to-r from-emerald-50 via-teal-50 to-sky-50 border border-emerald-200 rounded-2xl flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="p-2.5 bg-emerald-600 text-white rounded-xl shadow-xs shrink-0 mt-0.5">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                Classificação Inteligente de Compra — Regra do Especialista
                <span className="px-2 py-0.5 bg-emerald-600 text-white rounded-md text-[10px] font-extrabold uppercase">
                  Preço Real B3
                </span>
              </h4>
              <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
                Calculamos o <strong>Dividend Yield sobre a cotação real de mercado</strong> e o <strong>Aporte Estimado individual</strong>: cada linha mostra exatamente quanto investir naquele ativo sozinho para cobrir 100% da meta restante, facilitando ver onde você gasta menos para alcançar sua renda passiva.
              </p>
            </div>
          </div>

          <button
            onClick={() => setOrdenarPorMelhorCompra(!ordenarPorMelhorCompra)}
            className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 shrink-0 ${
              ordenarPorMelhorCompra
                ? 'bg-emerald-600 text-white shadow-sm shadow-emerald-600/30'
                : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
            }`}
          >
            <ArrowUpDown className="w-3.5 h-3.5" />
            {ordenarPorMelhorCompra ? 'Ordenado: Mais Atrativo 1º' : 'Ordem Padrão'}
          </button>
        </div>

        {/* Tabela de Projeções Renda Variável por Ativo */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-sky-600" />
              <h3 className="font-bold text-slate-800 text-base">Carteira de Renda Variável (FIIs & Ações) — Cotações B3 & Análise de Compra</h3>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-500">
              <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                Cotações Atualizadas B3
              </span>
              <span>•</span>
              <span>Clique na linha para ver histórico de compras</span>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
                <tr>
                  <th className="w-10 px-3 py-3 text-center"></th>
                  <th className="px-3 py-3 font-semibold whitespace-nowrap">Ticker</th>
                  <th className="px-3 py-3 font-semibold">Nome</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Qtd</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Preço Médio</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Preço Atual</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Rend./Cota</th>
                  <th className="px-4 py-3 text-center bg-emerald-50/70 border-x border-emerald-100 text-emerald-900 font-bold whitespace-nowrap">
                    Classificação / Oportunidade
                  </th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Total Investido</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap">Renda Mensal</th>
                  <th className="px-3 py-3 text-right font-semibold whitespace-nowrap" title="Investimento individual necessário neste ativo para atingir 100% da meta restante">
                    Aporte Estimado p/ Meta
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {isLoading ? (
                  <tr>
                    <td colSpan={11} className="px-6 py-8 text-center text-slate-400">
                      Carregando carteira de Renda Variável e cotações de mercado...
                    </td>
                  </tr>
                ) : listaOrdenada.length === 0 ? (
                  <tr>
                    <td colSpan={11} className="px-6 py-8 text-center text-slate-400">
                      Nenhum ativo de Renda Variável cadastrado para cálculo de projeção. Importe Notas de Corretagem ou Extratos.
                    </td>
                  </tr>
                ) : (
                  listaOrdenada.map((item: ProjecaoFiiItem) => {
                    const isExpanded = expandedTicker === item.ticker;
                    const temCompras = item.compras && item.compras.length > 0;
                    const rank = item.rankingOportunidade;

                    return (
                      <Fragment key={item.ticker}>
                        <tr
                          onClick={() => toggleAccordion(item.ticker)}
                          className={`cursor-pointer transition-colors ${
                            isExpanded ? 'bg-sky-50/50 hover:bg-sky-50' : 'hover:bg-slate-50/70'
                          }`}
                        >
                          <td className="w-10 px-3 py-4 text-center text-slate-400">
                            {isExpanded ? (
                              <ChevronDown className="w-4 h-4 text-sky-600 inline-block" />
                            ) : (
                              <ChevronRight className="w-4 h-4 text-slate-400 inline-block" />
                            )}
                          </td>
                          <td className="px-3 py-4 font-bold text-sky-700 font-mono whitespace-nowrap">
                            {item.ticker}
                          </td>
                          <td className="px-3 py-4 font-medium text-slate-800 text-xs max-w-[170px] truncate" title={item.nome}>
                            {item.nome}
                          </td>
                          <td className="px-3 py-4 text-right font-mono font-semibold text-slate-900 whitespace-nowrap">
                            {item.cotasAtuais}
                          </td>
                          <td className="px-3 py-4 text-right font-mono text-slate-600 whitespace-nowrap">
                            R$ {(item.precoMedio || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </td>
                          <td className="px-3 py-4 text-right font-mono whitespace-nowrap">
                            <div className="font-bold text-slate-900">
                              R$ {(item.precoAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                            {item.cotacaoEmTempoReal && (
                              <span className="inline-flex items-center gap-1 text-[10px] text-emerald-600 font-sans font-semibold">
                                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                                Ao Vivo B3
                              </span>
                            )}
                          </td>
                          <td className="px-3 py-4 text-right font-mono text-slate-600 whitespace-nowrap">
                            R$ {(item.dividendoMedioPorCota || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </td>

                          {/* COLUNA: CLASSIFICAÇÃO FINANCEIRA DE COMPRA */}
                          <td className="px-4 py-3 text-center bg-emerald-50/40 border-x border-emerald-100 whitespace-nowrap">
                            {rank === 1 && (
                              <div className="inline-flex flex-col items-center">
                                <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-amber-100 text-amber-900 border border-amber-300 rounded-lg text-xs font-black shadow-xs whitespace-nowrap">
                                  🥇 1º - MELHOR OPÇÃO
                                </span>
                                <span className="text-[11px] font-bold text-emerald-700 mt-1 whitespace-nowrap">
                                  DY: {item.dividendYieldMensalAtual?.toFixed(2)}% a.m. {item.dividendYieldAnualizado ? `(${item.dividendYieldAnualizado.toFixed(1)}% a.a.)` : ''}
                                </span>
                                {item.custoParaUmRealRenda != null && (
                                  <span className="text-[10px] text-slate-500 font-mono whitespace-nowrap">
                                    R$ {item.custoParaUmRealRenda.toFixed(2)} p/ R$ 1 renda
                                  </span>
                                )}
                              </div>
                            )}

                            {rank === 2 && (
                              <div className="inline-flex flex-col items-center">
                                <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-slate-100 text-slate-800 border border-slate-300 rounded-lg text-xs font-bold whitespace-nowrap">
                                  🥈 2º - ÓTIMA COMPRA
                                </span>
                                <span className="text-[11px] font-bold text-emerald-700 mt-1 whitespace-nowrap">
                                  DY: {item.dividendYieldMensalAtual?.toFixed(2)}% a.m. {item.dividendYieldAnualizado ? `(${item.dividendYieldAnualizado.toFixed(1)}% a.a.)` : ''}
                                </span>
                                {item.custoParaUmRealRenda != null && (
                                  <span className="text-[10px] text-slate-500 font-mono whitespace-nowrap">
                                    R$ {item.custoParaUmRealRenda.toFixed(2)} p/ R$ 1 renda
                                  </span>
                                )}
                              </div>
                            )}

                            {rank === 3 && (
                              <div className="inline-flex flex-col items-center">
                                <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-amber-50 text-amber-800 border border-amber-200 rounded-lg text-xs font-semibold whitespace-nowrap">
                                  🥉 3º - BOA COMPRA
                                </span>
                                <span className="text-[11px] font-bold text-emerald-700 mt-1 whitespace-nowrap">
                                  DY: {item.dividendYieldMensalAtual?.toFixed(2)}% a.m. {item.dividendYieldAnualizado ? `(${item.dividendYieldAnualizado.toFixed(1)}% a.a.)` : ''}
                                </span>
                                {item.custoParaUmRealRenda != null && (
                                  <span className="text-[10px] text-slate-500 font-mono whitespace-nowrap">
                                    R$ {item.custoParaUmRealRenda.toFixed(2)} p/ R$ 1 renda
                                  </span>
                                )}
                              </div>
                            )}

                            {rank && rank > 3 && (
                              <div className="inline-flex flex-col items-center">
                                <span className="inline-flex items-center px-2 py-0.5 bg-slate-100 text-slate-700 rounded text-xs font-medium whitespace-nowrap">
                                  {rank}º - MODERADO
                                </span>
                                <span className="text-[11px] font-semibold text-slate-600 mt-0.5 whitespace-nowrap">
                                  DY: {item.dividendYieldMensalAtual?.toFixed(2)}% a.m. {item.dividendYieldAnualizado ? `(${item.dividendYieldAnualizado.toFixed(1)}% a.a.)` : ''}
                                </span>
                                {item.custoParaUmRealRenda != null && (
                                  <span className="text-[10px] text-slate-500 font-mono whitespace-nowrap">
                                    R$ {item.custoParaUmRealRenda.toFixed(2)} p/ R$ 1 renda
                                  </span>
                                )}
                              </div>
                            )}

                            {!rank && (
                              <span className="text-xs text-slate-400 whitespace-nowrap">
                                Sem Histórico
                              </span>
                            )}
                          </td>

                          <td className="px-3 py-4 text-right font-mono font-bold text-slate-900 whitespace-nowrap">
                            R$ {(item.valorTotalInvestido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </td>
                          <td className="px-3 py-4 text-right font-mono font-bold text-emerald-600 whitespace-nowrap">
                            R$ {(item.rendimentoMensalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </td>
                          <td className="px-3 py-4 text-right font-mono whitespace-nowrap">
                            <div className="font-bold text-slate-900">
                              R$ {(item.investimentoEstimadoNecessario || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                            {item.cotasFaltantesParaMetaTotal != null && item.cotasFaltantesParaMetaTotal > 0 && (
                              <span className="text-[10px] text-slate-500 font-sans block">
                                {Number(item.cotasFaltantesParaMetaTotal).toLocaleString('pt-BR')} cotas
                              </span>
                            )}
                          </td>
                        </tr>

                        {/* Accordion - Detalhamento de Compras do Ticker */}
                        {isExpanded && (
                          <tr className="bg-slate-50/80">
                            <td colSpan={11} className="px-6 py-4 border-t border-b border-sky-100">
                              <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-xs space-y-3">
                                <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                                  <h4 className="font-bold text-slate-800 text-sm flex items-center gap-2">
                                    <FileText className="w-4 h-4 text-sky-600" />
                                    Histórico Detalhado de Compras e Operações — {item.ticker}
                                  </h4>
                                  <span className="text-xs font-semibold text-slate-500">
                                    Preço Médio Ponderado: R$ {(item.precoMedio || 0).toFixed(2)} | Cotação Mercado: R$ {(item.precoAtual || 0).toFixed(2)}
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
                      </Fragment>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </>
  );
}
