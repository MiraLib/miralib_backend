package cl.duoc.miralib.miralib.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.miralib.miralib.dto.User;
import cl.duoc.miralib.miralib.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final User usuario = new User(1, "Exequiel", "exe@miralib.test", "clave123");

    @Test
    public void getAllUsersDevuelveLoDelRepositorio() {
        when(userRepository.findAll()).thenReturn(List.of(usuario));
        assertEquals(List.of(usuario), userService.getAllUsers());
    }

    @Test
    public void saveUserGuardaElUsuario() {
        when(userRepository.save(usuario)).thenReturn(usuario);
        assertEquals(usuario, userService.saveUser(usuario));
    }

    @Test
    public void findByIdDevuelveElUsuarioONull() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(usuario, userService.findById(1));
        assertNull(userService.findById(99));
    }

    @Test
    public void deleteUserEliminaSiExiste() {
        when(userRepository.existsById(1)).thenReturn(true);
        userService.deleteUser(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    public void deleteUserFallaSiNoExiste() {
        when(userRepository.existsById(99)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> userService.deleteUser(99));
        verify(userRepository, never()).deleteById(99);
    }
}
