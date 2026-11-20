package cl.duoc.miralib.miralib.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import cl.duoc.miralib.miralib.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book libro(int stock) {
        return new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación", "x", stock,
                LocalDate.of(2026, 12, 31), null, false);
    }

    @Test
    public void getAllBooksDevuelveLoDelRepositorio() {
        when(bookRepository.findAll()).thenReturn(List.of(libro(4)));
        assertEquals(1, bookService.getAllBooks().size());
    }

    @Test
    public void saveBookGuardaElLibro() {
        Book b = libro(4);
        when(bookRepository.save(b)).thenReturn(b);
        assertEquals(b, bookService.saveBook(b));
    }

    @Test
    public void findByIdDevuelveNullSiNoExiste() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        assertNull(bookService.findById(99));
    }

    @Test
    public void sendBookDescuentaUnoDeStock() {
        when(bookRepository.findById(2)).thenReturn(Optional.of(libro(4)));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(3, bookService.sendBook(2).getStock());
    }

    @Test
    public void sendBookFallaSinStock() {
        when(bookRepository.findById(2)).thenReturn(Optional.of(libro(0)));

        RuntimeException e = assertThrows(RuntimeException.class, () -> bookService.sendBook(2));
        assertEquals("No se puede dar el libro: stock agotado", e.getMessage());
        verify(bookRepository, never()).save(any());
    }

    @Test
    public void sendBookFallaSiElLibroNoExiste() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookService.sendBook(99));
    }

    @Test
    public void returnBookSumaUnoDeStock() {
        when(bookRepository.findById(2)).thenReturn(Optional.of(libro(3)));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(4, bookService.returnBook(2).getStock());
    }

    @Test
    public void returnBookFallaSiElLibroNoExiste() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookService.returnBook(99));
    }

    @Test
    public void deleteBookEliminaSiExiste() {
        when(bookRepository.existsById(2)).thenReturn(true);
        bookService.deleteBook(2);
        verify(bookRepository).deleteById(2);
    }

    @Test
    public void deleteBookFallaSiNoExiste() {
        when(bookRepository.existsById(99)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> bookService.deleteBook(99));
        verify(bookRepository, never()).deleteById(99);
    }
}
