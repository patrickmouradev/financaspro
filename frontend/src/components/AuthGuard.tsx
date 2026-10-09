'use client';

import { useEffect, useRef } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/auth.store';
import api from '@/lib/axios';

const IDLE_TIMEOUT_MS = 20 * 60 * 1000; // 20 minutos de inatividade para logout
const REFRESH_INTERVAL_MS = 10 * 60 * 1000; // Renovar a cada 10 minutos se estiver ativo

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { token, lastActivity, updateActivity, setAuth, logout } = useAuthStore();
  const lastRefreshRef = useRef<number>(Date.now());

  // 1. Verificar Autenticação
  useEffect(() => {
    if (!token && pathname !== '/login') {
      router.push('/login');
    }
  }, [token, pathname, router]);

  // 2. Monitorar Atividade do Usuário
  useEffect(() => {
    let throttleTimer: NodeJS.Timeout | null = null;

    const handleUserActivity = () => {
      if (!throttleTimer) {
        throttleTimer = setTimeout(() => {
          updateActivity();
          throttleTimer = null;
        }, 5000); // Atualiza no máximo a cada 5 segundos
      }
    };

    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];
    events.forEach((ev) => window.addEventListener(ev, handleUserActivity, { passive: true }));

    return () => {
      events.forEach((ev) => window.removeEventListener(ev, handleUserActivity));
      if (throttleTimer) clearTimeout(throttleTimer);
    };
  }, [updateActivity]);

  // 3. Checagem periódica: Inatividade e Auto-renovação de Token
  useEffect(() => {
    if (!token) return;

    const interval = setInterval(async () => {
      const now = Date.now();
      const idleTime = now - lastActivity;

      // Se passou de 20 min sem atividade -> Logout automático
      if (idleTime >= IDLE_TIMEOUT_MS) {
        logout();
        router.push('/login?reason=idle');
        return;
      }

      // Se o usuário está ativo e se passou 10 min desde o último refresh -> Renovar Token
      const timeSinceLastRefresh = now - lastRefreshRef.current;
      if (idleTime < IDLE_TIMEOUT_MS && timeSinceLastRefresh >= REFRESH_INTERVAL_MS) {
        try {
          const resp = await api.post('/api/auth/refresh');
          const { token: newToken, email, nome } = resp.data;
          setAuth(newToken, email, nome);
          lastRefreshRef.current = Date.now();
        } catch (err) {
          console.error('Falha ao auto-renovar token:', err);
        }
      }
    }, 15000); // Verifica a cada 15 segundos

    return () => clearInterval(interval);
  }, [token, lastActivity, logout, router, setAuth]);

  if (!token && pathname !== '/login') {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center text-white text-sm">
        Carregando autenticação...
      </div>
    );
  }

  return <>{children}</>;
}
