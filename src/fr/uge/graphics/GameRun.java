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
import fr.uge.implement.HealingItem;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.ItemType;
import fr.uge.implement.Malediction;
import fr.uge.implement.Ration;
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

		// Déblocage des cellules nécessaires pour la forme S (3x2)
			//int[][] sCells = { {1,1}, {2,1}, {0,2}, {1,2}, {2,2} };
			//backpack.unlockCells(sCells);

			// Placement
			
			for (int y = 0; y < 7; y++) {
		    for (int x = 0; x < 5; x++) {
		        backpack.unlockCell(x, y);
		    }
		}
		


			var floor0 = dungeon.getFloor(0);
			GameView view = new GameView(context, floor0, backpack);

//		Rayane M2
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
//			Jusqu'ici car Boolean classique marchait pas 
//			+ affichage en continue
			
	

		
			backpack.place(new Sword(ItemType.SWORD, 10, 1, 1, 2,3), 2, 2);
			backpack.place(new Shield("Shield", 5, 1, 1,3), 2, 6);
			backpack.place(new Ration("Ration", 1, 1, 1), 3, 1);
			backpack.place(new HealingItem("Heal", 10, 1, 1), 3, 2);
			backpack.autoAdd(new Gold("Gold", 65));
			backpack.autoAdd(new Gold("Gold", 5));


			var hero = new Hero(40, 0, 3, backpack);
			var fight = new Battle(hero,backpack);

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

				if (controller.isInExpansionMode()) {
					view.expansionDisplay(controller,selectedSlots, hero, controller.getExpansionSystem());
				} else if (controller.isInCombat()) {
					view.combatDisplay(controller,fight.nbEnemy(), status, selectedSlots, hero, fight.getEnemy(), isDragging, draggedItem,
							dragOffsetX, dragOffsetY, controller.getLastAttackTime(),controller.getFloatingItems());
				} else if (controller.isInCorridor()) {
					view.corridorDisplay(controller,selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems(), controller.getLastChangeRoom(), controller.isTransitionFromMerchant());
				} else if (controller.isInTreasure()) {
					view.treasureDisplay(controller,selectedSlots, treasureGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				} else if (controller.isInMerchant()) {
					view.merchantDisplay(controller,selectedSlots, getMerchantGrid, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				} else {
					view.emptyRoomDisplay(controller,selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
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