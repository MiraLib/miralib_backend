package cl.duoc.miralib.miralib.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.dto.Pedido;
import cl.duoc.miralib.miralib.dto.User;
import cl.duoc.miralib.miralib.repository.PedidoRepository;
import cl.duoc.miralib.miralib.repository.UserRepository;

@Service
public class PedidoService {

    public static final int DIAS_PRESTAMO = 14;
    public static final int DIAS_EXTENSION = 7;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookService bookService;

    // Pedidos del usuario (más nuevos primero). Los vigentes que ya pasaron su fecha quedan "Vencido".
    @Transactional
    public List<Pedido> getPedidosDeUsuario(int usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
        LocalDate hoy = LocalDate.now();
        for (Pedido p : pedidos) {
            if (Pedido.VIGENTE.equals(p.getEstado()) && p.getFechaDevolucion().isBefore(hoy)) {
                p.setEstado(Pedido.VENCIDO);
                pedidoRepository.save(p);
            }
        }
        return pedidos;
    }

    // El usuario pide un libro: valida, descuenta stock y crea el pedido (todo o nada).
    @Transactional
    public Pedido crearPedido(int usuarioId, int libroId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Book libro = bookService.findById(libroId);
        if (libro == null) {
            throw new RuntimeException("Libro no encontrado");
        }
        if (Boolean.TRUE.equals(libro.getProximamente())) {
            throw new RuntimeException("Este libro aún no está disponible");
        }
        if (pedidoRepository.existsByUsuarioIdAndLibroIdAndEstadoNot(usuarioId, libroId, Pedido.DEVUELTO)) {
            throw new RuntimeException("Ya tienes este libro en tus pedidos actuales");
        }

        Book libroActualizado = bookService.sendBook(libroId); // valida stock y descuenta 1

        LocalDate hoy = LocalDate.now();
        Pedido pedido = new Pedido(0, usuario, libroActualizado, hoy, hoy.plusDays(DIAS_PRESTAMO), Pedido.VIGENTE);
        return pedidoRepository.save(pedido);
    }

    // El usuario devuelve el libro: +1 stock y el pedido queda "Devuelto" (se guarda como historial).
    @Transactional
    public Pedido devolverPedido(int pedidoId) {
        Pedido pedido = buscarActivo(pedidoId);
        pedido.setLibro(bookService.returnBook(pedido.getLibro().getId()));
        pedido.setEstado(Pedido.DEVUELTO);
        return pedidoRepository.save(pedido);
    }

    // "Re-Solicitar": extiende la devolución 7 días y el pedido vuelve a estar "Vigente".
    @Transactional
    public Pedido extenderPedido(int pedidoId) {
        Pedido pedido = buscarActivo(pedidoId);
        pedido.setFechaDevolucion(pedido.getFechaDevolucion().plusDays(DIAS_EXTENSION));
        pedido.setEstado(Pedido.VIGENTE);
        return pedidoRepository.save(pedido);
    }

    private Pedido buscarActivo(int pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        if (Pedido.DEVUELTO.equals(pedido.getEstado())) {
            throw new RuntimeException("Este pedido ya fue devuelto");
        }
        return pedido;
    }
}
