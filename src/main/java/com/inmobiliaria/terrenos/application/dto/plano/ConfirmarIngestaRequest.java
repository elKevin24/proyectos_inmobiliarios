package com.inmobiliaria.terrenos.application.dto.plano;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class ConfirmarIngestaRequest {
    private Long proyectoId;
    private List<LoteIngesta> lotes;

    @Data
    public static class LoteIngesta {
        private String numeroLote;
        private BigDecimal area;
        private String coordenadasPlanoJson;
    }
}
