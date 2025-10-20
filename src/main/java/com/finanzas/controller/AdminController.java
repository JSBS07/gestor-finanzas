package com.finanzas.controller;

import com.finanzas.entity.Usuario;
import com.finanzas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            long totalUsuarios = usuarioService.contarUsuarios();
            List<Usuario> usuarios = usuarioService.obtenerTodos();
            
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuarios", usuarios);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el dashboard de administración: " + e.getMessage());
        }
        
        return "admin-dashboard";
    }

    @PostMapping("/usuario/{id}/reset-password")
    public String resetearPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Resetear password llamado para usuario ID: " + id);
            
            Optional<Usuario> usuarioOpt = usuarioService.encontrarPorId(id);
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/dashboard";
            }
            
            Usuario usuario = usuarioOpt.get();
            String nuevaPassword = "123456";
            
            System.out.println("Reseteando password para: " + usuario.getEmail());
            System.out.println("Password anterior (hash): " + usuario.getPassword());
            
            // Usar el servicio para cambiar la contraseña (asegura encriptado consistente)
            boolean exito = usuarioService.cambiarPassword(usuario.getEmail(), nuevaPassword);
            
            if (exito) {
                // Verificar que se guardó correctamente
                Usuario usuarioVerificado = usuarioService.encontrarPorEmail(usuario.getEmail()).get();
                System.out.println("Nuevo hash guardado: " + usuarioVerificado.getPassword());
                
                // Verificar que el nuevo hash funciona
                boolean hashFunciona = passwordEncoder.matches(nuevaPassword, usuarioVerificado.getPassword());
                System.out.println("Hash verificado: " + hashFunciona);
                
                redirectAttributes.addFlashAttribute("success", 
                    "Contraseña restablecida para " + usuario.getEmail() + 
                    ". Nueva contraseña temporal: <strong>123456</strong><br>" +
                    "<small>El usuario debe cambiarla en 'Cambiar Contraseña' después de iniciar sesión.</small>");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al restablecer la contraseña");
            }
            
        } catch (Exception e) {
            System.out.println("Error en resetearPassword: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al restablecer la contraseña: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Eliminar usuario llamado para ID: " + id);
            
            Optional<Usuario> usuarioOpt = usuarioService.encontrarPorId(id);
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/dashboard";
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // No permitir eliminar administradores
            if (usuario.getRol().name().equals("ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar un usuario administrador");
                return "redirect:/admin/dashboard";
            }
            
            System.out.println("Eliminando usuario: " + usuario.getEmail());
            
            // Eliminar el usuario (las actividades se eliminarán en cascada por la relación)
            usuarioService.eliminarUsuario(id);
            
            redirectAttributes.addFlashAttribute("success", "Usuario " + usuario.getEmail() + " eliminado correctamente");
            
        } catch (Exception e) {
            System.out.println("Error en eliminarUsuario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
}