package br.com.otimizasistema.pedido;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido")
public class PedidoEager implements PedidoView {
    @Id
    private Long id;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    private String status;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<ItemPedido> itens;

    protected PedidoEager() {}

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getStatus() { return status; }
    public List<ItemPedido> getItens() { return itens; }
}
