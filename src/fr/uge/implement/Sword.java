package fr.uge.implement;

import java.util.Objects;

public record Sword(String name,int dmg) implements Item {
	public String name() {
    return this.name;
	}
	
	public Sword {
		Objects.requireNonNull(name);
		if (dmg < 0) {
			throw new IllegalArgumentException("Dammage can't be less than 0");
		}
	}
	
}
