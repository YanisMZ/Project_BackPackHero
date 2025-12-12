package fr.uge.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.implement.*;

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
	private boolean inCorridor = true;
	private boolean inTreasure = false;
	private boolean inCombat = false;
	private final List<Integer> selectedItems = new ArrayList<>();
	private final Item[][] treasureGrid = new Item[3][5]; // 3 lignes, 5 colonnes
	private final int treasureRows = 3;
	private final int treasureCols = 5;
	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCols = 5, backpackCellSize = 60, backpackPadding = 8;
	private int treasureStartX, treasureStartY;
	private final Dungeon dungeon;
	private int floorIndex = 0;
	private long lastAttackTime = 0;
	
	// Drag and drop state
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

	/**
	 * Creates a new game controller responsible for handling input events, updating
	 * states, and interacting with the game model.
	 */
	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Battle fight,
			Dungeon dungeon) {
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
		this.fight = Objects.requireNonNull(fight);
		this.hero = new Hero(40, 0);
		this.dungeon = dungeon;
	}

	public boolean isInCorridor() {
		return inCorridor;
	}

	public boolean isInTreasure() {
		return inTreasure;
	}

	public boolean isInCombat() {
		return inCombat;
	}

	public List<Integer> getSelectedSlots() {
		return selectedItems;
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

	public Item[][] getTreasureGrid() {
		return treasureGrid;
	}


/**
* main loop where every pressed key/point will get redirected to another function
* * @param pollTimeout The maximum time (in milliseconds) to wait for an event.
* Set to 0 for maximum responsiveness (during dragging).
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

		if (ke.key() == KeyboardEvent.Key.X)
			handleDeleteSelectedItems();

		if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
			return;
		List<Item> itemsUsed = selectedItems.stream().map(i -> {
			int x = i % backpack.width();
			int y = i / backpack.width();
			return backpack.grid()[y][x];
		}).filter(Objects::nonNull).toList();
		switch (ke.key()) {
		case A -> {
			lastAttackTime = System.currentTimeMillis();
			fight.attackEnemy(itemsUsed);
			fight.attackEnemy(itemsUsed);
			fight.enemyTurn();
			checkCombatEnd();
		}
		case D -> {
			fight.defendHero();
			fight.enemyTurn();
			checkCombatEnd();
		}
		default -> {
		}
		}
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

		// Check treasure chest click for potential drag (priority)
		if (inTreasure) {
			int[] treasureCoords = treasureSlotCoordsAt(mouseX, mouseY);
			if (treasureCoords != null) {
				int x = treasureCoords[0];
				int y = treasureCoords[1];
				Item item = treasureGrid[y][x];
				
				if (item != null) {
					// Trouver le coin supérieur gauche de l'item
					int itemStartX = x;
					int itemStartY = y;
					
					// Chercher vers la gauche et vers le haut
					while (itemStartX > 0 && treasureGrid[y][itemStartX - 1] == item) {
						itemStartX--;
					}
					while (itemStartY > 0 && treasureGrid[itemStartY - 1][x] == item) {
						itemStartY--;
					}
					
					draggedItem = item;
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

		// Check backpack click for potential drag
		int[] slotCoords = backpackSlotCoordsAt(mouseX, mouseY);
		if (slotCoords != null) {
			int x = slotCoords[0];
			int y = slotCoords[1];
			Item item = backpack.grid()[y][x];

			if (item != null) {
				draggedItem = item;
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
				handleRoomClick(room);
				return;
			}
		}

		// Handle room clicks
		if (!inCombat && !inTreasure) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1)
				handleRoomClick(room);
		}
	}

	private void handlePointerMove(int mouseX, int mouseY) {
		if (draggedItem != null && !isDragging) {
			int deltaX = Math.abs(mouseX - pointerDownX);
			int deltaY = Math.abs(mouseY - pointerDownY);

			if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
				isDragging = true;

				if (dragFromTreasure) {
					removeItemFromTreasure(draggedItem);
				} else {
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
		if (draggedItem != null && !isDragging) {
			if (!dragFromTreasure) {
				int[] slotCoords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
				if (slotCoords != null) {
					toggleSelection(slotCoords[0], slotCoords[1], draggedItem);
				}
			}

			draggedItem = null;
			dragStartX = -1;
			dragStartY = -1;
			dragFromTreasure = false;
			pointerDownX = -1;
			pointerDownY = -1;
			return;
		}

		if (isDragging && draggedItem != null) {
			boolean placed = false;

			// Try to place in backpack
			int[] targetCoords = backpackSlotCoordsAt(mouseX, mouseY);
			if (targetCoords != null) {
				int targetX = targetCoords[0];
				int targetY = targetCoords[1];

				if (backpack.place(draggedItem, targetX, targetY)) {
					placed = true;
					if (dragFromTreasure && isTreasureEmpty()) {
						setEmptyRoomState();
					}
				}
			}

			// Try to place in treasure if coming from backpack
			if (!placed && inTreasure && !dragFromTreasure) {
				int[] treasureCoords = treasureSlotCoordsAt(mouseX, mouseY);
				if (treasureCoords != null) {
					int targetX = treasureCoords[0];
					int targetY = treasureCoords[1];
					
					if (canPlaceInTreasure(draggedItem, targetX, targetY)) {
						for (int dy = 0; dy < draggedItem.height(); dy++) {
							for (int dx = 0; dx < draggedItem.width(); dx++) {
								treasureGrid[targetY + dy][targetX + dx] = draggedItem;
							}
						}
						placed = true;
					}
				}
			}

			if (!placed) {
				if (dragFromTreasure) {
					// Return to treasure at original position
					for (int dy = 0; dy < draggedItem.height(); dy++) {
						for (int dx = 0; dx < draggedItem.width(); dx++) {
							treasureGrid[dragStartY + dy][dragStartX + dx] = draggedItem;
						}
					}
				} else {
					if (!backpack.place(draggedItem, dragStartX, dragStartY)) {
						if (!backpack.autoAdd(draggedItem)) {
							// Item is lost
						}
					}
				}
			}
		}

		isDragging = false;
		draggedItem = null;
		dragStartX = -1;
		dragStartY = -1;
		dragFromTreasure = false;
		pointerDownX = -1;
		pointerDownY = -1;
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

		if (floor.playerOnEnemyRoom() && !floor.isVisited(clickedRoom)) {
			startCombat();
		} else if (floor.playerOnTreasureRoom() && !floor.isVisited(clickedRoom)) {
			generateTreasure();
			setTreasureState();
			floor.markVisited(clickedRoom);
		} else if (floor.playerOnCorridor()) {
			setCorridorState();
		} else if (floor.rooms().get(clickedRoom).type() == Room.Type.EXIT) {
			goToNextFloor();
		} else {
			setEmptyRoomState();
		}

		floor.markVisited(clickedRoom);
	}

	private void leaveTreasureRoom() {
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				treasureGrid[y][x] = null;
			}
		}

		if (floor.playerOnCorridor()) {
			setCorridorState();
		} else {
			setEmptyRoomState();
		}
	}

	private void startCombat() {
		fight.initEnemies();
		inCombat = true;
	}

	private void checkCombatEnd() {
		if (fight != null && !fight.isRunning()) {
			inCombat = false;
			generateTreasure();
			if (!isTreasureEmpty())
				setTreasureState();
		}
	}

	private void setCorridorState() {
		inCorridor = true;
		inTreasure = false;
		inCombat = false;
	}

	private void setTreasureState() {
		inTreasure = true;
		inCorridor = false;
		inCombat = false;

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
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				int cellX = treasureStartX + x * (backpackCellSize + backpackPadding);
				int cellY = treasureStartY + y * (backpackCellSize + backpackPadding);
				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && 
				    mouseY >= cellY && mouseY <= cellY + backpackCellSize) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private void generateTreasure() {
		// Clear the grid
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				treasureGrid[y][x] = null;
			}
		}
		
		int numItems = 1 + (int) (Math.random() * 5); // 1-5 items
		for (int i = 0; i < numItems; i++) {
			Item item = ItemFactory.randomItem();
			if (!placeItemInTreasure(item)) {
				break;
			}
		}
	}

	private boolean placeItemInTreasure(Item item) {
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				if (canPlaceInTreasure(item, x, y)) {
					for (int dy = 0; dy < item.height(); dy++) {
						for (int dx = 0; dx < item.width(); dx++) {
							treasureGrid[y + dy][x + dx] = item;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	private boolean canPlaceInTreasure(Item item, int x, int y) {
		if (x + item.width() > treasureCols || y + item.height() > treasureRows) {
			return false;
		}
		
		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {
				if (treasureGrid[y + dy][x + dx] != null) {
					return false;
				}
			}
		}
		return true;
	}

	private void removeItemFromTreasure(Item item) {
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				if (treasureGrid[y][x] == item) {
					treasureGrid[y][x] = null;
				}
			}
		}
	}

	private boolean isTreasureEmpty() {
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				if (treasureGrid[y][x] != null) {
					return false;
				}
			}
		}
		return true;
	}

	private void goToNextFloor() {
		if (floorIndex + 1 >= 3) {
			System.out.println("Fin du donjon ! Il n'y a plus d'étages.");
			System.exit(0);
			return;
		}
		floorIndex++;
		MapDungeon next = dungeon.getFloor(floorIndex);
		floor.rooms().clear();
		floor.rooms().addAll(next.rooms());
		floor.setPlayerIndex(0);
		floor.clearVisited();
		setCorridorState();
	}
}