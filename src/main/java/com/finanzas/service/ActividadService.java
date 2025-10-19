package com.finanzas.service;

import com.finanzas.entity.Actividad;
import com.finanzas.entity.Categoria;
import com.finanzas.entity.EstadoActividad;
import com.finanzas.entity.TipoActividad;
import com.finanzas.entity.Usuario;
import com.finanzas.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    // Guardar actividad
    public Actividad guardarActividad(Actividad actividad) {
        return actividadRepository.save(actividad);
    }

    // Encontrar actividad por ID
    public Optional<Actividad> encontrarPorId(Long id) {
        return actividadRepository.findById(id);
    }

    // Obtener actividades de un usuario
    public List<Actividad> obtenerPorUsuario(Usuario usuario) {
        return actividadRepository.findByUsuarioOrderByCreatedAtDesc(usuario);
    }

    // Obtener últimas actividades (para dashboard)
    public List<Actividad> obtenerRecientes(Usuario usuario) {
        return actividadRepository.findTop5ByUsuarioOrderByCreatedAtDesc(usuario);
    }

    // Obtener actividades por tipo
    public List<Actividad> obtenerPorTipo(Usuario usuario, TipoActividad tipo) {
        return actividadRepository.findByUsuarioAndTipoOrderByCreatedAtDesc(usuario, tipo);
    }

    // Calcular total por tipo y mes actual - SOLO ACTIVIDADES COMPLETADAS
    public BigDecimal calcularTotalPorTipoYMes(Usuario usuario, TipoActividad tipo) {
        LocalDate now = LocalDate.now();

        BigDecimal resultado = actividadRepository.sumMontoByUsuarioAndTipoAndMonth(
                usuario.getId(),
                tipo.name(),
                now.getYear(),
                now.getMonthValue()
        );

        return resultado != null ? resultado : BigDecimal.ZERO;
    }

    // Método alternativo más robusto - SOLO ACTIVIDADES COMPLETADAS
    public BigDecimal calcularTotalPorTipoYMesAlternativo(Usuario usuario, TipoActividad tipo) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // Convertir a LocalDateTime para comparar con createdAt (LocalDateTime)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);

        BigDecimal resultado = actividadRepository.sumMontoByUsuarioAndTipoAndDateRange(
                usuario.getId(),
                tipo,
                startDateTime,
                endDateTime
        );

        return resultado != null ? resultado : BigDecimal.ZERO;
    }

    // Eliminar actividad
    public void eliminarActividad(Long id) {
        actividadRepository.deleteById(id);
    }

    // Calcular balance del mes - SOLO ACTIVIDADES COMPLETADAS
    public BigDecimal calcularBalanceMes(Usuario usuario) {
        BigDecimal ingresos = calcularTotalPorTipoYMesAlternativo(usuario, TipoActividad.INGRESO);
        BigDecimal gastos = calcularTotalPorTipoYMesAlternativo(usuario, TipoActividad.GASTO);

        return ingresos.subtract(gastos);
    }

    // Método para obtener totales de forma segura - SOLO ACTIVIDADES COMPLETADAS
    public BigDecimal calcularTotalPorTipoYMesSeguro(Usuario usuario, TipoActividad tipo) {
        try {
            return calcularTotalPorTipoYMes(usuario, tipo);
        } catch (Exception e) {
            return calcularTotalPorTipoYMesAlternativo(usuario, tipo);
        }
    }

    // =========================
    // MÉTODOS PARA SISTEMA DE ESTADOS Y CATEGORÍAS
    // =========================

    // Obtener actividades por usuario y estado
    public List<Actividad> obtenerPorUsuarioYEstado(Usuario usuario, EstadoActividad estado) {
        return actividadRepository.findByUsuarioAndEstadoOrderByCreatedAtDesc(usuario, estado);
    }

    // Obtener actividades por usuario, tipo y estado
    public List<Actividad> obtenerPorUsuarioYTipoYEstado(Usuario usuario, TipoActividad tipo, EstadoActividad estado) {
        return actividadRepository.findByUsuarioAndTipoAndEstadoOrderByCreatedAtDesc(usuario, tipo, estado);
    }

    // Obtener actividades por usuario, tipo y categoría
    public List<Actividad> obtenerPorUsuarioYTipoYCategoria(Usuario usuario, TipoActividad tipo, Categoria categoria) {
        return actividadRepository.findByUsuarioAndTipoAndCategoriaOrderByCreatedAtDesc(usuario, tipo, categoria);
    }

    // Obtener actividades por usuario, estado y categoría
    public List<Actividad> obtenerPorUsuarioYEstadoYCategoria(Usuario usuario, EstadoActividad estado, Categoria categoria) {
        return actividadRepository.findByUsuarioAndEstadoAndCategoriaOrderByCreatedAtDesc(usuario, estado, categoria);
    }

    // Obtener actividades por usuario, tipo, estado y categoría
    public List<Actividad> obtenerPorUsuarioYTipoYEstadoYCategoria(Usuario usuario, TipoActividad tipo, EstadoActividad estado, Categoria categoria) {
        return actividadRepository.findByUsuarioAndTipoAndEstadoAndCategoriaOrderByCreatedAtDesc(usuario, tipo, estado, categoria);
    }
}