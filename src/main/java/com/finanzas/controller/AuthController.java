package com.finanzas.controller;

import com.finanzas.entity.RolUsuario;
import com.finanzas.entity.Usuario;
import com.finanzas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // Página de login
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Página de registro
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // Procesar registro
    @PostMapping("/register")
    public String register(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String nombre,
                          RedirectAttributes redirectAttributes) {
        
        // Verificar si el email ya existe
        if (usuarioService.existePorEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
            return "redirect:/register";
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setNombre(nombre);
        usuario.setRol(RolUsuario.USUARIO);

        usuarioService.guardarUsuario(usuario);

        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }

    // Página principal (redirige al dashboard)
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}