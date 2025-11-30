package fr.uge.implement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class Enemy {
	private ArrayList<Enemy> enemies;
	private int hp;
	
	public Enemy(int hp) {
		enemies = new ArrayList<>();
		this.hp = hp;
	}
	
	public void add(Enemy enemy) {
		Objects.requireNonNull(enemy);
		enemies.add(enemy);
	}
	
	@Override
	public String toString() {
	    return "Enemy : hp = " + hp +
	           (enemies.isEmpty() ? "" :
	                enemies.stream()
	                    .map(e -> "\n  " + e.toString().replace("\n", "\n  "))
	                    .collect(Collectors.joining()));
	}
}
