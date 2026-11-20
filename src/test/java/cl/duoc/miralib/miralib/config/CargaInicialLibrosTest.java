package cl.duoc.miralib.miralib.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
public class CargaInicialLibrosTest {

    private static final int LIBROS_EN_CATALOGO = 6; // 4 disponibles + 2 próximamente

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CargaInicialLibros cargaInicial;

    @Test
    public void conLaBaseVaciaCreaTodoElCatalogoConPortada() {
        when(bookRepository.findAll()).thenReturn(List.of());

        cargaInicial.run();

        ArgumentCaptor<Book> guardados = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository, times(LIBROS_EN_CATALOGO)).save(guardados.capture());
        List<Book> libros = guardados.getAllValues();

        assertTrue(libros.stream().allMatch(b -> b.getImagen() != null && b.getImagen().startsWith("https://")));
        assertEquals(2, libros.stream().filter(b -> Boolean.TRUE.equals(b.getProximamente())).count());
        assertTrue(libros.stream().filter(b -> Boolean.TRUE.equals(b.getProximamente())).allMatch(b -> b.getStock() == 0));
    }

    @Test
    public void siYaExistenConPortadaNoModificaNada() {
        List<Book> existentes = new ArrayList<>();
        when(bookRepository.findAll()).thenReturn(existentes);
        cargaInicial.run();
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository, times(LIBROS_EN_CATALOGO)).save(captor.capture());

        // Segunda ejecución: todos existen y tienen portada (stock cambiado por el uso)
        for (Book b : captor.getAllValues()) {
            b.setStock(b.getStock() + 7);
            existentes.add(b);
        }
        org.mockito.Mockito.clearInvocations(bookRepository);

        cargaInicial.run();

        verify(bookRepository, never()).save(any());
    }

    @Test
    public void siUnLibroExisteSinPortadaSoloLeAsignaLaPortada() {
        Book sinPortada = new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación", "x",
                1, LocalDate.of(2026, 12, 31), null, false);
        when(bookRepository.findAll()).thenReturn(List.of(sinPortada));

        cargaInicial.run();

        // Crea los 5 que faltan + actualiza Clean Code
        verify(bookRepository, times(LIBROS_EN_CATALOGO)).save(any(Book.class));
        assertTrue(sinPortada.getImagen().startsWith("https://"));
        assertEquals(1, sinPortada.getStock()); // el stock real no se pisa
        assertFalse(sinPortada.getProximamente());
    }

    @Test
    public void unaPortadaEnBlancoSeConsideraSinPortada() {
        Book enBlanco = new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación", "x",
                1, LocalDate.of(2026, 12, 31), "  ", false);
        when(bookRepository.findAll()).thenReturn(List.of(enBlanco));

        cargaInicial.run();

        assertTrue(enBlanco.getImagen().startsWith("https://"));
    }
}
