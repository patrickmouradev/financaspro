'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Plus, Tags, Check } from 'lucide-react';

export default function CategoriasPage() {
  const queryClient = useQueryClient();
  const [categoriaId, setCategoriaId] = useState('');
  const [palavraChave, setPalavraChave] = useState('');
  const [sucesso, setSucesso] = useState('');

  const { data: categorias = [] } = useQuery({
    queryKey: ['categorias'],
    queryFn: async () => {
      const resp = await api.get('/api/categorias');
      return resp.data;
    },
  });

  const { data: regras = [] } = useQuery({
    queryKey: ['regras'],
    queryFn: async () => {
      const resp = await api.get('/api/categorias/regras');
      return resp.data;
    },
  });

  const criarRegraMutation = useMutation({
    mutationFn: async () => {
      await api.post('/api/categorias/regras', {
        categoriaId: Number(categoriaId),
        palavraChave: palavraChave.trim().toUpperCase(),
        prioridade: 1,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['regras'] });
      setPalavraChave('');
      setCategoriaId('');
      setSucesso('Regra adicionada com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoriaId || !palavraChave) return;
    criarRegraMutation.mutate();
  };

  return (
    <>
      <Header title="Categorias & Regras de Categorização" />
      <main className="p-8 space-y-8 max-w-5xl mx-auto">
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs space-y-4">
          <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
            <Plus className="w-5 h-5 text-sky-600" />
            Adicionar Nova Regra por Palavra-Chave
          </h3>

          {sucesso && (
            <div className="p-3 bg-emerald-50 text-emerald-700 text-sm rounded-lg flex items-center gap-2 border border-emerald-200">
              <Check className="w-4 h-4" />
              {sucesso}
            </div>
          )}

          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1 uppercase">Categoria</label>
              <select
                value={categoriaId}
                onChange={(e) => setCategoriaId(e.target.value)}
                required
                className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
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
              <label className="block text-xs font-semibold text-slate-600 mb-1 uppercase">Palavra-Chave</label>
              <input
                type="text"
                value={palavraChave}
                onChange={(e) => setPalavraChave(e.target.value)}
                placeholder="Ex: UBER, IFOOD, CARREFOUR"
                required
                className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
              />
            </div>

            <div className="flex items-end">
              <button
                type="submit"
                disabled={criarRegraMutation.isPending}
                className="w-full py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors shadow-md shadow-sky-600/20"
              >
                {criarRegraMutation.isPending ? 'Salvando...' : 'Cadastrar Regra'}
              </button>
            </div>
          </form>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center gap-2">
            <Tags className="w-5 h-5 text-slate-500" />
            <h3 className="font-bold text-slate-800 text-base">Regras Cadastradas no Sistema</h3>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3">Palavra-Chave</th>
                <th className="px-6 py-3">Categoria Mapeada</th>
                <th className="px-6 py-3">Prioridade</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {regras.map((regra: any) => (
                <tr key={regra.id} className="hover:bg-slate-50/50">
                  <td className="px-6 py-3.5 font-semibold text-sky-700 font-mono text-xs">{regra.palavraChave}</td>
                  <td className="px-6 py-3.5 text-slate-800 font-medium">{regra.categoriaNome}</td>
                  <td className="px-6 py-3.5 text-slate-500 text-xs">{regra.prioridade}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </>
  );
}
