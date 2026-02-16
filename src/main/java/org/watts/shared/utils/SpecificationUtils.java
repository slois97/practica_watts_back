package org.watts.shared.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class SpecificationUtils {

    // Constructor privado para evitar instanciación
    private SpecificationUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }


    //Agrega un predicado a la lista basándose en el valor y el modo de coincidencia (MatchMode) de PrimeNG.
    //Soporta: startsWith, endsWith, equals, notEquals, notContains y contains (default).
    public static void addPredicate(List<Predicate> predicates, CriteriaBuilder cb, Path<String> path, String value, String matchMode) {
        if (value == null || value.isEmpty()) {
            return;
        }

        String mode = (matchMode != null) ? matchMode : "contains";

        switch (mode) {
            case "startsWith":
                predicates.add(cb.like(cb.lower(path), value.toLowerCase() + "%"));
                break;
            case "endsWith":
                predicates.add(cb.like(cb.lower(path), "%" + value.toLowerCase()));
                break;
            case "equals":
                predicates.add(cb.equal(cb.lower(path), value.toLowerCase()));
                break;
            case "notEquals":
                predicates.add(cb.notEqual(cb.lower(path), value.toLowerCase()));
                break;
            case "notContains":
                predicates.add(cb.notLike(cb.lower(path), "%" + value.toLowerCase() + "%"));
                break;
            case "contains":
            default:
                predicates.add(cb.like(cb.lower(path), "%" + value.toLowerCase() + "%"));
                break;
        }
    }
    // Metodo que maneja rangos de fechas de forma inteligente
    public static void addDateRangePredicate(List<Predicate> predicates, CriteriaBuilder cb, Path<LocalDateTime> path, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            // Caso: Entre dos fechas (inclusive)
            predicates.add(cb.between(path, fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59)));
        } else if (fechaInicio != null) {
            // Caso: Desde una fecha en adelante
            predicates.add(cb.greaterThanOrEqualTo(path, fechaInicio.atStartOfDay()));
        } else if (fechaFin != null) {
            // Caso: Hasta una fecha
            predicates.add(cb.lessThanOrEqualTo(path, fechaFin.atTime(23, 59, 59)));
        }
    }
}