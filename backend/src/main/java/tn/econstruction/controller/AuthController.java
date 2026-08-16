package tn.econstruction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.dto.RegistrationDTO;
import tn.econstruction.dto.LoginRequest;
import tn.econstruction.entity.User;
import tn.econstruction.mapper.UserMapper;
import tn.econstruction.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getCin(), request.getPassword());
        return ResponseEntity.ok(userMapper.toLoginResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationDTO dto) {
        var citizen = userService.registerCitizen(dto);
        return ResponseEntity.status(201)
                .body("Inscription réussie. Bienvenue " + citizen.getFirstName());
    }
}
