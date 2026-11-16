package br.com.otimizasistema.pedido;

import java.util.List;

public interface PedidoView {
    Long getId();
    String getStatus();
    List<ItemPedido> getItens();
}
