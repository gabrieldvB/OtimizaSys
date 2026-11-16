#!/usr/bin/env bash
set -euo pipefail

fase="${1:-}"
if [[ "$fase" != cache && "$fase" != orm && "$fase" != indice-antes && "$fase" != indice-depois ]]; then
  echo 'Uso: carga/medir.sh cache|orm|indice-antes|indice-depois' >&2
  exit 2
fi
command -v k6 >/dev/null || { echo 'k6 não encontrado' >&2; exit 1; }
mkdir -p resultados

case "$fase" in
  cache)
    variantes=(sem-cache com-cache)
    caminhos=('/api/clientes/42/sem-cache' '/api/clientes/42/com-cache')
    ;;
  orm)
    variantes=(lazy eager otimizado)
    caminhos=('/api/pedidos/lazy?inicio=1&quantidade=50' '/api/pedidos/eager?inicio=1&quantidade=50' '/api/pedidos/otimizado?inicio=1&quantidade=50')
    ;;
  *)
    variantes=("$fase")
    caminhos=('/api/pedidos/cliente/42')
    ;;
esac

for indice in "${!variantes[@]}"; do
  variante="${variantes[$indice]}"
  caminho="${caminhos[$indice]}"
  curl --fail --silent --show-error "http://localhost:8080${caminho}" >/dev/null
  VUS=5 CAMINHO="$caminho" DURACAO=5s k6 run --quiet carga/teste.js >/dev/null
  for vus in 10 50 100; do
    for rodada in 1 2 3; do
      echo "Medindo $fase $variante: $vus VUs, rodada $rodada"
      VUS="$vus" CAMINHO="$caminho" k6 run --quiet \
        --summary-export "resultados/${fase}-${variante}-${vus}-r${rodada}.json" carga/teste.js
    done
  done
done
