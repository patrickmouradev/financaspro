'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Settings, Lock, Check, Save } from 'lucide-react';

export default function ConfiguracoesPage() {
  const queryClient = useQueryClient();
  const [senhaBtg, setSenhaBtg] = useState('');
  const [sucesso, setSucesso] = useState('');

  const { data: parametros = [] } = useQuery({
    queryKey: ['parametros'],
    queryFn: async () => {
      const resp = await api.get('/api/parametros');
      return resp.data;
    },
  });

  const salvarParametroMutation = useMutation({
    mutationFn: async ({ chave, valor }: { chave: String; valor: string }) => {
      await api.put(`/api/parametros/${chave}`, { valor });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['parametros'] });
      setSucesso('Parâmetros de sistema atualizados com sucesso!');
      setTimeout(() => setSucesso(''), 3000);
    },
  });

  const handleSalvarSenhaBtg = (e: React.FormEvent) => {
    e.preventDefault();
    if (!senhaBtg) return;
    salvarParametroMutation.mutate({ chave: 'senha_fatura_btg', valor: senhaBtg });
  };

  return (
    <>
      <Header title="Configurações de Sistema & Parâmetros" />
      <main className="p-8 space-y-8 max-w-4xl mx-auto">
        {sucesso && (
          <div className="p-4 bg-emerald-50 text-emerald-700 text-sm rounded-lg flex items-center gap-2 border border-emerald-200">
            <Check className="w-5 h-5" />
            {sucesso}
          </div>
        )}

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6">
          <div className="border-b border-slate-100 pb-4">
            <h3 className="font-bold text-slate-800 text-lg flex items-center gap-2">
              <Lock className="w-5 h-5 text-sky-600" />
              Senha de Desproteção da Fatura BTG (.xlsx)
            </h3>
            <p className="text-xs text-slate-500">
              Configure a senha de abertura automática (seu CPF) para desproteger e parsear faturas BTG enviadas.
            </p>
          </div>

          <form onSubmit={handleSalvarSenhaBtg} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                Senha / CPF Cadastrado (Parâmetro: `senha_fatura_btg`)
              </label>
              <input
                type="password"
                value={senhaBtg}
                onChange={(e) => setSenhaBtg(e.target.value)}
                placeholder="Informe o CPF para descriptografar o Excel BTG..."
                className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800"
              />
            </div>

            <button
              type="submit"
              disabled={salvarParametroMutation.isPending}
              className="px-5 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors flex items-center gap-2 shadow-md shadow-sky-600/20"
            >
              <Save className="w-4 h-4" />
              {salvarParametroMutation.isPending ? 'Salvando...' : 'Salvar Parâmetro'}
            </button>
          </form>
        </div>

        {/* Tabela de Todos os Parâmetros */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center gap-2">
            <Settings className="w-5 h-5 text-slate-500" />
            <h3 className="font-bold text-slate-800 text-base">Parâmetros Ativos no Banco de Dados</h3>
          </div>

          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase">
              <tr>
                <th className="px-6 py-3">Chave</th>
                <th className="px-6 py-3">Valor</th>
                <th className="px-6 py-3">Descrição</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {parametros.map((param: any) => (
                <tr key={param.chave} className="hover:bg-slate-50/50">
                  <td className="px-6 py-3.5 font-bold font-mono text-sky-700 text-xs">{param.chave}</td>
                  <td className="px-6 py-3.5 font-mono text-slate-800">
                    {param.chave.includes('senha') ? '••••••••' : param.valor}
                  </td>
                  <td className="px-6 py-3.5 text-slate-500 text-xs">{param.descricao}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </>
  );
}
