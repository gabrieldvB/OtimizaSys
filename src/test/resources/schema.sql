CREATE TABLE cliente (id bigint PRIMARY KEY, nome varchar(100) NOT NULL);
CREATE TABLE pedido (id bigint PRIMARY KEY, cliente_id bigint NOT NULL REFERENCES cliente(id), criado_em timestamp with time zone NOT NULL, status varchar(20) NOT NULL);
CREATE TABLE item_pedido (id bigint PRIMARY KEY, pedido_id bigint NOT NULL REFERENCES pedido(id), produto varchar(60) NOT NULL, quantidade integer NOT NULL, preco numeric(10,2) NOT NULL);
INSERT INTO cliente (id, nome) VALUES (42, 'Cliente 42');
INSERT INTO pedido (id, cliente_id, criado_em, status)
SELECT x, 42, '2026-01-01T00:00:00Z', 'ABERTO' FROM SYSTEM_RANGE(1, 10);
INSERT INTO item_pedido (id, pedido_id, produto, quantidade, preco)
SELECT x, x, 'Produto', 2, 10.00 FROM SYSTEM_RANGE(1, 10);
