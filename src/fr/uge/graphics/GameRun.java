package fr.uge.graphics;

import java.awt.Color;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
import fr.uge.implement.BackpackExpansionSystem;
import fr.uge.implement.Battle;
import fr.uge.implement.Dungeon;
import fr.uge.implement.Gold;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.ItemType;
import fr.uge.implement.Shield;
import fr.uge.implement.Sword;

public class GameRun {

	public GameRun() {
	}

	public void run() {
		Application.run(Color.BLACK, context -> {
			int status = 0;
			Dungeon dungeon = new Dungeon();

			BackPack backpack = new BackPack(5, 7);

			int[][] startCells = { { 2, 3 }, { 2, 1 }, { 2, 0 }, { 0, 1 }, { 1, 1 }, { 2, 2 }, { 3, 3 }, { 3, 1 }, { 3, 2 } };
			backpack.unlockCells(startCells);

			var floor0 = dungeon.getFloor(0);
			GameView view = new GameView(context, floor0, backpack);

			AtomicBoolean areAssetsLoaded = new AtomicBoolean(false);
			long startTime = System.currentTimeMillis();

			new Thread(() -> {
				GameView.loadGameAssets();
				areAssetsLoaded.set(true);
			}).start();

			while (!areAssetsLoaded.get()) {
				view.loadingDisplay(startTime);
				context.pollOrWaitEvent(10);
			}

			backpack.place(new Sword(ItemType.SWORD, 10, 1, 1, 2), 2, 1);
			backpack.place(new Shield("Shield", 5, 1, 1), 2, 0);
			backpack.place(new Sword(ItemType.SWORD, 15, 1, 1, 2), 1, 1);
			backpack.autoAdd(new Gold("Gold", 10));
			backpack.autoAdd(new Gold("Gold", 5));
			// → une seule case avec Gold(quantity=15)

			var hero = new Hero(40, 0, 3, backpack);
			var fight = new Battle(hero);

			var screenInfo = context.getScreenInfo();
			var width = screenInfo.width();
			var height = screenInfo.height();

			GameController controller = new GameController(context, view, floor0, backpack, fight, dungeon, hero);

			while (true) {
				int pollTimeout = controller.isDragging() ? 0 : 10;
				controller.update(pollTimeout);

				List<Integer> selectedSlots = controller.getSelectedSlots();
				Item[][] treasureGrid = controller.getTreasureGrid();
				Item[][] getMerchantGrid = controller.getMerchantGrid();
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
							controller.getFloatingItems(), controller.getLastChangeRoom());
				} else if (controller.isInTreasure()) {
					view.treasureDisplay(selectedSlots, treasureGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				} else if (controller.isInMerchant()) {
					view.merchantDisplay(selectedSlots, getMerchantGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
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