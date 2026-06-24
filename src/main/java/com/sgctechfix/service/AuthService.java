package com.sgctechfix.service;

import com.sgctechfix.dto.AuthDTO;
import com.sgctechfix.exception.RegraDeNegocioException;
import com.sgctechfix.model.Usuario;
import com.sgctechfix.repository.UsuarioRepository;
import com.sgctechfix.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthDTO.TokenResponse registrar(AuthDTO.RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RegraDeNegocioException("E-mail já cadastrado: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Usuario.Role.USER)
                .build();

        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.gerarToken(userDetails);

        return AuthDTO.TokenResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .build();
    }

    public AuthDTO.TokenResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.gerarToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        return AuthDTO.TokenResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .build();
    }
}