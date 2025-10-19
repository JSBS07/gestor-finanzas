package com.finanzas.config;

import com.finanzas.entity.*;
import com.finanzas.repository.UsuarioRepository;
import com.finanzas.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existen usuarios
        if (usuarioRepository.count() == 0) {
            crearUsuariosDePrueba();
        }
    }

    private void crearUsuariosDePrueba() {
        System.out.println("Creando usuarios de prueba...");

        // Crear usuario ADMIN
        Usuario admin = new Usuario();
        admin.setEmail("admin@finanzas.com");
        admin.setPassword(passwordEncoder.encode("123456")); // Contraseña: 123456
        admin.setNombre("Administrador");
        admin.setRol(RolUsuario.ADMIN);
        usuarioRepository.save(admin);

        // Crear usuario NORMAL
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@finanzas.com");
        usuario.setPassword(passwordEncoder.encode("123456")); // Contraseña: 123456
        usuario.setNombre("Usuario Prueba");
        usuario.setRol(RolUsuario.USUARIO);
        usuarioRepository.save(usuario);

        // Crear actividades para el usuario normal - CORREGIDAS con el nuevo campo estado
        Actividad actividad1 = new Actividad();
        actividad1.setDescripcion("Pago de salario");
        actividad1.setMonto(new BigDecimal("2500000")); // 2.500.000 en formato sin puntos
        actividad1.setTipo(TipoActividad.INGRESO);
        actividad1.setCategoria(Categoria.SALARIO);
        actividad1.setUsuario(usuario);
        actividad1.setEstado(EstadoActividad.COMPLETADO); // Marcamos como completado
        actividadRepository.save(actividad1);

        Actividad actividad2 = new Actividad();
        actividad2.setDescripcion("Supermercado");
        actividad2.setMonto(new BigDecimal("150750")); // 150.750 en formato sin puntos
        actividad2.setTipo(TipoActividad.GASTO);
        actividad2.setCategoria(Categoria.ALIMENTACION);
        actividad2.setUsuario(usuario);
        actividad2.setEstado(EstadoActividad.COMPLETADO); // Marcamos como completado
        actividadRepository.save(actividad2);

        // Podemos agregar algunas actividades pendientes para demostrar la funcionalidad
        Actividad actividad3 = new Actividad();
        actividad3.setDescripcion("Pago de arriendo");
        actividad3.setMonto(new BigDecimal("800000")); // 800.000
        actividad3.setTipo(TipoActividad.GASTO);
        actividad3.setCategoria(Categoria.VIVIENDA);
        actividad3.setUsuario(usuario);
        actividad3.setEstado(EstadoActividad.PENDIENTE); // Pendiente
        actividadRepository.save(actividad3);

        Actividad actividad4 = new Actividad();
        actividad4.setDescripcion("Bono de rendimiento");
        actividad4.setMonto(new BigDecimal("500000")); // 500.000
        actividad4.setTipo(TipoActividad.INGRESO);
        actividad4.setCategoria(Categoria.OTROS_INGRESOS);
        actividad4.setUsuario(usuario);
        actividad4.setEstado(EstadoActividad.PENDIENTE); // Pendiente
        actividadRepository.save(actividad4);

        System.out.println("Usuarios de prueba creados exitosamente!");
        System.out.println("Admin: admin@finanzas.com / 123456");
        System.out.println("Usuario: usuario@finanzas.com / 123456");
        System.out.println("Actividades de prueba creadas:");
        System.out.println("- 2 actividades COMPLETADAS (afectan el balance)");
        System.out.println("- 2 actividades PENDIENTES (no afectan el balance)");
    }
}