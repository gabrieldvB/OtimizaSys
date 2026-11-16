package br.com.otimizasistema.pedido;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoEagerRepository extends JpaRepository<PedidoEager, Long> {
    @Query("select p from PedidoEager p where p.id between :inicio and :fim order by p.id")
    List<PedidoEager> buscar(@Param("inicio") Long inicio, @Param("fim") Long fim);
}
