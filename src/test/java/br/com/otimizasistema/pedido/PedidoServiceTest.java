package br.com.otimizasistema.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class PedidoServiceTest {
    private final PedidoLazyRepository lazyRepository = mock(PedidoLazyRepository.class);
    private final PedidoEagerRepository eagerRepository = mock(PedidoEagerRepository.class);
    private final PedidoService service = new PedidoService(lazyRepository, eagerRepository, mock(JdbcTemplate.class));

    @Test
    void variantesRetornamMesmoContratoEValorTotal() {
        ItemPedido item = mock(ItemPedido.class);
        when(item.getQuantidade()).thenReturn(2);
        when(item.getPreco()).thenReturn(new BigDecimal("10.00"));
        PedidoLazy lazy = mock(PedidoLazy.class);
        PedidoEager eager = mock(PedidoEager.class);
        when(lazy.getId()).thenReturn(1L);
        when(lazy.getStatus()).thenReturn("ABERTO");
        when(lazy.getItens()).thenReturn(List.of(item));
        when(eager.getId()).thenReturn(1L);
        when(eager.getStatus()).thenReturn("ABERTO");
        when(eager.getItens()).thenReturn(List.of(item));
        when(lazyRepository.buscar(1L, 10L)).thenReturn(List.of(lazy));
        when(lazyRepository.buscarComItens(1L, 10L)).thenReturn(List.of(lazy));
        when(eagerRepository.buscar(1L, 10L)).thenReturn(List.of(eager));

        List<PedidoDto> esperado = List.of(new PedidoDto(1L, "ABERTO", 1, new BigDecimal("20.00")));
        assertThat(service.listar("lazy", 1, 10)).isEqualTo(esperado);
        assertThat(service.listar("eager", 1, 10)).isEqualTo(esperado);
        assertThat(service.listar("otimizado", 1, 10)).isEqualTo(esperado);
    }

    @Test
    void rejeitaJanelaInvalida() {
        assertThatThrownBy(() -> service.listar("lazy", 1, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.listar("lazy", 99992, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
