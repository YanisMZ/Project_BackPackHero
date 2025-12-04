package fr.uge.graphics;

import java.awt.Color;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.Dungeon;

/**
 * Lance l'application de jeu et gère la boucle principale.
 */
public class GameRun {
		public GameRun() {
		
	}


  public void run() {
    Application.run(Color.WHITE, context -> {
      int nbEnemies = 3;
      int status = 2;
      
      Dungeon dungeon = new Dungeon();
      
      BackPack backpack = dungeon.backpack();
      BackPack.fillBackPackForTest(backpack);

      System.out.println(backpack.BackPackDisplay());

      var floor0 = dungeon.getFloor(0);

      GameView view = new GameView(context, floor0, backpack);
      GameController controller = new GameController(context, view, floor0, backpack);

      view.corridorDisplay();
      
      while (true) {
    	  controller.update();

    	  if (controller.isInCombat()) {
        	view.combatDisplay(nbEnemies, status);
        } else if (controller.isInCorridor()) {
        	view.corridorDisplay();
        } else if (controller.isInTreasure()) {
        	view.treasureDisplay();
        }
      }
    });
  }
}
