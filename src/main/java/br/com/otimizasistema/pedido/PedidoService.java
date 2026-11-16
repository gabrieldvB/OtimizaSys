package br.com.otimizasistema.pedido;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {
    private final PedidoLazyRepository lazyRepository;
    private final PedidoEagerRepository eagerRepository;
    private final JdbcTemplate jdbcTemplate;

    public PedidoService(PedidoLazyRepository lazyRepository, PedidoEagerRepository eagerRepository, JdbcTemplate jdbcTemplate) {
        this.lazyRepository = lazyRepository;
        this.eagerRepository = eagerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> listar(String variante, long inicio, int quantidade) {
        validarJanela(inicio, quantidade);
        long fim = inicio + quantidade - 1;
        List<? extends PedidoView> pedidos = switch (variante) {
            case "lazy" -> lazyRepository.buscar(inicio, fim);
            case "eager" -> eagerRepository.buscar(inicio, fim);
            case "otimizado" -> lazyRepository.buscarComItens(inicio, fim);
            default -> throw new IllegalArgumentException("Variante desconhecida: " + variante);
        };
        return pedidos.stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoClienteDto> listarPorCliente(long id) {
        return jdbcTemplate.query("SELECT id, cliente_id, criado_em, status FROM pedido "
                + "WHERE cliente_id = ? ORDER BY criado_em DESC LIMIT 20", (resultado, linha) ->
                new PedidoClienteDto(resultado.getLong("id"), resultado.getLong("cliente_id"),
                        resultado.getTimestamp("criado_em").toInstant(), resultado.getString("status")), id);
    }

    private void validarJanela(long inicio, int quantidade) {
        if (quantidade != 10 && quantidade != 50 && quantidade != 100) {
            throw new IllegalArgumentException("quantidade deve ser 10, 50 ou 100");
        }
        if (inicio < 1 || inicio > 100001L - quantidade) {
            throw new IllegalArgumentException("inicio fora da janela de 100.000 pedidos");
        }
    }

    private PedidoDto converter(PedidoView pedido) {
        BigDecimal total = pedido.getItens().stream()
                .map(item -> item.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoDto(pedido.getId(), pedido.getStatus(), pedido.getItens().size(), total);
    }
}
