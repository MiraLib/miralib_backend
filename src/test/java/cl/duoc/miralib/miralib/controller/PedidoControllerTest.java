package cl.duoc.miralib.miralib.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.dto.Pedido;
import cl.duoc.miralib.miralib.dto.User;
import cl.duoc.miralib.miralib.service.PedidoService;

@WebMvcTest(controllers = PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    private final User usuario = new User(1, "Exequiel", "exe.guzman@gmail.com", "choma123");
    private final Book libro = new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación",
            "Código limpio", 3, LocalDate.of(2026, 12, 31), null, false);

    @Test
    public void testGetPedidosDeUsuarioNoExponeContrasena() throws Exception {
        Pedido pedido = new Pedido(10, usuario, libro, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), Pedido.VIGENTE);
        when(pedidoService.getPedidosDeUsuario(1)).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/v1/pedidos/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].libro.nombreLibro").value("Clean Code"))
                .andExpect(jsonPath("$[0].fechaDevolucion").value("2026-12-04"))
                .andExpect(jsonPath("$[0].estado").value("Vigente"))
                .andExpect(jsonPath("$[0].usuario.nombre").value("Exequiel"))
                .andExpect(jsonPath("$[0].usuario.contrasena").doesNotExist());
    }

    @Test
    public void testCrearPedidoOk() throws Exception {
        Pedido pedido = new Pedido(11, usuario, libro, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), Pedido.VIGENTE);
        when(pedidoService.crearPedido(1, 2)).thenReturn(pedido);

        mockMvc.perform(post("/api/v1/pedidos/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"libroId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.libro.stock").value(3));
    }

    @Test
    public void testDevolverPedido() throws Exception {
        Pedido devuelto = new Pedido(10, usuario, libro, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), Pedido.DEVUELTO);
        when(pedidoService.devolverPedido(10)).thenReturn(devuelto);

        mockMvc.perform(put("/api/v1/pedidos/devolver/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Devuelto"));
    }

    @Test
    public void testDevolverPedidoYaDevueltoDevuelve400() throws Exception {
        when(pedidoService.devolverPedido(10)).thenThrow(new RuntimeException("Este pedido ya fue devuelto"));

        mockMvc.perform(put("/api/v1/pedidos/devolver/10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Este pedido ya fue devuelto"));
    }

    @Test
    public void testExtenderPedido() throws Exception {
        Pedido extendido = new Pedido(10, usuario, libro, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 11), Pedido.VIGENTE);
        when(pedidoService.extenderPedido(10)).thenReturn(extendido);

        mockMvc.perform(put("/api/v1/pedidos/extender/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDevolucion").value("2026-12-11"));
    }

    @Test
    public void testExtenderPedidoInexistenteDevuelve400() throws Exception {
        when(pedidoService.extenderPedido(99)).thenThrow(new RuntimeException("Pedido no encontrado"));

        mockMvc.perform(put("/api/v1/pedidos/extender/99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pedido no encontrado"));
    }

    @Test
    public void testCrearPedidoDevuelveErrorDelServicio() throws Exception {
        when(pedidoService.crearPedido(1, 2))
                .thenThrow(new RuntimeException("Ya tienes este libro en tus pedidos actuales"));

        mockMvc.perform(post("/api/v1/pedidos/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"libroId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ya tienes este libro en tus pedidos actuales"));
    }
}
