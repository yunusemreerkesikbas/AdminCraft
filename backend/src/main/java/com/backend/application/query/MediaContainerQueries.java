package com.backend.application.query;

public final class MediaContainerQueries {

    private MediaContainerQueries() {}

    public record GetContainerByMasterIdQuery(Long masterMediaId) {}

    public record GetContainerByIdQuery(Long containerId) {}

    public record GetVariantQuery(Long masterId, String formatCode) {}
}
