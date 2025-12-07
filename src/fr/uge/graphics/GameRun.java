package fr.uge.graphics;

import java.awt.Color;
import java.util.List;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.Battle;
import fr.uge.implement.Dungeon;
import fr.uge.implement.Hero;
import fr.uge.implement.Shield;
import fr.uge.implement.Sword;

public class GameRun {
  public GameRun() {

  }

  public void run() {
    Application.run(Color.BLACK, context -> {

      int status = 0;

      Dungeon dungeon = new Dungeon();

      BackPack backpack = dungeon.backpack();

      for (int i = 0; i < 2; i++) {
        backpack.add(new Sword("Épée " + (i + 1), 10 + i));
        backpack.add(new Shield("Shield", 10));
      }

      var floor0 = dungeon.getFloor(0);
      var hero = new Hero(40, 0);
      var fight = new Battle(hero);

      GameView view = new GameView(context, floor0, backpack);
      GameController controller = new GameController(context, view, floor0, backpack, fight);

      while (true) {
        controller.update();
        List<Integer> selectedSlots = controller.getSelectedSlots();
        if (controller.isInCombat()) {
          view.combatDisplay(fight.nbEnemy(), status, selectedSlots);
        } else if (controller.isInCorridor()) {
          view.corridorDisplay(selectedSlots);
        } else if (controller.isInTreasure()) {
          view.treasureDisplay(selectedSlots);
        } else {
          view.emptyRoomDisplay(selectedSlots);
        }
        if (hero.hp() <= 0) {
          System.out.println("Votre personnage est MORT !");
          System.exit(0); // Show new image and leave
        }
      }
    });
  }
}
