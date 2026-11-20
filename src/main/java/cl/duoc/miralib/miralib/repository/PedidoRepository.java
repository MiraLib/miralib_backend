package cl.duoc.miralib.miralib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.miralib.miralib.dto.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByUsuarioIdOrderByIdDesc(int usuarioId);

    // ¿El usuario ya tiene este libro sin devolver?
    boolean existsByUsuarioIdAndLibroIdAndEstadoNot(int usuarioId, int libroId, String estado);
}
