package com.finanzas.controller;

import com.finanzas.entity.RolUsuario;
import com.finanzas.entity.Usuario;
import com.finanzas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // Validar fortaleza de contraseña
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/register";
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(password); // Se encriptará automáticamente en el servicio
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

    // Página para cambiar contraseña
    @GetMapping("/cambiar-password")
    public String cambiarPasswordForm() {
        return "cambiar-password";
    }

    // Procesar cambio de contraseña
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.encontrarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            System.out.println("Cambiando password para: " + email);
            System.out.println("Password actual en BD: " + usuario.getPassword());

            // Verificar contraseña actual
            if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/cambiar-password";
            }

            // Validar que la nueva contraseña sea diferente
            if (passwordEncoder.matches(newPassword, usuario.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe ser diferente a la actual");
                return "redirect:/cambiar-password";
            }

            // Validar fortaleza de la nueva contraseña
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
                return "redirect:/cambiar-password";
            }

            // Verificar que las nuevas contraseñas coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las nuevas contraseñas no coinciden");
                return "redirect:/cambiar-password";
            }

            // Actualizar contraseña usando el servicio
            boolean exito = usuarioService.cambiarPassword(email, newPassword);
            
            if (exito) {
                redirectAttributes.addFlashAttribute("success", "Contraseña cambiada exitosamente");
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la nueva contraseña");
                return "redirect:/cambiar-password";
            }

        } catch (Exception e) {
            System.out.println("Error en cambiarPassword: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña: " + e.getMessage());
            return "redirect:/cambiar-password";
        }
    }
}