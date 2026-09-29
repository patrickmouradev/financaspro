'use client';

import { useState } from 'react';
import { Header } from '@/components/Header';
import api from '@/lib/axios';
import { Calculator, Scale, CheckCircle2, ArrowRight } from 'lucide-react';

export default function RendaFixaPage() {
  const [tipoAtivo, setTipoAtivo] = useState<'PREFIXADO' | 'POS_CDI' | 'IPCA_MAIS'>('POS_CDI');
  const [valorInvestido, setValorInvestido] = useState('10000');
  const [taxaAno, setTaxaAno] = useState('100'); // 100% do CDI
  const [prazoMeses, setPrazoMeses] = useState('12');
  const [isIsentoIr, setIsIsentoIr] = useState(false);

  const [resultado, setResultado] = useState<any>(null);
  const [loading, setLoading] = useState(false);

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
    } catch (err: any) {
      alert('Erro ao calcular simulação.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header title="Calculadora & Comparador de Renda Fixa" />
      <main className="p-8 space-y-8 max-w-6xl mx-auto">
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
      </main>
    </>
  );
}
