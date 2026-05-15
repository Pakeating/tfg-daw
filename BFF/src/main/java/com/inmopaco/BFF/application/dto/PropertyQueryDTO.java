package com.inmopaco.BFF.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyQueryDTO {

    private String propertyId;

    private String city;

    private String province;

    private String contractType;

    private String propertyType;
}
