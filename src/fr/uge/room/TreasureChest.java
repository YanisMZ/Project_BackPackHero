package fr.uge.room;

public class TreasureChest {

	private final Grid grid;

	public TreasureChest(int rows, int cols) {
		this.grid = new Grid(rows, cols);
	}

	public Grid getGrid() {
		return grid;
	}

	public void generateTreasure() {
		grid.generateRandomItems(1, 5);
	}
}
