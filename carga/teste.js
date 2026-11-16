import http from 'k6/http';
import { check, sleep } from 'k6';

const base = __ENV.BASE || 'http://localhost:8080';
const caminho = __ENV.CAMINHO || '/api/clientes/42/sem-cache';

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.DURACAO || '60s',
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const resposta = http.get(`${base}${caminho}`, { tags: { name: caminho } });
  check(resposta, { 'HTTP 200': (r) => r.status === 200 });
  sleep(0.2);
}
