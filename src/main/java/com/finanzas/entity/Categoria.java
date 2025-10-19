package com.finanzas.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Categoria {
    // Gastos
    ALIMENTACION("GASTO"),
    TRANSPORTE("GASTO"), 
    VIVIENDA("GASTO"),
    ENTRETENIMIENTO("GASTO"),
    SALUD("GASTO"),
    EDUCACION("GASTO"),
    ROPA("GASTO"),
    OTROS_GASTOS("GASTO"),
    
    // Ingresos
    SALARIO("INGRESO"),
    REGALO("INGRESO"),
    OTROS_INGRESOS("INGRESO");

    private final String tipo;

    Categoria(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    // Método para obtener categorías por tipo
    public static List<Categoria> getCategoriasPorTipo(TipoActividad tipo) {
        return Arrays.stream(values())
                .filter(cat -> cat.getTipo().equals(tipo.name()))
                .collect(Collectors.toList());
    }

    // Método para obtener el nombre formateado (más legible)
    public String getNombreFormateado() {
        return this.name().replace('_', ' ').toLowerCase()
                .replaceFirst(".", String.valueOf(this.name().charAt(0)));
    }
}