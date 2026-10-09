'use client';

import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import { MonthSelector } from '@/components/MonthSelector';
import api from '@/lib/axios';
import { Download, FileText, Edit2, X, Check, Tag, CreditCard, Wallet } from 'lucide-react';

export default function ExtratoPage() {
  const [selectedMonth, setSelectedMonth] = useState<string>('');
  const [editingLancamento, setEditingLancamento] = useState<any>(null);
  const [editForm, setEditForm] = useState<{ categoriaId: string; descricao: string; valor: string }>({
    categoriaId: '',
    descricao: '',
    valor: '',
  });
  const [isSaving, setIsSaving] = useState(false);

  const queryClient = useQueryClient();

  const { data: lancamentos = [], isLoading } = useQuery({
    queryKey: ['lancamentos', selectedMonth],
    queryFn: async () => {
      const url = selectedMonth ? `/api/extrato/lancamentos?anoMes=${selectedMonth}` : '/api/extrato/lancamentos';
      const resp = await api.get(url);
      return resp.data;
    },
  });

  const { data: categorias = [] } = useQuery({
    queryKey: ['categorias'],
    queryFn: async () => {
      const resp = await api.get('/api/categorias');
      return resp.data;
    },
  });

  // Filtro estrito para Conta Corrente e Gastos Domésticos (exclui notas de corretagem e investimentos)
  const lancamentosContaCorrente = lancamentos.filter((l: any) => {
    const origem = (l.origem || '').toUpperCase();
    const tipo = (l.tipo || '').toUpperCase();
    const cat = (l.categoriaNome || '').toUpperCase();

    const ehInvestimento = (
      origem.includes('CORRETAGEM') ||
      origem.includes('EXTRATO_INVESTIMENTO') ||
      tipo === 'COMPRA_ATIVO' ||
      tipo === 'VENDA_ATIVO' ||
      tipo === 'DIVIDENDO' ||
      tipo === 'RENDIMENTO' ||
      cat.includes('FUNDOS IMOBILIÁRIOS') ||
      cat.includes('AÇÕES') ||
      cat.includes('RENDA FIXA') ||
      cat.includes('PROVENTOS')
    );

    return !ehInvestimento;
  });

  const exportarPdf = async () => {
    const url = selectedMonth ? `/api/extrato/relatorio/pdf?anoMes=${selectedMonth}` : '/api/extrato/relatorio/pdf';
    const resp = await api.get(url, { responseType: 'blob' });
    const blobUrl = window.URL.createObjectURL(new Blob([resp.data]));
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', `extrato_conta_corrente_${selectedMonth || 'relatorio'}.pdf`);
    document.body.appendChild(link);
    link.click();
  };

  const exportarExcel = async () => {
    const url = selectedMonth ? `/api/extrato/relatorio/excel?anoMes=${selectedMonth}` : '/api/extrato/relatorio/excel';
    const resp = await api.get(url, { responseType: 'blob' });
    const blobUrl = window.URL.createObjectURL(new Blob([resp.data]));
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', `extrato_conta_corrente_${selectedMonth || 'relatorio'}.xlsx`);
    document.body.appendChild(link);
    link.click();
  };

  const openEditModal = (l: any) => {
    setEditingLancamento(l);
    setEditForm({
      categoriaId: l.categoriaId ? String(l.categoriaId) : '',
      descricao: l.descricao || '',
      valor: String(l.valor ?? ''),
    });
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingLancamento) return;
    setIsSaving(true);
    try {
      await api.put(`/api/extrato/lancamentos/${editingLancamento.id}`, {
        categoriaId: editForm.categoriaId ? Number(editForm.categoriaId) : null,
        descricao: editForm.descricao,
        valor: editForm.valor ? Number(editForm.valor) : undefined,
      });
      queryClient.invalidateQueries({ queryKey: ['lancamentos'] });
      queryClient.invalidateQueries({ queryKey: ['resumoMensal'] });
      setEditingLancamento(null);
    } catch (err) {
      alert('Erro ao atualizar lançamento.');
    } finally {
      setIsSaving(false);
    }
  };

  const formatDataHora = (dataStr: string) => {
    if (!dataStr) return '-';
    const d = new Date(dataStr);
    if (isNaN(d.getTime())) return dataStr;
    const dateFormatted = d.toLocaleDateString('pt-BR');
    const timeFormatted = d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    return timeFormatted !== '00:00' ? `${dateFormatted} ${timeFormatted}` : dateFormatted;
  };

  const getTransacaoText = (l: any) => {
    if (l.observacao && l.observacao.includes('Transação:')) {
      const match = l.observacao.match(/Transação:\s*([^|]+)/);
      if (match && match[1]?.trim()) {
        return match[1].trim();
      }
    }
    if (l.tipo) {
      return l.tipo.replace(/_/g, ' ');
    }
    return 'Geral';
  };

  return (
    <>
      <Header title="Extrato da Conta Corrente & Gastos Domésticos" />
      <main className="p-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <Wallet className="w-5 h-5 text-sky-600" />
              Extrato da Conta Corrente
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Histórico de despesas domésticas, contas pagas e movimentações do dia a dia (exclusivo para Conta Corrente).
            </p>
          </div>
          <div className="flex items-center gap-3">
            <MonthSelector
              selectedMonth={selectedMonth}
              onChange={(m) => setSelectedMonth(m)}
            />

            <button
              onClick={exportarPdf}
              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-2 border border-slate-300 transition-colors shrink-0"
            >
              <FileText className="w-4 h-4 text-rose-600" />
              Exportar PDF
            </button>
            <button
              onClick={exportarExcel}
              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-2 border border-slate-300 transition-colors shrink-0"
            >
              <Download className="w-4 h-4 text-emerald-600" />
              Exportar Excel
            </button>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3.5">Data e hora</th>
                <th className="px-6 py-3.5">Categoria</th>
                <th className="px-6 py-3.5">Transação</th>
                <th className="px-6 py-3.5">Descrição</th>
                <th className="px-6 py-3.5 text-right">Valor</th>
                <th className="px-6 py-3.5 text-center">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                    Carregando lançamentos...
                  </td>
                </tr>
              ) : lancamentosContaCorrente.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                    Nenhum lançamento de conta corrente encontrado para o período selecionado.
                  </td>
                </tr>
              ) : (
                lancamentosContaCorrente.map((l: any) => (
                  <tr key={l.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 text-slate-600 whitespace-nowrap text-xs">
                      {formatDataHora(l.dataLancamento)}
                    </td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 bg-slate-100 text-slate-700 rounded-md text-xs font-medium border border-slate-200 inline-flex items-center gap-1.5">
                        <Tag className="w-3 h-3 text-slate-400" />
                        {l.categoriaNome || 'Pendente'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-600 text-xs font-medium">
                      {getTransacaoText(l)}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-800">{l.descricao}</td>
                    <td className={`px-6 py-4 text-right font-bold whitespace-nowrap ${l.valor < 0 ? 'text-rose-600' : 'text-sky-600'}`}>
                      {l.valor < 0
                        ? `- R$ ${Math.abs(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                        : `+ R$ ${Number(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                      }
                    </td>
                    <td className="px-6 py-4 text-center">
                      <button
                        onClick={() => openEditModal(l)}
                        className="p-1.5 hover:bg-slate-100 text-slate-600 hover:text-sky-600 rounded-lg transition-colors"
                        title="Editar Lançamento"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Modal de Edição */}
        {editingLancamento && (
          <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4">
            <div className="bg-white rounded-xl shadow-xl border border-slate-200 w-full max-w-md p-6 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <h3 className="font-bold text-slate-800 text-base">Editar Lançamento</h3>
                <button
                  onClick={() => setEditingLancamento(null)}
                  className="p-1 text-slate-400 hover:text-slate-600 rounded-lg"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleSaveEdit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                    Categoria
                  </label>
                  <select
                    value={editForm.categoriaId}
                    onChange={(e) => setEditForm({ ...editForm, categoriaId: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:ring-2 focus:ring-sky-500 focus:outline-hidden"
                  >
                    <option value="">Selecione a categoria...</option>
                    {categorias.map((cat: any) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.nome} ({cat.tipo})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                    Descrição
                  </label>
                  <input
                    type="text"
                    value={editForm.descricao}
                    onChange={(e) => setEditForm({ ...editForm, descricao: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:ring-2 focus:ring-sky-500 focus:outline-hidden"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                    Valor (R$)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={editForm.valor}
                    onChange={(e) => setEditForm({ ...editForm, valor: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:ring-2 focus:ring-sky-500 focus:outline-hidden"
                    required
                  />
                </div>

                <div className="flex items-center justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setEditingLancamento(null)}
                    className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={isSaving}
                    className="px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg transition-colors flex items-center gap-1.5 disabled:opacity-50"
                  >
                    <Check className="w-4 h-4" />
                    {isSaving ? 'Salvando...' : 'Salvar'}
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
