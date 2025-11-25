package fr.uge.implement;

public record Sword(String name,int dmg) implements Item {
	public String name() {
    return this.name;
}
	
}
