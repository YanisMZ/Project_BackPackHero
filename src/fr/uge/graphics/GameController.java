package fr.uge.graphics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.backpack.BackPack;
import fr.uge.backpack.BackpackExpansionSystem;
import fr.uge.combat.Battle;
import fr.uge.combat.Battle.EnemyAction;
import fr.uge.enemy.Enemy;
import fr.uge.enemy.Hero;
import fr.uge.items.FloatingItem;
import fr.uge.items.Item;
import fr.uge.map.Dungeon;
import fr.uge.map.MapDungeon;
import fr.uge.room.Grid;
import fr.uge.room.HealerRoom;
import fr.uge.room.Merchant;
import fr.uge.room.Room;
import fr.uge.room.TreasureChest;
import fr.uge.room.Room.Type;

public class GameController {
	private final ApplicationContext context;
	private final GameView view;
	private final MapDungeon floor;
	private final BackPack backpack;
	private final Hero hero;
	private final Battle fight;
	private final Dungeon dungeon;
	private TreasureChest treasureChest;
	private final Merchant merchant;
	private final BackpackExpansionSystem expansionSystem;
	private final List<FloatingItem> floatingItems = new ArrayList<>();
	private final List<Integer> selectedItems = new ArrayList<>();

	private static final int TRANSITION_DURATION = 3000;
	private boolean inCorridor = true;
	private boolean inTreasure = false;
	private boolean inCombat = false;
	private boolean inMerchant = false;
	private boolean inExpansionMode = false;
	private boolean isDragging = false;
	private boolean dragFromTreasure = false;
	private boolean dragFromMerchant = false;
	private boolean transitionFromMerchant = false;
	private boolean transitionFromCorridor = false;
	private final HealerRoom healerRoom;
	private boolean inHealer = false;
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
	private Item placedMalediction = null;
	private boolean firstMaledictionDrag = true;

	private final Set<Integer> clearedTreasureRooms = new HashSet<>();
	private final Set<Integer> clearedEnemyRooms = new HashSet<>();
	private final Map<Integer, TreasureChest> treasureChests = new HashMap<>();

	private List<Integer> currentPath = new ArrayList<>();
	private int pathIndex = 0;
	private boolean isFollowingPath = false;
	private boolean isPlayerMoving = false;
	private int playerStartIndex = 0;
	private int playerTargetIndex = 0;
	private long moveStartTime = 0;
	private static final long MOVE_DURATION = 0;
	private int animatedPlayerIndex;

	private boolean combatPausedByMalediction = false;
	private boolean exitGuardDefeated = false;
	private boolean exitCombatActive = false;

	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Battle fight,
			Dungeon dungeon, Hero hero, TreasureChest TreasureChest, Merchant Merchant,
			BackpackExpansionSystem BackpackExpansionSystem, HealerRoom HealerRoom) {
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
		this.fight = Objects.requireNonNull(fight);
		this.dungeon = dungeon;
		this.hero = hero;
		this.treasureChest = TreasureChest;
		this.merchant = Merchant;
		this.expansionSystem = BackpackExpansionSystem;
		this.healerRoom = HealerRoom;
		setTreasureDisplayCoords();
	}

	// ===================== GETTERS =====================

	private boolean isTransitioning() {
		return System.currentTimeMillis() - lastChangeRoom < TRANSITION_DURATION;
	}

	public boolean isTransitionFromMerchant() {
		return transitionFromMerchant;
	}

	public boolean isTransitionFromCorridor() {
		return transitionFromCorridor;
	}

	public boolean isPlacingMalediction() {
		return placingMalediction;
	}

	public boolean isFollowingPath() {
		return isFollowingPath;
	}

	public List<Integer> getCurrentPath() {
		return currentPath;
	}

	public int getPathIndex() {
		return pathIndex;
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

	public HealerRoom getHealerRoom() {
		return healerRoom;
	}

	public boolean isPlayerMoving() {
		return isPlayerMoving;
	}

	public boolean isInCorridor() {
		if (isTransitioning()) {
			return true;
		}
		return inCorridor;
	}

	public boolean isInCombat() {
		if (isTransitioning()) {
			return false;
		}
		return inCombat;
	}

	public boolean isInMerchant() {
		if (isTransitioning()) {
			return false;
		}
		return inMerchant;
	}

	public boolean isInTreasure() {
		if (isTransitioning()) {
			return false;
		}
		return inTreasure;
	}

	public boolean isInHealer() {
		if (isTransitioning()) {
			return false;
		}
		return inHealer;
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
		if (!isPlayerMoving) {
			return 1.0f;
		}

		long elapsed = System.currentTimeMillis() - moveStartTime;
		float t = Math.min(1f, (float) elapsed / MOVE_DURATION);

		// easing (smooth)
		return 1 - (float) Math.pow(1 - t, 3);
	}

	// ===================== MAIN LOOP =====================
	public void update(int pollTimeout) {
		updatePlayerAnimation();

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

			if (placingMalediction || currentMalediction != null) {

				return true;
			}

			if (fight.isPlayerTurnActive()) {
				fight.endPlayerTurn();

				for (Battle.EnemyAction action : fight.getEnemyActions()) {
					if (action == Battle.EnemyAction.MALEDICTION) {
						triggerMalediction();
						break;
					}
				}

				checkCombatEnd();
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

		if (handleHealerClick(mouseX, mouseY))
			return;
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

		if (!inCombat && handleRoomNavigation(mouseX, mouseY))
			return;
	}

	private boolean handleHealerClick(int mouseX, int mouseY) {
		if (!inHealer)
			return false;

		var info = context.getScreenInfo();
		int buttonWidth = 200;
		int buttonHeight = 80;
		int buttonX = (info.width() - buttonWidth) / 2;
		int buttonY = (info.height() - buttonHeight) / 2;

		if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
			healerRoom.healHero(hero);
			return true;
		}
		return false;
	}

	private void checkDragThreshold(int mouseX, int mouseY) {
		int deltaX = Math.abs(mouseX - pointerDownX);
		int deltaY = Math.abs(mouseY - pointerDownY);

		if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
			isDragging = true;

			if (draggedItem != null && draggedItem.isMalediction() && !inCombat) {
				int penalty = 10;
				hero.takeDamage(penalty);
			}
		}

		if (dragFromTreasure) {
			treasureChest.getGrid().removeItem(draggedItem);
		} else if (!dragFromMerchant) {
			backpack.remove(draggedItem);
		}

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

			if (fItem.item == currentMalediction && !placingMalediction) {
				return true;
			}

			draggedItem = fItem.item;
			dragOffsetX = mouseX - fItem.position.x;
			dragOffsetY = mouseY - fItem.position.y;
			isDragging = false;
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;

			floatingItems.remove(fItem);

			dragFromTreasure = false;
			dragFromMerchant = false;

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
			if (item == placedMalediction && inCombat) {
				return true;
			}
			startBackpackDrag(x, y, item, mouseX, mouseY);
			return true;
		}
		return false;
	}

	private void startBackpackDrag(int x, int y, Item item, int mouseX, int mouseY) {

		draggedItem = item;
		dragOriginalItem = item;
		dragStartX = x;
		dragStartY = y;

		int cellX = backpackOriginX + x * (backpackCellSize + backpackPadding);
		int cellY = backpackOriginY + y * (backpackCellSize + backpackPadding);

		dragOffsetX = mouseX - cellX;
		dragOffsetY = mouseY - cellY;
		dragMouseX = mouseX - dragOffsetX;
		dragMouseY = mouseY - dragOffsetY;
	}

	private boolean handleRoomNavigation(int mouseX, int mouseY) {
		if (inCombat || isFollowingPath || isPlayerMoving) {
			return false;
		}

		int room = roomAt(mouseX, mouseY);
		if (room == -1 || room == floor.playerIndex()) {
			return false;
		}

		if (inTreasure) {
			leaveTreasureRoom();
		}

		List<Integer> path = floor.findClearPath(floor.playerIndex(), room);
		if (path == null || path.size() < 2) {

			return false;
		}

		this.currentPath = new ArrayList<>(path);
		this.pathIndex = 0;
		this.isFollowingPath = true;
		this.animatedPlayerIndex = floor.playerIndex();

		moveToNextRoomInPath();
		return true;
	}

	private void handlePointerMove(int mouseX, int mouseY) {

		if (draggedItem == currentMalediction && placingMalediction) {
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;
			isDragging = true;
			return;
		}

		if (draggedItem != null && !isDragging) {
			checkDragThreshold(mouseX, mouseY);
		}

		if (isDragging && draggedItem != null) {
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;
		}
	}

	private void triggerMalediction() {
		if (currentMalediction != null || placedMalediction != null) {
			return;
		}

		placingMalediction = true;
		combatPausedByMalediction = true;
		firstMaledictionDrag = true;

		currentMalediction = fight.chooseMalediction();

		Point spawn = new Point(300, 300);
		floatingItems.add(new FloatingItem(currentMalediction, spawn));

		draggedItem = currentMalediction;
		isDragging = true;

		dragOffsetX = currentMalediction.width() * (backpackCellSize + backpackPadding) / 2;
		dragOffsetY = currentMalediction.height() * (backpackCellSize + backpackPadding) / 2;

		dragMouseX = spawn.x;
		dragMouseY = spawn.y;

	}

	private boolean handleMaledictionPlacement(int mouseX, int mouseY) {

		int[] coords = backpackSlotCoordsAt(mouseX, mouseY);
		if (coords == null)
			return false;

		int x = coords[0];
		int y = coords[1];

		if (!backpack.canForcePlace(currentMalediction, x, y))
			return false;

		var blocking = backpack.blockingItems(currentMalediction, x, y);
		if (!blocking.isEmpty())
			return false;

		backpack.forcePlace(currentMalediction, x, y);
		floatingItems.removeIf(f -> f.item == currentMalediction);

		placedMalediction = currentMalediction;
		currentMalediction = null;
		placingMalediction = false;
		combatPausedByMalediction = false;

		return true;
	}

	private void handlePointerUp(int mouseX, int mouseY) {

		if (draggedItem == currentMalediction && placingMalediction) {

			boolean placed = handleMaledictionPlacement(mouseX, mouseY);

			if (placed) {
				resetDragState();
			} else {

				Point spawn = new Point(dragMouseX, dragMouseY);

				boolean alreadyFloating = floatingItems.stream().anyMatch(f -> f.item == currentMalediction);

				if (!alreadyFloating) {
					floatingItems.add(new FloatingItem(currentMalediction, spawn));
				}

				draggedItem = null;
				isDragging = false;
				dragOffsetX = 0;
				dragOffsetY = 0;
				dragMouseX = 0;
				dragMouseY = 0;

			}
			return;
		}

		if (isDragging && draggedItem != null) {
			handleDragAndDrop(mouseX, mouseY);
			return;
		}

		if (draggedItem != null && !isDragging) {
			handleSimpleClick();
		}
	}

	private void handleSimpleClick() {
		if (inCombat) {
			handleCombatClick();
		} else if (inTreasure) {
		} else if (inMerchant) {
			handleMerchantClick(pointerDownX, pointerDownY);
		} else {
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
		int totalHpBefore = fight.getEnemy().stream().mapToInt(e -> Math.max(0, e.hp())).sum();

		fight.useItem(item);
		lastAttackTime = System.currentTimeMillis();

		int totalHpAfter = fight.getEnemy().stream().mapToInt(e -> Math.max(0, e.hp())).sum();

		int damageDealt = totalHpBefore - totalHpAfter;
		if (damageDealt > 0) {
			hero.addExp(0.8 * damageDealt);
		}
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
			}
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

	private void processRoomType(int room) {
		Type roomType = floor.rooms().get(room).type();
		this.lastChangeRoom = System.currentTimeMillis();

		switch (roomType) {

		case ENEMY -> {
			if (clearedEnemyRooms.contains(room)) {
				setEmptyRoomState();
			} else {
				startCombat();
			}
		}

		case TREASURE -> {

			TreasureChest chest = treasureChests.computeIfAbsent(room, r -> {
				TreasureChest newChest = new TreasureChest(3, 5);
				newChest.generateTreasure();
				return newChest;
			});
			this.treasureChest = chest;

			if (chest.getGrid().isEmpty()) {
				clearedTreasureRooms.add(room);
				setCorridorState();
			} else {
				setTreasureState();
			}
		}

		case MERCHANT -> {
			merchant.generateStock();
			setMerchantState();
		}

		case HEALER -> {
			setHealerState();
		}

		case EXIT -> {
			if (!exitGuardDefeated) {
				startExitCombat();
			} else {
				goToNextFloor();
			}
		}

		default -> {
			setCorridorState();
		}
		}
	}

	private void leaveTreasureRoom() {
		int room = floor.playerIndex();
		TreasureChest chest = treasureChests.get(room);
		if (chest != null && chest.getGrid().isEmpty()) {

			clearedTreasureRooms.add(room);
		}

		floatingItems.clear();

		setCorridorState();
	}

	// ===================== COMBAT =====================
	private void startCombat() {
		fight.initEnemies();
		inCombat = true;

		inMerchant = false;
		inTreasure = false;
		inHealer = false;
		inCorridor = false;
	}

	private void checkCombatEnd() {
		if (fight == null || fight.isRunning())
			return;

		if (combatPausedByMalediction && (placingMalediction || currentMalediction != null)) {
			System.out.println("Tu dois placer la malédiction !");
			return;
		}

		if (combatPausedByMalediction && placedMalediction != null) {
			combatPausedByMalediction = false;
		}

		inCombat = false;

		if (exitCombatActive) {
			exitCombatActive = false;
			exitGuardDefeated = true;
			goToNextFloor();
			return;
		}

		clearedEnemyRooms.add(floor.playerIndex());

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
	private void setHealerState() {
		inHealer = true;
		inCorridor = false;
		inTreasure = false;
		inCombat = false;
		inMerchant = false;
	}

	private void setCorridorState() {
		inCorridor = true;
		inMerchant = false;
		inTreasure = false;
		inCombat = false;
		inHealer = false;
	}

	private void setTreasureState() {
		inTreasure = true;
		inCorridor = false;
		inCombat = false;
		inMerchant = false;
		inHealer = false;
		setTreasureDisplayCoords();
	}

	private void setMerchantState() {
		inMerchant = true;
		inCorridor = false;
		inTreasure = false;
		inCombat = false;
		inHealer = false;
		setMerchantDisplayCoords();
	}

	private void setEmptyRoomState() {
		inTreasure = false;
		inCorridor = false;
		inCombat = false;
		inMerchant = false;
		inHealer = false;
	}

	private void startExitCombat() {
		exitCombatActive = true;

		startCombat();
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
					if (!inCombat) {
						int penalty = 10;
						hero.takeDamage(penalty);

						backpack.remove(item);

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
			System.exit(0);
			return;
		}

		floatingItems.clear();
		floorIndex++;
		MapDungeon next = dungeon.getFloor(floorIndex);
		floor.rooms().clear();
		floor.rooms().addAll(next.rooms());
		floor.movePlayerTo(0);
		floor.clearVisited();
		setCorridorState();
		exitGuardDefeated = false;
		exitCombatActive = false;

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

	public void updatePlayerAnimation() {
		if (!isPlayerMoving) {
			return;
		}

		long elapsed = System.currentTimeMillis() - moveStartTime;

		if (elapsed >= MOVE_DURATION) {
			completeMovement();
		}
	}

	private void completeMovement() {

		isPlayerMoving = false;

		if (isFollowingPath && pathIndex < currentPath.size() - 1) {
			moveToNextRoomInPath();
			return;
		}

		finishPathMovement();
	}

	public Room.Type getCurrentRoomType() {
		return floor.rooms().get(floor.playerIndex()).type();
	}

	public Room.Type getPreviousRoomType() {
		return floor.rooms().get(floor.previousPlayerIndex()).type();
	}

	private void moveToNextRoomInPath() {
		if (!isFollowingPath || pathIndex >= currentPath.size() - 1) {
			finishPathMovement();
			return;
		}

		pathIndex++;

		playerStartIndex = animatedPlayerIndex;
		playerTargetIndex = currentPath.get(pathIndex);

		animatedPlayerIndex = playerTargetIndex;

		isPlayerMoving = true;
		moveStartTime = System.currentTimeMillis();
	}

	private void finishPathMovement() {

		isFollowingPath = false;
		isPlayerMoving = false;

		int finalRoom = currentPath.get(currentPath.size() - 1);

		lastChangeRoom = System.currentTimeMillis();
		transitionFromMerchant = floor.rooms().get(floor.playerIndex()).type() == Room.Type.MERCHANT;
		transitionFromCorridor = !transitionFromMerchant;
		cleanupFloatingItems();
		floor.movePlayerTo(finalRoom);
		floor.markVisited(finalRoom);

		processRoomType(finalRoom);
	}

	private void cleanupFloatingItems() {

		if (draggedItem != null && !backpack.contains(draggedItem)) {
			draggedItem = null;
			isDragging = false;
			resetDragState();
		}

		if (currentMalediction != null && !backpack.contains(currentMalediction)) {
			currentMalediction = null;
			placingMalediction = false;
			combatPausedByMalediction = false;
		}

		if (placedMalediction != null && !backpack.contains(placedMalediction)) {
			placedMalediction = null;
		}

		if (!floatingItems.isEmpty()) {
			floatingItems.clear();

		}
	}
}
