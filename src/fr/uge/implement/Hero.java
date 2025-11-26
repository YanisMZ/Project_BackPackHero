package fr.uge.implement;

public class Hero {
	private int hp;
	private int mana;
	
	public Hero initCharacter() {
		var champ = new Hero();
		champ.hp = 40;
		champ.mana = 0;
		
		return champ;
	}
	
	
}
