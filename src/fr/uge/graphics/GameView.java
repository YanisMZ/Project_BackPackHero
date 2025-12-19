package fr.uge.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.forax.zen.ApplicationContext;

import fr.uge.implement.*;

public record GameView(ApplicationContext context, MapDungeon floor, BackPack backpack) {

	// ===================== CONSTANTES =====================
	private static final int CELL_SIZE = 60;
	private static final int PADDING = 8;
	private static final int BACKPACK_ORIGIN_X = 20;
	private static final int BACKPACK_ORIGIN_Y = 550;
	private static final int GRID_CELL_SIZE = 120;
	private static final int GRID_PADDING = 10;
	private static final int GRID_COLS = 4;

	// ===================== ASSETS =====================
	private static final List<BufferedImage> loadingAnimation = loadFrames(161, "loadingscreen", "loading");
	private static BufferedImage corridorImage;
	private static BufferedImage treasureRoomImage;
	private static BufferedImage treasureImage;
	private static BufferedImage heroImage;
	private static BufferedImage heroImage2;
	private static BufferedImage enemyRoomImage0;
	private static BufferedImage enemyRoomImage1;
	private static BufferedImage enemyRoomImage2;
	private static BufferedImage enemyRoomImage3;
	private static BufferedImage attackOrDefenseBanner;
	private static BufferedImage attackBanner;
	private static BufferedImage defendBanner;
	private static BufferedImage injuredEnemy;

	private static Map<String, BufferedImage> weaponAssets = new HashMap<>();
	private static List<BufferedImage> fightingAnimation1;
	private static List<BufferedImage> fightingAnimation2;
	private static List<BufferedImage> fightingAnimation3;
	private static List<BufferedImage> corridorToCorridorAnimation;

	// ===================== ASSET LOADING =====================
	private static BufferedImage loadImage(String fileName) {
		try {
			File file = Path.of("./data", fileName).toFile();
			BufferedImage result = ImageIO.read(file);
			if (result == null) {
				throw new IllegalArgumentException("Error loading: " + fileName);
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Unable to load: " + fileName, e);
		}
	}

	public static void loadGameAssets() {
		if (corridorImage != null)
			return;

		loadStaticImages();
		loadAnimations();
		loadAllWeapons();
		System.out.println("Assets chargés !");
	}

	private static void loadStaticImages() {
		corridorImage = loadImage("corridor2.png");
		treasureRoomImage = loadImage("treasureroom.png");
		treasureImage = loadImage("treasure.png");
		heroImage = loadImage("hero.png");
		heroImage2 = loadImage("hero2.png");
		enemyRoomImage0 = loadImage("fight0.png");
		enemyRoomImage1 = loadImage("fight1.png");
		enemyRoomImage2 = loadImage("fight2.png");
		enemyRoomImage3 = loadImage("fight3.png");
		attackOrDefenseBanner = loadImage("attackdefend.png");
		attackBanner = loadImage("attack.png");
		defendBanner = loadImage("defend.png");
	}

	private static void loadAnimations() {
		fightingAnimation1 = loadAttackFrames(1, 157);
		fightingAnimation2 = loadAttackFrames(2, 78);
		fightingAnimation3 = loadAttackFrames(3, 78);
		corridorToCorridorAnimation = loadFrames(160, "animationroom", "room");
	}

	private static List<BufferedImage> loadFrames(int nbFrames, String folder, String name) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames; i++) {
			frames.add(loadImage("./" + folder + "/" + name + "(" + i + ").jpg"));
			System.out.println("./" + folder + "/" + name + "(" + i + ").jpg");
		}
		return frames;
	}

	private static List<BufferedImage> loadAttackFrames(int nbEnemies, int nbFrames) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames; i++) {
			frames.add(loadImage("./fighting" + nbEnemies + "/hit_(" + i + ").png"));
			System.out.println("Frame chargé : hit_(" + i + ").png");
		}
		return frames;
	}

	private static void loadAllWeapons() {
		File folder = new File("./data/weapons");
		File[] files = folder.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".png")) {
					String key = file.getName().substring(0, file.getName().lastIndexOf('.'));
					
					BufferedImage img = loadImage("weapons/" + file.getName());

					if (img != null) {
						weaponAssets.put(key, img);
						System.out.println("Arme chargée : " + key);
					}
				}
			}
		} else {
			System.out.println("Erreur : Dossier weapons introuvable !");
		}
	}

	// ===================== MAIN DISPLAYS =====================
	public void loadingDisplay(long startTime) {
		int totalTime = 8000;
		context.renderFrame(g -> {
			clearScreen(g);
			drawAnimation(g, 0, totalTime, loadingAnimation);
			long currentTime = System.currentTimeMillis();
			long totalElapsed = currentTime - startTime;
			long loopElapsed = totalElapsed % totalTime;
			long loopStartTime = currentTime - loopElapsed;

			drawAnimation(g, loopStartTime, totalTime, loadingAnimation);
		});
	}

	public void render(List<Integer> selectedSlots, boolean isDragging, Item draggedItem, int dragOffsetX,
			int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void combatDisplay(int nbEnemies, int status, List<Integer> selectedSlots, Hero hero, List<Enemy> enemies,
			boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY, long lastAttackTime,
			List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			if (isAnimationPlaying(lastAttackTime, 4500)) {
				drawCombatAnimation(g, nbEnemies, lastAttackTime);
			} else {
				drawCombatScene(g, nbEnemies, status, enemies);
			}
			drawAllBars(g, hero, enemies);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);

		});
	}

	public void corridorDisplay(List<Integer> selectedSlots, Hero hero, boolean isDragging, Item draggedItem,
			int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems, long lastChangeRoom) {
		context.renderFrame(g -> {
			clearScreen(g);
			if (isAnimationPlaying(lastChangeRoom, 8000)) {
				drawAnimation(g, lastChangeRoom, 8000, corridorToCorridorAnimation);
			} else {
				drawCorridor(g);
				drawHero(g);
			}
			drawAllHeroBars(g, hero);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void expansionDisplay(List<Integer> selectedSlots, Hero hero, BackpackExpansionSystem expansionSystem) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawEmptyRoom(g);
			drawAllHeroBars(g, hero);
			drawHero(g);
			drawGrid(g);
			drawBackPackWithExpansion(g, selectedSlots, expansionSystem);
		});
	}

	public void merchantDisplay(List<Integer> selectedSlots, Item[][] merchantGrid, Hero hero, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawAllHeroBars(g, hero);
			drawMerchantStock(g, merchantGrid, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void treasureDisplay(List<Integer> selectedSlots, Item[][] treasureGrid, Hero hero, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawAllHeroBars(g, hero);
			drawTreasure(g);
			drawTreasureChest(g, treasureGrid, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void emptyRoomDisplay(List<Integer> selectedSlots, Hero hero, boolean isDragging, Item draggedItem,
			int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawEmptyRoom(g);
			drawAllHeroBars(g, hero);
			drawHero(g);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	// ===================== COMBAT HELPERS =====================
	private void drawCombatAnimation(Graphics2D g, int nbEnemies, long startTime) {
		List<BufferedImage> animation = switch (nbEnemies) {
		case 1 -> fightingAnimation1;
		case 2 -> fightingAnimation2;
		case 3 -> fightingAnimation3;
		default -> fightingAnimation1;
		};
		drawAnimation(g, startTime, 4500, animation);
	}

	private void drawCombatScene(Graphics2D g, int nbEnemies, int status, List<Enemy> enemies) {
		if (nbEnemies > 0 && enemies.get(0).hp() < enemies.get(0).maxHp()) {
			drawInjuredRat(g, nbEnemies, status);
		} else {
			drawCombat(g, nbEnemies, status);
		}
	}

	private void drawAllBars(Graphics2D g, Hero hero, List<Enemy> enemies) {
		drawHeroHealthBar(g, hero);
		drawHeroManaBar(g, hero);
		drawHeroStaminaBar(g, hero);
		drawEnemyHealthBars(g, enemies);
	}

	private void drawAllHeroBars(Graphics2D g, Hero hero) {
		drawHeroHealthBar(g, hero);
		drawHeroManaBar(g, hero);
		drawHeroStaminaBar(g, hero);
	}

	// ===================== ANIMATIONS =====================
	private boolean isAnimationPlaying(long startTime, int duration) {
		return System.currentTimeMillis() - startTime < duration;
	}

	private void drawAnimation(Graphics2D g, long startTime, int duration, List<BufferedImage> frames) {
		long elapsed = System.currentTimeMillis() - startTime;
		if (elapsed > duration)
			return;

		int frameIndex = (int) ((elapsed * frames.size()) / duration);
		frameIndex = Math.min(frameIndex, frames.size() - 1);

		var info = context.getScreenInfo();
		g.drawImage(frames.get(frameIndex), 0, 0, info.width(), info.height(), null);
	}

	// ===================== BACKGROUNDS =====================
	private void drawEmptyRoom(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(enemyRoomImage0, 0, 0, info.width(), info.height(), null);
	}

	private void drawTreasure(Graphics2D g) {
		var info = context.getScreenInfo();
		int w = info.width(), h = info.height();
		g.drawImage(treasureRoomImage, 0, 0, w, h, null);
		g.drawImage(treasureImage, w / 2, h / 2, w / 2, h / 2, null);
	}

	private void drawHero(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(heroImage2, info.width() / 4, info.height() / 4, info.width(), info.height(), null);
	}

	private void drawCorridor(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(corridorImage, 0, 0, info.width(), info.height(), null);
	}

	private void drawInjuredRat(Graphics2D g, int nbEnemies, int status) {
		var info = context.getScreenInfo();
		BufferedImage bg = switch (nbEnemies) {
		case 1 -> injuredEnemy;
		case 2 -> enemyRoomImage2;
		case 3 -> enemyRoomImage3;
		default -> enemyRoomImage3;
		};
		g.drawImage(bg, 0, 0, info.width(), info.height(), null);
		drawBattleBanner(g, status);
	}

	private void drawCombat(Graphics2D g, int nbEnemies, int status) {
		var info = context.getScreenInfo();
		BufferedImage bg = switch (nbEnemies) {
		case 1 -> enemyRoomImage1;
		case 2 -> enemyRoomImage2;
		case 3 -> enemyRoomImage3;
		default -> enemyRoomImage3;
		};
		g.drawImage(bg, 0, 0, info.width(), info.height(), null);
		drawBattleBanner(g, status);
	}

	private void drawBattleBanner(Graphics2D g, int status) {
		var info = context.getScreenInfo();
		int bannerW = info.width() / 5;
		int bannerH = info.height() / 5;
		int x = (info.width() - bannerW) / 2;
		int y = info.height() - bannerH - info.height() / 50;

		BufferedImage banner = switch (status) {
		case 0 -> attackOrDefenseBanner;
		case 1 -> attackBanner;
		case 2 -> defendBanner;
		default -> throw new IllegalArgumentException("Invalid status: " + status);
		};
		g.drawImage(banner, x, y, bannerW, bannerH, null);
	}

	// ===================== GRID (MAP) =====================
	private void drawGrid(Graphics2D g) {
		var adjacents = floor.adjacentRooms();
		for (int i = 0; i < floor.rooms().size(); i++) {
			drawRoomCell(g, i, floor.rooms().get(i), adjacents.contains(i));
		}
	}

	private void drawRoomCell(Graphics2D g, int index, Room room, boolean isAdjacent) {
		int row = index / GRID_COLS;
		int col = index % GRID_COLS;
		int x = GRID_PADDING + col * (GRID_CELL_SIZE + GRID_PADDING);
		int y = GRID_PADDING + row * (GRID_CELL_SIZE + GRID_PADDING);

		Color color = isAdjacent ? Color.GREEN : getRoomColor(room);
		g.setColor(color);
		g.fill(new Rectangle2D.Float(x, y, GRID_CELL_SIZE, GRID_CELL_SIZE));

		g.setColor(Color.BLACK);
		g.draw(new Rectangle2D.Float(x, y, GRID_CELL_SIZE, GRID_CELL_SIZE));

		if (index == floor.playerIndex()) {
			drawPlayerIcon(g, x, y);
		}

		g.setColor(Color.BLACK);
		g.drawString(room.name(), x + 8, y + GRID_CELL_SIZE / 2);
	}

	private Color getRoomColor(Room room) {
		return switch (room.type()) {
		case ENEMY -> new Color(200, 80, 80);
		case TREASURE -> new Color(250, 220, 80);
		case MERCHANT -> new Color(80, 180, 250);
		case HEALER -> new Color(100, 220, 100);
		case EXIT -> new Color(180, 80, 250);
		default -> new Color(180, 180, 180);
		};
	}

	private void drawPlayerIcon(Graphics2D g, int x, int y) {
		g.setColor(Color.RED);
		int imgSize = GRID_CELL_SIZE / 2;
		int offset = (GRID_CELL_SIZE - imgSize) / 2;
		g.drawImage(heroImage, x + offset, y + offset, imgSize, imgSize, null);
	}

	// ===================== BACKPACK =====================
	private void drawBackPack(Graphics2D g, List<Integer> selectedSlots, boolean isDragging, Item draggedItem,
			int dragOffsetX, int dragOffsetY) {
		g.setColor(Color.BLACK);
		g.drawString("Backpack :", BACKPACK_ORIGIN_X, BACKPACK_ORIGIN_Y - 10);

		drawBackpackCells(g, selectedSlots, isDragging, draggedItem);
		drawBackpackItems(g, isDragging, draggedItem);
		if (isDragging && draggedItem != null) {
			drawDraggedItem(g, draggedItem, dragOffsetX, dragOffsetY);
		}
	}

	private void drawBackpackCells(Graphics2D g, List<Integer> selectedSlots, boolean isDragging, Item draggedItem) {
		Item[][] grid = backpack.grid();
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				drawBackpackCell(g, x, y, grid[y][x], selectedSlots, isDragging, draggedItem);
			}
		}
	}

	private void drawBackpackCell(Graphics2D g, int x, int y, Item item, List<Integer> selectedSlots, boolean isDragging,
			Item draggedItem) {
		int cellX = BACKPACK_ORIGIN_X + x * (CELL_SIZE + PADDING);
		int cellY = BACKPACK_ORIGIN_Y + y * (CELL_SIZE + PADDING);
		boolean isUnlocked = backpack.isUnlocked(x, y);

		Color color = getCellColor(isUnlocked, item, isDragging, draggedItem);
		g.setColor(color);
		g.fill(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));

		g.setColor(item != null && item != draggedItem ? Color.GRAY : Color.BLACK);
		g.draw(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));

		if (!isUnlocked) {
			drawLock(g, cellX, cellY);
		}

		int slot = y * backpack.width() + x;
		if (selectedSlots != null && selectedSlots.contains(slot) && isUnlocked && item == null) {
			g.setColor(Color.RED);
			g.drawRect(cellX - 2, cellY - 2, CELL_SIZE + 4, CELL_SIZE + 4);
		}
	}

	private Color getCellColor(boolean isUnlocked, Item item, boolean isDragging, Item draggedItem) {
		if (!isUnlocked)
			return new Color(60, 60, 60);
		if (isDragging && item == draggedItem)
			return Color.YELLOW;
		if (item == null)
			return Color.YELLOW;
		return Color.BLACK;
	}

	private void drawLock(Graphics2D g, int cellX, int cellY) {
		g.setColor(Color.DARK_GRAY);
		int lockSize = CELL_SIZE / 3;
		g.fillRect(cellX + lockSize, cellY + lockSize, lockSize, lockSize);
	}

	private void drawBackpackItems(Graphics2D g, boolean isDragging, Item draggedItem) {
		Item[][] grid = backpack.grid();
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				Item item = grid[y][x];
				if (item == null || (isDragging && item == draggedItem) || !backpack.isUnlocked(x, y))
					continue;

				if (isItemTopLeft(grid, x, y, item)) {
					int cellX = BACKPACK_ORIGIN_X + x * (CELL_SIZE + PADDING);
					int cellY = BACKPACK_ORIGIN_Y + y * (CELL_SIZE + PADDING);
					drawItem(g, item, cellX, cellY);
				}
			}
		}
	}

	// ===================== EXPANSION MODE =====================
	private void drawBackPackWithExpansion(Graphics2D g, List<Integer> selectedSlots,
			BackpackExpansionSystem expansionSystem) {
		drawExpansionInstructions(g, expansionSystem);
		drawExpansionCells(g, expansionSystem);
		drawExpansionItems(g);
	}

	private void drawExpansionInstructions(Graphics2D g, BackpackExpansionSystem expansionSystem) {
		g.setColor(Color.WHITE);
		g.drawString("EXPANSION DU SAC - " + expansionSystem.getPendingUnlocks() + " case(s) à débloquer",
				BACKPACK_ORIGIN_X, BACKPACK_ORIGIN_Y - 30);
		g.drawString("Cliquez sur une case verte pour la débloquer", BACKPACK_ORIGIN_X, BACKPACK_ORIGIN_Y - 10);
		g.setColor(Color.YELLOW);
		g.drawString("(ESPACE pour passer)", BACKPACK_ORIGIN_X,
				BACKPACK_ORIGIN_Y + (backpack.height() + 1) * (CELL_SIZE + PADDING));
	}

	private void drawExpansionCells(Graphics2D g, BackpackExpansionSystem expansionSystem) {
		Item[][] grid = backpack.grid();
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				drawExpansionCell(g, x, y, grid[y][x], expansionSystem);
			}
		}
	}

	private void drawExpansionCell(Graphics2D g, int x, int y, Item item, BackpackExpansionSystem expansionSystem) {
		int cellX = BACKPACK_ORIGIN_X + x * (CELL_SIZE + PADDING);
		int cellY = BACKPACK_ORIGIN_Y + y * (CELL_SIZE + PADDING);

		boolean isUnlocked = backpack.isUnlocked(x, y);
		boolean isExpandable = expansionSystem.isExpansionAvailable(x, y);

		Color color = getExpansionCellColor(isUnlocked, isExpandable, item);
		g.setColor(color);
		g.fill(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));

		drawExpansionBorder(g, cellX, cellY, isExpandable);

		if (!isUnlocked && !isExpandable) {
			drawLock(g, cellX, cellY);
		}
	}

	private Color getExpansionCellColor(boolean isUnlocked, boolean isExpandable, Item item) {
		if (isExpandable)
			return new Color(0, 255, 0, 150);
		if (isUnlocked)
			return item == null ? Color.YELLOW : Color.BLACK;
		return new Color(80, 80, 80);
	}

	private void drawExpansionBorder(Graphics2D g, int cellX, int cellY, boolean isExpandable) {
		if (isExpandable) {
			g.setColor(Color.GREEN);
			g.setStroke(new java.awt.BasicStroke(3));
		} else {
			g.setColor(Color.BLACK);
			g.setStroke(new java.awt.BasicStroke(1));
		}
		g.draw(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));
		g.setStroke(new java.awt.BasicStroke(1));
	}

	private void drawExpansionItems(Graphics2D g) {
		Item[][] grid = backpack.grid();
		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				Item item = grid[y][x];
				if (item != null && backpack.isUnlocked(x, y) && isItemTopLeft(grid, x, y, item)) {
					int cellX = BACKPACK_ORIGIN_X + x * (CELL_SIZE + PADDING);
					int cellY = BACKPACK_ORIGIN_Y + y * (CELL_SIZE + PADDING);
					drawItem(g, item, cellX, cellY);
				}
			}
		}
	}

	// ===================== TREASURE CHEST =====================
	public void drawTreasureChest(Graphics2D g, Item[][] treasureGrid, List<Integer> selectedSlots, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY) {
		var coords = getTreasureCoords();

		g.setColor(Color.BLACK);
		g.drawString("Coffre au trésor :", coords[0], coords[1] - 10);

		drawTreasureCells(g, treasureGrid, coords, isDragging, draggedItem);
		drawTreasureItems(g, treasureGrid, coords, isDragging, draggedItem);

		if (isDragging && draggedItem != null) {
			drawDraggedItem(g, draggedItem, dragOffsetX, dragOffsetY);
		}
	}

	private int[] getTreasureCoords() {
		var info = context.getScreenInfo();
		int chestX = info.width() / 2 - 100;
		int chestY = info.height() / 3 - 75;
		return new int[] { chestX, chestY + 170 };
	}

	private void drawTreasureCells(Graphics2D g, Item[][] grid, int[] coords, boolean isDragging, Item draggedItem) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[0].length; x++) {
				drawTreasureCell(g, x, y, grid[y][x], coords, isDragging, draggedItem);
			}
		}
	}

	private void drawTreasureCell(Graphics2D g, int x, int y, Item item, int[] coords, boolean isDragging,
			Item draggedItem) {
		int cellX = coords[0] + x * (CELL_SIZE + PADDING);
		int cellY = coords[1] + y * (CELL_SIZE + PADDING);

		g.setColor(
				isDragging && item == draggedItem ? Color.ORANGE : (item == null ? Color.ORANGE : new Color(255, 200, 100)));
		g.fill(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));

		g.setColor(Color.BLACK);
		g.draw(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));
	}

	private void drawTreasureItems(Graphics2D g, Item[][] grid, int[] coords, boolean isDragging, Item draggedItem) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[0].length; x++) {
				Item item = grid[y][x];
				if (item != null && !(isDragging && item == draggedItem) && isItemTopLeft(grid, x, y, item)) {
					int cellX = coords[0] + x * (CELL_SIZE + PADDING);
					int cellY = coords[1] + y * (CELL_SIZE + PADDING);
					drawItem(g, item, cellX, cellY);
				}
			}
		}
	}

	// ===================== MERCHANT =====================
	public void drawMerchantStock(Graphics2D g, Item[][] merchantGrid, List<Integer> selectedSlots, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY) {
		var coords = getTreasureCoords(); // Même position que trésor

		drawMerchantHeader(g, coords);
		drawMerchantCells(g, merchantGrid, coords, isDragging, draggedItem);
		drawMerchantItems(g, merchantGrid, coords, isDragging, draggedItem);

		if (isDragging && draggedItem != null) {
			drawDraggedItem(g, draggedItem, dragOffsetX, dragOffsetY);
		}
	}

	private void drawMerchantHeader(Graphics2D g, int[] coords) {
		g.setColor(Color.WHITE);
		g.drawString("Stock du Marchand :", coords[0], coords[1] - 30);
		g.setColor(Color.CYAN);
		g.drawString("Cliquez pour acheter | Cliquez sur votre sac pour vendre", coords[0], coords[1] - 10);
	}

	private void drawMerchantCells(Graphics2D g, Item[][] grid, int[] coords, boolean isDragging, Item draggedItem) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[0].length; x++) {
				drawMerchantCell(g, x, y, grid[y][x], coords, isDragging, draggedItem);
			}
		}
	}

	private void drawMerchantCell(Graphics2D g, int x, int y, Item item, int[] coords, boolean isDragging,
			Item draggedItem) {
		int cellX = coords[0] + x * (CELL_SIZE + PADDING);
		int cellY = coords[1] + y * (CELL_SIZE + PADDING);

		g.setColor(isDragging && item == draggedItem ? new Color(100, 200, 255)
				: (item == null ? new Color(100, 200, 255) : new Color(150, 220, 255)));
		g.fill(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));

		g.setColor(Color.BLACK);
		g.draw(new Rectangle2D.Float(cellX, cellY, CELL_SIZE, CELL_SIZE));
	}

	private void drawMerchantItems(Graphics2D g, Item[][] grid, int[] coords, boolean isDragging, Item draggedItem) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[0].length; x++) {
				Item item = grid[y][x];
				if (item != null && !(isDragging && item == draggedItem) && isItemTopLeft(grid, x, y, item)) {
					int cellX = coords[0] + x * (CELL_SIZE + PADDING);
					int cellY = coords[1] + y * (CELL_SIZE + PADDING);
					drawMerchantItem(g, item, cellX, cellY);
				}
			}
		}
	}

	private void drawMerchantItem(Graphics2D g, Item item, int cellX, int cellY) {
		int itemW = item.width() * (CELL_SIZE + PADDING) - PADDING;
		int itemH = item.height() * (CELL_SIZE + PADDING) - PADDING;

		drawItemImage(g, item, cellX, cellY, itemW, itemH);
		drawItemPrice(g, item, cellX, cellY, itemW, itemH);
		drawStackQuantity(g, item, cellX, cellY, itemW, Color.CYAN);

		g.setColor(new Color(0, 0, 0, 150));
		g.drawRect(cellX, cellY, itemW, itemH);
	}

	private void drawItemPrice(Graphics2D g, Item item, int x, int y, int w, int h) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y + h - 18, w, 18);
		g.setColor(Color.YELLOW);
		g.drawString(item.price() + " 💰", x + 5, y + h - 5);
	}

	// ===================== ITEM RENDERING =====================
	private void drawItem(Graphics2D g, Item item, int cellX, int cellY) {
		int itemW = item.width() * (CELL_SIZE + PADDING) - PADDING;
		int itemH = item.height() * (CELL_SIZE + PADDING) - PADDING;

		drawItemImage(g, item, cellX, cellY, itemW, itemH);
		drawStackQuantity(g, item, cellX, cellY, itemW, Color.YELLOW);

		g.setColor(Color.BLACK);
		g.drawRect(cellX, cellY, itemW, itemH);
	}

	private void drawItemImage(Graphics2D g, Item item, int x, int y, int w, int h) {
		String key = null;
		var name = item.name().toLowerCase();
		var damage = item.attackValue();

		if (name.contains("sword") || name.contains("epee")) {
			key = item.isRotated() ? "sword90" : "sword";
		} else if (name.contains("bow") || name.contains("arc")) {
			key = item.isRotated() ? "bow90" : "bow";
		} else if (name.contains("shield") || name.contains("bouclier")) {
			key = "shield";
		}

		if (key != null && !key.equals("shield")) {
			if (damage >= 20) {
				key += "o";
			} else if (damage >= 10) {
				key += "v";
			}
		}

		BufferedImage img = (key != null) ? weaponAssets.get(key) : null;

		if (img != null) {
			g.drawImage(img, x, y, w, h, null);
		} else {
			g.setColor(new Color(0, 0, 0, 0));
		}
	}

	private void drawStackQuantity(Graphics2D g, Item item, int x, int y, int w, Color color) {
		if (item.isStackable() && item.quantity() > 1) {
			g.setColor(Color.BLACK);
			g.fillRect(x + w - 26, y + 2, 24, 16);
			g.setColor(color);
			g.drawString(String.valueOf(item.quantity()), x + w - 22, y + 14);
		}
	}

	private void drawDraggedItem(Graphics2D g, Item item, int mouseX, int mouseY) {
		int itemW = item.width() * (CELL_SIZE + PADDING) - PADDING;
		int itemH = item.height() * (CELL_SIZE + PADDING) - PADDING;

		g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.7f));

		drawItemImage(g, item, mouseX, mouseY, itemW, itemH);
		drawStackQuantity(g, item, mouseX, mouseY, itemW, Color.YELLOW);

		g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
		g.setColor(Color.BLACK);
		g.drawRect(mouseX, mouseY, itemW, itemH);
	}

	// ===================== FLOATING ITEMS =====================
	private void drawFloatingItems(Graphics2D g, List<FloatingItem> floatingItems) {
		for (FloatingItem fItem : floatingItems) {
			drawFloatingItem(g, fItem);
		}
	}

	private void drawFloatingItem(Graphics2D g, FloatingItem fItem) {
		Item item = fItem.item();
		int x = fItem.position.x;
		int y = fItem.position.y;
		int w = item.width() * (CELL_SIZE + PADDING) - PADDING;
		int h = item.height() * (CELL_SIZE + PADDING) - PADDING;

		g.setColor(Color.CYAN);
		g.fillRect(x, y, w, h);

		drawItemImage(g, item, x, y, w, h);

		g.setColor(Color.BLACK);
		g.drawRect(x, y, w, h);
	}

	// ===================== HEALTH BARS =====================
	private void drawHeroHealthBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int startX = info.width() - 250;
		int startY = 10;
		int barWidth = 200;

		double hpPercent = hero.hp() / (double) hero.HeroMaxHp();
		int hpBarWidth = (int) (hpPercent * barWidth);

		g.setColor(Color.WHITE);
		g.drawString("HERO", startX, startY + 15);

		g.setColor(Color.RED);
		g.fillRect(startX, startY + 20, barWidth, 15);

		g.setColor(Color.GREEN);
		g.fillRect(startX, startY + 20, Math.max(0, hpBarWidth), 15);

		g.setColor(Color.BLACK);
		g.drawRect(startX, startY + 20, barWidth, 15);
		g.drawString(hero.hp() + " HP", startX + barWidth / 2 - 20, startY + 32);
	}

	private void drawHeroManaBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int startX = info.width() - 250;
		int startY = 50;
		int barWidth = 200;

		g.setColor(Color.WHITE);
		g.drawString("MANA", startX, startY - 5);

		g.setColor(Color.DARK_GRAY);
		g.fillRect(startX, startY, barWidth, 15);

		g.setColor(Color.BLUE);
		g.fillRect(startX, startY, barWidth, 15);

		g.setColor(Color.BLACK);
		g.drawRect(startX, startY, barWidth, 15);
		g.drawString(hero.mana() + " MANA", startX + barWidth / 2 - 20, startY + 12);
	}

	private void drawHeroStaminaBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int startX = info.width() - 250;
		int startY = 50;
		int barWidth = 200;

		double staminaPercent = hero.currentStamina() / (double) hero.maxStamina();
		int staminaWidth = (int) (staminaPercent * barWidth);

		g.setColor(Color.WHITE);
		g.drawString("STAMINA", startX, startY - 5);

		g.setColor(Color.DARK_GRAY);
		g.fillRect(startX, startY, barWidth, 15);

		g.setColor(Color.ORANGE);
		g.fillRect(startX, startY, staminaWidth, 15);

		g.setColor(Color.BLACK);
		g.drawRect(startX, startY, barWidth, 15);
		g.drawString(hero.currentStamina() + " / " + hero.maxStamina(), startX + barWidth / 2 - 15, startY + 12);
	}

	private void drawEnemyHealthBars(Graphics2D g, List<Enemy> enemies) {
		if (enemies == null || enemies.isEmpty())
			return;

		var info = context.getScreenInfo();
		int startX = info.width() - 250;
		int startY = 90;
		int barWidth = 200;
		int spacing = 40;

		int currentY = startY;
		for (int i = 0; i < enemies.size(); i++) {
			Enemy enemy = enemies.get(i);
			if (enemy.isAlive()) {
				drawEnemyBar(g, enemy, i + 1, startX, currentY, barWidth);
				currentY += spacing;
			}
		}
	}

	private void drawEnemyBar(Graphics2D g, Enemy enemy, int num, int x, int y, int w) {
		double hpPercent = enemy.hp() / (double) enemy.maxHp();
		int hpBarWidth = (int) (hpPercent * w);

		g.setColor(Color.WHITE);
		g.drawString(enemy.name() + num, x, y + 15);

		g.setColor(Color.RED);
		g.fillRect(x, y + 20, w, 15);

		g.setColor(Color.MAGENTA);
		g.fillRect(x, y + 20, Math.max(0, hpBarWidth), 15);

		g.setColor(Color.WHITE);
		g.drawRect(x, y + 20, w, 15);
		g.drawString(enemy.hp() + " HP", x + w / 2 - 20, y + 32);
	}

	// ===================== UTILITIES =====================
	private boolean isItemTopLeft(Item[][] grid, int x, int y, Item item) {
		if (x > 0 && grid[y][x - 1] == item)
			return false;
		if (y > 0 && grid[y - 1][x] == item)
			return false;
		return true;
	}

	private void clearScreen(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
	}
}