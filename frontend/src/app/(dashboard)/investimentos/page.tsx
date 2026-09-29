'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Plus, TrendingUp, DollarSign, PieChart } from 'lucide-react';

export default function InvestimentosPage() {
  const queryClient = useQueryClient();
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
      const resp = await api.get('/api/investimentos/carteira');
      return resp.data;
    },
  });

  const { data: ativos = [] } = useQuery({
    queryKey: ['ativos'],
    queryFn: async () => {
      const resp = await api.get('/api/ativos');
      return resp.data;
    },
  });

  const { data: operacoes = [] } = useQuery({
    queryKey: ['operacoes'],
    queryFn: async () => {
      const resp = await api.get('/api/operacoes');
      return resp.data;
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

  return (
    <>
      <Header title="Módulo de Investimentos — Ações & Carteira" />
      <main className="p-8 space-y-8">
        <div className="flex justify-between items-center">
          <div>
            <p className="text-sm text-slate-500">Gestão de posições, preço médio e rentabilidade acumulada</p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="px-4 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm flex items-center gap-2 shadow-md shadow-sky-600/20"
          >
            <Plus className="w-4 h-4" />
            Nova Operação (Compra/Venda)
          </button>
        </div>

        {/* Resumo da Carteira */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Total Investido</p>
            <h3 className="text-2xl font-bold text-slate-800 mt-1">
              R$ {(carteira?.valorTotalInvestido || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </h3>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Valor Atual da Carteira</p>
            <h3 className="text-2xl font-bold text-sky-700 mt-1">
              R$ {(carteira?.valorTotalAtual || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </h3>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Lucro / Prejuízo</p>
            <h3 className={`text-2xl font-bold mt-1 ${
              (carteira?.lucroPrejuizoTotal || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
            }`}>
              R$ {(carteira?.lucroPrejuizoTotal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </h3>
          </div>
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <p className="text-xs font-semibold uppercase text-slate-400">Rentabilidade Total</p>
            <h3 className={`text-2xl font-bold mt-1 ${
              (carteira?.variacaoPercentualTotal || 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
            }`}>
              {(carteira?.variacaoPercentualTotal || 0).toFixed(2)} %
            </h3>
          </div>
        </div>

        {/* Tabela da Carteira */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-sky-600" />
            <h3 className="font-bold text-slate-800 text-base">Posição Atual dos Ativos</h3>
          </div>
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3">Ticker</th>
                <th className="px-6 py-3">Nome</th>
                <th className="px-6 py-3 text-right">Qtd</th>
                <th className="px-6 py-3 text-right">Preço Médio</th>
                <th className="px-6 py-3 text-right">Cotação Atual</th>
                <th className="px-6 py-3 text-right">Total Investido</th>
                <th className="px-6 py-3 text-right">Valor Atual</th>
                <th className="px-6 py-3 text-right">Lucro/Prejuízo</th>
                <th className="px-6 py-3 text-right">% Carteira</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {carteira?.itens?.length === 0 || !carteira ? (
                <tr>
                  <td colSpan={9} className="px-6 py-8 text-center text-slate-400">
                    Nenhum ativo com posição aberta. Registre uma compra acima.
                  </td>
                </tr>
              ) : (
                carteira.itens.map((item: any) => (
                  <tr key={item.ticker} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 font-bold text-sky-700">{item.ticker}</td>
                    <td className="px-6 py-4 font-medium text-slate-800">{item.nomeAtivo}</td>
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

        {/* Modal de Operação */}
        {showModal && (
          <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-4">
              <h3 className="font-bold text-slate-800 text-lg">Registrar Operação</h3>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Ativo</label>
                  <select
                    value={ativoId}
                    onChange={(e) => setAtivoId(e.target.value)}
                    required
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
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
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
                    >
                      <option value="COMPRA">COMPRA</option>
                      <option value="VENDA">VENDA</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Data</label>
                    <input
                      type="date"
                      value={dataOperacao}
                      onChange={(e) => setDataOperacao(e.target.value)}
                      required
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
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
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
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
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Taxas R$</label>
                    <input
                      type="number"
                      step="0.01"
                      value={taxas}
                      onChange={(e) => setTaxas(e.target.value)}
                      className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm"
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
