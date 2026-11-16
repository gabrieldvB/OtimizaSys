CREATE TABLE cliente (id bigint PRIMARY KEY, nome varchar(100) NOT NULL);
CREATE TABLE pedido (
  id bigint PRIMARY KEY,
  cliente_id bigint NOT NULL REFERENCES cliente(id),
  criado_em timestamptz NOT NULL,
  status varchar(20) NOT NULL
);
CREATE TABLE item_pedido (
  id bigint PRIMARY KEY,
  pedido_id bigint NOT NULL REFERENCES pedido(id),
  produto varchar(60) NOT NULL,
  quantidade integer NOT NULL,
  preco numeric(10,2) NOT NULL
);
INSERT INTO cliente(id, nome)
SELECT g, 'Cliente ' || g FROM generate_series(1, 1000) AS g;
INSERT INTO pedido(id, cliente_id, criado_em, status)
SELECT g, ((g - 1) % 1000) + 1,
       timestamptz '2026-01-01 00:00:00+00' + g * interval '1 minute',
       CASE WHEN g % 2 = 0 THEN 'PAGO' ELSE 'ABERTO' END
FROM generate_series(1, 100000) AS g;
INSERT INTO item_pedido(id, pedido_id, produto, quantidade, preco)
SELECT (p.id - 1) * 3 + n, p.id, 'Produto ' || n, n, n * 10.00
FROM pedido AS p CROSS JOIN generate_series(1, 3) AS n;
CREATE INDEX idx_item_pedido_pedido ON item_pedido(pedido_id);
ANALYZE cliente;
ANALYZE pedido;
ANALYZE item_pedido;
