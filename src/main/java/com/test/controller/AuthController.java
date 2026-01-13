package com.test.controller;

import com.test.payload.*;
import com.test.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request,
                                                    HttpServletResponse response) {
        AuthResponseDTO authResponseDTO = authService.register(request);

        // Créer le cookie JWT
        Cookie jwtCookie = createJwtCookie(authResponseDTO.getToken());
        response.addCookie(jwtCookie);

        // Retourner la réponse sans le token (déjà dans le cookie)
        authResponseDTO.setToken(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                                 HttpServletResponse response) {
        AuthResponseDTO authResponseDTO = authService.login(request);

        // Créer le cookie JWT
        Cookie jwtCookie = createJwtCookie(authResponseDTO.getToken());
        response.addCookie(jwtCookie);

        // Retourner la réponse sans le token (déjà dans le cookie)
        authResponseDTO.setToken(null);

        return ResponseEntity.status(HttpStatus.OK).body(authResponseDTO);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
        UserResponseDTO user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletResponse response) {
        // Supprimer le cookie JWT
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // true en production avec HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Supprime le cookie
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new MessageResponseDTO("Déconnexion réussie"));
    }


    // Méthode utilitaire pour créer le cookie JWT
    private Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);  // Pas accessible via JavaScript (sécurité XSS)
        cookie.setSecure(false);    // true en production avec HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpiration / 1000)); // Convertir ms en secondes
        cookie.setAttribute("SameSite", "Lax"); // Protection CSRF
        return cookie;
    }
}
