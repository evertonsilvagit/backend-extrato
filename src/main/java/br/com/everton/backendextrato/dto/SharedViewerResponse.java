package br.com.everton.backendextrato.dto;

import java.time.OffsetDateTime;

public record SharedViewerResponse(
        Long id,
        String email,
        String name,
        String photo,
        OffsetDateTime grantedAt
) {
}
