package cl.duoc.miralib.miralib.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Libro pedido por un usuario.
 * estado: "Vigente" (lo tiene el usuario), "Vencido" (pasó la fecha de devolución) o "Devuelto".
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    public static final String VIGENTE = "Vigente";
    public static final String VENCIDO = "Vencido";
    public static final String DEVUELTO = "Devuelto";

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({ "contrasena" }) // nunca devolver la contraseña dentro de un pedido
    private User usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id")
    private Book libro;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPedido;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDevolucion;

    private String estado;
}
