package cl.duoc.miralib.miralib.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import cl.duoc.miralib.miralib.dto.User;
import cl.duoc.miralib.miralib.service.UserService;

@WebMvcTest(controllers = UserController.class) 
public class UserControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private UserService userService;

    @Test 
    public void testGetAllUsers() throws Exception{
        User usr1 = new User(1, "Exequiel", "exe.guzman@gmail.com", "choma123");
        User usr2 = new User(2, "Felipe", "fe.araya@gmail.com", "kuro123");

        when(userService.getAllUsers()).thenReturn(List.of(usr1, usr2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Exequiel"))
                .andExpect(jsonPath("$[1].nombre").value("Felipe"));
    }

    @Test
    public void testSaveUserConIdCero() throws Exception {
        // El frontend registra usuarios enviando id: 0 (User.id es int primitivo)
        when(userService.saveUser(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7);
            return u;
        });

        mockMvc.perform(post("/api/v1/users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":0,\"nombre\":\"Ana\",\"correo\":\"ana@miralib.test\",\"contrasena\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.correo").value("ana@miralib.test"));
    }

    @Test
    public void testFindById() throws Exception {
        when(userService.findById(1)).thenReturn(new User(1, "Exequiel", "exe@miralib.test", "x"));

        mockMvc.perform(get("/api/v1/users/id").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Exequiel"));
    }

    @Test
    public void testDeleteUserOk() throws Exception {
        mockMvc.perform(delete("/api/v1/users/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario eliminado correctamente"));
    }

    @Test
    public void testDeleteUserInexistenteDevuelve404() throws Exception {
        doThrow(new RuntimeException("No se encontró ese Usuario con esa ID: 99")).when(userService).deleteUser(99);

        mockMvc.perform(delete("/api/v1/users/delete/99"))
                .andExpect(status().isNotFound());
    }

}
