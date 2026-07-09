package com.unz.eval.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annoter une méthode avec @Audited pour tracer automatiquement son appel.
 * L'action, l'utilisateur, l'horodatage et le résultat sont enregistrés.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
    String description() default "";
}
