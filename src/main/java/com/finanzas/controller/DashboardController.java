package com.finanzas.controller;

import com.finanzas.entity.Actividad;
import com.finanzas.entity.Categoria;
import com.finanzas.entity.EstadoActividad;
import com.finanzas.entity.TipoActividad;
import com.finanzas.entity.Usuario;
import com.finanzas.service.ActividadService;
import com.finanzas.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            Usuario usuario = email != null ? usuarioService.encontrarPorEmail(email).orElse(null) : null;

            if (usuario == null) {
                logger.warn("Usuario no encontrado para email: {}", email);
                return "redirect:/login";
            }

            List<Actividad> pendientes = actividadService.obtenerPorUsuarioYEstado(usuario, EstadoActividad.PENDIENTE);
            List<Actividad> completadas = actividadService.obtenerPorUsuarioYEstado(usuario, EstadoActividad.COMPLETADO);

            // Solo actividades COMPLETADAS afectan el balance
            BigDecimal totalIngresos = actividadService.calcularTotalPorTipoYMesSeguro(usuario, TipoActividad.INGRESO);
            BigDecimal totalGastos = actividadService.calcularTotalPorTipoYMesSeguro(usuario, TipoActividad.GASTO);
            BigDecimal balance = totalIngresos.subtract(totalGastos);

            model.addAttribute("actividadesPendientes", pendientes != null ? pendientes : Collections.emptyList());
            model.addAttribute("actividadesCompletadas", completadas != null ? completadas : Collections.emptyList());
            model.addAttribute("tipos", TipoActividad.values());
            model.addAttribute("categorias", Categoria.values());
            model.addAttribute("totalIngresos", totalIngresos);
            model.addAttribute("totalGastos", totalGastos);
            model.addAttribute("balance", balance);

            logger.debug("Dashboard cargado para usuario={}, pendientes={}, completadas={}, ingresos={}, gastos={}, balance={}",
                    email,
                    pendientes != null ? pendientes.size() : 0,
                    completadas != null ? completadas.size() : 0,
                    totalIngresos, totalGastos, balance);

        } catch (Exception e) {
            logger.error("Error cargando dashboard", e);
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
        }

        return "dashboard";
    }

    @PostMapping("/actividad/nueva")
    public String crearActividad(@RequestParam String descripcion,
                                 @RequestParam String monto,
                                 @RequestParam String tipo,
                                 @RequestParam String categoria,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        logger.debug("POST crearActividad recibido: descripcion='{}' monto='{}' tipo='{}' categoria='{}'", descripcion, monto, tipo, categoria);
        try {
            // Validaciones básicas
            if (descripcion == null || descripcion.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La descripción es obligatoria");
                return "redirect:/dashboard";
            }

            String email = authentication != null ? authentication.getName() : null;
            Usuario usuario = email != null ? usuarioService.encontrarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado")) : null;

            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }

            BigDecimal montoParseado = parseMontoColombiano(monto);
            
            // Validar rango colombiano: mínimo 1.000, máximo 40.000.000
            if (montoParseado.compareTo(new BigDecimal("1000")) < 0) {
                redirectAttributes.addFlashAttribute("error", "El monto mínimo es $1.000");
                return "redirect:/dashboard";
            }
            if (montoParseado.compareTo(new BigDecimal("40000000")) > 0) {
                redirectAttributes.addFlashAttribute("error", "El monto máximo es $40.000.000");
                return "redirect:/dashboard";
            }

            Actividad a = new Actividad();
            a.setDescripcion(descripcion.trim());
            a.setMonto(montoParseado);
            a.setTipo(TipoActividad.valueOf(tipo));
            a.setCategoria(Categoria.valueOf(categoria));
            a.setEstado(EstadoActividad.PENDIENTE);
            a.setCreatedAt(LocalDateTime.now());
            a.setUsuario(usuario);

            Actividad guardada = actividadService.guardarActividad(a);
            logger.info("Actividad creada id={} descripcion='{}' monto={} usuario={}", 
                guardada.getId(), guardada.getDescripcion(), guardada.getMonto(), usuario.getEmail());
            redirectAttributes.addFlashAttribute("success", "Actividad creada correctamente");
        } catch (IllegalArgumentException e) {
            logger.error("Error en parámetros de actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error en los datos: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creando actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear actividad: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/actividad/editar/{id}")
    public String editarActividad(@PathVariable Long id,
                                  @RequestParam String descripcion,
                                  @RequestParam String monto,
                                  @RequestParam String tipo,
                                  @RequestParam String categoria,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        logger.debug("POST editarActividad id={} descripcion='{}' monto='{}' tipo='{}' categoria='{}'", id, descripcion, monto, tipo, categoria);
        try {
            Optional<Actividad> optional = actividadService.encontrarPorId(id);
            if (optional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Actividad no encontrada");
                return "redirect:/dashboard";
            }
            Actividad actividad = optional.get();

            String email = authentication != null ? authentication.getName() : null;
            Usuario usuario = email != null ? usuarioService.encontrarPorEmail(email).orElse(null) : null;
            if (usuario == null || actividad.getUsuario() == null || !actividad.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para modificar esta actividad");
                return "redirect:/dashboard";
            }

            BigDecimal montoParseado = parseMontoColombiano(monto);
            
            // Validar rango colombiano
            if (montoParseado.compareTo(new BigDecimal("1000")) < 0) {
                redirectAttributes.addFlashAttribute("error", "El monto mínimo es $1.000");
                return "redirect:/dashboard";
            }
            if (montoParseado.compareTo(new BigDecimal("40000000")) > 0) {
                redirectAttributes.addFlashAttribute("error", "El monto máximo es $40.000.000");
                return "redirect:/dashboard";
            }

            actividad.setDescripcion(descripcion != null ? descripcion.trim() : actividad.getDescripcion());
            actividad.setMonto(montoParseado);
            actividad.setTipo(TipoActividad.valueOf(tipo));
            actividad.setCategoria(Categoria.valueOf(categoria));

            actividadService.guardarActividad(actividad);
            redirectAttributes.addFlashAttribute("success", "Actividad actualizada correctamente");
        } catch (Exception e) {
            logger.error("Error actualizando actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar actividad: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/actividad/eliminar/{id}")
    public String eliminarActividad(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.debug("POST eliminarActividad id={}", id);
        try {
            Optional<Actividad> optional = actividadService.encontrarPorId(id);
            if (optional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Actividad no encontrada");
                return "redirect:/dashboard";
            }
            Actividad actividad = optional.get();

            String email = authentication != null ? authentication.getName() : null;
            Usuario usuario = email != null ? usuarioService.encontrarPorEmail(email).orElse(null) : null;
            if (usuario == null || actividad.getUsuario() == null || !actividad.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta actividad");
                return "redirect:/dashboard";
            }

            actividadService.eliminarActividad(id);
            redirectAttributes.addFlashAttribute("success", "Actividad eliminada correctamente");
        } catch (Exception e) {
            logger.error("Error eliminando actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar actividad: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/actividad/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String nuevoEstado,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        logger.debug("POST cambiarEstado id={} nuevoEstado={}", id, nuevoEstado);
        try {
            Optional<Actividad> optional = actividadService.encontrarPorId(id);
            if (optional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Actividad no encontrada");
                return "redirect:/dashboard";
            }
            Actividad actividad = optional.get();

            String email = authentication != null ? authentication.getName() : null;
            Usuario usuario = email != null ? usuarioService.encontrarPorEmail(email).orElse(null) : null;
            if (usuario == null || actividad.getUsuario() == null || !actividad.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cambiar el estado de esta actividad");
                return "redirect:/dashboard";
            }

            EstadoActividad estado = EstadoActividad.valueOf(nuevoEstado);
            actividad.setEstado(estado);
            actividadService.guardarActividad(actividad);

            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente");
        } catch (Exception e) {
            logger.error("Error cambiando estado", e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // Método especializado para formato colombiano
    private BigDecimal parseMontoColombiano(String montoStr) {
        if (montoStr == null || montoStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El monto no puede estar vacío");
        }
        
        String limpio = montoStr.trim()
                .replace(" ", "")
                .replace("$", "")
                .replace("€", "")
                .replace("COP", "")
                .replace("cop", "");
        
        // Formato colombiano: 1.000,00 o 1.000
        // Eliminar puntos de separación de miles y convertir coma decimal a punto
        if (limpio.contains(",")) {
            // Formato: 1.000,50 -> quitar puntos, mantener coma
            String[] partes = limpio.split(",");
            String parteEntera = partes[0].replace(".", "");
            String parteDecimal = partes.length > 1 ? partes[1] : "00";
            limpio = parteEntera + "." + parteDecimal;
        } else {
            // Formato: 1000 o 1.000 (sin decimales)
            limpio = limpio.replace(".", "");
        }
        
        try {
            BigDecimal resultado = new BigDecimal(limpio);
            if (resultado.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto debe ser mayor a 0");
            }
            return resultado;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de monto inválido. Use formato colombiano: 1.000 o 1.000,00");
        }
    }
}