package br.com.otimizasistema.pedido;

import java.time.Instant;

public record PedidoClienteDto(Long id, Long clienteId, Instant criadoEm, String status) {}
