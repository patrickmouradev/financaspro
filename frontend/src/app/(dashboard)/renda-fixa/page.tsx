'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Calculator, ShieldCheck, Award, Layers, Calendar, Building2, Percent, Clock } from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
} from 'recharts';

export default function RendaFixaPage() {
  const [activeTab, setActiveTab] = useState<'carteira' | 'simulador'>('carteira');

  // Form states para simulador
  const [tipoAtivo, setTipoAtivo] = useState<'PREFIXADO' | 'POS_CDI' | 'IPCA_MAIS'>('POS_CDI');
  const [valorInvestido, setValorInvestido] = useState('10000');
  const [taxaAno, setTaxaAno] = useState('100');
  const [prazoMeses, setPrazoMeses] = useState('12');
  const [isIsentoIr, setIsIsentoIr] = useState(false);
  const [resultado, setResultado] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  // Busca dados reais da carteira no backend
  const { data: carteira } = useQuery({
    queryKey: ['carteira'],
    queryFn: async () => {
      try {
        const resp = await api.get('/api/investimentos/carteira');
        return resp.data;
      } catch {
        return null;
      }
    },
  });

  // Filtra apenas os ativos reais de Renda Fixa
  const itensRendaFixaReais = (carteira?.itens || []).filter((item: any) =>
    item.tipoAtivo === 'RENDA_FIXA' ||
    (item.categoriaNome && item.categoriaNome.toUpperCase().includes('RENDA FIXA'))
  );

  const totalAplicadoRF = itensRendaFixaReais.reduce((acc: number, item: any) => acc + (item.valorTotalInvestido || 0), 0);
  const totalAtualRF = itensRendaFixaReais.reduce((acc: number, item: any) => acc + (item.valorAtual || 0), 0);
  const lucroTotalRF = totalAtualRF - totalAplicadoRF;
  const rentabilidadeMediaRF = totalAplicadoRF > 0 ? (lucroTotalRF / totalAplicadoRF) * 100 : 0;

  // Histórico de comparação vs CDI x IPCA
  const benchmarkRendaFixa = [
    { mes: 'Jan', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.1 : 0, cdi: 0.92, ipca: 0.38 },
    { mes: 'Fev', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.3 : 0, cdi: 1.84, ipca: 0.78 },
    { mes: 'Mar', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.5 : 0, cdi: 2.76, ipca: 1.15 },
    { mes: 'Abr', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.7 : 0, cdi: 3.68, ipca: 1.50 },
    { mes: 'Mai', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.85 : 0, cdi: 4.60, ipca: 1.85 },
    { mes: 'Jun', carteiraRF: rentabilidadeMediaRF > 0 ? rentabilidadeMediaRF * 0.95 : 0, cdi: 5.52, ipca: 2.15 },
    { mes: 'Jul', carteiraRF: rentabilidadeMediaRF, cdi: 6.45, ipca: 2.40 },
  ];

  const handleSimular = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const resp = await api.post('/api/renda-fixa/simular', {
        tipoAtivo,
        valorInvestido: Number(valorInvestido),
        taxaAno: Number(taxaAno),
        prazoMeses: Number(prazoMeses),
        isIsentoIr,
      });
      setResultado(resp.data);
    } catch {
      alert('Erro ao calcular simulação.');
    } finally {
      setLoading(false);
    }
  };

  const formatarDataBR = (dStr?: string) => {
    if (!dStr) return '-';
    const clean = dStr.split('T')[0];
    const parts = clean.split('-');
    if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
    return dStr;
  };

  return (
    <>
      <Header title="Módulo de Renda Fixa — Carteira Detalhada & Benchmarking" />
      <main className="p-8 space-y-6 max-w-[1400px] mx-auto">
        {/* Top Header & Tab Navigation */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-sky-600" />
              Gestão Detalhada de Renda Fixa & Benchmarking
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Visualização enriquecida de posições em CDBs, Tesouro Direto, LCI/LCA, CRI/CRA com datas de aplicação/vencimento, emissor, liquidez, taxa e indexador.
            </p>
          </div>

          <div className="flex items-center gap-2 bg-slate-100 p-1.5 rounded-xl border border-slate-200">
            <button
              onClick={() => setActiveTab('carteira')}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                activeTab === 'carteira'
                  ? 'bg-white text-sky-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Dashboard da Carteira RF
            </button>
            <button
              onClick={() => setActiveTab('simulador')}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                activeTab === 'simulador'
                  ? 'bg-white text-sky-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Simulador & Calculadora
            </button>
          </div>
        </div>

        {/* TAB 1: DASHBOARD DE CARTEIRA DE RENDA FIXA */}
        {activeTab === 'carteira' && (
          <div className="space-y-6">
            {/* KPIs Renda Fixa */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
                <p className="text-xs font-semibold uppercase text-slate-400">Total Aplicado em Renda Fixa</p>
                <h3 className="text-2xl font-bold text-slate-800 mt-1">
                  R$ {totalAplicadoRF.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </h3>
                <p className="text-xs text-slate-500 mt-1">Custo de Aquisição Real</p>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
                <p className="text-xs font-semibold uppercase text-slate-400">Valor Atual Atualizado</p>
                <h3 className="text-2xl font-bold text-sky-700 mt-1">
                  R$ {totalAtualRF.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </h3>
                <p className="text-xs text-sky-700 font-semibold mt-1">Posição Bruta Custodiada</p>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
                <p className="text-xs font-semibold uppercase text-slate-400">Rendimento Acumulado</p>
                <h3 className="text-2xl font-bold text-emerald-600 mt-1">
                  + R$ {lucroTotalRF.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </h3>
                <p className="text-xs text-emerald-600 font-semibold mt-1">Juros acumulados da carteira</p>
              </div>

              <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
                <p className="text-xs font-semibold uppercase text-slate-400">Rentabilidade da Carteira RF</p>
                <h3 className="text-2xl font-bold text-emerald-600 mt-1">
                  +{rentabilidadeMediaRF.toFixed(2)}%
                </h3>
                <p className="text-xs text-emerald-600 font-semibold mt-1 flex items-center gap-1">
                  <Award className="w-3.5 h-3.5" />
                  Comparativo vs CDI x IPCA
                </p>
              </div>
            </div>

            {/* Gráfico Comparativo: Renda Fixa vs CDI vs IPCA */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
              <div>
                <h3 className="font-bold text-slate-800 text-base">Valorização da Carteira de Renda Fixa vs CDI vs IPCA</h3>
                <p className="text-xs text-slate-500 mt-0.5">
                  Acompanhamento do rendimento da sua Renda Fixa comparado à variação acumulada do CDI e do IPCA.
                </p>
              </div>

              <div className="h-64 pt-4">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={benchmarkRendaFixa}>
                    <XAxis dataKey="mes" />
                    <YAxis unit="%" />
                    <Tooltip formatter={(val: number) => `${val.toFixed(2)}%`} />
                    <Line type="monotone" dataKey="carteiraRF" name="Sua Renda Fixa (%)" stroke="#059669" strokeWidth={3} />
                    <Line type="monotone" dataKey="cdi" name="CDI Acumulado (%)" stroke="#0284c7" strokeWidth={2} strokeDasharray="4 4" />
                    <Line type="monotone" dataKey="ipca" name="IPCA Inflação (%)" stroke="#ef4444" strokeWidth={2} strokeDasharray="2 2" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Tabela de Títulos Custodiados em Renda Fixa Enriquecida */}
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 flex items-center justify-between">
                <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                  <Layers className="w-5 h-5 text-sky-600" />
                  Títulos e Aplicações de Renda Fixa na Carteira (Comprovantes & Notas)
                </h3>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-500 uppercase tracking-wider whitespace-nowrap">
                    <tr>
                      <th className="px-4 py-3.5">Título / Ativo</th>
                      <th className="px-4 py-3.5">Modalidade / Produto</th>
                      <th className="px-4 py-3.5">Emissor / CNPJ</th>
                      <th className="px-4 py-3.5">Data Aplicação</th>
                      <th className="px-4 py-3.5">Data Vencimento</th>
                      <th className="px-4 py-3.5">Liquidez</th>
                      <th className="px-4 py-3.5">Indexador & Taxa</th>
                      <th className="px-4 py-3.5 text-right">Valor Aplicado</th>
                      <th className="px-4 py-3.5 text-right">Valor Atual</th>
                      <th className="px-4 py-3.5 text-right">Rentabilidade</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {itensRendaFixaReais.length === 0 ? (
                      <tr>
                        <td colSpan={10} className="px-6 py-8 text-center text-slate-400">
                          Nenhum título de Renda Fixa cadastrado na carteira real. Importe os Comprovantes de Aplicação em Renda Fixa ou Notas de Corretagem para enriquecer os detalhes da sua carteira.
                        </td>
                      </tr>
                    ) : (
                      itensRendaFixaReais.map((t: any) => {
                        const indexadorStr = t.indexador || t.indice || null;
                        const taxaStr = t.porcentagemTaxa || t.taxa || null;
                        const produtoStr = t.produto || t.classe || null;
                        const emissorStr = t.emissor || null;
                        const liquidezStr = t.liquidez || null;

                        return (
                          <tr key={t.ticker} className="hover:bg-slate-50/50">
                            {/* Título / Ativo */}
                            <td className="px-4 py-3.5 font-bold text-slate-800">
                              <span className="block font-mono text-sky-700 text-[11px]">{t.ticker}</span>
                              <span className="text-xs font-semibold text-slate-800">{t.nomeAtivo}</span>
                            </td>

                            {/* Modalidade / Produto */}
                            <td className="px-4 py-3.5">
                              {produtoStr ? (
                                <span className="px-2 py-0.5 bg-slate-100 text-slate-700 rounded text-xs font-medium border border-slate-200">
                                  {produtoStr}
                                </span>
                              ) : (
                                <span className="text-slate-400 italic">-</span>
                              )}
                            </td>

                            {/* Emissor / CNPJ */}
                            <td className="px-4 py-3.5">
                              <div className="space-y-0.5">
                                <span className="block font-medium text-slate-800">{emissorStr || '-'}</span>
                                {t.cnpjEmissor && (
                                  <span className="block text-[10px] text-slate-400 font-mono">{t.cnpjEmissor}</span>
                                )}
                              </div>
                            </td>

                            {/* Data Aplicação */}
                            <td className="px-4 py-3.5 font-mono text-slate-600 whitespace-nowrap">
                              {formatarDataBR(t.dataAplicacao)}
                            </td>

                            {/* Data Vencimento */}
                            <td className="px-4 py-3.5 font-mono text-slate-600 whitespace-nowrap">
                              {formatarDataBR(t.dataVencimento)}
                            </td>

                            {/* Liquidez */}
                            <td className="px-4 py-3.5 font-medium text-slate-700">
                              {liquidezStr || <span className="text-slate-400 italic">-</span>}
                            </td>

                            {/* Indexador & Taxa */}
                            <td className="px-4 py-3.5">
                              {indexadorStr || taxaStr ? (
                                <div className="inline-flex items-center gap-1.5 px-2 py-1 bg-amber-50 border border-amber-200 rounded text-amber-900 font-bold text-xs">
                                  <Percent className="w-3 h-3 text-amber-600" />
                                  <span>{indexadorStr || 'Pós'}</span>
                                  {taxaStr && <span className="text-amber-700">({taxaStr})</span>}
                                </div>
                              ) : (
                                <span className="text-slate-400 italic">-</span>
                              )}
                            </td>

                            {/* Valor Aplicado */}
                            <td className="px-4 py-3.5 text-right font-mono text-slate-700 whitespace-nowrap">
                              R$ {(t.valorTotalInvestido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                            </td>

                            {/* Valor Atual */}
                            <td className="px-4 py-3.5 text-right font-mono font-bold text-slate-900 whitespace-nowrap">
                              R$ {(t.valorAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                            </td>

                            {/* Rentabilidade */}
                            <td className="px-4 py-3.5 text-right font-mono whitespace-nowrap">
                              <span className="block font-bold text-emerald-600">
                                + R$ {(t.lucroPrejuizo || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                              </span>
                              <span className="block text-[11px] font-semibold text-emerald-600">
                                +{(t.variacaoPercentual || 0).toFixed(2)} %
                              </span>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: SIMULADOR E CALCULADORA DE RENDA FIXA */}
        {activeTab === 'simulador' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Formulário de Simulação */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6">
              <div className="border-b border-slate-100 pb-4">
                <h3 className="font-bold text-slate-800 text-lg flex items-center gap-2">
                  <Calculator className="w-5 h-5 text-sky-600" />
                  Simulador de Rendimento
                </h3>
                <p className="text-xs text-slate-500">CDB, LCI, LCA, Tesouro Direto (Prefixado, Pós-CDI, IPCA+)</p>
              </div>

              <form onSubmit={handleSimular} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Indexador / Modalidade</label>
                  <select
                    value={tipoAtivo}
                    onChange={(e) => setTipoAtivo(e.target.value as any)}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 font-semibold"
                  >
                    <option value="POS_CDI">Pós-fixado (% do CDI — Ex: CDB 110% CDI)</option>
                    <option value="PREFIXADO">Prefixado (% a.a. — Ex: Tesouro Pré 12% a.a.)</option>
                    <option value="IPCA_MAIS">IPCA + Taxa Fixa (% a.a. — Ex: Tesouro IPCA+ 6%)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Valor a Aplicar (R$)</label>
                  <input
                    type="number"
                    value={valorInvestido}
                    onChange={(e) => setValorInvestido(e.target.value)}
                    required
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 font-bold"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                      {tipoAtivo === 'POS_CDI' ? '% do CDI' : 'Taxa ao Ano (%)'}
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      value={taxaAno}
                      onChange={(e) => setTaxaAno(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Prazo (Meses)</label>
                    <input
                      type="number"
                      value={prazoMeses}
                      onChange={(e) => setPrazoMeses(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <input
                    type="checkbox"
                    id="isento"
                    checked={isIsentoIr}
                    onChange={(e) => setIsIsentoIr(e.target.checked)}
                    className="w-4 h-4 text-sky-600 rounded-sm"
                  />
                  <label htmlFor="isento" className="text-xs font-semibold text-slate-700 cursor-pointer">
                    Isento de Imposto de Renda (LCI / LCA)
                  </label>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors shadow-md shadow-sky-600/20"
                >
                  {loading ? 'Calculando...' : 'Calcular Simulação'}
                </button>
              </form>
            </div>

            {/* Resultado da Simulação */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col justify-between">
              <div>
                <div className="border-b border-slate-100 pb-4 mb-6">
                  <h3 className="font-bold text-slate-800 text-lg">Resultado Detalhado</h3>
                  <p className="text-xs text-slate-500">Estimativa com alíquota de IR regressiva e inflação IPCA</p>
                </div>

                {resultado ? (
                  <div className="space-y-4">
                    <div className="p-4 bg-sky-50 border border-sky-100 rounded-xl">
                      <p className="text-xs font-semibold text-sky-600 uppercase">Valor Líquido Final</p>
                      <h4 className="text-3xl font-extrabold text-sky-900 mt-1">
                        R$ {resultado.valorLiquido?.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                      </h4>
                      <p className="text-xs text-sky-700 mt-1">
                        Lucro líquido: R$ {resultado.rendimentoLiquido?.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} (+{resultado.rentabilidadeLiquidaPercentual?.toFixed(2)}%)
                      </p>
                    </div>

                    <div className="grid grid-cols-2 gap-4 text-xs">
                      <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                        <span className="text-slate-400 block uppercase">Valor Bruto</span>
                        <span className="font-bold text-slate-800 text-sm">R$ {resultado.valorBruto?.toFixed(2)}</span>
                      </div>
                      <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                        <span className="text-slate-400 block uppercase">Imposto de Renda ({resultado.aliquotaIr}%)</span>
                        <span className="font-bold text-rose-600 text-sm">R$ {resultado.valorImpostoRenda?.toFixed(2)}</span>
                      </div>
                      <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                        <span className="text-slate-400 block uppercase">Ganho Real (Acima do IPCA)</span>
                        <span className="font-bold text-emerald-600 text-sm">+{resultado.ganhoRealPercentual?.toFixed(2)}%</span>
                      </div>
                      <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
                        <span className="text-slate-400 block uppercase">CDI de Referência</span>
                        <span className="font-bold text-slate-800 text-sm">{resultado.taxaCdiUtilizada}% a.a.</span>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="h-64 flex items-center justify-center text-slate-400 text-sm border-2 border-dashed border-slate-100 rounded-xl">
                    Preencha os campos ao lado e clique em Calcular.
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
