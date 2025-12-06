package fr.uge.implement;

public record Room(String name, Type type) {
  public enum Type { ENEMY, TREASURE, CORRIDOR, MERCHANT, HEALER, EXIT }
}

