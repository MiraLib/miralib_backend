package cl.duoc.miralib.miralib.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.miralib.miralib.dto.Pedido;
import cl.duoc.miralib.miralib.dto.PedidoRequest;
import cl.duoc.miralib.miralib.service.PedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Pedido> getPedidosDeUsuario(@PathVariable int usuarioId) {
        return pedidoService.getPedidosDeUsuario(usuarioId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequest req) {
        try {
            return ResponseEntity.ok(pedidoService.crearPedido(req.usuarioId(), req.libroId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/devolver/{id}")
    public ResponseEntity<?> devolverPedido(@PathVariable int id) {
        try {
            return ResponseEntity.ok(pedidoService.devolverPedido(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/extender/{id}")
    public ResponseEntity<?> extenderPedido(@PathVariable int id) {
        try {
            return ResponseEntity.ok(pedidoService.extenderPedido(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
