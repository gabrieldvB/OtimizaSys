package br.com.otimizasistema.pedido;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {
    @Id
    private Long id;
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
    private String produto;
    private Integer quantidade;
    private BigDecimal preco;

    protected ItemPedido() {}

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getProduto() { return produto; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getPreco() { return preco; }
}
