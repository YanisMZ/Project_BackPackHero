package fr.uge.graphics;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.forax.zen.Application;

import fr.uge.backpack.BackPack;
import fr.uge.backpack.BackpackExpansionSystem;
import fr.uge.combat.Battle;
import fr.uge.enemy.Hero;
import fr.uge.items.Gold;
import fr.uge.items.HealingItem;
import fr.uge.items.Item;
import fr.uge.items.ItemType;
import fr.uge.items.Ration;
import fr.uge.items.Shield;
import fr.uge.items.Sword;
import fr.uge.map.Dungeon;
import fr.uge.room.HealerRoom;
import fr.uge.room.Merchant;
import fr.uge.room.TreasureChest;

/**
 * Main class to launch the game.
 * It will create the window and the main loop.
 */
public class GameRun {

	public GameRun() {
	}
	/**
	 * Start the application and init variable
	 */
	public void run() {
		Application.run(Color.BLACK, context -> {
			int status = 0;
			Dungeon dungeon = new Dungeon();

			BackPack backpack = new BackPack(5, 7);
			int[][] sCells = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 2 }, { 2, 3 }, { 2, 4 }, { 3, 2 }, { 3, 3 }, { 3, 4 } };
			backpack.unlockCells(sCells);

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

			backpack.place(new Sword(ItemType.SWORD, 10, 1, 1, 2, 5), 2, 2);
			backpack.place(new HealingItem("Heal", 10, 1, 1), 3, 2);
			backpack.autoAdd(new Gold("Gold", 10));

			var hero = new Hero(40, 0, 3, 0, backpack);
			var fight = new Battle(hero, backpack, new Random());

			var screenInfo = context.getScreenInfo();
			var width = screenInfo.width();
			var height = screenInfo.height();

			var treasureChest = new TreasureChest(3, 5);
			var merchant = new Merchant(3, 5);
			var expansionSystem = new BackpackExpansionSystem(backpack);
			var healerRoom = new HealerRoom();

			GameController controller = new GameController(context, view, floor0, backpack, fight, dungeon, hero,
					treasureChest, merchant, expansionSystem, healerRoom);

			while (true) {
				boolean needsFastUpdate = controller.isDragging() || controller.isPlayerMoving()
						|| controller.isFollowingPath();
				int pollTimeout = needsFastUpdate ? 0 : 10;

				controller.update(pollTimeout);

				List<Integer> selectedSlots = controller.getSelectedSlots();
				Item[][] treasureGrid = controller.getTreasureGrid();
				Item[][] getMerchantGrid = controller.getMerchantGrid();
				boolean isDragging = controller.isDragging();
				Item draggedItem = controller.getDraggedItem();
				int dragOffsetX = controller.getDragOffsetX();
				int dragOffsetY = controller.getDragOffsetY();

				if (controller.isInExpansionMode()) {
					view.expansionDisplay(controller, selectedSlots, hero, controller.getExpansionSystem());
				} else if (controller.isInCombat()) {
					view.combatDisplay(controller, fight.nbEnemy(), status, selectedSlots, hero, fight.getEnemy(), isDragging,
							draggedItem, dragOffsetX, dragOffsetY, controller.getLastAttackTime(), controller.getFloatingItems());
				} else if (controller.isInCorridor()) {
					view.corridorDisplay(controller, selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems(), controller.getLastChangeRoom(), controller.isTransitionFromMerchant(),
							controller.isTransitionFromCorridor());
				} else if (controller.isInTreasure()) {
					view.treasureDisplay(controller, selectedSlots, treasureGrid, hero, isDragging, draggedItem, dragOffsetX,
							dragOffsetY, controller.getFloatingItems());
				} else if (controller.isInMerchant()) {
					view.merchantDisplay(controller, selectedSlots, getMerchantGrid, hero, isDragging, draggedItem, dragOffsetX,
							dragOffsetY, controller.getFloatingItems());
				} else if (controller.isInHealer()) {
					view.healerDisplay(controller, controller.getSelectedSlots(), controller.getHero(),
							controller.getHealerRoom(), controller.isDragging(), controller.getDraggedItem(),
							controller.getDragOffsetX(), controller.getDragOffsetY(), controller.getFloatingItems());
				} else {
					view.emptyRoomDisplay(controller, selectedSlots, hero, isDragging, draggedItem, dragOffsetX, dragOffsetY,
							controller.getFloatingItems());
				}

				if (hero.hp() <= 0) {
					System.out.println("Votre personnage est MORT ! Fin du jeu");
					System.exit(0);
				}
			}
		});
	}
}