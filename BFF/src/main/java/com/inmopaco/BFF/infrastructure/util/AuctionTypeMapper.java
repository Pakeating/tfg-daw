package com.inmopaco.BFF.infrastructure.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to map frontend auction type names to database-specific names.
 */
public class AuctionTypeMapper {

    private static final Map<String, List<String>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("Judicial (Apremio, Concursal y Voluntaria)", 
            List.of("JUDICIAL EN VÍA DE APREMIO", "JUDICIAL CONCURSAL", "JUDICIAL VOLUNTARIA"));
        
        TYPE_MAP.put("Agencia Tributaria (AEAT)", 
            List.of("AGENCIA TRIBUTARIA"));
        
        TYPE_MAP.put("Recaudación Tributaria", 
            List.of("RECAUDACIÓN TRIBUTARIA"));
        
        TYPE_MAP.put("Notarial (Hipotecarias y otras)", 
            List.of("NOTARIAL HIPOTECARIA", "OTRAS SUBASTAS NOTARIALES"));
        
        TYPE_MAP.put("Administrativa General", 
            List.of("ADMINISTRATIVA GENERAL"));
    }

    /**
     * Maps a frontend type name to its database counterpart(s).
     * Returns a list because one frontend type may cover multiple DB types.
     */
    public static List<String> map(String type) {
        if (type == null || type.isBlank()) {
            return Collections.emptyList();
        }
        return TYPE_MAP.getOrDefault(type, List.of(type));
    }
}
