package autoservice.model.service;

import autoservice.model.dto.create.UserCreateDto;
import autoservice.model.entities.User;
import autoservice.model.enums.Role;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void addUser_success() {
        UserCreateDto dto = new UserCreateDto("name", "password");

        User user = new User();
        user.setPassword("12345678");
        user.setUsername("name");
        user.setRole(Role.USER);
        given(passwordEncoder.encode(dto.password())).willReturn("12345678");

        User result = userService.addUser(dto);

        assertEquals("12345678", result.getPassword());
        assertEquals("name", result.getUsername());
        assertEquals(Role.USER, result.getRole());

        verify(passwordEncoder).encode(any());
        verify(userRepository).save(any());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        User result = userService.getUserById(1L);
        assertEquals(user, result);

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.getUserById(1L));

        assertEquals("user with id 1 not found",notFoundException.getMessage());
    }
}
