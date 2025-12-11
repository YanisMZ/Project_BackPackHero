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
	private final List<Item> treasureChest = new ArrayList<>();
	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCols = 5, backpackCellSize = 60, backpackPadding = 8;
	private int treasureStartX, treasureStartY;
	private final int treasureCols = 5;
	private final Dungeon dungeon;
	private int floorIndex = 0;
	
	// Drag and drop state
	private Item draggedItem = null;
	private int dragStartX = -1;
	private int dragStartY = -1;
	private int dragMouseX = 0;
	private int dragMouseY = 0;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean isDragging = false;
	private boolean dragFromTreasure = false; // true si l'item vient du coffre
	private int dragTreasureIndex = -1; // index dans le coffre
	private int pointerDownX = -1;
	private int pointerDownY = -1;
	private static final int DRAG_THRESHOLD = 5; // pixels de mouvement avant de considérer un drag


  /**
   * Creates a new game controller responsible for handling input events, updating
   * states, and interacting with the game model.
   *
   * @param context  Zen application context
   * @param view     graphics part
   * @param floor    dungeon map
   * @param backpack player's inventory
   * @param fight    battle system
   */
  public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Battle fight,Dungeon dungeon) {
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

  /**
   * main loop where evey pressed key/point will get redirected to an other
   * function
   */
  public void update() {
    var event = context.pollOrWaitEvent(10);
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
   * 
   * @param ke keyboard event received from the user
   */
  private void handleKeyboard(KeyboardEvent ke) {
    if (ke.key() == KeyboardEvent.Key.Q)
      System.exit(0);
   
    if (ke.key() == KeyboardEvent.Key.X)
    	handleDeleteSelectedItems();
      
    
    if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
      return;
    List<Item> itemsUsed = selectedItems.stream()
    		.map(i -> {
    			int x = i % backpack.width();
    			int y = i / backpack.width();
    			return backpack.grid()[y][x];
    		})
    		.filter(Objects::nonNull).toList();
    switch (ke.key()) {
    case A -> {
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
   * 
   * @param pe pointer event received from the user
   */
	private void handlePointer(PointerEvent pe) {
		int mouseX = pe.location().x();
		int mouseY = pe.location().y();

		switch (pe.action()) {
			case POINTER_DOWN -> handlePointerDown(mouseX, mouseY);
			case POINTER_UP -> handlePointerUp(mouseX, mouseY);
			case POINTER_MOVE -> handlePointerMove(mouseX, mouseY);
			default -> {}
		}
	}
	
	private void handlePointerDown(int mouseX, int mouseY) {
		// Store pointer down position
		pointerDownX = mouseX;
		pointerDownY = mouseY;
		
		// Check treasure chest click for potential drag (priority)
		if (inTreasure) {
			int treasureIndex = treasureSlotAt(mouseX, mouseY);
			if (treasureIndex != -1 && treasureIndex < treasureChest.size()) {
				Item item = treasureChest.get(treasureIndex);
				if (item != null) {
					// Prepare for potential dragging from treasure
					draggedItem = item;
					dragFromTreasure = true;
					dragTreasureIndex = treasureIndex;
					
					// Calculate offset
					int row = treasureIndex / treasureCols;
					int col = treasureIndex % treasureCols;
					int cellX = treasureStartX + col * (backpackCellSize + backpackPadding);
					int cellY = treasureStartY + row * (backpackCellSize + backpackPadding);
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
				// Prepare for potential dragging from backpack
				draggedItem = item;
				dragStartX = x;
				dragStartY = y;
				dragFromTreasure = false;
				
				// Calculate offset from top-left of item to mouse position
				int cellX = backpackOriginX + x * (backpackCellSize + backpackPadding);
				int cellY = backpackOriginY + y * (backpackCellSize + backpackPadding);
				dragOffsetX = mouseX - cellX;
				dragOffsetY = mouseY - cellY;
				
				// Initial mouse position
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
		// Check if we have a potential drag item and haven't started dragging yet
		if (draggedItem != null && !isDragging) {
			// Calculate distance moved
			int deltaX = Math.abs(mouseX - pointerDownX);
			int deltaY = Math.abs(mouseY - pointerDownY);
			
			// If moved beyond threshold, start dragging
			if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
				isDragging = true;
				
				// Remove item from source
				if (dragFromTreasure) {
					// Remove from treasure chest
					treasureChest.remove(dragTreasureIndex);
				} else {
					// Remove from backpack
					backpack.remove(draggedItem);
				}
			}
		}
		
		if (isDragging && draggedItem != null) {
			// Update the position where the item should be drawn (compensate for offset)
			dragMouseX = mouseX - dragOffsetX;
			dragMouseY = mouseY - dragOffsetY;
		}
	}
	
	private void handlePointerUp(int mouseX, int mouseY) {
		// If we have a dragged item but never started dragging, it's a click (selection)
		if (draggedItem != null && !isDragging) {
			// Only handle selection for backpack items (not treasure)
			if (!dragFromTreasure) {
				int[] slotCoords = backpackSlotCoordsAt(pointerDownX, pointerDownY);
				if (slotCoords != null) {
					toggleSelection(slotCoords[0], slotCoords[1], draggedItem);
				}
			}
			// If clicked on treasure without dragging, do nothing (old behavior removed)
			
			// Reset drag state
			draggedItem = null;
			dragStartX = -1;
			dragStartY = -1;
			dragFromTreasure = false;
			dragTreasureIndex = -1;
			pointerDownX = -1;
			pointerDownY = -1;
			return;
		}
		
		// Handle actual drag and drop
		if (isDragging && draggedItem != null) {
			boolean placed = false;
			
			// Try to place in backpack
			int[] targetCoords = backpackSlotCoordsAt(mouseX, mouseY);
			if (targetCoords != null) {
				int targetX = targetCoords[0];
				int targetY = targetCoords[1];
				
				// Try to place at target position
				if (backpack.place(draggedItem, targetX, targetY)) {
					placed = true;
					// If item came from treasure and was successfully placed, we're done
					if (dragFromTreasure && treasureChest.isEmpty()) {
						setEmptyRoomState();
					}
				}
			}
			
			// If not placed successfully
			if (!placed) {
				if (dragFromTreasure) {
					// Return to treasure chest
					treasureChest.add(dragTreasureIndex, draggedItem);
				} else {
					// Return to original backpack position
					if (!backpack.place(draggedItem, dragStartX, dragStartY)) {
						// Original position is blocked, try auto-add
						if (!backpack.autoAdd(draggedItem)) {
							// Can't place anywhere in backpack, return to treasure if from treasure
							if (dragFromTreasure) {
								treasureChest.add(draggedItem);
							}
							// Otherwise item is lost (shouldn't happen normally)
						}
					}
				}
			}
		}
		
		// Reset drag state
		isDragging = false;
		draggedItem = null;
		dragStartX = -1;
		dragStartY = -1;
		dragFromTreasure = false;
		dragTreasureIndex = -1;
		pointerDownX = -1;
		pointerDownY = -1;
	}
	
  /**
   * add or remove an item from selection in the backpack
   *
   * @param slot index clicked
   * @param clicked item in that slot (may be null)
   */
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
    
    selectedItems.stream()
      .sorted((a, b) -> b - a)
      .forEach(slot -> {
      	int x = slot % backpack.width();
      	int y = slot / backpack.width();
      	Item item = backpack.grid()[y][x];
      	if (item != null) {
      		backpack.remove(item);
      	}
      });
    
    selectedItems.clear();
  }
  
  /**
   * manage clicking inside the inventory and selection and swap or move behavior
   *
   * @param clickedRoom index of the clicked backpack slot
   */
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
		}
			else if (floor.rooms().get(clickedRoom).type() == Room.Type.EXIT) {
		    goToNextFloor();
		} else {
			setEmptyRoomState();
		}

		floor.markVisited(clickedRoom);
	}
	
	
	/**
	 * Exits the treasure room state and returns to the appropriate game state
	 * based on the current room type.
	 */
	private void leaveTreasureRoom() {
	    treasureChest.clear();
	    
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
			if (!treasureChest.isEmpty())
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
  /**
   * Computes the room located under a mouse click.
   *
   * @param mouseX x coordinate
   * @param mouseY y coordinate
   * @return the room index or -1 if no room has been found
   */
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

	/**
	 * Returns the grid coordinates [x, y] of the backpack slot at mouse position
	 * or null if outside backpack.
	 */
	private int[] backpackSlotCoordsAt(int mouseX, int mouseY) {
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				int cellX = backpackOriginX + x * (backpackCellSize + backpackPadding);
				int cellY = backpackOriginY + y * (backpackCellSize + backpackPadding);
				
				if (mouseX >= cellX && mouseX <= cellX + backpackCellSize && 
						mouseY >= cellY && mouseY <= cellY + backpackCellSize) {
					return new int[]{x, y};
				}
			}
		}
		return null;
	}

	private int treasureSlotAt(int mouseX, int mouseY) {
    for (int i = 0; i < treasureChest.size(); i++) {
        int row = i / treasureCols;
        int col = i % treasureCols;
        int x = treasureStartX + col * (backpackCellSize + backpackPadding);
        int y = treasureStartY + row * (backpackCellSize + backpackPadding);
        if (mouseX >= x && mouseX <= x + backpackCellSize && mouseY >= y && mouseY <= y + backpackCellSize)
            return i;
    }
    return -1;
}

	/** Generates random items using ItemFactory */
	private void generateTreasure() {
		treasureChest.clear();
		int numItems = 1 + (int) (Math.random() * 5); // 1-5 items
		for (int i = 0; i < numItems; i++) {
			treasureChest.add(ItemFactory.randomItem());
		}
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


	public List<Item> getTreasure() {
		return List.copyOf(treasureChest);
	}
}