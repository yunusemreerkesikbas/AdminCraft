package com.backend.application.query;

public class ComponentTypeQueries {

    public record GetComponentTypeByIdQuery(Long id) {}

    public record GetComponentTypeByCodeQuery(String code) {}

    public record GetAllComponentTypesQuery() {}

    public record GetComponentTypesByCategoryQuery(String category) {}
}

