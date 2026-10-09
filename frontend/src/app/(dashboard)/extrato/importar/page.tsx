'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { UploadCloud, CheckCircle2, AlertCircle, Lock, Tag, FileText, SlidersHorizontal, Edit3, ChevronDown, ChevronUp } from 'lucide-react';

export default function ImportarPage() {
  const [file, setFile] = useState<File | null>(null);
  const [tipoArquivo, setTipoArquivo] = useState<string>('AUTO');
  const [senha, setSenha] = useState('');
  const [previewData, setPreviewData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [sucesso, setSucesso] = useState('');
  const [erro, setErro] = useState('');
  const [expandedRows, setExpandedRows] = useState<Record<number, boolean>>({});

  // Busca categorias para o dropdown de edição no preview
  const { data: categorias = [] } = useQuery({
    queryKey: ['categorias'],
    queryFn: async () => {
      try {
        const resp = await api.get('/api/categorias');
        return resp.data;
      } catch {
        return [];
      }
    },
  });

  const handlePreview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;

    setLoading(true);
    setErro('');
    setSucesso('');
    setPreviewData(null);

    const formData = new FormData();
    formData.append('arquivo', file);
    formData.append('tipoArquivo', tipoArquivo);
    if (senha.trim()) {
      formData.append('senha', senha.trim());
    }

    try {
      const resp = await api.post('/api/extrato/importar', formData);
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
      setSucesso(`Importação de ${previewData.lancamentos.length} lançamentos confirmada e salva com sucesso!`);
      setPreviewData(null);
      setFile(null);
    } catch (err: any) {
      setErro('Erro ao confirmar importação no banco de dados.');
    } finally {
      setLoading(false);
    }
  };

  const handleItemChange = (index: number, field: string, value: any) => {
    if (!previewData?.lancamentos) return;
    const novosLancamentos = [...previewData.lancamentos];
    novosLancamentos[index] = {
      ...novosLancamentos[index],
      [field]: value === '' ? null : value,
    };
    setPreviewData({
      ...previewData,
      lancamentos: novosLancamentos,
    });
  };

  const toggleExpandRow = (index: number) => {
    setExpandedRows((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  };

  const isRendaFixaItem = (item: any) => {
    return (
      item.tipo === 'COMPRA_RENDA_FIXA' ||
      (item.origem && item.origem.toUpperCase().includes('RENDA_FIXA')) ||
      item.produto ||
      item.emissor ||
      item.indice ||
      item.liquidez
    );
  };

  return (
    <>
      <Header title="Central de Importação — PDFs, Extratos & Notas de Corretagem" />
      <main className="p-8 space-y-6 max-w-6xl mx-auto">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-6">
          <form onSubmit={handlePreview} className="space-y-5">
            {/* Seletor de Tipo de Arquivo (Combo Box) */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <SlidersHorizontal className="w-3.5 h-3.5 text-sky-600" />
                Selecione o Tipo do Arquivo a Importar
              </label>
              <select
                value={tipoArquivo}
                onChange={(e) => setTipoArquivo(e.target.value)}
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:ring-2 focus:ring-sky-500 focus:outline-hidden"
              >
                <option value="AUTO">🎯 Detecção Automática (Recomendado)</option>
                <option value="COMPROVANTE_RENDA_FIXA">🏦 Comprovante de Aplicação em Renda Fixa (AUVP / BTG)</option>
                <option value="NOTA_CORRETAGEM_RENDA_FIXA">🧾 Nota de Corretagem Renda Fixa (B3 / BTG)</option>
                <option value="EXTRATO_INVESTIMENTO">📈 Extrato de Conta Investimento (AUVP / BTG Pactual)</option>

                <option value="EXTRATO_CONTA_CORRENTE">💳 Extrato de Conta Corrente</option>
                <option value="FATURA_CARTAO">📄 Fatura de Cartão de Crédito BTG</option>
                <option value="NOTA_CORRETAGEM">📜 Nota de Corretagem B3 / BTG (Ações & FIIs)</option>
              </select>
            </div>

            {/* Zona de Upload (PDF, XLSX, XLS) */}
            <div className="border-2 border-dashed border-slate-200 rounded-2xl p-8 text-center bg-slate-50 hover:bg-slate-100/50 transition-colors">
              <UploadCloud className="w-10 h-10 text-sky-600 mx-auto mb-2" />
              <p className="text-sm font-semibold text-slate-700">
                {file ? file.name : 'Selecione ou solte o arquivo PDF, XLSX ou XLS aqui'}
              </p>
              <p className="text-xs text-slate-400 mt-1">
                Formatos suportados: PDF (Comprovante Renda Fixa, Nota de Corretagem, Extrato Investimento), XLSX ou XLS
              </p>
              <input
                type="file"
                accept=".pdf,.xlsx,.xls"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                className="mt-4 text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-sky-600 file:text-white hover:file:bg-sky-700 cursor-pointer"
              />
            </div>

            {/* Input de Senha / CPF */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5 text-slate-400" />
                Senha / CPF de Abertura do Arquivo (Necessário se o PDF/Excel for protegido)
              </label>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="Informe o CPF para desproteger o arquivo BTG / AUVP..."
                className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:ring-2 focus:ring-sky-500 focus:outline-hidden"
              />
            </div>

            <button
              type="submit"
              disabled={!file || loading}
              className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-xl text-sm transition-colors shadow-md shadow-sky-600/20 disabled:opacity-50"
            >
              {loading ? 'Processando Preview...' : 'Gerar Preview da Importação'}
            </button>
          </form>

          {erro && (
            <div className="p-4 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-xl flex items-center gap-2">
              <AlertCircle className="w-5 h-5 shrink-0" />
              {erro}
            </div>
          )}

          {sucesso && (
            <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 shrink-0" />
              {sucesso}
            </div>
          )}
        </div>

        {/* Preview dos Lançamentos com Permissão para Editar Antes de Salvar */}
        {previewData && (
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
              <div>
                <h3 className="font-bold text-slate-800 text-base flex items-center gap-2">
                  <Edit3 className="w-4 h-4 text-sky-600" />
                  Preview Editável dos Lançamentos Extraídos
                </h3>
                <p className="text-xs text-slate-500">
                  Verifique e edite os dados extraídos (Data, Valor, Categoria, Índice, Taxa, Emissor, Liquidez) antes de salvar no Banco de Dados.
                </p>
              </div>
              <button
                onClick={handleConfirmar}
                disabled={loading || !previewData.lancamentos || previewData.lancamentos.length === 0}
                className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl text-sm transition-colors shadow-md shadow-emerald-600/20 disabled:opacity-50 shrink-0"
              >
                Confirmar e Salvar no Banco
              </button>
            </div>

            {previewData.alertas && previewData.alertas.length > 0 && (
              <div className="p-3 bg-amber-50 border border-amber-200 text-amber-800 text-xs rounded-xl space-y-1">
                {previewData.alertas.map((alerta: string, aIdx: number) => (
                  <p key={aIdx}>⚠️ {alerta}</p>
                ))}
              </div>
            )}

            <div className="max-h-[600px] overflow-y-auto border border-slate-200 rounded-xl">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-500 uppercase sticky top-0 z-10">
                  <tr>
                    <th className="px-3 py-2.5 w-8">#</th>
                    <th className="px-3 py-2.5">Data Lançamento</th>
                    <th className="px-3 py-2.5">Descrição</th>
                    <th className="px-3 py-2.5">Valor (R$)</th>
                    <th className="px-3 py-2.5">Categoria Sugerida</th>
                    <th className="px-3 py-2.5 text-center">Editar Detalhes RF</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {previewData.lancamentos?.map((item: any, idx: number) => {
                    const isRF = isRendaFixaItem(item);
                    const isExpanded = expandedRows[idx] ?? isRF;

                    return (
                      <tr key={idx} className="hover:bg-slate-50/50 flex-col">
                        <td colSpan={6} className="p-0">
                          <div className="p-3 grid grid-cols-12 gap-3 items-center border-b border-slate-100">
                            <div className="col-span-1 text-slate-400 font-mono text-[10px]">
                              {idx + 1}
                            </div>

                            {/* Data */}
                            <div className="col-span-2">
                              <input
                                type="text"
                                value={item.dataLancamento ? item.dataLancamento.split('T')[0] : ''}
                                onChange={(e) => handleItemChange(idx, 'dataLancamento', e.target.value)}
                                className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded text-xs font-mono font-medium text-slate-800"
                              />
                            </div>

                            {/* Descrição */}
                            <div className="col-span-4">
                              <input
                                type="text"
                                value={item.descricao || ''}
                                onChange={(e) => handleItemChange(idx, 'descricao', e.target.value)}
                                className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded text-xs font-semibold text-slate-800"
                              />
                            </div>

                            {/* Valor */}
                            <div className="col-span-2">
                              <input
                                type="number"
                                step="0.01"
                                value={item.valor ?? ''}
                                onChange={(e) => handleItemChange(idx, 'valor', Number(e.target.value))}
                                className={`w-full p-1.5 border border-slate-200 rounded text-xs font-bold font-mono text-right ${
                                  (item.valor ?? 0) < 0 ? 'text-rose-600 bg-rose-50/30' : 'text-emerald-600 bg-emerald-50/30'
                                }`}
                              />
                            </div>

                            {/* Categoria */}
                            <div className="col-span-2">
                              <select
                                value={item.categoriaId || ''}
                                onChange={(e) => {
                                  const catId = e.target.value ? Number(e.target.value) : null;
                                  const catEncontrada = categorias.find((c: any) => c.id === catId);
                                  handleItemChange(idx, 'categoriaId', catId);
                                  handleItemChange(idx, 'categoriaNome', catEncontrada?.nome || null);
                                }}
                                className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded text-xs font-medium text-slate-700"
                              >
                                <option value="">Sem Categoria</option>
                                {categorias.map((cat: any) => (
                                  <option key={cat.id} value={cat.id}>
                                    {cat.nome}
                                  </option>
                                ))}
                              </select>
                            </div>

                            {/* Botão para Expandir Detalhes de Renda Fixa */}
                            <div className="col-span-1 text-center">
                              <button
                                type="button"
                                onClick={() => toggleExpandRow(idx)}
                                className={`p-1.5 rounded-lg border text-xs font-medium inline-flex items-center justify-center gap-1 ${
                                  isExpanded
                                    ? 'bg-sky-100 border-sky-300 text-sky-800'
                                    : 'bg-slate-100 border-slate-200 text-slate-600 hover:bg-slate-200'
                                }`}
                                title="Expandir/Recolher Detalhes de Renda Fixa"
                              >
                                {isExpanded ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
                              </button>
                            </div>
                          </div>

                          {/* Painel Expandido: Detalhes Renda Fixa (Índice, Taxa, Liquidez, Vencimento, Produto, Emissor, CNPJ) */}
                          {isExpanded && (
                            <div className="px-6 py-4 bg-sky-50/40 border-b border-sky-100 grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Produto / Modalidade
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo (ex: CDB, LCI, LCA)"
                                  value={item.produto ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'produto', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs text-slate-800 font-medium"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Emissor
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo (ex: Banco BTG Pactual)"
                                  value={item.emissor ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'emissor', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs text-slate-800 font-medium"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  CNPJ Emissor
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo"
                                  value={item.cnpjEmissor ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'cnpjEmissor', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs font-mono text-slate-800"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Liquidez
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo (ex: No Vencimento, Diária)"
                                  value={item.liquidez ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'liquidez', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs text-slate-800 font-medium"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Índice (CDI / IPCA / SELIC / PRE)
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo"
                                  value={item.indice ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'indice', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs font-bold text-amber-800"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Porcentagem / Taxa
                                </label>
                                <input
                                  type="text"
                                  placeholder="Nulo (ex: 100% ou 3,5%)"
                                  value={item.taxa ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'taxa', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs font-bold text-sky-800"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Data da Aplicação / Compra
                                </label>
                                <input
                                  type="date"
                                  value={item.dataAplicacao ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'dataAplicacao', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs text-slate-800 font-mono"
                                />
                              </div>

                              <div>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">
                                  Data de Vencimento
                                </label>
                                <input
                                  type="date"
                                  value={item.dataVencimento ?? ''}
                                  onChange={(e) => handleItemChange(idx, 'dataVencimento', e.target.value)}
                                  className="w-full p-1.5 bg-white border border-slate-200 rounded text-xs text-slate-800 font-mono"
                                />
                              </div>
                            </div>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
