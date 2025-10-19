package com.finanzas.repository;

import com.finanzas.entity.Actividad;
import com.finanzas.entity.Categoria;
import com.finanzas.entity.EstadoActividad;
import com.finanzas.entity.TipoActividad;
import com.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // Encontrar actividades por usuario ordenadas por fecha
    List<Actividad> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Encontrar actividades por usuario y tipo
    List<Actividad> findByUsuarioAndTipoOrderByCreatedAtDesc(Usuario usuario, TipoActividad tipo);

    // Encontrar últimas actividades (para dashboard)
    List<Actividad> findTop5ByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Calcular total por tipo y mes usando EXTRACT (PostgreSQL) - SOLO COMPLETADAS
    @Query(value = "SELECT COALESCE(SUM(a.monto), 0) " +
                   "FROM actividades a " +
                   "WHERE a.usuario_id = :usuarioId " +
                   "AND a.tipo = :tipo " +
                   "AND a.estado = 'COMPLETADO' " +  // FILTRO AGREGADO
                   "AND EXTRACT(YEAR FROM a.created_at) = :year " +
                   "AND EXTRACT(MONTH FROM a.created_at) = :month",
           nativeQuery = true)
    BigDecimal sumMontoByUsuarioAndTipoAndMonth(@Param("usuarioId") Long usuarioId,
                                                @Param("tipo") String tipo,
                                                @Param("year") int year,
                                                @Param("month") int month);

    // Método alternativo usando BETWEEN para fechas - SOLO COMPLETADAS
    @Query("SELECT SUM(a.monto) FROM Actividad a " +
           "WHERE a.usuario.id = :usuarioId " +
           "AND a.tipo = :tipo " +
           "AND a.estado = 'COMPLETADO' " +  // FILTRO AGREGADO
           "AND a.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumMontoByUsuarioAndTipoAndDateRange(@Param("usuarioId") Long usuarioId,
                                                    @Param("tipo") TipoActividad tipo,
                                                    @Param("startDate") java.time.LocalDateTime startDate,
                                                    @Param("endDate") java.time.LocalDateTime endDate);

    // Encontrar actividades por usuario y estado
    List<Actividad> findByUsuarioAndEstadoOrderByCreatedAtDesc(Usuario usuario, EstadoActividad estado);

    // Encontrar actividades por usuario, tipo y estado
    List<Actividad> findByUsuarioAndTipoAndEstadoOrderByCreatedAtDesc(Usuario usuario, TipoActividad tipo, EstadoActividad estado);

    // Encontrar actividades por usuario y categoría
    List<Actividad> findByUsuarioAndCategoriaOrderByCreatedAtDesc(Usuario usuario, Categoria categoria);

    // Encontrar actividades por usuario, tipo y categoría
    List<Actividad> findByUsuarioAndTipoAndCategoriaOrderByCreatedAtDesc(Usuario usuario, TipoActividad tipo, Categoria categoria);

    // Encontrar actividades por usuario, estado y categoría
    List<Actividad> findByUsuarioAndEstadoAndCategoriaOrderByCreatedAtDesc(Usuario usuario, EstadoActividad estado, Categoria categoria);

    // Encontrar actividades por usuario, tipo, estado y categoría
    List<Actividad> findByUsuarioAndTipoAndEstadoAndCategoriaOrderByCreatedAtDesc(Usuario usuario, TipoActividad tipo, EstadoActividad estado, Categoria categoria);
}