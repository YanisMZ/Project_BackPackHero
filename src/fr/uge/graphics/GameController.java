package fr.uge.graphics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.BackPack;
import fr.uge.implement.BackpackExpansionSystem;
import fr.uge.implement.Battle;
import fr.uge.implement.Dungeon;
import fr.uge.implement.FloatingItem;
import fr.uge.implement.Grid;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.Merchant;
import fr.uge.implement.Room;
import fr.uge.implement.TreasureChest;

/**
 * Controls the main game logic, including movement between rooms, combat flow,
 * interactions with the backpack, and treasure chest handling.
 */
public class GameController {

	private final ApplicationContext context;
	private final GameView view;
	private final MapDungeon floor;
	private final BackPack backpack;
	private final Hero hero;
	private final Battle fight;
	private final Dungeon dungeon;
	private boolean inCorridor = true;
	private boolean inTreasure = false;
	private boolean inCombat = false;
	private final List<Integer> selectedItems = new ArrayList<>();
	private int floorIndex = 0;
	private long lastAttackTime = 0;
	private long lastChangeRoom = 0;

	private final TreasureChest treasureChest;

	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCols = 5, backpackCellSize = 60, backpackPadding = 8;
	private int treasureStartX, treasureStartY;
	private int merchantStartX, merchantStartY;
	private Item draggedItem = null;
	private int dragStartX = -1;
	private int dragStartY = -1;
	private int dragMouseX = 0;
	private int dragMouseY = 0;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean isDragging = false;
	private boolean dragFromTreasure = false;
	private int pointerDownX = -1;
	private int pointerDownY = -1;
	private static final int DRAG_THRESHOLD = 5;
	private final List<FloatingItem> floatingItems = new ArrayList<>();
	private final BackpackExpansionSystem expansionSystem;
	private boolean inExpansionMode = false;
	private Item dragOriginalItem = null;
	private final Merchant merchant;
	private boolean inMerchant = false;
	private boolean dragFromMerchant = false;

	/**
	 * Creates a new game controller responsible for handling input events, updating
	 * states, and interacting with the game model.
	 */
	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Battle fight,
			Dungeon dungeon, Hero hero) {
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
		this.fight = Objects.requireNonNull(fight);
		this.dungeon = dungeon;
		this.hero = hero;
		setTreasureDisplayCoords();
		this.treasureChest = new TreasureChest(3, 5);
		this.merchant = new Merchant(3, 5);
		this.expansionSystem = new BackpackExpansionSystem(backpack);
	}

	public boolean isInCorridor() {
		return inCorridor;
	}

	public boolean isInTreasure() {
		return inTreasure;
	}

	public boolean isInMerchant() {
		return inMerchant;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public boolean isInCombat() {
		return inCombat;
	}

	public List<Integer> getSelectedSlots() {
		return selectedItems;
	}

	public BackpackExpansionSystem getExpansionSystem() {
		return expansionSystem;
	}

	public boolean isInExpansionMode() {
		return inExpansionMode;
	}

	public boolean isDragging() {
		return isDragging;
	}

	public Item getDraggedItem() {
		return draggedItem;
	}

	public int getDragOffsetX() {
		return dragMouseX;
	}

	public int getDragOffsetY() {
		return dragMouseY;
	}

	public long getLastAttackTime() {
		return lastAttackTime;
	}

	public long getLastChangeRoom() {
		return lastChangeRoom;
	}

	public Hero getHero() {
		return hero;
	}

	public Battle getBattle() {
		return fight;
	}

	public Item[][] getTreasureGrid() {

		return treasureChest.getGrid().getGrid();
	}

	public Item[][] getMerchantGrid() {
		return merchant.getStock().getGrid();
	}

	public List<FloatingItem> getFloatingItems() {
		return floatingItems;
	}

	/**
	 * main loop where every pressed key/point will get redirected to another
	 * function
	 */
	public void update(int pollTimeout) {
		var event = context.pollOrWaitEvent(pollTimeout);
		if (event == null)
			return;

		switch (event) {
		case KeyboardEvent ke -> handleKeyboard(ke);
		case PointerEvent pe -> handlePointer(pe);
		default -> {
		}
		}
	}

	/**
	 * this function will manage every keyboard inputs in the game
	 */
	private void handleKeyboard(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.Q)
			System.exit(0);

		// Rotation d'item avec un seul appui
		if (ke.key() == KeyboardEvent.Key.R && ke.action() == KeyboardEvent.Action.KEY_PRESSED && isDragging
				&& draggedItem != null) {

			draggedItem = draggedItem.rotate();
			dragOriginalItem = draggedItem;
			return;
		}

		if (ke.key() == KeyboardEvent.Key.SPACE && inExpansionMode) {
			inExpansionMode = false;
			if (!treasureChest.getGrid().isEmpty()) {
				setTreasureState();
			}
			return;
		}

		// Supprimer des items uniquement hors combat
		if (ke.key() == KeyboardEvent.Key.X && !inCombat)
			handleDeleteSelectedItems();

		if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
			return;

		switch (ke.key()) {
		case KeyboardEvent.Key.CTRL -> {
			if (fight.isPlayerTurnActive()) {
				fight.endPlayerTurn();
				checkCombatEnd();
			} else {
				System.out.println("Votre tour est déjà terminé !");
			}
		}
		default -> {
		}
		}
	}

	private FloatingItem findFloatingItemAt(int mouseX, int mouseY) {
		for (FloatingItem f : floatingItems) {
			int x = f.position.x;
			int y = f.position.y;
			int width = f.item.width() * (backpackCellSize + backpackPadding) - backpackPadding;
			int height = f.item.height() * (backpackCellSize + backpackPadding) - backpackPadding;

			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
				return f;
			}
		}
		return null;
	}

	/**
	 * this function will manage every pointer input in the game
	 */
	private void handlePointer(PointerEvent pe) {
		int mouseX = pe.location().x();
		int mouseY = pe.location().y();

		switch (pe.action()) {
		case POINTER_DOWN -> handlePointerDown(mouseX, mouseY);
		case POINTER_UP -> handlePointerUp(mouseX, mouseY);
		case POINTER_MOVE -> handlePointerMove(mouseX, mouseY);
		default -> {
		}
		}
	}

	private void handlePointerDown(int mouseX, int mouseY) {
		pointerDownX = mouseX;
		pointerDownY = mouseY;

		if (inExpansionMode) {
			int[] slotCoords = backpackSlotCoordsAt(mouseX, mouseY);
			if (slotCoords != null) {
				int x = slotCoords[0];
				int y = slotCoords[1];

				if (expansionSystem.unlockCell(x, y)) {
					// Case débloquée avec succès
					if (!expansionSystem.hasPendingUnlocks()) {
						// Tous les déblocages sont faits
						inExpansionMode = false;

						// Passer au trésor si disponible
						if (!treasureChest.getGrid().isEmpty()) {
							setTreasureState();
						}
					}
				}
				return;
			}
		}

		// Vérifie si on clique sur un item flottant
		FloatingItem fItem = findFloatingItemAt(mouseX, mouseY);
		if (fItem != null) {
			draggedItem = fItem.item;
			dragOffsetX = mouseX - fItem.position.x;
			dragOffsetY = mouseY - fItem.position.y;
			isDragging = true;
			floatingItems.remove(fItem);
			dragFromTreasure = false;
			return;
		}

		// Check treasure chest click for potential drag (priority)
		if (inTreasure) {
			int[] treasureCoords = treasureSlotCoordsAt(mouseX, mouseY);
			if (treasureCoords != null) {
				int x = treasureCoords[0];
				int y = treasureCoords[1];
				Item item = treasureChest.getGrid().getGrid()[y][x];

				if (item != null) {
					int itemStartX = x;
					int itemStartY = y;

					while (itemStartX > 0 && treasureChest.getGrid().getGrid()[y][itemStartX - 1] == item) {
						itemStartX--;
					}
					while (itemStartY > 0 && treasureChest.getGrid().getGrid()[itemStartY - 1][x] == item) {
						itemStartY--;
					}

					draggedItem = item;
					dragOriginalItem = item;
					dragFromTreasure = true;
					dragStartX = itemStartX;
					dragStartY = itemStartY;

					int cellX = treasureStartX + itemStartX * (backpackCellSize + backpackPadding);
					int cellY = treasureStartY + itemStartY * (backpackCellSize + backpackPadding);
					dragOffsetX = mouseX - cellX;
					dragOffsetY = mouseY - cellY;

					dragMouseX = mouseX - dragOffsetX;
					dragMouseY = mouseY - dragOffsetY;
					return;
				}
			}
		}

		int[] slotCoords = backpackSlotCoordsAt(mouseX, mouseY);
		// =====================
		// DRAG DEPUIS LE MARCHAND
		// =====================
		if (inMerchant) {
			int[] merchantCoords = merchantSlotCoordsAt(mouseX, mouseY);
			if (merchantCoords != null) {
				Item item = merchant.getStock().getGrid()[merchantCoords[1]][merchantCoords[0]];
				if (item != null) {
					draggedItem = item;
					dragOriginalItem = item;
					dragFromMerchant = true;

					int cellX = merchantStartX + merchantCoords[0] * (backpackCellSize + backpackPadding);
					int cellY = merchantStartY + merchantCoords[1] * (backpackCellSize + backpackPadding);
					dragOffsetX = mouseX - cellX;
					dragOffsetY = mouseY - cellY;

					dragMouseX = mouseX - dragOffsetX;
					dragMouseY = mouseY - dragOffsetY;
					return;
				}
			}
		}

		if (slotCoords != null) {
			int x = slotCoords[0];
			int y = slotCoords[1];

			// AJOUTEZ CETTE VÉRIFICATION :
			if (!backpack.isUnlocked(x, y)) {
				return; // Ne pas permettre de drag sur case verrouillée
			}

			Item item = backpack.grid()[y][x];

			if (item != null) {
				draggedItem = item;
				dragOriginalItem = item;
				dragStartX = x;
				dragStartY = y;
				dragFromTreasure = false;

				int cellX = backpackOriginX + x * (backpackCellSize + backpackPadding);
				int cellY = backpackOriginY + y * (backpackCellSize + backpackPadding);
				dragOffsetX = mouseX - cellX;
				dragOffsetY = mouseY - cellY;

				dragMouseX = mouseX - dragOffsetX;
				dragMouseY = mouseY - dragOffsetY;
			}
			return;
		}

		// Handle treasure room clicks (only if not dragging)
		if (inTreasure) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1) {
				leaveTreasureRoom();
				lastChangeRoom = System.currentTimeMillis();
				handleRoomClick(room);
				return;
			}
		}

		if (inMerchant) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1) {
				leaveMerchantRoom();
				lastChangeRoom = System.currentTimeMillis();
				handleRoomClick(room);
				return;
			}
		}

		// Handle room clicks
		if (!inCombat && !inTreasure) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1) {
				handleRoomClick(room);
				lastChangeRoom = System.currentTimeMillis();
			}
		}

	}

	private void handlePointerMove(int mouseX, int mouseY) {
		if (draggedItem != null && !isDragging) {
			int deltaX = Math.abs(mouseX - pointerDownX);
			int deltaY = Math.abs(mouseY - pointerDownY);

			if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
				isDragging = true;

				// Retirer l'item du sac ou du coffre uniquement au début du drag
				if (dragFromTreasure) {
					treasureChest.getGrid().removeItem(draggedItem);
				} else if (!dragFromMerchant) {
					backpack.remove(draggedItem);
				}
			}
		}

		if (isDragging && draggedItem != null) {
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;
		}
	}

	private void handlePointerUp(int mouseX, int mouseY) {

		// =====================
		// CLIC SIMPLE (pas un drag)
		// =====================
		if (draggedItem != null && !isDragging) {

			if (inMerchant) {
				handleMerchantClick(pointerDownX, pointerDownY);
				draggedItem = null;
				dragStartX = -1;
				dragStartY = -1;
				dragFromTreasure = false;
				pointerDownX = -1;
				pointerDownY = -1;
				return;
			}

			if (inCombat && !dragFromTreasure) {
				int[] slotCoords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
				if (slotCoords != null) {
					int x = slotCoords[0];
					int y = slotCoords[1];
					Item clickedItem = backpack.grid()[y][x];
					if (clickedItem != null && fight.isPlayerTurnActive()) {
						fight.useItem(clickedItem);
						lastAttackTime = System.currentTimeMillis();
					}
				}
			} else if (!inCombat && !dragFromTreasure) {
				int[] slotCoords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
				if (slotCoords != null) {
					toggleSelection(slotCoords[0], slotCoords[1], draggedItem);
				}
			}

			// cleanup
			draggedItem = null;
			dragStartX = -1;
			dragStartY = -1;
			dragFromTreasure = false;
			pointerDownX = -1;
			pointerDownY = -1;
			return;
		}

		// Dans GameController.java, remplacez la section DRAG & DROP de handlePointerUp
		// par ceci :

		// =====================
		// DRAG & DROP
		// =====================
		if (isDragging && draggedItem != null) {
			boolean placed = false;

			// =====================
			// CAS SPÉCIAL : ACHAT DEPUIS LE MARCHAND
			// =====================
			if (dragFromMerchant) {
				int price = draggedItem.price();

				if (hero.hasEnoughGold(price)) {
					// Tenter de placer dans le backpack
					if (hero.getBackpack().autoAdd(draggedItem)) {
						hero.removeGold(price); // 💰 Retirer l'or
						merchant.getStock().removeItem(draggedItem); // 🗑️ Retirer du stock
						placed = true;
						System.out.println("✅ Achat réussi : " + draggedItem.name() + " pour " + price + " or");
					} else {
						System.out.println("❌ Pas de place dans le sac !");
					}
				} else {
					System.out.println("❌ Pas assez d'or ! Prix: " + price + ", Or: " + hero.gold());
				}

				// Nettoyage pour drag depuis marchand
				dragFromMerchant = false;
				draggedItem = null;
				dragStartX = -1;
				dragStartY = -1;
				dragFromTreasure = false;
				pointerDownX = -1;
				pointerDownY = -1;
				isDragging = false;
				return; // ⚠️ Important : sortir ici pour éviter le code "flottant" plus bas
			}

			// =====================
			// CAS NORMAL : Placement dans le backpack
			// =====================
			int[] targetCoords = backpackSlotCoordsAt(mouseX, mouseY);
			if (targetCoords != null) {
				// 🔵 Déplacement interne → placement normal
				if (!dragFromTreasure) {
					placed = backpack.place(draggedItem, targetCoords[0], targetCoords[1]);
				}
				// 🟢 Depuis un trésor → stack possible
				else {
					placed = backpack.autoAdd(draggedItem);
				}
			}

			// =====================
			// Placement dans le coffre (si en mode trésor)
			// =====================
			if (!placed && inTreasure && !dragFromTreasure) {
				int[] treasureCoords = treasureSlotCoordsAt(mouseX, mouseY);
				if (treasureCoords != null) {
					Grid grid = treasureChest.getGrid();
					int x = treasureCoords[0];
					int y = treasureCoords[1];

					if (grid.canPlace(draggedItem, x, y)) {
						for (int dy = 0; dy < draggedItem.height(); dy++) {
							for (int dx = 0; dx < draggedItem.width(); dx++) {
								grid.getGrid()[y + dy][x + dx] = draggedItem;
							}
						}
						placed = true;
					}
				}
			}

			// =====================
			// Item lâché dans le vide → devient flottant
			// =====================
			if (!placed) {
				floatingItems.add(new FloatingItem(draggedItem, new Point(mouseX - dragOffsetX, mouseY - dragOffsetY)));
			}

			// Nettoyage final
			draggedItem = null;
			dragStartX = -1;
			dragStartY = -1;
			dragFromTreasure = false;
			pointerDownX = -1;
			pointerDownY = -1;
			isDragging = false;
		}
	}

	private void toggleSelection(int x, int y, Item clicked) {
		if (clicked == null)
			return;
		int slot = y * backpack.width() + x;
		if (selectedItems.contains(slot))
			selectedItems.remove(Integer.valueOf(slot));
		else
			selectedItems.add(slot);
	}

	private void handleDeleteSelectedItems() {
		if (selectedItems.isEmpty())
			return;

		selectedItems.stream().sorted((a, b) -> b - a).forEach(slot -> {
			int x = slot % backpack.width();
			int y = slot / backpack.width();
			Item item = backpack.grid()[y][x];
			if (item != null) {
				backpack.remove(item);
			}
		});

		selectedItems.clear();
	}

	private void handleRoomClick(int clickedRoom) {
		if (!floor.adjacentRooms().contains(clickedRoom))
			return;

		floor.setPlayerIndex(clickedRoom);
		floatingItems.clear();

		if (floor.playerOnEnemyRoom() && !floor.isVisited(clickedRoom)) {
			startCombat();
		} else if (floor.playerOnTreasureRoom() && !floor.isVisited(clickedRoom)) {
			treasureChest.generateTreasure();
			setTreasureState();
			floor.markVisited(clickedRoom);
		}

		else if (floor.playerOnMerchantRoom()) {
			System.out.println("je suis dans le marchand");
			merchant.generateStock();
			setMerchantState();
			floor.markVisited(clickedRoom);
		}

		else if (floor.playerOnCorridor()) {
			setCorridorState();
		} else if (floor.playeOnExitRom()) {
			goToNextFloor();
		} else {
			setEmptyRoomState();
		}

		floor.markVisited(clickedRoom);
	}

	private void leaveTreasureRoom() {
		treasureChest.getGrid().clear();
		floatingItems.clear(); // supprime tous les items flottants
		if (floor.playerOnCorridor())
			setCorridorState();
		else
			setEmptyRoomState();
	}

	private void leaveMerchantRoom() {
		merchant.getStock().clear();
		floatingItems.clear();
		if (floor.playerOnCorridor())
			setCorridorState();
		else
			setEmptyRoomState();
	}

	private void startCombat() {
		fight.initEnemies();
		inCombat = true;
	}

	private void checkCombatEnd() {
		System.out.println(">>> checkCombatEnd appelé");
		if (fight != null && !fight.isRunning()) {
			System.out.println(">>> Combat terminé !");
			inCombat = false;

			int defeatedEnemies = fight.getDefeatedEnemiesCount();
			System.out.println(">>> Ennemis vaincus : " + defeatedEnemies);

			expansionSystem.addPendingUnlocks(defeatedEnemies);

			if (expansionSystem.hasPendingUnlocks()) {
				inExpansionMode = true;
				System.out.println(">>> MODE EXPANSION ACTIVÉ !");
			}

			treasureChest.generateTreasure();
			if (!treasureChest.getGrid().isEmpty() && !inExpansionMode)
				setTreasureState();
		}
	}

	private void setCorridorState() {
		inCorridor = true;
		inMerchant = false;
		inTreasure = false;
		inCombat = false;
	}

	private void setTreasureState() {
		inTreasure = true;
		inCorridor = false;
		inCombat = false;
		inMerchant = false;

		setTreasureDisplayCoords();
	}

	private void setMerchantState() {
		inMerchant = true;
		inCorridor = false;
		inTreasure = false;
		inCombat = false;
		setMerchantDisplayCoords();
	}

	private void setTreasureDisplayCoords() {
		var info = context.getScreenInfo();
		int chestWidth = 200;
		int chestHeight = 150;
		int chestX = info.width() / 2 - chestWidth / 2;
		int chestY = info.height() / 3 - chestHeight / 2;

		treasureStartX = chestX;
		treasureStartY = chestY + chestHeight + 20;
	}

	private void setEmptyRoomState() {
		inTreasure = false;
		inCorridor = false;
		inCombat = false;
		inMerchant = false;

	}

	public int roomAt(int mouseX, int mouseY) {
		int cols = 4, cellSize = 120, padding = 10;
		for (int i = 0; i < floor.rooms().size(); i++) {
			int row = i / cols, col = i % cols;
			int x = padding + col * (cellSize + padding);
			int y = padding + row * (cellSize + padding);
			if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize)
				return i;
		}
		return -1;
	}

	private int[] backpackSlotCoordsAt(int mouseX, int mouseY) {
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				int cellX = backpackOriginX + x * (backpackCellSize + backpackPadding);
				int cellY = backpackOriginY + y * (backpackCellSize + backpackPadding);

				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && mouseY >= cellY
						&& mouseY <= cellY + backpackCellSize) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private int[] treasureSlotCoordsAt(int mouseX, int mouseY) {
		int treasureRows = treasureChest.getGrid().getRows();
		int treasureCols = treasureChest.getGrid().getCols();

		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				int cellX = treasureStartX + x * (backpackCellSize + backpackPadding);
				int cellY = treasureStartY + y * (backpackCellSize + backpackPadding);
				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && mouseY >= cellY
						&& mouseY <= cellY + backpackCellSize) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private void setMerchantDisplayCoords() {
		var info = context.getScreenInfo();
		int chestWidth = 200;
		int chestHeight = 150;
		int chestX = info.width() / 2 - chestWidth / 2;
		int chestY = info.height() / 3 - chestHeight / 2;

		merchantStartX = chestX;
		merchantStartY = chestY + chestHeight + 20;
	}

	private int[] merchantSlotCoordsAt(int mouseX, int mouseY) {

		for (int y = 0; y < merchant.getStock().getRows(); y++) {
			for (int x = 0; x < merchant.getStock().getCols(); x++) {
				int cellX = merchantStartX + x * (backpackCellSize + backpackPadding);
				int cellY = merchantStartY + y * (backpackCellSize + backpackPadding);
				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && mouseY >= cellY
						&& mouseY <= cellY + backpackCellSize) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private void handleMerchantClick(int mouseX, int mouseY) {
		if (!inMerchant)
			return;

		int[] stockCoords = merchantSlotCoordsAt(mouseX, mouseY);
		if (stockCoords != null) {
			Item item = merchant.getStock().getGrid()[stockCoords[1]][stockCoords[0]];
			if (item != null) {
				merchant.buyItem(item, hero);
				return;
			}
		}

		int[] backpackCoords = backpackSlotCoordsAt(mouseX, mouseY);
		if (backpackCoords != null) {
			Item item = backpack.grid()[backpackCoords[1]][backpackCoords[0]];
			if (item != null && item.isSellable()) { // ✅ Polymorphisme au lieu de instanceof
				merchant.sellItem(item, hero);
			}
		}
	}

	private void goToNextFloor() {

		if (floorIndex + 1 >= 3) {
			System.out.println("Fin du donjon ! Il n'y a plus d'étages.");
			System.exit(0);
			return;
		}
		floatingItems.clear();
		floorIndex++;
		MapDungeon next = dungeon.getFloor(floorIndex);
		floor.rooms().clear();
		floor.rooms().addAll(next.rooms());
		floor.setPlayerIndex(0);
		floor.clearVisited();
		setCorridorState();
	}
}