package com.example.BackGestion.Services;

import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Repository.UsuarioRepository;
import com.example.BackGestion.Util.RutValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.BackGestion.dto.LoginProjection;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public LoginProjection autenticar(String email, String password) {
        return usuarioRepository.autenticarUsuario(email, password)
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas o usuario no encontrado"));
    }

    public Usuario guardar(Usuario usuario) {
        if (usuario.getRut() != null && !usuario.getRut().isBlank()) {
            usuario.setRut(RutValidator.formatear(usuario.getRut()));
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Integer id, Usuario usuarioDetalles) {
        Usuario usuario = obtenerPorId(id);
        if (usuarioDetalles.getRut() != null && !usuarioDetalles.getRut().isBlank()) {
            usuario.setRut(RutValidator.formatear(usuarioDetalles.getRut()));
        }
        usuario.setNombre(usuarioDetalles.getNombre());
        usuario.setApellidoPaterno(usuarioDetalles.getApellidoPaterno());
        usuario.setApellidoMaterno(usuarioDetalles.getApellidoMaterno());
        usuario.setEmail(usuarioDetalles.getEmail());
        if (usuarioDetalles.getPassword() != null && !usuarioDetalles.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDetalles.getPassword()));
        }
        usuario.setRolId(usuarioDetalles.getRolId());
        usuario.setActivo(usuarioDetalles.getActivo());
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Integer id) {
        Usuario usuario = obtenerPorId(id);
        usuarioRepository.delete(usuario);
    }
}

