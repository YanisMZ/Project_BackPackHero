package fr.uge.graphics;

import java.awt.Color;
import java.util.List;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.Battle;
import fr.uge.implement.Dungeon;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.Shield;
import fr.uge.implement.Sword;

public class GameRun {
	public GameRun() {
	}

	public void run() {
		Application.run(Color.BLACK, context -> {

			int status = 0;
			Dungeon dungeon = new Dungeon();
			BackPack backpack = new BackPack(5, 5);

			// Ajout manuel des objets au sac (coordonnées x,y)
			backpack.addAt(new Sword("Épée 1", 10, 1, 2), 0, 0);
			backpack.addAt(new Shield("Bouclier", 5, 1, 1), 2, 0);
			backpack.addAt(new Sword("Épée 2", 15, 1, 2), 3, 0);

			var floor0 = dungeon.getFloor(0);
			var hero = new Hero(40, 0);
			var fight = new Battle(hero);

			GameView view = new GameView(context, floor0, backpack);
			GameController controller = new GameController(context, view, floor0, backpack, fight, dungeon);

			while (true) {
				controller.update();

				// Récupération de l'état du controller
				List<Integer> selectedSlots = controller.getSelectedSlots();
				Item[][] treasureGrid = controller.getTreasureGrid(); // Changement : grille 2D au lieu de List
				boolean isDragging = controller.isDragging();
				Item draggedItem = controller.getDraggedItem();
				int dragOffsetX = controller.getDragOffsetX();
				int dragOffsetY = controller.getDragOffsetY();

				// Rendu selon l'état du jeu avec support du drag and drop
				if (controller.isInCombat()) {
					view.combatDisplay(fight.nbEnemy(), status, selectedSlots, hero, fight.getEnemy(), isDragging, draggedItem,
							dragOffsetX, dragOffsetY, controller.getLastAttackTime());
				} else if (controller.isInCorridor()) {
					view.corridorDisplay(selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY);
				} else if (controller.isInTreasure()) {
					// Changement : passer la grille 2D au lieu d'une liste
					view.treasureDisplay(selectedSlots, treasureGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY);
				} else {
					view.emptyRoomDisplay(selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY);
				}

				// Vérification de la mort du héros
				if (hero.hp() <= 0) {
					System.out.println("Votre personnage est MORT !");
					System.exit(0);
				}
			}
		});
	}
}