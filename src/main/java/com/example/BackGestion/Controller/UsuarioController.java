package com.example.BackGestion.Controller;

import com.example.BackGestion.Model.Usuario;
import com.example.BackGestion.Services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BackGestion.dto.LoginProjection;
import com.example.BackGestion.dto.LoginRequest;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del colegio")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista completa de usuarios registrados en la BD")
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico según su ID")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Llama al procedimiento almacenado para autenticar por email y password")
    public ResponseEntity<LoginProjection> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente")
    public ResponseEntity<Usuario> actualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

