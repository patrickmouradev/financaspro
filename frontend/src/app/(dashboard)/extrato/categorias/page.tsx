'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Plus, Tags, Check, Edit2, Trash2, ShieldCheck, Palette, Layers, AlertCircle, X, Wallet, TrendingUp, CreditCard, FileSpreadsheet } from 'lucide-react';

interface Categoria {
  id: number;
  nome: string;
  icone: string;
  cor: string;
  tipo: 'DESPESA' | 'RECEITA';
  regras?: any[];
}

export default function CategoriasPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<'categorias' | 'regras'>('categorias');
  const [contextoFiltro, setContextoFiltro] = useState<'TODOS' | 'CONTA_CORRENTE' | 'INVESTIMENTO' | 'CARTAO' | 'CORRETAGEM'>('TODOS');

  const [sucesso, setSucesso] = useState('');
  const [erro, setErro] = useState('');

  // Form Categoria
  const [editId, setEditId] = useState<number | null>(null);
  const [catNome, setCatNome] = useState('');
  const [catTipo, setCatTipo] = useState<'DESPESA' | 'RECEITA'>('DESPESA');
  const [catCor, setCatCor] = useState('#0284c7');
  const [catIcone, setCatIcone] = useState('Tag');
  const [showCatModal, setShowCatModal] = useState(false);

  // Form Regra
  const [regraCatId, setRegraCatId] = useState('');
  const [palavraChave, setPalavraChave] = useState('');

  // Queries
  const { data: categorias = [], isLoading: loadingCats } = useQuery({
    queryKey: ['categorias'],
    queryFn: async () => {
      const resp = await api.get('/api/categorias');
      return resp.data as Categoria[];
    },
  });

  const { data: regras = [] } = useQuery({
    queryKey: ['regras'],
    queryFn: async () => {
      const resp = await api.get('/api/categorias/regras');
      return resp.data;
    },
  });

  // Mutations Categoria
  const salvarCategoriaMutation = useMutation({
    mutationFn: async () => {
      const payload = { nome: catNome, tipo: catTipo, cor: catCor, icone: catIcone };
      if (editId) {
        await api.put(`/api/categorias/${editId}`, payload);
      } else {
        await api.post('/api/categorias', payload);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      setShowCatModal(false);
      resetCatForm();
      setSucesso(editId ? 'Categoria atualizada com sucesso!' : 'Categoria criada com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
    onError: (err: any) => {
      setErro(err.response?.data?.mensagem || 'Erro ao salvar categoria.');
    }
  });

  const deletarCategoriaMutation = useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/api/categorias/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      setSucesso('Categoria removida com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
    onError: () => {
      setErro('Não foi possível excluir a categoria. Verifique se existem lançamentos vinculados.');
      setTimeout(() => setErro(''), 4000);
    }
  });

  // Mutations Regra
  const criarRegraMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/api/categorias/${regraCatId}/regras?palavraChave=${encodeURIComponent(palavraChave.trim().toUpperCase())}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['regras'] });
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      setPalavraChave('');
      setRegraCatId('');
      setSucesso('Regra por palavra-chave cadastrada com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
  });

  const removerRegraMutation = useMutation({
    mutationFn: async (regraId: number) => {
      await api.delete(`/api/categorias/regras/${regraId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['regras'] });
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      setSucesso('Regra removida com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
  });

  const resetCatForm = () => {
    setEditId(null);
    setCatNome('');
    setCatTipo('DESPESA');
    setCatCor('#0284c7');
    setCatIcone('Tag');
  };

  const handleOpenEditCat = (cat: Categoria) => {
    setEditId(cat.id);
    setCatNome(cat.nome);
    setCatTipo(cat.tipo);
    setCatCor(cat.cor || '#0284c7');
    setCatIcone(cat.icone || 'Tag');
    setShowCatModal(true);
  };

  const handleSubmitCat = (e: React.FormEvent) => {
    e.preventDefault();
    if (!catNome.trim()) return;
    salvarCategoriaMutation.mutate();
  };

  const handleSubmitRegra = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regraCatId || !palavraChave.trim()) return;
    criarRegraMutation.mutate();
  };

  // Filtra categorias por contexto/tabela
  const categoriasFiltradas = categorias.filter((cat) => {
    const nomeUpper = cat.nome.toUpperCase();
    if (contextoFiltro === 'CONTA_CORRENTE') {
      return !['FUNDOS IMOBILIÁRIOS', 'AÇÕES', 'RENDA FIXA', 'FUNDOS DE INVESTIMENTO', 'DIVIDENDOS & PROVENTOS'].includes(nomeUpper);
    }
    if (contextoFiltro === 'INVESTIMENTO') {
      return ['INVESTIMENTOS', 'DIVIDENDOS & PROVENTOS', 'FUNDOS IMOBILIÁRIOS', 'AÇÕES', 'RENDA FIXA', 'FUNDOS DE INVESTIMENTO'].includes(nomeUpper);
    }
    if (contextoFiltro === 'CARTAO') {
      return ['ALIMENTAÇÃO', 'SUPERMERCADO', 'TRANSPORTE', 'SAÚDE & CUIDADOS', 'LAZER & ENTRETENIMENTO', 'COMPRAS', 'OUTROS'].includes(nomeUpper);
    }
    if (contextoFiltro === 'CORRETAGEM') {
      return ['FUNDOS IMOBILIÁRIOS', 'AÇÕES', 'RENDA FIXA', 'FUNDOS DE INVESTIMENTO', 'IMPOSTOS & TAXAS'].includes(nomeUpper);
    }
    return true;
  });

  return (
    <>
      <Header title="Menu Administrativo — Gestão de Categorias por Tabela" />
      <main className="p-8 space-y-6 max-w-6xl mx-auto">
        {/* Top Header & Navigation Tabs */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div>
            <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-sky-600" />
              Administração de Categorias Distintas por Módulo
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Tabelas de categorias separadas para Conta Corrente, Conta Investimento, Fatura do Cartão e Nota de Corretagem B3.
            </p>
          </div>

          <div className="flex items-center gap-2 bg-slate-100 p-1.5 rounded-xl border border-slate-200 self-start sm:self-auto">
            <button
              onClick={() => setActiveTab('categorias')}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                activeTab === 'categorias'
                  ? 'bg-white text-sky-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Tabela de Categorias ({categoriasFiltradas.length})
            </button>
            <button
              onClick={() => setActiveTab('regras')}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                activeTab === 'regras'
                  ? 'bg-white text-sky-700 shadow-xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Regras Automáticas ({regras.length})
            </button>
          </div>
        </div>

        {/* Feedback Banners */}
        {sucesso && (
          <div className="p-4 bg-emerald-50 text-emerald-700 text-sm rounded-xl flex items-center gap-2 border border-emerald-200 shadow-xs">
            <Check className="w-5 h-5 text-emerald-600 shrink-0" />
            <span>{sucesso}</span>
          </div>
        )}

        {erro && (
          <div className="p-4 bg-rose-50 text-rose-700 text-sm rounded-xl flex items-center gap-2 border border-rose-200 shadow-xs">
            <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />
            <span>{erro}</span>
          </div>
        )}

        {/* Sub-Navegação por Contexto/Tabela */}
        {activeTab === 'categorias' && (
          <div className="flex items-center gap-2 bg-white p-2 rounded-xl border border-slate-200 shadow-xs overflow-x-auto">
            <button
              onClick={() => setContextoFiltro('TODOS')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors ${
                contextoFiltro === 'TODOS' ? 'bg-sky-600 text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              Todas as Categorias ({categorias.length})
            </button>

            <button
              onClick={() => setContextoFiltro('CONTA_CORRENTE')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-1.5 ${
                contextoFiltro === 'CONTA_CORRENTE' ? 'bg-sky-600 text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              <Wallet className="w-3.5 h-3.5" />
              Extrato Conta Corrente
            </button>

            <button
              onClick={() => setContextoFiltro('INVESTIMENTO')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-1.5 ${
                contextoFiltro === 'INVESTIMENTO' ? 'bg-sky-600 text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              <TrendingUp className="w-3.5 h-3.5" />
              Extrato Conta Investimento
            </button>

            <button
              onClick={() => setContextoFiltro('CARTAO')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-1.5 ${
                contextoFiltro === 'CARTAO' ? 'bg-sky-600 text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              <CreditCard className="w-3.5 h-3.5" />
              Fatura Cartão de Crédito
            </button>

            <button
              onClick={() => setContextoFiltro('CORRETAGEM')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-1.5 ${
                contextoFiltro === 'CORRETAGEM' ? 'bg-sky-600 text-white' : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              <FileSpreadsheet className="w-3.5 h-3.5" />
              Nota de Corretagem B3
            </button>
          </div>
        )}

        {/* ABA 1: CRUD COMPLETO DE CATEGORIAS */}
        {activeTab === 'categorias' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                <Layers className="w-5 h-5 text-slate-500" />
                Categorias do Módulo Selecionado
              </h3>

              <button
                onClick={() => {
                  resetCatForm();
                  setShowCatModal(true);
                }}
                className="px-4 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-xs transition-colors flex items-center gap-2 shadow-md shadow-sky-600/20"
              >
                <Plus className="w-4 h-4" />
                Nova Categoria
              </button>
            </div>

            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  <tr>
                    <th className="px-6 py-3.5">Categoria</th>
                    <th className="px-6 py-3.5">Tipo</th>
                    <th className="px-6 py-3.5">Cor / Identificador</th>
                    <th className="px-6 py-3.5">Regras Vinculadas</th>
                    <th className="px-6 py-3.5 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {loadingCats ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                        Carregando tabela de categorias...
                      </td>
                    </tr>
                  ) : categoriasFiltradas.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-8 text-center text-slate-400">
                        Nenhuma categoria encontrada para a tabela selecionada.
                      </td>
                    </tr>
                  ) : (
                    categoriasFiltradas.map((cat) => (
                      <tr key={cat.id} className="hover:bg-slate-50/50">
                        <td className="px-6 py-4 font-bold text-slate-800 flex items-center gap-3">
                          <div
                            className="w-4 h-4 rounded-full border shadow-xs shrink-0"
                            style={{ backgroundColor: cat.cor || '#0284c7' }}
                          />
                          <span>{cat.nome}</span>
                        </td>
                        <td className="px-6 py-4">
                          <span
                            className={`px-2.5 py-1 rounded-md text-xs font-bold border ${
                              cat.tipo === 'RECEITA'
                                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                                : 'bg-rose-50 text-rose-700 border-rose-200'
                            }`}
                          >
                            {cat.tipo}
                          </span>
                        </td>
                        <td className="px-6 py-4 font-mono text-xs text-slate-500">
                          {cat.cor || '#0284c7'}
                        </td>
                        <td className="px-6 py-4 text-xs font-medium text-slate-600">
                          {cat.regras ? `${cat.regras.length} palavra(s)-chave` : '0 regras'}
                        </td>
                        <td className="px-6 py-4 text-right space-x-2 whitespace-nowrap">
                          <button
                            onClick={() => handleOpenEditCat(cat)}
                            className="p-1.5 text-sky-600 hover:bg-sky-50 rounded-lg transition-colors"
                            title="Editar Categoria"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              if (confirm(`Deseja realmente excluir a categoria "${cat.nome}"?`)) {
                                deletarCategoriaMutation.mutate(cat.id);
                              }
                            }}
                            className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                            title="Excluir Categoria"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ABA 2: REGRAS AUTOMÁTICAS POR PALAVRA-CHAVE */}
        {activeTab === 'regras' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
              <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                <Plus className="w-5 h-5 text-sky-600" />
                Cadastrar Nova Regra de Categorização Automática
              </h3>

              <form onSubmit={handleSubmitRegra} className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1 uppercase">Palavra-Chave</label>
                  <input
                    type="text"
                    value={palavraChave}
                    onChange={(e) => setPalavraChave(e.target.value)}
                    placeholder="Ex: UBER, IFOOD, CARREFOUR, XPML11"
                    required
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1 uppercase">Categoria Mapeada</label>
                  <select
                    value={regraCatId}
                    onChange={(e) => setRegraCatId(e.target.value)}
                    required
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                  >
                    <option value="">Selecione a categoria...</option>
                    {categorias.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.nome} ({cat.tipo})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex items-end">
                  <button
                    type="submit"
                    disabled={criarRegraMutation.isPending}
                    className="w-full py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors shadow-md shadow-sky-600/20"
                  >
                    {criarRegraMutation.isPending ? 'Salvando...' : 'Salvar Regra'}
                  </button>
                </div>
              </form>
            </div>

            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
              <div className="p-4 border-b border-slate-100 flex items-center gap-2">
                <Tags className="w-5 h-5 text-slate-500" />
                <h3 className="font-bold text-slate-800 text-base">Regras por Palavra-Chave Cadastradas</h3>
              </div>

              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
                  <tr>
                    <th className="px-6 py-3">Palavra-Chave</th>
                    <th className="px-6 py-3">Categoria Vinculada</th>
                    <th className="px-6 py-3 text-right">Ação</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {regras.map((regra: any) => (
                    <tr key={regra.id} className="hover:bg-slate-50/50">
                      <td className="px-6 py-3.5 font-semibold text-sky-700 font-mono text-xs">{regra.palavraChave}</td>
                      <td className="px-6 py-3.5 text-slate-800 font-medium">{regra.categoriaNome}</td>
                      <td className="px-6 py-3.5 text-right">
                        <button
                          onClick={() => removerRegraMutation.mutate(regra.id)}
                          className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                          title="Remover Regra"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* MODAL CRIAR / EDITAR CATEGORIA */}
      {showCatModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-md p-6 space-y-5">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-800 text-base">
                {editId ? 'Editar Categoria' : 'Cadastrar Nova Categoria'}
              </h3>
              <button onClick={() => setShowCatModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitCat} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Nome da Categoria</label>
                <input
                  type="text"
                  value={catNome}
                  onChange={(e) => setCatNome(e.target.value)}
                  placeholder="Ex: Alimentação, Moradia, Fundos Imobiliários"
                  required
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">Tipo de Lançamento</label>
                <select
                  value={catTipo}
                  onChange={(e) => setCatTipo(e.target.value as 'DESPESA' | 'RECEITA')}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
                >
                  <option value="DESPESA">DESPESA (Saída)</option>
                  <option value="RECEITA">RECEITA (Entrada)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1 flex items-center gap-2">
                  <Palette className="w-4 h-4 text-sky-600" />
                  Cor Identificadora
                </label>
                <div className="flex items-center gap-3">
                  <input
                    type="color"
                    value={catCor}
                    onChange={(e) => setCatCor(e.target.value)}
                    className="w-10 h-10 rounded-lg border border-slate-200 cursor-pointer"
                  />
                  <input
                    type="text"
                    value={catCor}
                    onChange={(e) => setCatCor(e.target.value)}
                    className="flex-1 p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm font-mono text-slate-800"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCatModal(false)}
                  className="px-4 py-2 text-slate-600 text-xs font-semibold hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={salvarCategoriaMutation.isPending}
                  className="px-5 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-xs transition-colors shadow-md shadow-sky-600/20"
                >
                  {salvarCategoriaMutation.isPending ? 'Salvando...' : 'Salvar Categoria'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
