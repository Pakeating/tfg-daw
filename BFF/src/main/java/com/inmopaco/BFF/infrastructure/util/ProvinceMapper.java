package com.inmopaco.BFF.infrastructure.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to map standard province names to database-specific names.
 */
public class ProvinceMapper {

    private static final Map<String, String> PROVINCE_MAP = new HashMap<>();

    static {
        PROVINCE_MAP.put("Álava", "Araba/Álava");
        PROVINCE_MAP.put("Alicante", "Alicante/Alacant");
        PROVINCE_MAP.put("Baleares", "Illes Balears");
        PROVINCE_MAP.put("Castellón", "Castellón/Castelló");
        PROVINCE_MAP.put("Guipúzcoa", "Gipuzkoa");
        PROVINCE_MAP.put("La Coruña", "A Coruña");
        PROVINCE_MAP.put("Orense", "Ourense");
        PROVINCE_MAP.put("Valencia", "Valencia/València");
        PROVINCE_MAP.put("Vizcaya", "Bizkaia");
    }

    /**
     * Maps a list of standard province names to their database counterparts.
     * If no mapping is found, the original name is kept.
     */
    public static List<String> map(List<String> provinces) {
        if (provinces == null) {
            return null;
        }
        return provinces.stream()
                .map(p -> PROVINCE_MAP.getOrDefault(p, p))
                .collect(Collectors.toList());
    }
}
