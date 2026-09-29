'use client';

import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Download, Filter, FileText } from 'lucide-react';

export default function ExtratoPage() {
  const { data: lancamentos = [], isLoading } = useQuery({
    queryKey: ['lancamentos'],
    queryFn: async () => {
      const resp = await api.get('/api/extrato');
      return resp.data;
    },
  });

  const exportarPdf = async () => {
    const resp = await api.get('/api/extrato/relatorio/pdf', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([resp.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'relatorio_extrato.pdf');
    document.body.appendChild(link);
    link.click();
  };

  const exportarExcel = async () => {
    const resp = await api.get('/api/extrato/relatorio/excel', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([resp.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'relatorio_extrato.xlsx');
    document.body.appendChild(link);
    link.click();
  };

  return (
    <>
      <Header title="Extrato & Gastos" />
      <main className="p-8 space-y-6">
        <div className="flex items-center justify-between">
          <p className="text-sm text-slate-500">Histórico completo de lançamentos importados e categorizados</p>
          <div className="flex items-center gap-3">
            <button
              onClick={exportarPdf}
              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-2 border border-slate-300 transition-colors"
            >
              <FileText className="w-4 h-4 text-rose-600" />
              Exportar PDF
            </button>
            <button
              onClick={exportarExcel}
              className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-2 border border-slate-300 transition-colors"
            >
              <Download className="w-4 h-4 text-emerald-600" />
              Exportar Excel
            </button>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3">Data</th>
                <th className="px-6 py-3">Descrição</th>
                <th className="px-6 py-3">Origem</th>
                <th className="px-6 py-3">Categoria</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3 text-right">Valor</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                    Carregando lançamentos...
                  </td>
                </tr>
              ) : lancamentos.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                    Nenhum lançamento encontrado. Importe uma fatura ou extrato BTG.
                  </td>
                </tr>
              ) : (
                lancamentos.map((l: any) => (
                  <tr key={l.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-4 text-slate-600">{l.dataLancamento?.substring(0, 10)}</td>
                    <td className="px-6 py-4 font-medium text-slate-800">{l.descricao}</td>
                    <td className="px-6 py-4 text-slate-500 text-xs">{l.origem}</td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-1 bg-slate-100 text-slate-700 rounded-md text-xs font-medium border border-slate-200">
                        {l.categoriaNome || 'Outros'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                        l.statusCategorizacao === 'AUTO' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-amber-50 text-amber-700 border border-amber-200'
                      }`}>
                        {l.statusCategorizacao}
                      </span>
                    </td>
                    <td className={`px-6 py-4 text-right font-bold ${l.valor < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                      R$ {Math.abs(l.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </td>
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
