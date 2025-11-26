package fr.uge.main;

import fr.uge.graphics.GameRun;
import fr.uge.implement.BackPack;
import fr.uge.implement.Dungeon;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.Sword;

public class Main {

	public static void main(String[] args) {
		var backPack = new BackPack();
	    BackPack.fillBackPackForTest(backPack);
	    System.out.println(backPack.BackPackDisplay());
	    
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
