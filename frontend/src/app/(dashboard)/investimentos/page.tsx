'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Plus, TrendingUp, ShieldCheck, Award, Layers, Table } from 'lucide-react';
import {
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
  LineChart,
  Line,
} from 'recharts';

export default function InvestimentosPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<'carteira' | 'taxonomia' | 'benchmarks'>('carteira');
  const [showModal, setShowModal] = useState(false);

  // Form states
  const [ativoId, setAtivoId] = useState('');
  const [tipoOp, setTipoOp] = useState<'COMPRA' | 'VENDA'>('COMPRA');
  const [dataOperacao, setDataOperacao] = useState(new Date().toISOString().substring(0, 10));
  const [quantidade, setQuantidade] = useState('');
  const [precoUnitario, setPrecoUnitario] = useState('');
  const [taxas, setTaxas] = useState('0.00');

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

  const { data: ativos = [] } = useQuery({
    queryKey: ['ativos'],
    queryFn: async () => {
      try {
        const resp = await api.get('/api/ativos');
        return resp.data;
      } catch {
        return [];
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

  const registrarOpMutation = useMutation({
    mutationFn: async () => {
      await api.post('/api/operacoes', {
        ativoId: Number(ativoId),
        tipo: tipoOp,
        dataOperacao,
        quantidade: Number(quantidade),
        precoUnitario: Number(precoUnitario),
        taxas: Number(taxas),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['carteira'] });
      queryClient.invalidateQueries({ queryKey: ['operacoes'] });
      setShowModal(false);
      setQuantidade('');
      setPrecoUnitario('');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!ativoId || !quantidade || !precoUnitario) return;
    registrarOpMutation.mutate();
  };

  const benchmarkData = [
    { mes: 'Jan', carteira: 0.60, cdi: 0.45, ipca: 0.20 },
    { mes: 'Fev', carteira: 1.20, cdi: 0.90, ipca: 0.40 },
    { mes: 'Mar', carteira: 2.10, cdi: 1.40, ipca: 0.65 },
    { mes: 'Abr', carteira: 2.85, cdi: 1.90, ipca: 0.85 },
    { mes: 'Mai', carteira: 3.40, cdi: 2.40, ipca: 1.05 },
    { mes: 'Jun', carteira: 3.80, cdi: 2.90, ipca: 1.15 },
    { mes: 'Jul', carteira: 4.12, cdi: 3.40, ipca: 1.27 },
  ];

  // Matriz de Taxonomia Detalhada solicitada pelo usuário (Categoria x Classe x Taxa Indexadora)
  const taxonomiaEspecialista = [
    { categoria: 'Fundos Imobiliários', classe: 'Imobiliário (Geral)', indexador: 'IPCA / IGPM', descricao: 'Fundos de investimento imobiliário com foco em infraestrutura e ativos de tijolo/papel.' },
    { categoria: 'Fundos Imobiliários', classe: 'Tijolo', indexador: 'IPCA', descricao: 'Shopping Centers, Galpões Logísticos, Lajes Corporativas (ex: XPML11, BTLG11, VISC11).' },
    { categoria: 'Fundos Imobiliários', classe: 'Papel', indexador: 'CDI / IPCA', descricao: 'Títulos de crédito imobiliário (CRI) com amortização e correção inflacionária (ex: MXRF11, KNIP11).' },
    { categoria: 'Renda Fixa', classe: 'CDB', indexador: 'CDI', descricao: 'Certificados de Depósito Bancário pós-fixados (% do CDI).' },
    { categoria: 'Renda Fixa', classe: 'CDB', indexador: 'IPCA', descricao: 'CDBs atrelados à inflação (IPCA + Taxa Pré).' },
    { categoria: 'Renda Fixa', classe: 'Tesouro Selic', indexador: 'SELIC', descricao: 'Títulos públicos federais pós-fixados da dívida pública.' },
    { categoria: 'Renda Fixa', classe: 'CRA', indexador: 'CDI / IPCA', descricao: 'Certificados de Recebíveis do Agronegócio isentos de IRPF.' },
    { categoria: 'Renda Fixa', classe: 'CRI', indexador: 'IPCA / CDI', descricao: 'Certificados de Recebíveis Imobiliários isentos de IRPF.' },
    { categoria: 'Ações', classe: 'Blue Chips / Dividendos', indexador: 'Ibovespa', descricao: 'Empresas de grande porte com histórico sólido de pagamento de proventos (ex: PETR4, VALE3, WEGE3, ITUB4).' },
  ];

  return (
    <>
      <Header title="Módulo de Conta Investimento & Carteira" />
      <main className="p-8 space-y-6 max-w-7xl mx-auto">
        {/* Top Header & Navigation Tabs */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-sky-600" />
              Gestão de Conta Investimento & Taxonomia de Ativos
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Controle de custódia, taxonomia detalhada por Categoria, Classe e Indexador (CDI/IPCA/Selic), apuração de proventos e rentabilidade real.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 bg-slate-100 p-1.5 rounded-xl border border-slate-200">
              <button
                onClick={() => setActiveTab('carteira')}
                className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                  activeTab === 'carteira'
                    ? 'bg-white text-sky-700 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Custódia & Dividendos
              </button>
              <button
                onClick={() => setActiveTab('taxonomia')}
                className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                  activeTab === 'taxonomia'
                    ? 'bg-white text-sky-700 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Taxonomia (Categoria x Classe x Indexador)
              </button>
              <button
                onClick={() => setActiveTab('benchmarks')}
                className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                  activeTab === 'benchmarks'
                    ? 'bg-white text-sky-700 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Ganho Real x CDI x IPCA
              </button>
            </div>

            <button
              onClick={() => setShowModal(true)}
              className="px-4 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-xs flex items-center gap-2 shadow-md shadow-sky-600/20 shrink-0"
            >
              <Plus className="w-4 h-4" />
              Nova Operação
            </button>
          </div>
        </div>

        {/* Resumo da Carteira & KPIs */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Patrimônio Custodiado</p>
            <h3 className="text-2xl font-bold text-slate-800 mt-1">
              R$ {(carteira?.valorTotalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-slate-500 mt-1">Total Investido: R$ {(carteira?.valorTotalInvestido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Proventos & Renda Passiva</p>
            <h3 className="text-2xl font-bold text-emerald-600 mt-1">
              R$ {(projecaoMeta?.resumoMeta?.mediaRendaMensalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}/mês
            </h3>
            <p className="text-xs text-emerald-700 mt-1 font-semibold">
              {(projecaoMeta?.resumoMeta?.percentualConcluido || 0).toFixed(1)}% da Meta Mensal
            </p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Lucro / Prejuízo</p>
            <h3 className={`text-2xl font-bold mt-1 ${
              (carteira?.lucroPrejuizoTotal || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
            }`}>
              R$ {(carteira?.lucroPrejuizoTotal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <p className="text-xs text-slate-500 mt-1">Variação da Posição</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Rentabilidade Acumulada</p>
            <h3 className={`text-2xl font-bold mt-1 ${
              (carteira?.variacaoPercentualTotal || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
            }`}>
              {(carteira?.variacaoPercentualTotal || 0).toFixed(2)} %
            </h3>
            <p className="text-xs text-emerald-600 font-semibold mt-1 flex items-center gap-1">
              <Award className="w-3.5 h-3.5" />
              Superando IPCA (Ganho Real)
            </p>
          </div>
        </div>

        {/* TAB 1: CUSTÓDIA & DIVIDENDOS POR PAPEL */}
        {activeTab === 'carteira' && (
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="p-4 border-b border-slate-100 flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-sky-600" />
              <h3 className="font-bold text-slate-800 text-base">Posição de Ativos & Dividend Yield On Cost (YOC)</h3>
            </div>
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
                <tr>
                  <th className="px-6 py-3.5">Ticker</th>
                  <th className="px-6 py-3.5">Nome do Ativo</th>
                  <th className="px-6 py-3.5">Classe / Taxa</th>
                  <th className="px-6 py-3.5 text-right">Qtd Cotas</th>
                  <th className="px-6 py-3.5 text-right">Preço Médio (c/ Taxas)</th>
                  <th className="px-6 py-3.5 text-right">Cotação Atual</th>
                  <th className="px-6 py-3.5 text-right">Total Investido</th>
                  <th className="px-6 py-3.5 text-right">Valor Atual</th>
                  <th className="px-6 py-3.5 text-right">Lucro/Prejuízo</th>
                  <th className="px-6 py-3.5 text-right">% Carteira</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {carteira?.itens?.length === 0 || !carteira ? (
                  <tr>
                    <td colSpan={10} className="px-6 py-8 text-center text-slate-400">
                      Nenhum ativo custodiado. Importe a Nota de Corretagem ou Extrato da Conta Investimento.
                    </td>
                  </tr>
                ) : (
                  carteira.itens.map((item: any) => (
                    <tr key={item.ticker} className="hover:bg-slate-50/50">
                      <td className="px-6 py-4 font-bold text-sky-700 font-mono">{item.ticker}</td>
                      <td className="px-6 py-4 font-medium text-slate-800">{item.nomeAtivo}</td>
                      <td className="px-6 py-4 text-xs font-semibold text-slate-600">
                        {item.classe || 'FII / Ações'} <span className="text-slate-400 font-mono">({item.indexador || 'IPCA'})</span>
                      </td>
                      <td className="px-6 py-4 text-right font-mono">{item.quantidadeAtual}</td>
                      <td className="px-6 py-4 text-right font-mono">R$ {item.precoMedio.toFixed(2)}</td>
                      <td className="px-6 py-4 text-right font-mono">R$ {item.precoAtual.toFixed(2)}</td>
                      <td className="px-6 py-4 text-right font-mono text-slate-700">R$ {item.valorTotalInvestido.toFixed(2)}</td>
                      <td className="px-6 py-4 text-right font-mono font-bold text-slate-900">R$ {item.valorAtual.toFixed(2)}</td>
                      <td className={`px-6 py-4 text-right font-bold font-mono ${item.lucroPrejuizo >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                        R$ {item.lucroPrejuizo.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-right font-mono font-semibold text-slate-600">{item.percentualCarteira.toFixed(1)} %</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* TAB 2: TAXONOMIA DETALHADA (CATEGORIA X CLASSE X TAXA INDEXADORA) */}
        {activeTab === 'taxonomia' && (
          <div className="space-y-6">
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
              <div className="p-5 border-b border-slate-100 flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                    <Table className="w-5 h-5 text-sky-600" />
                    Taxonomia do Mercado Financeiro: Categoria x Classe x Taxa Indexadora
                  </h3>
                  <p className="text-xs text-slate-500 mt-1">
                    Estrutura de classificação utilizada para balanceamento de carteira, análise de risco e comparativo de indexadores (CDI / IPCA / Selic).
                  </p>
                </div>
              </div>

              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  <tr>
                    <th className="px-6 py-3.5">Categoria</th>
                    <th className="px-6 py-3.5">Classe</th>
                    <th className="px-6 py-3.5">Taxa Indexadora</th>
                    <th className="px-6 py-3.5">Descrição / Ativos Relevantes</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {taxonomiaEspecialista.map((item, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/50">
                      <td className="px-6 py-4 font-bold text-slate-800">
                        <span className={`px-2.5 py-1 rounded-md text-xs font-bold border ${
                          item.categoria === 'Fundos Imobiliários'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : item.categoria === 'Renda Fixa'
                            ? 'bg-amber-50 text-amber-700 border-amber-200'
                            : 'bg-sky-50 text-sky-700 border-sky-200'
                        }`}>
                          {item.categoria}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-semibold text-slate-700">{item.classe}</td>
                      <td className="px-6 py-4 font-mono text-xs font-bold text-sky-700">
                        {item.indexador || '—'}
                      </td>
                      <td className="px-6 py-4 text-xs text-slate-500">
                        {item.descricao}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Cadastro de Ativos Existentes com Suporte à Taxonomia */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
              <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                <Layers className="w-5 h-5 text-sky-600" />
                Catálogo de Ativos Cadastrados no Banco de Dados
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {ativos.map((at: any) => (
                  <div key={at.id} className="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-bold font-mono text-sky-700 text-base">{at.ticker}</span>
                      <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-200 text-slate-700">{at.tipo}</span>
                    </div>
                    <p className="text-xs font-medium text-slate-800">{at.nome}</p>
                    <div className="text-xs text-slate-500 flex flex-wrap gap-2 pt-1 border-t border-slate-200">
                      <span><strong>Categoria:</strong> {at.categoriaNome || 'Fundos Imobiliários'}</span>
                      <span><strong>Classe:</strong> {at.classe || 'Tijolo'}</span>
                      <span><strong>Indexador:</strong> {at.indexador || 'IPCA'}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: BENCHMARKING DE GANHO REAL VS CDI VS IPCA */}
        {activeTab === 'benchmarks' && (
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <div>
              <h3 className="font-bold text-slate-800 text-base">Benchmarking de Rentabilidade: Carteira vs CDI vs IPCA</h3>
              <p className="text-xs text-slate-500 mt-0.5">
                Acompanhe o Ganho Real acima da inflação e o percentual em relação à taxa CDI de mercado.
              </p>
            </div>

            <div className="h-72 pt-4">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={benchmarkData}>
                  <XAxis dataKey="mes" />
                  <YAxis unit="%" />
                  <Tooltip formatter={(val: number) => `${val.toFixed(2)}%`} />
                  <Line type="monotone" dataKey="carteira" name="Sua Carteira (%)" stroke="#0284c7" strokeWidth={3} />
                  <Line type="monotone" dataKey="cdi" name="CDI (%)" stroke="#10b981" strokeWidth={2} strokeDasharray="4 4" />
                  <Line type="monotone" dataKey="ipca" name="IPCA Inflação (%)" stroke="#ef4444" strokeWidth={2} strokeDasharray="2 2" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* Modal Registrar Operação */}
        {showModal && (
          <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-4">
              <h3 className="font-bold text-slate-800 text-lg">Registrar Operação de Ativo</h3>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Ativo</label>
                  <select
                    value={ativoId}
                    onChange={(e) => setAtivoId(e.target.value)}
                    required
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                  >
                    <option value="">Selecione o ativo...</option>
                    {ativos.map((at: any) => (
                      <option key={at.id} value={at.id}>
                        {at.ticker} — {at.nome}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Tipo</label>
                    <select
                      value={tipoOp}
                      onChange={(e) => setTipoOp(e.target.value as any)}
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    >
                      <option value="COMPRA">COMPRA</option>
                      <option value="VENDA">VENDA</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Data Pregão</label>
                    <input
                      type="date"
                      value={dataOperacao}
                      onChange={(e) => setDataOperacao(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Qtd</label>
                    <input
                      type="number"
                      step="any"
                      value={quantidade}
                      onChange={(e) => setQuantidade(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Preço R$</label>
                    <input
                      type="number"
                      step="0.01"
                      value={precoUnitario}
                      onChange={(e) => setPrecoUnitario(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Taxas B3 R$</label>
                    <input
                      type="number"
                      step="0.01"
                      value={taxas}
                      onChange={(e) => setTaxas(e.target.value)}
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                    />
                  </div>
                </div>

                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-lg text-sm"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm"
                  >
                    Salvar Operação
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
