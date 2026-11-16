package br.com.otimizasistema.pedido;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) { this.service = service; }

    @GetMapping("/{variante:lazy|eager|otimizado}")
    public List<PedidoDto> listar(@PathVariable String variante, @RequestParam long inicio, @RequestParam int quantidade) {
        return service.listar(variante, inicio, quantidade);
    }

    @GetMapping("/cliente/{id}")
    public List<PedidoClienteDto> listarPorCliente(@PathVariable long id) { return service.listarPorCliente(id); }
}
