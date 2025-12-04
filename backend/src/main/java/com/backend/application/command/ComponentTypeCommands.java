package com.backend.application.command;

public class ComponentTypeCommands {

  public record CreateComponentTypeCommand(
      String name,
      String category,
      Long userId) {
  }

  public record UpdateComponentTypeCommand(
      Long id,
      String name,
      String category,
      Long userId) {
  }

  public record DeleteComponentTypeCommand(Long id) {
  }
}
