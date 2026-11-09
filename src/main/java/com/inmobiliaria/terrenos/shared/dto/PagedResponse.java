package com.inmobiliaria.terrenos.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada genérica")
public class PagedResponse<T> {

    @Schema(description = "Lista de elementos de la página actual")
    private List<T> content;

    @Schema(description = "Número de página actual (0-based)", example = "0")
    private int page;

    @Schema(description = "Tamaño de página", example = "20")
    private int size;

    @Schema(description = "Total de elementos", example = "150")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "8")
    private int totalPages;

    @Schema(description = "¿Es la primera página?", example = "true")
    private boolean first;

    @Schema(description = "¿Es la última página?", example = "false")
    private boolean last;

    @Schema(description = "¿Está vacía?", example = "false")
    private boolean empty;
}
