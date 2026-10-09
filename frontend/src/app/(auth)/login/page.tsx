'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import api from '@/lib/axios';
import { useAuthStore } from '@/store/auth.store';
import { Lock, Mail, ArrowRight, AlertCircle } from 'lucide-react';

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [email, setEmail] = useState('patrickmoura@gmail.com');
  const [senha, setSenha] = useState('140908');
  const [erro, setErro] = useState('');
  const [info, setInfo] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const reason = searchParams.get('reason');
    if (reason === 'idle') {
      setInfo('Sua sessão expirou por inatividade (20 min sem uso). Faça login novamente.');
    } else if (reason === 'expired') {
      setInfo('Sua sessão expirou. Por favor, faça login novamente.');
    }
  }, [searchParams]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setInfo('');
    setLoading(true);

    try {
      const resp = await api.post('/api/auth/login', { email, senha });
      const { token, email: userEmail, nome } = resp.data;
      setAuth(token, userEmail, nome);
      router.push('/');
    } catch (err: any) {
      setErro(err.response?.data?.mensagem || 'Erro ao realizar login. Verifique e-mail e senha.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl p-8 border border-slate-100">
      <div className="text-center mb-8">
        <div className="w-12 h-12 bg-sky-600 rounded-xl flex items-center justify-center font-bold text-white text-2xl mx-auto shadow-lg shadow-sky-600/30">
          F
        </div>
        <h1 className="text-2xl font-bold text-slate-800 mt-4 tracking-tight">Entrar no FinançasPro</h1>
        <p className="text-sm text-slate-500 mt-1">Acesse sua plataforma de gestão e investimentos</p>
      </div>

      {info && (
        <div className="mb-4 p-3 bg-amber-50 border border-amber-200 text-amber-800 text-xs rounded-lg flex items-start gap-2">
          <AlertCircle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <span>{info}</span>
        </div>
      )}

      {erro && (
        <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-lg flex items-start gap-2">
          <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
          <span>{erro}</span>
        </div>
      )}

      <form onSubmit={handleLogin} className="space-y-5">
        <div>
          <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            E-mail
          </label>
          <div className="relative">
            <Mail className="w-5 h-5 absolute left-3 top-3 text-slate-400" />
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
              placeholder="seu@email.com"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            Senha
          </label>
          <div className="relative">
            <Lock className="w-5 h-5 absolute left-3 top-3 text-slate-400" />
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-sky-500"
              placeholder="••••••••"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-3 bg-sky-600 hover:bg-sky-700 text-white font-semibold rounded-lg text-sm transition-colors flex items-center justify-center gap-2 shadow-md shadow-sky-600/20 disabled:opacity-50"
        >
          {loading ? 'Entrando...' : 'Acessar Painel'}
          <ArrowRight className="w-4 h-4" />
        </button>
      </form>
    </div>
  );
}

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center p-4">
      <Suspense fallback={<div className="text-white">Carregando...</div>}>
        <LoginContent />
      </Suspense>
    </div>
  );
}
