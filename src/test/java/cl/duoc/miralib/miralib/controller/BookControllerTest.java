package cl.duoc.miralib.miralib.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.miralib.miralib.dto.Book;
import cl.duoc.miralib.miralib.service.BookService;
import java.time.LocalDate;


@WebMvcTest (controllers = BookController.class) 
public class BookControllerTest {

    
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private BookService bookService;

    @Test 
    public void testGetAllBooks() throws Exception{
        Book usr1 = new Book(1, "Agustín", "La metamorfosis", "Frank kafka", "Novela corta", 
        "La historia comienza cuando Gregorio Samsa —un joven viajante de comercio que mantiene económicamente a toda su familia— despierta una mañana tras un sueño intranquilo y descubre que se ha convertido en un monstruoso insecto (similar a un escarabajo gigante). Su primera preocupación no es su terrible condición física, sino el hecho de llegar tarde al trabajo y decepcionar a su jefe"
        , 20, LocalDate.of(2026,12,24), "https://ejemplo.cl/portadas/la-metamorfosis.jpg", false);
        Book usr2 = new Book(2, "Valentina", "El Gato Negro", "Edgar Allan Poe", "Terror", 
            "Narra la confesión en prisión de un hombre condenado a muerte que pasa de ser amable y amante de los animales a volverse violento y asesino a causa del alcoholismo y la culpa."
        , 5, LocalDate.of(2027,4,10), null, true);

        when(bookService.getAllBooks()).thenReturn(List.of(usr1, usr2));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCompartidor").value("Agustín"))
                .andExpect(jsonPath("$[1].nombreCompartidor").value("Valentina"))
                .andExpect(jsonPath("$[0].imagen").value("https://ejemplo.cl/portadas/la-metamorfosis.jpg"))
                .andExpect(jsonPath("$[0].proximamente").value(false))
                .andExpect(jsonPath("$[1].proximamente").value(true));
    }

    private Book cleanCode(int stock) {
        return new Book(2, "MiraLib", "Clean Code", "Robert C. Martin", "Programación", "x", stock,
                LocalDate.of(2026, 12, 31), "https://portadas.test/clean-code.jpg", false);
    }

    @Test
    public void testSaveBook() throws Exception {
        when(bookService.saveBook(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/v1/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":0,\"nombreLibro\":\"Clean Code\",\"stock\":4,\"imagen\":\"https://portadas.test/cc.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreLibro").value("Clean Code"))
                .andExpect(jsonPath("$.imagen").value("https://portadas.test/cc.jpg"));
    }

    @Test
    public void testFindById() throws Exception {
        when(bookService.findById(2)).thenReturn(cleanCode(4));

        mockMvc.perform(get("/api/v1/books/id").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(4));
    }

    @Test
    public void testSendBookOk() throws Exception {
        when(bookService.sendBook(2)).thenReturn(cleanCode(3));

        mockMvc.perform(put("/api/v1/books/send/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    public void testSendBookSinStockDevuelve400() throws Exception {
        when(bookService.sendBook(2)).thenThrow(new RuntimeException("No se puede dar el libro: stock agotado"));

        mockMvc.perform(put("/api/v1/books/send/2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No se puede dar el libro: stock agotado"));
    }

    @Test
    public void testDeleteBookOk() throws Exception {
        mockMvc.perform(delete("/api/v1/books/delete/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Libro eliminado correctamente"));
    }

    @Test
    public void testDeleteBookInexistenteDevuelve404() throws Exception {
        doThrow(new RuntimeException("No se encontró el libro con ID: 99")).when(bookService).deleteBook(99);

        mockMvc.perform(delete("/api/v1/books/delete/99"))
                .andExpect(status().isNotFound());
    }

}
