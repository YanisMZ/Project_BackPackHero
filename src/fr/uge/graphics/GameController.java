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
    List<Item> itemsUsed = selectedItems.stream().map(i -> backpack.grid()[i]).filter(Objects::nonNull).toList();
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
  /**
   * this function will manage every pointer input in the game
   * 
   * @param pe pointer event received from the user
   */
	private void handlePointer(PointerEvent pe) {
		if (pe.action() != PointerEvent.Action.POINTER_DOWN)
			return;

		int mouseX = pe.location().x();
		int mouseY = pe.location().y();

		int slot = backpackSlotAt(mouseX, mouseY);
		if (slot != -1) {
			handleBackpackClick(slot);
			return;
		}

		if (inTreasure) {
			int treasureIndex = treasureSlotAt(mouseX, mouseY);
			if (treasureIndex != -1) {
				takeFromTreasure(treasureIndex);
				return;
			}
			int room = roomAt(mouseX, mouseY);
			if (room != -1) {
				leaveTreasureRoom();
				handleRoomClick(room);
				return;
			}
		}

		if (!inCombat && !inTreasure) {
			int room = roomAt(mouseX, mouseY);
			if (room != -1)
				handleRoomClick(room);
		}
	}
  /**
   * add or remove an item from selection in the backpack
   *
   * @param slot index clicked
   * @param clicked item in that slot (may be null)
   */
	private void toggleSelection(int slot, Item clicked) {
		if (clicked == null)
			return;
		if (selectedItems.contains(slot))
			selectedItems.remove(Integer.valueOf(slot));
		else
			selectedItems.add(slot);
	}
  /**
   * manage clicking inside the inventory and selection and swap or move behavior
   *
   * @param slot index of the clicked backpack slot
   */
	private void handleBackpackClick(int slot) {
		Item[] slots = backpack.grid();
		Item clicked = slots[slot];

    if (selectedItems.size() != 1) {
      toggleSelection(slot, clicked);
      return;
    }

		int selected = selectedItems.get(0);
		if (selected == slot) {
			toggleSelection(slot, clicked);
			return;
		}

		if (slots[slot] == null) {
			backpack.move(selected, slot);
			selectedItems.clear();
			return;
		}

		toggleSelection(slot, clicked);
	}
  /**
   * same has handleBackpackClick but a click on a room if it's adjacent and it update hte game state
   *
   * @param clickedRoom index of the room clicked
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
	
	
	private void handleDeleteSelectedItems() {
    if (selectedItems.isEmpty())
      return;
    
    selectedItems.stream()
      .sorted((a, b) -> b - a)
      .forEach(this::dropBackpackItem);
    
    selectedItems.clear();
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

	private int backpackSlotAt(int mouseX, int mouseY) {
		for (int i = 0; i < backpack.grid().length; i++) {
			int row = i / backpackCols, col = i % backpackCols;
			int x = backpackOriginX + col * (backpackCellSize + backpackPadding);
			int y = backpackOriginY + row * (backpackCellSize + backpackPadding);
			if (mouseX >= x && mouseX <= x + backpackCellSize && mouseY >= y && mouseY <= y + backpackCellSize)
				return i;
		}
		return -1;
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

	private boolean takeFromTreasure(int treasureIndex) {
		if (treasureIndex < 0 || treasureIndex >= treasureChest.size())
			return false;
		Item item = treasureChest.get(treasureIndex);
		if (backpack.add(item) == 1) {
			treasureChest.remove(treasureIndex);
			if (treasureChest.isEmpty())
				setEmptyRoomState();
			return true;
		}
		return false;
	}

	private boolean dropBackpackItem(int slot) {
		Item[] grid = backpack.grid();
		if (slot < 0 || slot >= grid.length || grid[slot] == null)
			return false;
		return backpack.remove(grid[slot]);
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
