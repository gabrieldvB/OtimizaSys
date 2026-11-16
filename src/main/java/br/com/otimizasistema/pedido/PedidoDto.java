package br.com.otimizasistema.pedido;

import java.math.BigDecimal;

public record PedidoDto(Long id, String status, int quantidadeDeItens, BigDecimal valorTotal) {}
