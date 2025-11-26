package fr.uge.implement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class Ennemy {
	private ArrayList<Ennemy> ennemies;
	private int hp;
	
	public Ennemy(int hp) {
		ennemies = new ArrayList<>();
		this.hp = hp;
	}
	
	public void add(Ennemy ennemy) {
		Objects.requireNonNull(ennemy);
		ennemies.add(ennemy);
	}
	
	public String toString() {
		return ennemies.stream().map(Ennemy::toString).collect(Collectors.joining("\n"));
	}
	
}
