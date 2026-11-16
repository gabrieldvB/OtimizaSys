#!/usr/bin/env bash
set -euo pipefail
fase="${1:-}"
if [[ "$fase" != antes && "$fase" != depois ]]; then
  echo 'Uso: carga/planos.sh antes|depois' >&2
  exit 2
fi
mkdir -p resultados
consulta='EXPLAIN (ANALYZE, BUFFERS) SELECT id, cliente_id, criado_em, status FROM pedido WHERE cliente_id = 42 ORDER BY criado_em DESC LIMIT 20;'
executar_sql() {
  if [[ "${DB_MODE:-docker}" == local ]]; then
    PGPASSWORD=trabalho_local psql -h 127.0.0.1 -p 5433 -U trabalho -d trabalho -v ON_ERROR_STOP=1 -c "$1"
  else
    docker compose exec -T db psql -v ON_ERROR_STOP=1 -U trabalho -d trabalho -c "$1"
  fi
}
indice_existe() {
  if [[ "${DB_MODE:-docker}" == local ]]; then
    PGPASSWORD=trabalho_local psql -h 127.0.0.1 -p 5433 -U trabalho -d trabalho -At \
      -c "SELECT to_regclass('public.idx_pedido_cliente_data') IS NOT NULL"
  else
    docker compose exec -T db psql -U trabalho -d trabalho -At \
      -c "SELECT to_regclass('public.idx_pedido_cliente_data') IS NOT NULL"
  fi
}
if [[ "$fase" == antes && "$(indice_existe)" == t ]]; then
  echo 'O índice já existe; recrie o banco do laboratório antes de medir a fase sem índice.' >&2
  exit 1
fi
if [[ "$fase" == depois ]]; then
  executar_sql 'CREATE INDEX IF NOT EXISTS idx_pedido_cliente_data ON pedido(cliente_id, criado_em DESC); ANALYZE pedido;'
fi
executar_sql "$consulta" | tee "resultados/plano-${fase}.txt"
