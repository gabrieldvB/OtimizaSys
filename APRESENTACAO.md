# Apresentação prática — até 7 minutos

Use a API e o banco locais do repositório. Deixe o [README](README.md), os [planos](resultados/) e os gráficos abertos antes de começar. A apresentação pode ser feita ao vivo; o vídeo é opcional no enunciado.

## 0:00–0:40 — Arquitetura e dados

Mostre `compose.yaml`, `sql/01-schema-seed.sql` e a contagem confirmada: **1.000 clientes, 100.000 pedidos, 300.000 itens**. Explique que cliente, pedido e item_pedido são três tabelas relacionadas, com duas visões JPA de pedido para a comparação ORM.

## 0:40–1:50 — Cache, atualização e TTL

Mostre [cache-demonstracao.json](resultados/cache-demonstracao.json): miss = 1 SELECT, hit = 0, `PUT` = SELECT + UPDATE, leitura após `PUT` = 1 SELECT com nome novo, leitura após 61 segundos = 1 SELECT. Mostre `ClienteService` com `@Cacheable` e `@CacheEvict`. Explique que o TTL de 60 s limita a desatualização quando a escrita ocorre fora da API.

```bash
curl -s http://localhost:8080/api/clientes/42/com-cache
curl -s http://localhost:8080/api/clientes/42/sem-cache
```

## 1:50–3:10 — LAZY, EAGER e JOIN FETCH

Mostre [orm-consultas.csv](resultados/orm-consultas.csv), [SQL representativo](resultados/sql-representativo.txt) e o DTO em `PedidoService`. Para 50 pedidos, LAZY e EAGER fizeram **51 SELECTs** cada; JOIN FETCH fez **1**. As três variantes retornaram o mesmo JSON e **3.442 bytes**. Na carga de 100 VUs, a média caiu de **31,037 ms** em LAZY para **11,625 ms** em JOIN FETCH.

```bash
curl -s 'http://localhost:8080/api/pedidos/otimizado?inicio=1&quantidade=10'
```

## 3:10–4:10 — Índice

Mostre [plano antes](resultados/plano-antes.txt) e [plano depois](resultados/plano-depois.txt). O PostgreSQL passou de `Seq Scan` com `Sort` e 4,690 ms para `Index Scan` sem `Sort` explícito e 0,151 ms. O índice ocupa [3.104 kB](resultados/indice-tamanho.txt) e aumenta o custo de escrita.

## 4:10–5:10 — Uma execução curta do k6

Execute somente uma demonstração curta. Os resultados completos já estão guardados em `resultados/`.

```bash
DURACAO=5s VUS=10 CAMINHO='/api/pedidos/otimizado?inicio=1&quantidade=50' k6 run carga/teste.js
```

Mostre o p95, p99, req/s e erros na saída. VUs são usuários simultâneos; req/s é a vazão observada.

## 5:10–6:40 — Gráficos e conclusão

Mostre os gráficos de [p95 ORM](resultados/orm-p95_ms.svg), [p95 índice](resultados/indice-p95_ms.svg) e [p95 cache](resultados/cache-p95_ms.svg), junto da [tabela completa](resultados/resumo.csv). As **63 rodadas válidas** tiveram 0% de erros. JOIN FETCH trouxe ganho claro em 50 e 100 VUs, o índice reduziu cerca de 78–81% da média do endpoint testado, e o cache piorou média e p95 em 50 VUs. Explique que a pausa de 0,2 s limita req/s e que as rodadas de 15 s não comprovam estabilidade prolongada.
