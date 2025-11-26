package com.backend.application.query;

public class ComponentQueries {

    public record GetComponentByIdQuery(Long id) {}

    public record GetComponentWithI18nQuery(Long id) {}

    public record GetAllComponentsQuery() {}

    public record GetAllComponentsWithTranslationsQuery() {}

    public record GetComponentsByTypeIdQuery(Long typeId) {}
}

