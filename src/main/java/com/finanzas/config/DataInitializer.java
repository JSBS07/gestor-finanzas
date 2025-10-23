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
        admin.setPassword(passwordEncoder.encode("admin123")); // CONTRASEÑA CIFRADA
        admin.setNombre("Administrador");
        admin.setRol(RolUsuario.ADMIN);
        admin.setPasswordTemporal(false);
        usuarioRepository.save(admin);

        // Crear usuario NORMAL  
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@finanzas.com");
        usuario.setPassword(passwordEncoder.encode("usuario123")); // CONTRASEÑA CIFRADA
        usuario.setNombre("Usuario Prueba");
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setPasswordTemporal(false);
        usuarioRepository.save(usuario);

        System.out.println("USUARIOS DE PRUEBA CREADOS:");
        System.out.println("Admin: admin@finanzas.com / admin123");
        System.out.println("Usuario: usuario@finanzas.com / usuario123");

        crearActividadesDePrueba(usuario);
    }

    private void crearActividadesDePrueba(Usuario usuario) {
        Actividad actividad1 = new Actividad();
        actividad1.setDescripcion("Pago de salario");
        actividad1.setMonto(new BigDecimal("2500000"));
        actividad1.setTipo(TipoActividad.INGRESO);
        actividad1.setCategoria(Categoria.SALARIO);
        actividad1.setUsuario(usuario);
        actividad1.setEstado(EstadoActividad.COMPLETADO);
        actividadRepository.save(actividad1);

        Actividad actividad2 = new Actividad();
        actividad2.setDescripcion("Supermercado");
        actividad2.setMonto(new BigDecimal("150750"));
        actividad2.setTipo(TipoActividad.GASTO);
        actividad2.setCategoria(Categoria.ALIMENTACION);
        actividad2.setUsuario(usuario);
        actividad2.setEstado(EstadoActividad.COMPLETADO);
        actividadRepository.save(actividad2);

        System.out.println("Actividades de prueba creadas exitosamente!");
    }
}