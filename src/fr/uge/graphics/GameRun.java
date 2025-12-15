package fr.uge.graphics;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.BackpackExpansionSystem;
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

			// Créer le sac avec une taille max de 5x5
			BackPack backpack = new BackPack(5, 5);

			// Débloquer une zone de départ (3x3 en haut à gauche)
			int[][] startCells = { { 2, 3 }, { 2, 1 }, { 2, 0 }, { 0, 1 }, { 1, 1 }, { 2, 2 }, { 3, 3 }, { 3, 1 }, { 3,  2} };
			backpack.unlockCells(startCells);

			// Ajouter les items de départ
			backpack.place(new Sword("Sword 1", 10, 1, 1, 2), 2, 1);
			backpack.place(new Shield("Shield", 5, 1, 1), 2, 0);
			backpack.place(new Sword("Sword 2", 15, 1, 1, 2), 1, 1);

			var floor0 = dungeon.getFloor(0);
			var hero = new Hero(40, 0, 3);
			var fight = new Battle(hero);

			var screenInfo = context.getScreenInfo();
			var width = screenInfo.width();
			var height = screenInfo.height();

			context.renderFrame(graphics -> {
				graphics.setColor(Color.ORANGE);
				graphics.fill(new Rectangle2D.Float(0, 0, width, height));
			});

			GameView view = new GameView(context, floor0, backpack);
			GameController controller = new GameController(context, view, floor0, backpack, fight, dungeon);

			while (true) {
				int pollTimeout = controller.isDragging() ? 0 : 10;
				controller.update(pollTimeout);

				List<Integer> selectedSlots = controller.getSelectedSlots();
				Item[][] treasureGrid = controller.getTreasureGrid();
				boolean isDragging = controller.isDragging();
				Item draggedItem = controller.getDraggedItem();
				int dragOffsetX = controller.getDragOffsetX();
				int dragOffsetY = controller.getDragOffsetY();

				// Vérifier d'abord le mode expansion
				if (controller.isInExpansionMode()) {
					view.expansionDisplay(selectedSlots, hero, controller.getExpansionSystem());
				} else if (controller.isInCombat()) {
					view.combatDisplay(fight.nbEnemy(), status, selectedSlots, hero, fight.getEnemy(), isDragging, draggedItem,
							dragOffsetX, dragOffsetY, controller.getLastAttackTime());
				} else if (controller.isInCorridor()) {
					view.corridorDisplay(selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				} else if (controller.isInTreasure()) {
					view.treasureDisplay(selectedSlots, treasureGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				} else {
					view.emptyRoomDisplay(selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				}

				if (hero.hp() <= 0) {
					System.out.println("Votre personnage est MORT !");
					System.exit(0);
				}
			}
		});
	}
}