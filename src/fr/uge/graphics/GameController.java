package fr.uge.graphics;

import java.util.Objects;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import fr.uge.implement.*;

public class GameController {

	private final ApplicationContext context;
	private final GameView view;
	private final MapDungeon floor;
	private final BackPack backpack;
	private final Hero hero;
	private final Combat fight;
	private boolean inCorridor = true;
	private boolean inTreasure = false;
	private boolean inCombat = false;
	private Integer selectedBackpackIndex = null;
	private final int backpackOriginX = 20, backpackOriginY = 550;
	private final int backpackCols = 5, backpackCellSize = 60, backpackPadding = 8;

	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack, Combat fight) {
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
		this.fight = Objects.requireNonNull(fight);
		this.hero = new Hero(40, 0);
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

	public Integer getSelectedBackpackIndex() {
		return selectedBackpackIndex;
	}

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

	private void handleKeyboard(KeyboardEvent ke) {
		if (ke.key() == KeyboardEvent.Key.Q)
			System.exit(0);
		if (!inCombat || ke.action() != KeyboardEvent.Action.KEY_RELEASED)
			return;

		switch (ke.key()) {
		case A -> {
			fight.attackEnemy();
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

	private void handlePointer(PointerEvent pe) {
		if (inCombat || pe.action() != PointerEvent.Action.POINTER_DOWN)
			return;

		int mouseX = pe.location().x(), mouseY = pe.location().y();
		int slot = backpackSlotAt(mouseX, mouseY);
		if (slot != -1) {
			handleBackpackClick(slot);
			return;
		}

		int room = roomAt(mouseX, mouseY);
		if (room != -1)
			handleRoomClick(room);
	}

	private void handleBackpackClick(int slot) {
		Item[] slots = backpack.grid();
		Item clicked = slots[slot];

		if (selectedBackpackIndex == null) {
			if (clicked != null)
				selectedBackpackIndex = slot;
		} else {
			backpack.move(selectedBackpackIndex, slot);
			selectedBackpackIndex = null;
		}
	}

	private void handleRoomClick(int clickedRoom) {
		if (!floor.adjacentRooms().contains(clickedRoom))
			return;
		floor.setPlayerIndex(clickedRoom);

		if (floor.playerOnEnemyRoom() && !floor.isVisited(clickedRoom))
			startCombat();
		else if (floor.playerOnCorridor())
			setCorridorState();
		else if (floor.playerOnTreasureRoom())
			setTreasureState();
		else
			setEmptyRoomState();

		floor.markVisited(clickedRoom);
	}

	private void startCombat() {
		fight.initEnemies();
		inCombat = true;
	}

	private void checkCombatEnd() {
		if (fight != null && !fight.isRunning())
			inCombat = false;
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
}
