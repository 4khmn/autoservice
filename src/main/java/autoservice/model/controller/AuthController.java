package autoservice.model.controller;

import autoservice.model.dto.create.UserCreateDto;
import autoservice.model.dto.request.LoginRequest;
import autoservice.model.dto.response.LoginResponseDto;
import autoservice.model.entities.User;
import autoservice.model.service.UserService;
import autoservice.model.config.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);

        return ResponseEntity.ok(new LoginResponseDto(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registration(@RequestBody UserCreateDto dto) {
        log.info("POST /api/register — user with username={} trying to register", dto.username());
        User user = userService.addUser(dto);
        log.info("User created successfully with id={}", user.getId());
        return ResponseEntity.ok("User added successfully");
    }
}
