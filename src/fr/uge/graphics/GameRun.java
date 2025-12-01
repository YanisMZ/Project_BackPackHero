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
<<<<<<< Updated upstream
=======
			int nb_enemies = 2;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream

				if (controller.combat) {
					view.combatDisplay();
=======
				if (controller.combat) {
					view.combatDisplay(nb_enemies);
>>>>>>> Stashed changes
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
