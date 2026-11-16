package br.com.otimizasistema.pedido;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoLazyRepository extends JpaRepository<PedidoLazy, Long> {
    @Query("select p from PedidoLazy p where p.id between :inicio and :fim order by p.id")
    List<PedidoLazy> buscar(@Param("inicio") Long inicio, @Param("fim") Long fim);

    @Query("select distinct p from PedidoLazy p left join fetch p.itens where p.id between :inicio and :fim order by p.id")
    List<PedidoLazy> buscarComItens(@Param("inicio") Long inicio, @Param("fim") Long fim);
}
