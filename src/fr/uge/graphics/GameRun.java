package fr.uge.graphics;

import java.awt.Color;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.Combat;
import fr.uge.implement.Dungeon;
import fr.uge.implement.Hero;

public class GameRun {
  public GameRun() {

  }

  public void run() {
    Application.run(Color.BLACK, context -> {

      int status = 2;

      Dungeon dungeon = new Dungeon();

      BackPack backpack = dungeon.backpack();
      BackPack.fillBackPackForTest(backpack);

      System.out.println(backpack.BackPackDisplay());

      var floor0 = dungeon.getFloor(0);
      var hero = new Hero(40, 0);
      var fight = new Combat(hero);

      GameView view = new GameView(context, floor0, backpack);
      GameController controller = new GameController(context, view, floor0, backpack, fight);

      view.corridorDisplay();

      while (true) {

        controller.update();

        if (controller.isInCombat()) {
          view.combatDisplay(fight.nbEnemy(), status);
        } else if (controller.isInCorridor()) {
          view.corridorDisplay();
        } else if (controller.isInTreasure()) {
          view.treasureDisplay();
        } else {
          view.emptyRoomDisplay();
        }
      }
    });
  }
}
