package br.com.otimizasistema.cliente;

import java.util.NoSuchElementException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public ClienteDto semCache(Long id) { return buscar(id); }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "clientes", key = "#id")
    public ClienteDto comCache(Long id) { return buscar(id); }

    @Transactional
    @CacheEvict(cacheNames = "clientes", key = "#id")
    public ClienteDto atualizar(Long id, String nome) {
        Cliente cliente = encontrar(id);
        cliente.setNome(nome);
        return new ClienteDto(cliente.getId(), cliente.getNome());
    }

    private ClienteDto buscar(Long id) {
        Cliente cliente = encontrar(id);
        return new ClienteDto(cliente.getId(), cliente.getNome());
    }

    private Cliente encontrar(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: " + id));
    }
}
