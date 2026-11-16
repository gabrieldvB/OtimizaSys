package br.com.otimizasistema;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import br.com.otimizasistema.cliente.ClienteDto;
import br.com.otimizasistema.pedido.PedidoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.jpa.defer-datasource-initialization=false",
        "logging.level.org.hibernate.SQL=WARN"
})
class ApiIntegrationTest {
    @Autowired
    private TestRestTemplate http;

    @Test
    void variantesEntregamMesmoJson() {
        String lazy = pedidos("lazy");
        assertThat(pedidos("eager")).isEqualTo(lazy);
        assertThat(pedidos("otimizado")).isEqualTo(lazy);
        PedidoDto[] valores = http.getForObject("/api/pedidos/lazy?inicio=1&quantidade=10", PedidoDto[].class);
        assertThat(valores).hasSize(10);
        assertThat(valores[0].valorTotal()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void atualizacaoInvalidaCache() {
        ClienteDto antes = http.getForObject("/api/clientes/42/com-cache", ClienteDto.class);
        assertThat(antes.nome()).isEqualTo("Cliente 42");
        ResponseEntity<ClienteDto> atualizacao = http.exchange("/api/clientes/42", HttpMethod.PUT,
                new HttpEntity<>(new Atualizacao("Atualizado")), ClienteDto.class);
        assertThat(atualizacao.getStatusCode()).isEqualTo(HttpStatus.OK);
        ClienteDto depois = http.getForObject("/api/clientes/42/com-cache", ClienteDto.class);
        assertThat(depois.nome()).isEqualTo("Atualizado");
    }

    private String pedidos(String variante) {
        return http.getForObject("/api/pedidos/" + variante + "?inicio=1&quantidade=10", String.class);
    }

    private record Atualizacao(String nome) {}
}
