package fr.uge.room;

import java.util.Objects;

public record Room(String name, Type type) {
	public Room {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
	}

	public enum Type {
		ENEMY, TREASURE, CORRIDOR, MERCHANT, HEALER, EXIT
	}
}
