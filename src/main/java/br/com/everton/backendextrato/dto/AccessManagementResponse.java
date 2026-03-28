package br.com.everton.backendextrato.dto;

import java.util.List;

public record AccessManagementResponse(
        List<SharedViewerResponse> viewers,
        List<AccessibleOwnerResponse> accessibleOwners
) {
}
