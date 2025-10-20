package com.finanzas.config;

import com.finanzas.entity.*;
import com.finanzas.repository.UsuarioRepository;
import com.finanzas.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;


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
        } else {
            // Verificar que los usuarios de prueba existen y tienen contraseñas correctas
            verificarUsuariosDePrueba();
        }
    }

    private void crearUsuariosDePrueba() {
        System.out.println("Creando usuarios de prueba...");

        // Crear usuario ADMIN
        Usuario admin = new Usuario();
        admin.setEmail("admin@finanzas.com");
        admin.setPassword("123456"); // Se encriptará automáticamente
        admin.setNombre("Administrador");
        admin.setRol(RolUsuario.ADMIN);
        usuarioRepository.save(admin);

        // Crear usuario NORMAL
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@finanzas.com");
        usuario.setPassword("123456"); // Se encriptará automáticamente
        usuario.setNombre("Usuario Prueba");
        usuario.setRol(RolUsuario.USUARIO);
        usuarioRepository.save(usuario);

        // Verificar que se crearon correctamente
        System.out.println("Usuarios de prueba creados:");
        System.out.println("Admin: " + admin.getEmail() + " - Hash: " + admin.getPassword());
        System.out.println("Usuario: " + usuario.getEmail() + " - Hash: " + usuario.getPassword());

        // Crear actividades para el usuario normal
        crearActividadesDePrueba(usuario);
    }

    private void verificarUsuariosDePrueba() {
        System.out.println("Verificando usuarios de prueba...");
        
        Optional<Usuario> adminOpt = usuarioRepository.findByEmail("admin@finanzas.com");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail("usuario@finanzas.com");
        
        if (adminOpt.isPresent()) {
            Usuario admin = adminOpt.get();
            boolean adminPasswordCorrecta = passwordEncoder.matches("123456", admin.getPassword());
            System.out.println("Admin password correcta: " + adminPasswordCorrecta);
        }
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean usuarioPasswordCorrecta = passwordEncoder.matches("123456", usuario.getPassword());
            System.out.println("Usuario password correcta: " + usuarioPasswordCorrecta);
        }
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

        System.out.println("Usuarios de prueba creados exitosamente!");
        System.out.println("Admin: admin@finanzas.com / 123456");
        System.out.println("Usuario: usuario@finanzas.com / 123456");
    }
}