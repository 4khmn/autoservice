package autoservice.model.service;

import autoservice.model.dto.create.UserCreateDto;
import autoservice.model.entities.User;
import autoservice.model.enums.Role;
import autoservice.model.exceptions.NotFoundException;
import autoservice.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User addUser(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("user with id " + id + " not found")
        );
    }
}
