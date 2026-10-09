import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8087',
});

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('financaspro_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  // Se for FormData, remove o Content-Type padrão para o browser/axios definir a boundary correta
  if (config.data instanceof FormData && config.headers) {
    delete config.headers['Content-Type'];
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (typeof window !== 'undefined' && error.response && error.response.status === 401) {
      const isLoginPath = window.location.pathname === '/login';
      const isLoginEndpoint = error.config?.url?.includes('/api/auth/login');

      const msg = (error.response?.data?.mensagem || '').toLowerCase();
      const isFilePasswordOrBusinessError = msg.includes('senha') || msg.includes('cpf') || msg.includes('protegido') || msg.includes('arquivo');

      if (!isLoginPath && !isLoginEndpoint && !isFilePasswordOrBusinessError) {
        localStorage.removeItem('financaspro_token');
        localStorage.removeItem('financaspro_email');
        localStorage.removeItem('financaspro_nome');
        localStorage.removeItem('financaspro_last_activity');
        window.location.href = '/login?reason=expired';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
