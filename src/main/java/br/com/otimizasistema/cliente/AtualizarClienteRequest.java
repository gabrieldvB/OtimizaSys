package br.com.otimizasistema.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarClienteRequest(@NotBlank @Size(max = 100) String nome) {}
