'use client';

import { useState } from 'react';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { UploadCloud, FileSpreadsheet, CheckCircle2, AlertCircle } from 'lucide-react';

export default function ImportarPage() {
  const [file, setFile] = useState<File | null>(null);
  const [tipoOrigem, setTipoOrigem] = useState<'BTG_FATURA' | 'BTG_EXTRATO'>('BTG_FATURA');
  const [previewData, setPreviewData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [sucesso, setSucesso] = useState('');
  const [erro, setErro] = useState('');

  const handlePreview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;

    setLoading(true);
    setErro('');
    setSucesso('');
    setPreviewData(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('origem', tipoOrigem);

    try {
      const resp = await api.post('/api/extrato/importar/preview', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setPreviewData(resp.data);
    } catch (err: any) {
      setErro(err.response?.data?.mensagem || 'Erro ao processar preview do arquivo.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmar = async () => {
    if (!previewData?.lancamentos) return;
    setLoading(true);
    setErro('');

    try {
      await api.post('/api/extrato/importar/confirmar', previewData.lancamentos);
      setSucesso(`Importação de ${previewData.lancamentos.length} lançamentos confirmada com sucesso!`);
      setPreviewData(null);
      setFile(null);
    } catch (err: any) {
      setErro('Erro ao confirmar importação no banco de dados.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header title="Importar Fatura & Extrato BTG" />
      <main className="p-8 space-y-6 max-w-4xl mx-auto">
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs space-y-6">
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => setTipoOrigem('BTG_FATURA')}
              className={`flex-1 py-3 px-4 rounded-lg font-semibold text-sm border transition-all ${
                tipoOrigem === 'BTG_FATURA'
                  ? 'bg-sky-50 border-sky-500 text-sky-700 shadow-xs'
                  : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100'
              }`}
            >
              Fatura BTG (.xlsx protegido por senha)
            </button>
            <button
              type="button"
              onClick={() => setTipoOrigem('BTG_EXTRATO')}
              className={`flex-1 py-3 px-4 rounded-lg font-semibold text-sm border transition-all ${
                tipoOrigem === 'BTG_EXTRATO'
                  ? 'bg-sky-50 border-sky-500 text-sky-700 shadow-xs'
                  : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100'
              }`}
            >
              Extrato Conta Corrente BTG (.xls)
            </button>
          </div>

          <form onSubmit={handlePreview} className="space-y-4">
            <div className="border-2 border-dashed border-slate-200 rounded-xl p-8 text-center bg-slate-50 hover:bg-slate-100/50 transition-colors">
              <UploadCloud className="w-10 h-10 text-sky-600 mx-auto mb-2" />
              <p className="text-sm font-semibold text-slate-700">
                {file ? file.name : 'Selecione ou solte a planilha da fatura/extrato aqui'}
              </p>
              <p className="text-xs text-slate-400 mt-1">Formato aceito: .xlsx ou .xls</p>
              <input
                type="file"
                accept=".xlsx,.xls"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                className="mt-4 text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-sky-600 file:text-white hover:file:bg-sky-700 cursor-pointer"
              />
            </div>

            <button
              type="submit"
              disabled={!file || loading}
              className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors shadow-md shadow-sky-600/20 disabled:opacity-50"
            >
              {loading ? 'Processando Preview...' : 'Gerar Preview da Importação'}
            </button>
          </form>

          {erro && (
            <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-lg flex items-center gap-2">
              <AlertCircle className="w-5 h-5 shrink-0" />
              {erro}
            </div>
          )}

          {sucesso && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-lg flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 shrink-0" />
              {sucesso}
            </div>
          )}
        </div>

        {previewData && (
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
              <div>
                <h3 className="font-bold text-slate-800">Preview dos Lançamentos</h3>
                <p className="text-xs text-slate-500">
                  Total de itens: {previewData.totalImportados} | Categorizados Auto:{' '}
                  {previewData.totalCategorizadosAuto} | Pendentes: {previewData.totalPendentes}
                </p>
              </div>
              <button
                onClick={handleConfirmar}
                disabled={loading}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg text-sm transition-colors shadow-md shadow-emerald-600/20"
              >
                Confirmar e Salvar Importação
              </button>
            </div>

            <div className="max-h-96 overflow-y-auto border border-slate-200 rounded-lg">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-500 uppercase sticky top-0">
                  <tr>
                    <th className="px-4 py-2.5">Data</th>
                    <th className="px-4 py-2.5">Descrição</th>
                    <th className="px-4 py-2.5">Categoria Sugerida</th>
                    <th className="px-4 py-2.5 text-right">Valor</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {previewData.lancamentos.map((item: any, idx: number) => (
                    <tr key={idx}>
                      <td className="px-4 py-2 text-slate-600">{item.dataLancamento?.substring(0, 10)}</td>
                      <td className="px-4 py-2 font-medium text-slate-800">{item.descricao}</td>
                      <td className="px-4 py-2 text-slate-600">{item.categoriaNome || 'Outros'}</td>
                      <td className="px-4 py-2 text-right font-bold text-slate-800">
                        R$ {Math.abs(item.valor).toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
