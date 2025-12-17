package fr.uge.implement;

import java.util.Random;


/**
 * Représente et gère le coffre au trésor (grille, placement, génération).
 * La gestion des coordonnées d'affichage est déléguée au GameController.
 */
public class TreasureChest {

	private final Item[][] grid;
	private final int rows;
	private final int cols;
	private int startX; // Coordonnée X de dessin (Injectée par GameController)
	private int startY; // Coordonnée Y de dessin (Injectée par GameController)

	// Constantes de cellule (utilisées par GameController pour le dessin/clic)
	public static final int CELL_SIZE = 60; 
	public static final int PADDING = 8; 
	
	// Suppression de : private final Random random = new Random();

	/**
	 * Construit un coffre au trésor sans dépendance à ApplicationContext.
	 */
	public TreasureChest(int rows, int cols, int initialStartX, int initialStartY) {
		this.rows = rows;
		this.cols = cols;
		this.grid = new Item[rows][cols];
		this.startX = initialStartX;
		this.startY = initialStartY;
	}
	
	// --- GETTERS ---
	
	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}
	
	public Item[][] getGrid() {
		return grid;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}
	
	/**
	 * Permet à GameController de mettre à jour les coordonnées si la fenêtre change.
	 */
	public void setCoordinates(int newStartX, int newStartY) {
		this.startX = newStartX;
		this.startY = newStartY;
	}
	
	// --- LOGIQUE DE GRILLE ---
	
	public void generateTreasure() {
        for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				grid[y][x] = null;
			}
		}
		
		// Instanciation locale de Random (pas de variable d'instance)
		var random = new Random();
		
		int numItems = 1 + random.nextInt(5);
		for (int i = 0; i < numItems; i++) {
			Item item = ItemFactory.randomItem();
			if (!placeItem(item)) {
				break;
			}
		}
	}
	
	public boolean placeItem(Item item) {

    // 🔥 CAS STACKABLE (ex: Gold)
    if (item.isStackable()) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Item cell = grid[y][x];
                if (cell != null
                        && cell.isStackable()
                        && cell.name().equals(item.name())) {

                    grid[y][x] = cell.addQuantity(item.quantity());
                    return true;
                }
            }
        }
    }

    // 🔁 Placement normal
    for (int y = 0; y < rows; y++) {
        for (int x = 0; x < cols; x++) {
            if (canPlace(item, x, y)) {
                for (int dy = 0; dy < item.height(); dy++) {
                    for (int dx = 0; dx < item.width(); dx++) {
                        grid[y + dy][x + dx] = item;
                    }
                }
                return true;
            }
        }
    }
    return false;
}

	
	public boolean placeItemAt(Item item, int x, int y) {
        if (canPlace(item, x, y)) {
			for (int dy = 0; dy < item.height(); dy++) {
				for (int dx = 0; dx < item.width(); dx++) {
					grid[y + dy][x + dx] = item;
				}
			}
			return true;
		}
		return false;
	}

	public boolean canPlace(Item item, int x, int y) {
        if (x + item.width() > cols || y + item.height() > rows) {
			return false;
		}
		
		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {
				if (grid[y + dy][x + dx] != null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void removeItem(Item item) {
        for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (grid[y][x] == item) {
					grid[y][x] = null;
				}
			}
		}
	}

	public boolean isEmpty() {
        for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (grid[y][x] != null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void clear() {
        for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				grid[y][x] = null;
			}
		}
	}
}