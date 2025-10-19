package com.finanzas.service;

import com.finanzas.entity.Usuario;
import com.finanzas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Guardar usuario (encriptando password)
    public Usuario guardarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    // Encontrar usuario por ID
    public Optional<Usuario> encontrarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // Encontrar usuario por email
    public Optional<Usuario> encontrarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Verificar si existe usuario por email
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // Contar total de usuarios
    public long contarUsuarios() {
        return usuarioRepository.count();
    }
}