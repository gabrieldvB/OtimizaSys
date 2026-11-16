package br.com.otimizasistema.cliente;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping("/{id}/sem-cache")
    public ClienteDto semCache(@PathVariable Long id) { return service.semCache(id); }

    @GetMapping("/{id}/com-cache")
    public ClienteDto comCache(@PathVariable Long id) { return service.comCache(id); }

    @PutMapping("/{id}")
    public ClienteDto atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarClienteRequest request) {
        return service.atualizar(id, request.nome());
    }
}
