package fr.uge.implement;

import java.util.Objects;

public record Magical(String name) implements Item {
  public Magical {
    Objects.requireNonNull(name);
  }
}
