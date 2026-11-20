package cl.duoc.miralib.miralib.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.dto.Pedido;
import cl.duoc.miralib.miralib.dto.User;
import cl.duoc.miralib.miralib.repository.PedidoRepository;
import cl.duoc.miralib.miralib.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private PedidoService pedidoService;

    private final User usuario = new User(1, "Exequiel", "exe.guzman@gmail.com", "choma123");

    private Book libro(int stock, Boolean proximamente) {
        return new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación", "x", stock,
                LocalDate.of(2026, 12, 31), null, proximamente);
    }

    @Test
    public void crearPedidoDescuentaStockYQuedaVigente14Dias() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(bookService.findById(2)).thenReturn(libro(4, false));
        when(pedidoRepository.existsByUsuarioIdAndLibroIdAndEstadoNot(1, 2, Pedido.DEVUELTO)).thenReturn(false);
        when(bookService.sendBook(2)).thenReturn(libro(3, false));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido pedido = pedidoService.crearPedido(1, 2);

        assertEquals(Pedido.VIGENTE, pedido.getEstado());
        assertEquals(3, pedido.getLibro().getStock());
        assertEquals(LocalDate.now().plusDays(14), pedido.getFechaDevolucion());
    }

    @Test
    public void noSePuedePedirDosVecesElMismoLibro() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(bookService.findById(2)).thenReturn(libro(4, false));
        when(pedidoRepository.existsByUsuarioIdAndLibroIdAndEstadoNot(1, 2, Pedido.DEVUELTO)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> pedidoService.crearPedido(1, 2));
        verify(bookService, never()).sendBook(2);
    }

    @Test
    public void noSePuedePedirUnLibroProximamente() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(bookService.findById(2)).thenReturn(libro(0, true));

        assertThrows(RuntimeException.class, () -> pedidoService.crearPedido(1, 2));
        verify(bookService, never()).sendBook(2);
    }

    @Test
    public void devolverSumaStockYQuedaDevuelto() {
        Pedido pedido = new Pedido(10, usuario, libro(3, false), LocalDate.now(), LocalDate.now().plusDays(14), Pedido.VIGENTE);
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(pedido));
        when(bookService.returnBook(2)).thenReturn(libro(4, false));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido devuelto = pedidoService.devolverPedido(10);

        assertEquals(Pedido.DEVUELTO, devuelto.getEstado());
        assertEquals(4, devuelto.getLibro().getStock());
    }

    @Test
    public void extenderSuma7DiasYVuelveAVigente() {
        LocalDate devolucion = LocalDate.now().minusDays(1);
        Pedido pedido = new Pedido(10, usuario, libro(3, false), LocalDate.now().minusDays(15), devolucion, Pedido.VENCIDO);
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido extendido = pedidoService.extenderPedido(10);

        assertEquals(devolucion.plusDays(7), extendido.getFechaDevolucion());
        assertEquals(Pedido.VIGENTE, extendido.getEstado());
    }

    @Test
    public void alListarMarcaComoVencidoUnPedidoVigenteFueraDePlazo() {
        Pedido vencido = new Pedido(10, usuario, libro(3, false), LocalDate.now().minusDays(20), LocalDate.now().minusDays(1), Pedido.VIGENTE);
        Pedido alDia = new Pedido(11, usuario, libro(3, false), LocalDate.now(), LocalDate.now().plusDays(14), Pedido.VIGENTE);
        Pedido devuelto = new Pedido(12, usuario, libro(3, false), LocalDate.now().minusDays(30), LocalDate.now().minusDays(16), Pedido.DEVUELTO);
        when(pedidoRepository.findByUsuarioIdOrderByIdDesc(1)).thenReturn(List.of(vencido, alDia, devuelto));

        List<Pedido> pedidos = pedidoService.getPedidosDeUsuario(1);

        assertEquals(Pedido.VENCIDO, pedidos.get(0).getEstado());
        assertEquals(Pedido.VIGENTE, pedidos.get(1).getEstado());
        assertEquals(Pedido.DEVUELTO, pedidos.get(2).getEstado()); // el historial no cambia
        verify(pedidoRepository).save(vencido);
        verify(pedidoRepository, never()).save(alDia);
    }

    @Test
    public void noSePuedePedirConUnUsuarioInexistente() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException e = assertThrows(RuntimeException.class, () -> pedidoService.crearPedido(99, 2));
        assertEquals("Usuario no encontrado", e.getMessage());
    }

    @Test
    public void noSePuedePedirUnLibroInexistente() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(bookService.findById(99)).thenReturn(null);

        RuntimeException e = assertThrows(RuntimeException.class, () -> pedidoService.crearPedido(1, 99));
        assertEquals("Libro no encontrado", e.getMessage());
    }

    @Test
    public void noSePuedeExtenderUnPedidoInexistente() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> pedidoService.extenderPedido(99));
    }

    @Test
    public void noSePuedeDevolverDosVeces() {
        Pedido pedido = new Pedido(10, usuario, libro(3, false), LocalDate.now(), LocalDate.now(), Pedido.DEVUELTO);
        when(pedidoRepository.findById(10)).thenReturn(Optional.of(pedido));

        assertThrows(RuntimeException.class, () -> pedidoService.devolverPedido(10));
        verify(bookService, never()).returnBook(2);
    }
}
