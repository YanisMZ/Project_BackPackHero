package fr.uge.main;

import fr.uge.graphics.GameRun;
import fr.uge.implement.BackPack;
import fr.uge.implement.Dungeon;
import fr.uge.implement.Enemy;
import fr.uge.implement.Hero;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.Sword;

public class Main {

	public static void main(String[] args) {
		
		Hero hero = new Hero().initCharacter();
		Enemy boss = new Enemy(200);
        Enemy goblin1 = new Enemy(50);
        Enemy goblin2 = new Enemy(60);
        Enemy orc = new Enemy(120);
        
        // Add the enemies to the boss
        boss.add(goblin1);
        boss.add(goblin2);
        boss.add(orc);

        // Print
        System.out.println(hero);
        System.out.println("Enemies list:");
        System.out.println(boss.toString());
	    
	    Dungeon dungeon = new Dungeon();

	    MapDungeon floor1 = dungeon.getFloor(0);
	    floor1.show();

	    MapDungeon floor2 = dungeon.getFloor(1);
	    floor2.show();

	    MapDungeon floor3 = dungeon.getFloor(2);
	    floor3.show();
	    
	    
	    GameRun gameRun = new GameRun();
		gameRun.run();
	}

}
