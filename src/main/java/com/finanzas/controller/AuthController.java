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
    public String register(@RequestParam String nombre,
                          @RequestParam String email,
                          @RequestParam String password,
                          RedirectAttributes redirectAttributes) {
        
        // Validaciones del servidor que coincidan con el frontend
        try {
            // 1. Validar formato del nombre
            if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,50}$")) {
                redirectAttributes.addFlashAttribute("error", "Formato de nombre inválido");
                return "redirect:/register";
            }
            
            // 2. Validar que el nombre tenga 1-2 palabras
            String[] palabras = nombre.trim().split("\\s+");
            if (palabras.length < 1 || palabras.length > 2) {
                redirectAttributes.addFlashAttribute("error", "El nombre debe contener 1 o 2 palabras");
                return "redirect:/register";
            }
            
            // 3. Validar longitud de cada palabra
            for (String palabra : palabras) {
                if (palabra.length() < 3 || palabra.length() > 20) {
                    redirectAttributes.addFlashAttribute("error", "Cada palabra debe tener entre 3 y 20 caracteres");
                    return "redirect:/register";
                }
            }
            
            // 4. Validar formato de email (solo Gmail)
            if (!email.endsWith("@gmail.com")) {
                redirectAttributes.addFlashAttribute("error", "Solo se aceptan cuentas @gmail.com");
                return "redirect:/register";
            }
            
            // 5. Validar parte local del email
            String localPart = email.split("@")[0];
            if (localPart.length() < 3 || localPart.length() > 20) {
                redirectAttributes.addFlashAttribute("error", "El email debe tener entre 3 y 20 caracteres antes del @");
                return "redirect:/register";
            }
            
            if (!localPart.matches("^[a-zA-Z0-9.]+$")) {
                redirectAttributes.addFlashAttribute("error", "El email contiene caracteres no permitidos");
                return "redirect:/register";
            }
            
            // 6. Validar contraseña (mínimo 8 caracteres ahora)
            if (password.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                return "redirect:/register";
            }
            
            // 7. Verificar si el email ya existe (esta validación ya la tienes)
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
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
            return "redirect:/register";
        }
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

    // Procesar cambio de contraseña (versión final: quita flag de passwordTemporal si existe)
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

            // Validar fortaleza de la nueva contraseña (mínimo 8 caracteres)
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 8 caracteres");
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
                //  Quitar el flag de contraseña temporal si existe
                if (usuario.isPasswordTemporal()) {
                    usuario.setPasswordTemporal(false);
                    usuarioService.guardarUsuario(usuario);
                }

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
