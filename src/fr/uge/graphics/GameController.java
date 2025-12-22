package fr.uge.graphics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.*;
import fr.uge.implement.Battle.EnemyAction;

public class GameController {
	private final ApplicationContext context;
	private final GameView view;
	private final MapDungeon floor;
	private final BackPack backpack;
	private final Hero hero;
	private final Battle fight;
	private final Dungeon dungeon;
	private final TreasureChest treasureChest;
	private final Merchant merchant;
	private final BackpackExpansionSystem expansionSystem;
	private final List<FloatingItem> floatingItems = new ArrayList<>();
	private final List<Integer> selectedItems = new ArrayList<>();

	private boolean inCorridor = true;
	private boolean inTreasure = false;
	private boolean inCombat = false;
	private boolean inMerchant = false;
	private boolean inExpansionMode = false;
	private boolean isDragging = false;
	private boolean dragFromTreasure = false;
	private boolean dragFromMerchant = false;
	private boolean transitionFromMerchant = false;

	private int floorIndex = 0;
	private long lastAttackTime = 0;
	private long lastChangeRoom = 0;

	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCellSize = 60, backpackPadding = 8;
	private int treasureStartX, treasureStartY;
	private int merchantStartX, merchantStartY;

	private Item draggedItem = null;
	private Item dragOriginalItem = null;
	private int dragStartX = -1, dragStartY = -1;
	private int dragMouseX = 0, dragMouseY = 0;
	private int dragOffsetX = 0, dragOffsetY = 0;
	private int pointerDownX = -1, pointerDownY = -1;
	private static final int DRAG_THRESHOLD = 5;
	private boolean placingMalediction = false;
	private Item currentMalediction = null;
	private Item placedMalediction = null; // nouvelle variable

	// deplacment joueur
	private boolean isPlayerMoving = false;
	private int playerStartIndex = 0;
	private int playerTargetIndex = 0;
	private long moveStartTime = 0;
	private static final long MOVE_DURATION = 10000; // 300ms pour l'animation

	// Combat suspendu
	private boolean combatPausedByMalediction = false;

	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Battle fight,
			Dungeon dungeon, Hero hero) {
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
		this.fight = Objects.requireNonNull(fight);
		this.dungeon = dungeon;
		this.hero = hero;
		this.treasureChest = new TreasureChest(3, 5);
		this.merchant = new Merchant(3, 5);
		this.expansionSystem = new BackpackExpansionSystem(backpack);
		setTreasureDisplayCoords();
	}

	// ===================== GETTERS =====================
	public boolean isTransitionFromMerchant() {
		return transitionFromMerchant;
	}

	public boolean isPlacingMalediction() {
		return placingMalediction;
	}

	public List<EnemyAction> getEnemyActions() {
    return fight.getEnemyActions();
}

	public List<Enemy> getEnemies() {
		return fight.getEnemy();
	}

	public Item getCurrentMalediction() {
		return currentMalediction;
	}

	public boolean isPlayerMoving() {
		return isPlayerMoving;
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

	public boolean isInCombat() {
		return inCombat;
	}

	public boolean isInExpansionMode() {
		return inExpansionMode;
	}

	public boolean isDragging() {
		return isDragging;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public int getPlayerStartIndex() {
		return playerStartIndex;
	}

	public int getPlayerTargetIndex() {
		return playerTargetIndex;
	}

	public Hero getHero() {
		return hero;
	}

	public Battle getBattle() {
		return fight;
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

	public List<Integer> getSelectedSlots() {
		return selectedItems;
	}

	public BackpackExpansionSystem getExpansionSystem() {
		return expansionSystem;
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

	public float getPlayerAnimationProgress() {
		if (!isPlayerMoving)
			return 1.0f;

		long elapsed = System.currentTimeMillis() - moveStartTime;
		if (elapsed >= MOVE_DURATION) {
			isPlayerMoving = false;
			return 1.0f;
		}

		// Easing function pour un mouvement plus fluide (ease-out)
		float t = (float) elapsed / MOVE_DURATION;
		return 1 - (float) Math.pow(1 - t, 3); // cubic ease-out
	}

	// ===================== MAIN LOOP =====================
	public void update(int pollTimeout) {
		updatePlayerAnimation(); // Ajoutez cette ligne

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

	// ===================== KEYBOARD HANDLING =====================
	private void handleKeyboard(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.Q)
			System.exit(0);

		if (handleRotation(ke))
			return;
		if (handleExpansionExit(ke))
			return;
		if (handleDelete(ke))
			return;
		if (handleCombatEndTurn(ke))
			return;

		// ✅ Suppression du blocage global - on laisse le reste fonctionner normalement
	}

	private boolean handleRotation(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.R && ke.action() == KeyboardEvent.Action.KEY_PRESSED && isDragging
				&& draggedItem != null) {
			draggedItem = draggedItem.rotate();
			dragOriginalItem = draggedItem;
			return true;
		}
		return false;
	}

	private boolean handleExpansionExit(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.SPACE && inExpansionMode) {
			inExpansionMode = false;
			if (!treasureChest.getGrid().isEmpty()) {
				setTreasureState();
			}
			return true;
		}
		return false;
	}

	private boolean handleDelete(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.X && !inCombat) {
			handleDeleteSelectedItems();
			return true;
		}
		return false;
	}

	private boolean handleCombatEndTurn(KeyboardEvent ke) {
		if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
			return false;
		if (ke.key() == KeyboardEvent.Key.CTRL) {
			if (fight.isPlayerTurnActive()) {
				fight.endPlayerTurn();

				// Vérifier si un ennemi déclenche une malédiction
				for (Battle.EnemyAction action : fight.getEnemyActions()) {
					if (action == Battle.EnemyAction.MALEDICTION) {
						triggerMalediction();
						break;
					}
				}

				checkCombatEnd();
			} else {
				System.out.println("Votre tour est déjà terminé !");
			}
			return true;
		}
		return false;
	}

	// ===================== POINTER HANDLING =====================
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

		int[] backpackCoords = backpackSlotCoordsAt(mouseX, mouseY);
		Item clickedBackpackItem = (backpackCoords != null) ? backpack.grid()[backpackCoords[1]][backpackCoords[0]] : null;

		// ⚠️ Mode placement de la malédiction
		if (placingMalediction) {
			if (backpackCoords != null) {
				if (clickedBackpackItem != null && !clickedBackpackItem.isMalediction()) {
					// On peut déplacer tous les items normaux
					startBackpackDrag(backpackCoords[0], backpackCoords[1], clickedBackpackItem, mouseX, mouseY);
					return;
				} else if (clickedBackpackItem == null) {
					// Tentative de placer la malédiction
					handleMaledictionPlacement(mouseX, mouseY);
					return;
				}
				// Si clic sur la malédiction elle-même, on bloque
				System.out.println("⛔ Cliquez sur une case déverrouillée pour placer la malédiction !");
				return;
			}
			// Ignorer les clics hors sac
			return;
		}

		// Mode normal : gestion classique
		if (handleExpansionClick(mouseX, mouseY))
			return;
		if (handleFloatingItemClick(mouseX, mouseY))
			return;
		if (handleTreasureClick(mouseX, mouseY))
			return;
		if (handleMerchantDragStart(mouseX, mouseY))
			return;
		if (handleBackpackDragStart(mouseX, mouseY))
			return;
		if (handleRoomNavigation(mouseX, mouseY))
			return;
	}

	private void checkDragThreshold(int mouseX, int mouseY) {
		int deltaX = Math.abs(mouseX - pointerDownX);
		int deltaY = Math.abs(mouseY - pointerDownY);

		if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
			isDragging = true;

			// ⚠️ Pénalité uniquement si la malédiction est déplacée hors combat
			if (draggedItem != null && draggedItem.isMalediction() && !inCombat) {
				int penalty = 10;
				hero.takeDamage(penalty);
				System.out.println("💀 Vous déplacez la malédiction ! Vous perdez " + penalty + " PV !");
				System.out.println("❤️ HP restants : " + hero.hp() + "/" + hero.maxHp());
			}

			// Supprimer l'item du sac ou du coffre si nécessaire
			if (dragFromTreasure) {
				treasureChest.getGrid().removeItem(draggedItem);
			} else if (!dragFromMerchant) {
				backpack.remove(draggedItem);
			}

			// Nettoyer les références si c'est la malédiction
			if (draggedItem != null && draggedItem.isMalediction()) {
				if (draggedItem == placedMalediction)
					placedMalediction = null;
				if (draggedItem == currentMalediction)
					currentMalediction = null;
				floatingItems.removeIf(f -> f.item == draggedItem);
				placingMalediction = false;
				combatPausedByMalediction = false;
			}
		}
	}

	private boolean handleExpansionClick(int mouseX, int mouseY) {
		if (!inExpansionMode)
			return false;

		int[] coords = backpackSlotCoordsAt(mouseX, mouseY);
		if (coords != null && expansionSystem.unlockCell(coords[0], coords[1])) {
			if (!expansionSystem.hasPendingUnlocks()) {
				inExpansionMode = false;
				if (!treasureChest.getGrid().isEmpty()) {
					setTreasureState();
				}
			}
			return true;
		}
		return false;
	}

	private boolean handleFloatingItemClick(int mouseX, int mouseY) {
		FloatingItem fItem = findFloatingItemAt(mouseX, mouseY);
		if (fItem != null) {
			// ⚠️ Bloquer uniquement la malédiction
			if (fItem.item == currentMalediction) {
				System.out.println("⛔ Cliquez sur une case déverrouillée pour placer la malédiction !");
				return true; // Consomme l'événement sans démarrer le drag
			}

			draggedItem = fItem.item;
			dragOffsetX = mouseX - fItem.position.x;
			dragOffsetY = mouseY - fItem.position.y;
			isDragging = true;
			floatingItems.remove(fItem);
			dragFromTreasure = false;
			return true;
		}
		return false;
	}

	private boolean handleTreasureClick(int mouseX, int mouseY) {
		if (!inTreasure)
			return false;

		int[] coords = treasureSlotCoordsAt(mouseX, mouseY);
		if (coords != null) {
			Item item = treasureChest.getGrid().getGrid()[coords[1]][coords[0]];
			if (item != null) {
				startTreasureDrag(coords[0], coords[1], item, mouseX, mouseY);
				return true;
			}
		}
		return false;
	}

	private void startTreasureDrag(int x, int y, Item item, int mouseX, int mouseY) {
		int[] start = findItemOrigin(treasureChest.getGrid().getGrid(), x, y, item);
		draggedItem = item;
		dragOriginalItem = item;
		dragFromTreasure = true;
		dragStartX = start[0];
		dragStartY = start[1];

		int cellX = treasureStartX + start[0] * (backpackCellSize + backpackPadding);
		int cellY = treasureStartY + start[1] * (backpackCellSize + backpackPadding);
		dragOffsetX = mouseX - cellX;
		dragOffsetY = mouseY - cellY;
		dragMouseX = mouseX - dragOffsetX;
		dragMouseY = mouseY - dragOffsetY;
	}

	private boolean handleMerchantDragStart(int mouseX, int mouseY) {
		if (!inMerchant)
			return false;

		int[] coords = merchantSlotCoordsAt(mouseX, mouseY);
		if (coords != null) {
			Item item = merchant.getStock().getGrid()[coords[1]][coords[0]];
			if (item != null) {
				startMerchantDrag(coords[0], coords[1], item, mouseX, mouseY);
				return true;
			}
		}
		return false;
	}

	private void startMerchantDrag(int x, int y, Item item, int mouseX, int mouseY) {
		draggedItem = item;
		dragOriginalItem = item;
		dragFromMerchant = true;

		int cellX = merchantStartX + x * (backpackCellSize + backpackPadding);
		int cellY = merchantStartY + y * (backpackCellSize + backpackPadding);
		dragOffsetX = mouseX - cellX;
		dragOffsetY = mouseY - cellY;
		dragMouseX = mouseX - dragOffsetX;
		dragMouseY = mouseY - dragOffsetY;
	}

	private boolean handleBackpackDragStart(int mouseX, int mouseY) {
		int[] coords = backpackSlotCoordsAt(mouseX, mouseY);
		if (coords == null)
			return false;

		int x = coords[0], y = coords[1];
		if (!backpack.isUnlocked(x, y))
			return false;

		Item item = backpack.grid()[y][x];
		if (item != null) {
			startBackpackDrag(x, y, item, mouseX, mouseY);
			return true;
		}
		return false;
	}

	private void startBackpackDrag(int x, int y, Item item, int mouseX, int mouseY) {
		// ⚠️ Bloquer le drag de la malédiction UNIQUEMENT en combat
		if (item == placedMalediction && inCombat) {
			System.out.println("⛔ Impossible de déplacer la malédiction en combat !");
			return;
		}

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

	private boolean handleRoomNavigation(int mouseX, int mouseY) {
		if (inCombat || (!inTreasure && !inMerchant)) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1 && !inCombat) {
				transitionFromMerchant = false;
				handleRoomClick(room);
				lastChangeRoom = System.currentTimeMillis();
				return true;
			}
		}

		if ((inTreasure || inMerchant) && roomAt(mouseX, mouseY) != -1) {
			int room = roomAt(mouseX, mouseY);
			if (inCorridor) {
			}
			if (inMerchant) {
				leaveMerchantRoom();
				transitionFromMerchant = true;
			} else {
				transitionFromMerchant = false;
			}

			if (inTreasure)
				leaveTreasureRoom();

			lastChangeRoom = System.currentTimeMillis();
			handleRoomClick(room);
			return true;
		}
		return false;
	}

	private void handlePointerMove(int mouseX, int mouseY) {
		if (draggedItem != null && !isDragging) {
			checkDragThreshold(mouseX, mouseY);
		}

		if (isDragging && draggedItem != null) {
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;
		}
	}

	private void triggerMalediction() {
		// Vérifie qu'aucune malédiction n'est déjà active
		if (currentMalediction != null || placedMalediction != null) {
			System.out.println("⛔ Une malédiction est déjà active !");
			return;
		}

		placingMalediction = true;
		combatPausedByMalediction = true;

		currentMalediction = fight.chooseMalediction();
		floatingItems.add(new FloatingItem(currentMalediction, new Point(300, 300)));

		System.out.println("☠️ Une malédiction apparaît ! Place-la immédiatement !");
	}

	private void handleMaledictionPlacement(int mouseX, int mouseY) {
		int[] coords = backpackSlotCoordsAt(mouseX, mouseY);
		if (coords == null) {
			System.out.println("❌ Cliquez sur une case du sac !");
			return;
		}

		int x = coords[0];
		int y = coords[1];

		if (!backpack.canForcePlace(currentMalediction, x, y)) {
			System.out.println("❌ Placement impossible (cases verrouillées ou hors limites)");
			return;
		}

		var blocking = backpack.blockingItems(currentMalediction, x, y);
		if (!blocking.isEmpty()) {
			System.out.println("⛔ Objets bloquants ! Déplacez-les d'abord : " + blocking.size() + " objet(s)");
			return;
		}

		// Placement réussi
		backpack.forcePlace(currentMalediction, x, y);
		floatingItems.removeIf(f -> f.item == currentMalediction);

		placedMalediction = currentMalediction;
		currentMalediction = null;
		placingMalediction = false;
		combatPausedByMalediction = false;

		System.out.println("☠️ Malédiction placée ! Le combat reprend.");
	}

	private void handlePointerUp(int mouseX, int mouseY) {
		if (draggedItem != null && !isDragging) {
			handleSimpleClick();
			return;
		}

		if (isDragging && draggedItem != null) {
			handleDragAndDrop(mouseX, mouseY);
		}
	}

	private void handleSimpleClick() {
		if (inMerchant) {
			handleMerchantClick(pointerDownX, pointerDownY);
		} else if (inCombat && !dragFromTreasure) {
			handleCombatClick();
		} else if (!inCombat && !dragFromTreasure) {
			handleSelectionClick();
		}
		resetDragState();
	}

	private void handleCombatClick() {
		int[] coords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
		if (coords == null)
			return;

		Item item = backpack.grid()[coords[1]][coords[0]];
		if (item == null)
			return;
		if (!fight.isPlayerTurnActive())
			return;

		fight.useItem(item);
		lastAttackTime = System.currentTimeMillis();

		// Vérifier si un ennemi déclenche une malédiction
		for (Battle.EnemyAction action : fight.getEnemyActions()) {
			if (action == Battle.EnemyAction.MALEDICTION) {
				triggerMalediction();
				break;
			}
		}

		checkCombatEnd();
	}

	private void handleSelectionClick() {
		int[] coords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
		if (coords != null) {
			toggleSelection(coords[0], coords[1], draggedItem);
		}
	}

	private void handleDragAndDrop(int mouseX, int mouseY) {
		if (dragFromMerchant) {
			handleMerchantPurchase();
			return;
		}

		boolean placed = tryPlaceInBackpack(mouseX, mouseY);
		if (!placed)
			placed = tryPlaceInTreasure(mouseX, mouseY);
		if (!placed)
			createFloatingItem(mouseX, mouseY);

		resetDragState();
	}

	private void handleMerchantPurchase() {
		int price = draggedItem.price();

		if (hero.hasEnoughGold(price)) {
			if (hero.getBackpack().autoAdd(draggedItem)) {
				hero.removeGold(price);
				merchant.getStock().removeItem(draggedItem);
				System.out.println("✅ Achat : " + draggedItem.name() + " (" + price + " or)");
			} else {
				System.out.println("❌ Pas de place !");
			}
		} else {
			System.out.println("❌ Pas assez d'or ! (" + price + " requis)");
		}
		resetDragState();
	}

	private boolean tryPlaceInBackpack(int mouseX, int mouseY) {
		int[] coords = backpackSlotCoordsAt(mouseX, mouseY);
		if (coords == null)
			return false;

		if (dragFromTreasure) {
			return backpack.autoAdd(draggedItem);
		} else {
			return backpack.place(draggedItem, coords[0], coords[1]);
		}
	}

	private boolean tryPlaceInTreasure(int mouseX, int mouseY) {
		if (!inTreasure || dragFromTreasure)
			return false;

		int[] coords = treasureSlotCoordsAt(mouseX, mouseY);
		if (coords == null)
			return false;

		Grid grid = treasureChest.getGrid();
		if (grid.canPlace(draggedItem, coords[0], coords[1])) {
			placeInGrid(grid.getGrid(), draggedItem, coords[0], coords[1]);
			return true;
		}
		return false;
	}

	private void createFloatingItem(int mouseX, int mouseY) {
		floatingItems.add(new FloatingItem(draggedItem, new Point(mouseX - dragOffsetX, mouseY - dragOffsetY)));
	}

	// ===================== ROOM HANDLING =====================
	private void handleRoomClick(int clickedRoom) {
		if (!floor.adjacentRooms().contains(clickedRoom))
			return;

		// Démarrer l'animation
		playerStartIndex = floor.playerIndex();
		playerTargetIndex = clickedRoom;
		isPlayerMoving = true;
		moveStartTime = System.currentTimeMillis();

		floor.setPlayerIndex(clickedRoom);
		floatingItems.clear();

		processRoomType(clickedRoom);
		floor.markVisited(clickedRoom);
	}

	public void updatePlayerAnimation() {
		if (isPlayerMoving) {
			long elapsed = System.currentTimeMillis() - moveStartTime;
			if (elapsed >= MOVE_DURATION) {
				isPlayerMoving = false;
			}
		}
	}

	private void processRoomType(int room) {
		if (floor.playerOnEnemyRoom() && !floor.isVisited(room)) {
			startCombat();
		} else if (floor.playerOnTreasureRoom() && !floor.isVisited(room)) {
			treasureChest.generateTreasure();
			setTreasureState();
		} else if (floor.playerOnMerchantRoom()) {
			merchant.generateStock();
			setMerchantState();
		} else if (floor.playerOnCorridor()) {
			setCorridorState();
		} else if (floor.playeOnExitRom()) {
			goToNextFloor();
		} else {
			setEmptyRoomState();
		}
	}

	private void leaveTreasureRoom() {
		treasureChest.getGrid().clear();
		floatingItems.clear();
		if (floor.playerOnCorridor()) {
			setCorridorState();
		} else {
			setEmptyRoomState();
		}
	}

	private void leaveMerchantRoom() {
		merchant.getStock().clear();
		floatingItems.clear();
		if (floor.playerOnCorridor()) {
			setCorridorState();
		} else {
			setEmptyRoomState();
		}
	}

	// ===================== COMBAT =====================
	private void startCombat() {
		fight.initEnemies();
		inCombat = true;
	}

	private void checkCombatEnd() {
		if (fight == null || fight.isRunning())
			return;

		inCombat = false;
		int defeated = fight.getDefeatedEnemiesCount();
		expansionSystem.addPendingUnlocks(defeated);

		if (expansionSystem.hasPendingUnlocks()) {
			inExpansionMode = true;
		}

		treasureChest.generateTreasure();
		if (!treasureChest.getGrid().isEmpty() && !inExpansionMode) {
			setTreasureState();
		}
	}

	// ===================== STATE MANAGEMENT =====================
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

	private void setEmptyRoomState() {
		inTreasure = false;
		inCorridor = false;
		inCombat = false;
		inMerchant = false;
	}

	// ===================== MERCHANT =====================
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
			if (item != null && item.isSellable()) {
				merchant.sellItem(item, hero);
			}
		}
	}

	// ===================== ITEMS =====================
	private void toggleSelection(int x, int y, Item clicked) {
		if (clicked == null)
			return;
		int slot = y * backpack.width() + x;
		if (selectedItems.contains(slot)) {
			selectedItems.remove(Integer.valueOf(slot));
		} else {
			selectedItems.add(slot);
		}
	}

	private void handleDeleteSelectedItems() {
		if (selectedItems.isEmpty())
			return;

		selectedItems.stream().sorted((a, b) -> b - a).forEach(slot -> {
			int x = slot % backpack.width();
			int y = slot / backpack.width();
			Item item = backpack.grid()[y][x];

			if (item != null) {
				if (item.isMalediction()) {
					if (inCombat) {
						System.out.println("⛔ Impossible de supprimer la malédiction en combat !");
					} else {
						int penalty = 10;
						hero.takeDamage(penalty);
						System.out.println("💀 Malédiction supprimée ! Vous perdez " + penalty + " PV !");
						System.out.println("❤️ HP restants : " + hero.hp() + "/" + hero.maxHp());

						backpack.remove(item);

						// Nettoyer toutes les références
						if (item == placedMalediction)
							placedMalediction = null;
						if (item == currentMalediction)
							currentMalediction = null;
						floatingItems.removeIf(f -> f.item == item);
						placingMalediction = false;
						combatPausedByMalediction = false;
					}
				} else {
					backpack.remove(item);
				}
			}
		});

		selectedItems.clear();
	}

	private void resetDragState() {

		draggedItem = null;
		dragStartX = -1;
		dragStartY = -1;
		dragFromTreasure = false;
		dragFromMerchant = false;
		pointerDownX = -1;
		pointerDownY = -1;
		isDragging = false;
	}

	// ===================== FLOOR NAVIGATION =====================
	private void goToNextFloor() {
		if (floorIndex + 1 >= 3) {
			System.out.println("Fin du donjon !");
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

	// ===================== COORDINATES =====================
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
		return findSlotAt(mouseX, mouseY, backpackOriginX, backpackOriginY, backpack.width(), backpack.height());
	}

	private int[] treasureSlotCoordsAt(int mouseX, int mouseY) {
		return findSlotAt(mouseX, mouseY, treasureStartX, treasureStartY, treasureChest.getGrid().getCols(),
				treasureChest.getGrid().getRows());
	}

	private int[] merchantSlotCoordsAt(int mouseX, int mouseY) {
		return findSlotAt(mouseX, mouseY, merchantStartX, merchantStartY, merchant.getStock().getCols(),
				merchant.getStock().getRows());
	}

	private int[] findSlotAt(int mouseX, int mouseY, int originX, int originY, int cols, int rows) {
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				int cellX = originX + x * (backpackCellSize + backpackPadding);
				int cellY = originY + y * (backpackCellSize + backpackPadding);
				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && mouseY >= cellY
						&& mouseY <= cellY + backpackCellSize) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private void setTreasureDisplayCoords() {
		var info = context.getScreenInfo();
		int chestX = info.width() / 2 - 100;
		int chestY = info.height() / 3 - 75;
		treasureStartX = chestX;
		treasureStartY = chestY + 170;
	}

	private void setMerchantDisplayCoords() {
		var info = context.getScreenInfo();
		int chestX = info.width() / 2 - 100;
		int chestY = info.height() / 3 - 75;
		merchantStartX = chestX;
		merchantStartY = chestY + 170;
	}

	// ===================== UTILITIES =====================
	private FloatingItem findFloatingItemAt(int mouseX, int mouseY) {
		for (FloatingItem f : floatingItems) {
			int width = f.item.width() * (backpackCellSize + backpackPadding) - backpackPadding;
			int height = f.item.height() * (backpackCellSize + backpackPadding) - backpackPadding;
			if (mouseX >= f.position.x && mouseX <= f.position.x + width && mouseY >= f.position.y
					&& mouseY <= f.position.y + height) {
				return f;
			}
		}
		return null;
	}

	private int[] findItemOrigin(Item[][] grid, int x, int y, Item item) {
		int startX = x, startY = y;
		while (startX > 0 && grid[y][startX - 1] == item)
			startX--;
		while (startY > 0 && grid[startY - 1][x] == item)
			startY--;
		return new int[] { startX, startY };
	}

	private void placeInGrid(Item[][] grid, Item item, int x, int y) {
		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {
				grid[y + dy][x + dx] = item;
			}
		}
	}
}