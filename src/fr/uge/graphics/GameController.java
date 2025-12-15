package fr.uge.graphics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.BackPack;
import fr.uge.implement.Battle;
import fr.uge.implement.Dungeon;
import fr.uge.implement.FloatingItem;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.MapDungeon;
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

	private final TreasureChest treasureChest;

	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCols = 5, backpackCellSize = 60, backpackPadding = 8;
	private int treasureStartX, treasureStartY;
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
		this.dungeon = dungeon;
		this.hero = new Hero(40, 0, 3);
		setTreasureDisplayCoords();
		this.treasureChest = new TreasureChest(3, 5, treasureStartX, treasureStartY);
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

	public Hero getHero() {
		return hero;
	}

	public Battle getBattle() {
		return fight;
	}

	/** Retourne la grille du trésor gérée par l'objet TreasureChest. */
	public Item[][] getTreasureGrid() {
		return treasureChest.getGrid();
	}
	
	 public List<FloatingItem> getFloatingItems() { return floatingItems; }

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

		// Supprimer des items uniquement hors combat
		if (ke.key() == KeyboardEvent.Key.X && !inCombat)
			handleDeleteSelectedItems();

		if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
			return;

		switch (ke.key()) {
		
		// CTRL : Terminer le tour du joueur (les ennemis attaquent automatiquement)
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
				Item item = treasureChest.getGrid()[y][x];

				if (item != null) {
					int itemStartX = x;
					int itemStartY = y;

					while (itemStartX > 0 && treasureChest.getGrid()[y][itemStartX - 1] == item) {
						itemStartX--;
					}
					while (itemStartY > 0 && treasureChest.getGrid()[itemStartY - 1][x] == item) {
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

            // Retirer l'item du sac ou du coffre uniquement au début du drag
            if (dragFromTreasure) {
                treasureChest.removeItem(draggedItem);
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
    // Clic simple sans drag
    if (draggedItem != null && !isDragging) {
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

        draggedItem = null;
        dragStartX = -1;
        dragStartY = -1;
        dragFromTreasure = false;
        pointerDownX = -1;
        pointerDownY = -1;
        return;
    }

    // Drag en cours
    if (isDragging && draggedItem != null) {
        boolean placed = false;

        // Essayer de placer dans le sac
        int[] targetCoords = backpackSlotCoordsAt(mouseX, mouseY);
        if (targetCoords != null && backpack.place(draggedItem, targetCoords[0], targetCoords[1])) {
            placed = true;
        }

        // Essayer de placer dans le coffre
        if (!placed && inTreasure && !dragFromTreasure) {
            int[] treasureCoords = treasureSlotCoordsAt(mouseX, mouseY);
            if (treasureCoords != null && treasureChest.placeItemAt(draggedItem, treasureCoords[0], treasureCoords[1])) {
                placed = true;
            }
        }

        // Si pas placé, item devient flottant
        if (!placed) {
            floatingItems.add(new FloatingItem(draggedItem, new Point(mouseX - dragOffsetX, mouseY - dragOffsetY)));
        }

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

    // On supprime les items flottants à chaque changement de salle
    floatingItems.clear();

    if (floor.playerOnEnemyRoom() && !floor.isVisited(clickedRoom)) {
        startCombat();
    } else if (floor.playerOnTreasureRoom() && !floor.isVisited(clickedRoom)) {
        treasureChest.generateTreasure();
        setTreasureState();
        floor.markVisited(clickedRoom);
    } else if (floor.playerOnCorridor()) {
        setCorridorState();
    } else {
        setEmptyRoomState();
    }

    floor.markVisited(clickedRoom);
}


	private void leaveTreasureRoom() {
    treasureChest.clear();
    floatingItems.clear(); // supprime tous les items flottants
    if (floor.playerOnCorridor()) setCorridorState();
    else setEmptyRoomState();
}

	private void startCombat() {
		fight.initEnemies();
		inCombat = true;
	}

	private void checkCombatEnd() {
		if (fight != null && !fight.isRunning()) {
			inCombat = false;
			treasureChest.generateTreasure();
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

		setTreasureDisplayCoords();

		treasureChest.setCoordinates(treasureStartX, treasureStartY);
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
		int treasureRows = treasureChest.getRows();
		int treasureCols = treasureChest.getCols();

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