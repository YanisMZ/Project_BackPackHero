package fr.uge.graphics;

import java.awt.Color;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.Dungeon;

/**
 * Runs the game application and manages player turns.
 */
public class GameRun {
	public GameRun() {
	}

	/**
	 * Starts the game application and handles the main game loop.
	 */
	public void run() {
		Application.run(Color.WHITE, context -> {
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

				if (controller.combat) {
					view.combatDisplay();
				}
				if (controller.corridor) {
					view.corridorDisplay();
				}
				if (controller.treasure) {
					view.treasureDisplay();
				}
			}
		});
	}
}
