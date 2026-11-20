package cl.duoc.miralib.miralib.dto;

/** Cuerpo de POST /api/v1/pedidos/add: qué usuario pide qué libro. */
public record PedidoRequest(int usuarioId, int libroId) {
}
