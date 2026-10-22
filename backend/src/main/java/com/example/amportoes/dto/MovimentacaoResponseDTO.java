
package com.example.amportoes.dto;

import com.example.amportoes.entity.TipoMovimentacao;
import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
        Long id,
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        TipoMovimentacao tipo,
        String observacao,
        LocalDateTime dataMovimentacao
) {
}
