package com.inmopaco.AIService.domain.enums;

import java.util.Objects;
import java.util.Optional;

public enum AIRequestType {
    // enum con template de prompt. Meterlo con {} para indicar donde va el input
    AUCTIONS_DEBT_ANALYSIS("Eres un experto analista en subastas judiciales y seguridad social en España. Tu misión es analizar el siguiente texto extraído de un documento y realizar lo siguiente:\n\n" +
            "1. **Identificación del Tipo de Documento**: Determina si es un Edicto, Certificación de Cargas, Mandamiento de Embargo, Acta de Subasta o simplemente un documento de Bases Legales/Procedimentales.\n" +
            "2. **Análisis de Deudas y Cargas**: \n" +
            "   - Si el documento contiene información sobre gravámenes: Identifica las cargas que el adjudicatario (comprador) debe asumir (posteriores a la carga que ejecuta, IBI, comunidad, etc.).\n" +
            "   - Si el documento es meramente procedimental o legal y NO contiene datos de deudas específicas: Indícalo claramente y resume brevemente el propósito del documento.\n\n" +
            "Por favor, estructura tu respuesta de la siguiente manera:\n" +
            "- **Tipo de Documento**: [Nombre del tipo detectado]\n" +
            "- **Cargas Preferentes**: [Hipotecas anteriores o embargos que permanecen]\n" +
            "- **Deudas Pendientes**: [IBI, Comunidad de Propietarios, etc.]\n" +
            "- **Importes**: [Cantidades exactas si aparecen]\n" +
            "- **Resumen de Riesgo/Observaciones**: [Conclusión sobre la información hallada]\n\n" +
            "Responde de forma concisa, profesional y en español. Si el documento no permite extraer datos económicos, justifica el porqué basándote en su tipo.\n\n" +
            "Texto del documento:\n{}")

    ;

    private final String promptTemplate;

    AIRequestType(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public String format(String input) {
        return promptTemplate.replace("{}", Optional.ofNullable(input).orElse(""));
    }
}

