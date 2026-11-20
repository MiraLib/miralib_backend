package cl.duoc.miralib.miralib.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.repository.BookRepository;

@Component
public class CargaInicialLibros implements CommandLineRunner {

    private static final LocalDate FECHA_ENTREGA = LocalDate.of(2026, 12, 31);

    private static final List<Book> CATALOGO = List.of(
        libro("El Señor de los Anillos", "J.R.R. Tolkien", "Fantasía",
            "Un hobbit recibe la misión de destruir un anillo de poder para salvar la Tierra Media del señor oscuro Sauron.",
            10, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSy6cmOQrHkPXXtr7a6ooxbTrFAEQ6UoGfZ6eJuQtdvwqkVcgYmUymrUEYu&s=10"),
        libro("Clean Code", "Robert C. Martin", "Programación",
            "Una guía imprescindible para la ingeniería de software ágil y la escritura de código limpio y mantenible.",
            4, "https://cdn.kobo.com/book-images/eb3afab7-f376-4bdd-8442-33f6d0b89432/1200/1200/False/clean-code-a-handbook-of-agile-software-craftsmanship-1.jpg"),
        libro("1984", "George Orwell", "Ficción Distópica",
            "Una novela política que imagina un mundo donde un gobierno totalitario vigila cada movimiento de sus ciudadanos.",
            8, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR4KXP1b3olekQNl1SxbRMWZp2AVPis15merbxqRhsv40RhslNOgT4wG3v4&s=10"),
        libro("Hábitos Atómicos", "James Clear", "Desarrollo Personal",
            "Un método sencillo y comprobado para desarrollar buenos hábitos y eliminar los malos.",
            23, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRsaNTeSPwjKoHFzodT6pnykvKEVZqsBqDYkfK78uqTT2M_s6AL7lqq3lZL&s=10"),

        // Proximamente (aún no disponibles para pedir)
        proximamente("Coraline", "Neil Gaiman", "Literatura infantil",
            "Una niña descubre una puerta secreta en su nueva casa que lleva a una versión aparentemente mejor de su vida.",
            "https://images.cdn1.buscalibre.com/fit-in/660x660/0b/a9/0ba9cadc466f5329d6abf5a68d76cd1a.jpg"),
        proximamente("Las aventuras de Alicia en el país de las maravillas", "Lewis Carroll", "Cuento de hadas",
            "Alicia cae por una madriguera de conejo a un mundo fantástico lleno de criaturas extrañas y situaciones absurdas.",
            "https://cdnx.jumpseller.com/cafe-entre-paginas/image/48935279/ALICIA_EN_EL_PAIS_DE_LAS_MARAVILLAS_1_.jpeg?1716167664")
    );

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void run(String... args) {
        List<Book> existentes = bookRepository.findAll();

        for (Book base : CATALOGO) {
            Book guardado = existentes.stream()
                .filter(b -> base.getNombreLibro().equals(b.getNombreLibro()))
                .findFirst()
                .orElse(null);

            if (guardado == null) {
                bookRepository.save(copia(base));
            } else if (guardado.getImagen() == null || guardado.getImagen().isBlank()) {
                guardado.setImagen(base.getImagen());
                bookRepository.save(guardado);
            }
        }
    }

    private static Book libro(String nombre, String autor, String categoria, String sinopsis, int stock, String imagen) {
        return new Book(0, "MiraLib", nombre, autor, categoria, sinopsis, stock, FECHA_ENTREGA, imagen, false);
    }

    private static Book proximamente(String nombre, String autor, String categoria, String sinopsis, String imagen) {
        return new Book(0, "MiraLib", nombre, autor, categoria, sinopsis, 0, null, imagen, true);
    }

    private static Book copia(Book b) {
        return new Book(0, b.getNombreCompartidor(), b.getNombreLibro(), b.getAutorLibro(), b.getCategoriaLibro(),
            b.getSipnosisLibro(), b.getStock(), b.getFechaDeEntrega(), b.getImagen(), b.getProximamente());
    }
}
