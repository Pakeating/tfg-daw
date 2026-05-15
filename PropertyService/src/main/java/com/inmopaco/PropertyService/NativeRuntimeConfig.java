package com.inmopaco.PropertyService;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.util.ClassUtils;

@Configuration
@ImportRuntimeHints(MariaDBHints.class)
public class NativeRuntimeConfig {
}

class MariaDBHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Clases fundamentales de MariaDB 2.7.x para reflexión
        String[] mariaClasses = {
                "org.mariadb.jdbc.Driver",
                "org.mariadb.jdbc.util.Options",
                "org.mariadb.jdbc.Configuration",
                "com.zaxxer.hikari.HikariConfig",
                "com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry"
        };

        for (String className : mariaClasses) {
            if (ClassUtils.isPresent(className, classLoader)) {
                hints.reflection().registerType(ClassUtils.resolveClassName(className, classLoader),
                        builder -> builder.withMembers(
                                MemberCategory.INVOKE_PUBLIC_METHODS,
                                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                                MemberCategory.DECLARED_FIELDS
                        ));
            }
        }

        // Registro específico para el dialecto de hibernate para JPA
        hints.reflection().registerType(org.hibernate.dialect.MariaDBDialect.class,
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
    }
}
