package fr.uge.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
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

import fr.uge.backpack.BackPack;
import fr.uge.backpack.BackpackExpansionSystem;
import fr.uge.combat.Battle;
import fr.uge.enemy.Enemy;
import fr.uge.enemy.Hero;
import fr.uge.items.FloatingItem;
import fr.uge.items.Item;
import fr.uge.items.Malediction;
import fr.uge.map.MapDungeon;
import fr.uge.room.HealerRoom;
import fr.uge.room.Room;

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
	private static final List<BufferedImage> loadingAnimation = loadFrames(161, "loadingscreen", "loading", "jpg");
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
	private static BufferedImage merchantImage;
	private static BufferedImage healerRoomImage;

	private static Map<String, BufferedImage> weaponAssets = new HashMap<>();
	private static List<BufferedImage> fightingAnimation1;
	private static List<BufferedImage> fightingAnimation2;
	private static List<BufferedImage> fightingAnimation3;
	private static List<BufferedImage> corridorToCorridorAnimation;
	private static List<BufferedImage> merchantToCorridorAnimation;

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
		injuredEnemy = loadImage("injuredRat.jpg");
		merchantImage = loadImage("merchantback.png");
	}

	private static List<BufferedImage> loadFrames(int nbFrames, String folder, String name, String type) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames / 10; i++) { // a modifier le /10 !!!
			frames.add(loadImage("./" + folder + "/" + name + "(" + i + ")." + type));
			System.out.println("./" + folder + "/" + name + "(" + i + ").jpg");
		}
		return frames;
	}

	private static List<BufferedImage> loadAttackFrames(int nbEnemies, int nbFrames) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames / 10; i++) { // a modifier le /10 !!!
			if (i % 2 == 0) {
				frames.add(loadImage("./fighting" + nbEnemies + "/hit_(" + i + ").png"));
				System.out.println("Frame chargé : hit_(" + i + ").png");
			}
		}
		return frames;
	}

	private static void loadAnimations() {
		fightingAnimation1 = loadAttackFrames(1, 157);
		fightingAnimation2 = loadAttackFrames(2, 78);
		fightingAnimation3 = loadAttackFrames(3, 78);
		corridorToCorridorAnimation = loadFrames(160, "animationroom", "room", "jpg");
		merchantToCorridorAnimation = loadFrames(160, "animationmerchant", "merchant", "png");
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
						System.out.println("Weapon loaded : " + key);
					}
				}
			}
		} else {
			System.out.println("Error : Weapon folder not found");
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

	public void render(GameController controller, List<Integer> selectedSlots, boolean isDragging, Item draggedItem,
			int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void combatDisplay(GameController controller, int nbEnemies, int status, List<Integer> selectedSlots,
			Hero hero, List<Enemy> enemies, boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY,
			long lastAttackTime, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			if (isAnimationPlaying(lastAttackTime, 4500)) {
				drawCombatAnimation(g, nbEnemies, lastAttackTime);
			} else {
				drawCombatScene(g, nbEnemies, status, enemies);
			}
			drawAllBars(g, hero, enemies);
			drawEnemyActionBubbles(g, controller.getEnemyActions(), enemies);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);

		});
	}

	public void corridorDisplay(GameController controller, List<Integer> selectedSlots, Hero hero, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems, long lastChangeRoom,
			boolean fromMerchant, boolean fromCorridor) {
		context.renderFrame(g -> {
			clearScreen(g);
			int animationDuration = 3000;

			if (isAnimationPlaying(lastChangeRoom, animationDuration)) {
				List<BufferedImage> animToPlay = (controller.getPreviousRoomType() == Room.Type.MERCHANT)
						? merchantToCorridorAnimation
						: corridorToCorridorAnimation;
				if (animToPlay != null && !animToPlay.isEmpty()) {
					drawAnimation(g, lastChangeRoom, animationDuration, animToPlay);
				}
			} else {
				drawCorridor(g);
				drawHero(g);
			}

			drawAllHeroBars(g, hero);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void expansionDisplay(GameController controller, List<Integer> selectedSlots, Hero hero,
			BackpackExpansionSystem expansionSystem) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawEmptyRoom(g);
			drawAllHeroBars(g, hero);
			drawHero(g);
			drawGrid(g, controller);
			drawBackPackWithExpansion(g, selectedSlots, expansionSystem);
		});
	}

	public void merchantDisplay(GameController controller, List<Integer> selectedSlots, Item[][] merchantGrid, Hero hero,
			boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawMerchantBackground(g);
			drawAllHeroBars(g, hero);
			drawMerchantStock(g, merchantGrid, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void treasureDisplay(GameController controller, List<Integer> selectedSlots, Item[][] treasureGrid, Hero hero,
			boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawAllHeroBars(g, hero);
			drawTreasure(g);
			drawTreasureChest(g, treasureGrid, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void emptyRoomDisplay(GameController controller, List<Integer> selectedSlots, Hero hero, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawEmptyRoom(g);
			drawAllHeroBars(g, hero);
			drawHero(g);
			drawGrid(g, controller);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void healerDisplay(GameController controller, List<Integer> selectedSlots, Hero hero, HealerRoom healerRoom,
			boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawHealerRoom(g, hero, healerRoom);
			drawAllHeroBars(g, hero);
			drawGrid(g, controller);
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
		drawHeroStats(g, hero);
		drawEnemyHealthBars(g, enemies);
	}

	private void drawAllHeroBars(Graphics2D g, Hero hero) {
		drawHeroStats(g, hero);
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
	private void drawGrid(Graphics2D g, GameController controller) {
		var adjacents = floor.adjacentRooms();

		// Dessiner toutes les cases
		for (int i = 0; i < floor.rooms().size(); i++) {
			boolean isAdjacent = adjacents.contains(i);
			boolean isAccessible = floor.isRoomAccessible(i);

			drawRoomCell(g, i, floor.rooms().get(i), isAdjacent, isAccessible);
		}

		// Dessiner le joueur avec animation
		drawAnimatedPlayer(g, controller);

		// Dessiner le chemin si en cours
		if (controller.isFollowingPath()) {
			drawPath(g, controller.getCurrentPath(), controller.getPathIndex());
		}
	}

	private void drawPath(Graphics2D g, List<Integer> path, int currentIndex) {
		if (path == null || path.isEmpty())
			return;

		g.setColor(new Color(255, 255, 0, 100)); // Jaune transparent
		g.setStroke(new java.awt.BasicStroke(4));

		for (int i = currentIndex; i < path.size() - 1; i++) {
			int[] from = getCellPosition(path.get(i));
			int[] to = getCellPosition(path.get(i + 1));

			int fromX = from[0] + GRID_CELL_SIZE / 2;
			int fromY = from[1] + GRID_CELL_SIZE / 2;
			int toX = to[0] + GRID_CELL_SIZE / 2;
			int toY = to[1] + GRID_CELL_SIZE / 2;

			g.drawLine(fromX, fromY, toX, toY);
		}

		g.setStroke(new java.awt.BasicStroke(1));
	}

	private void drawAnimatedPlayer(Graphics2D g, GameController controller) {
		int currentIndex;
		int imgSize = GRID_CELL_SIZE / 2;

		if (controller.isPlayerMoving()) {
			// Animation en cours
			float progress = controller.getPlayerAnimationProgress();
			int startIdx = controller.getPlayerStartIndex();
			int targetIdx = controller.getPlayerTargetIndex();

			// Calculer les positions de départ et d'arrivée
			int[] startPos = getCellPosition(startIdx);
			int[] targetPos = getCellPosition(targetIdx);

			// Interpoler la position
			int x = (int) (startPos[0] + (targetPos[0] - startPos[0]) * progress);
			int y = (int) (startPos[1] + (targetPos[1] - startPos[1]) * progress);

			// Centrer l'icône
			int offset = (GRID_CELL_SIZE - imgSize) / 2;
			x += offset;
			y += offset;

			g.setColor(Color.RED);
			g.drawImage(heroImage, x, y, imgSize, imgSize, null);
		} else {
			// Position statique
			currentIndex = floor.playerIndex();
			int[] pos = getCellPosition(currentIndex);
			int offset = (GRID_CELL_SIZE - imgSize) / 2;

			g.setColor(Color.RED);
			g.drawImage(heroImage, pos[0] + offset, pos[1] + offset, imgSize, imgSize, null);
		}
	}

	private int[] getCellPosition(int index) {
		int row = index / GRID_COLS;
		int col = index % GRID_COLS;
		int x = GRID_PADDING + col * (GRID_CELL_SIZE + GRID_PADDING);
		int y = GRID_PADDING + row * (GRID_CELL_SIZE + GRID_PADDING);
		return new int[] { x, y };
	}

	private void drawRoomCell(Graphics2D g, int index, Room room, boolean isAdjacent, boolean isAccessible) {
		int row = index / GRID_COLS;
		int col = index % GRID_COLS;
		int x = GRID_PADDING + col * (GRID_CELL_SIZE + GRID_PADDING);
		int y = GRID_PADDING + row * (GRID_CELL_SIZE + GRID_PADDING);

		Color color;
		if (isAdjacent) {
			color = Color.GREEN;
		} else if (isAccessible && index != floor.playerIndex()) {
			color = new Color(100, 255, 100); // Vert clair pour accessible
		} else {
			color = getRoomColor(room);
		}

		g.setColor(color);
		g.fill(new Rectangle2D.Float(x, y, GRID_CELL_SIZE, GRID_CELL_SIZE));

		g.setColor(Color.BLACK);
		g.draw(new Rectangle2D.Float(x, y, GRID_CELL_SIZE, GRID_CELL_SIZE));

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

	private void drawMerchantBackground(Graphics2D g) {
		var info = context.getScreenInfo();
		BufferedImage img = merchantImage;
		g.drawImage(img, 0, 0, info.width(), info.height(), null);

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

//===================== HEALERROOM=====================

	private void drawHealerRoom(Graphics2D g, Hero hero, HealerRoom healerRoom) {
		var info = context.getScreenInfo();

		// Background (utiliser healerRoomImage si disponible, sinon couleur)
		if (healerRoomImage != null) {
			g.drawImage(healerRoomImage, 0, 0, info.width(), info.height(), null);
		} else {
			g.setColor(new Color(100, 150, 100));
			g.fillRect(0, 0, info.width(), info.height());
		}

		// Bouton de soin
		int buttonWidth = 200;
		int buttonHeight = 80;
		int buttonX = (info.width() - buttonWidth) / 2;
		int buttonY = (info.height() - buttonHeight) / 2;

		boolean canHeal = healerRoom.canHeal(hero);

		// Fond du bouton
		g.setColor(canHeal ? new Color(100, 200, 100) : new Color(150, 150, 150));
		g.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 20, 20);

		// Bordure
		g.setColor(canHeal ? new Color(50, 150, 50) : new Color(100, 100, 100));
		g.setStroke(new java.awt.BasicStroke(3));
		g.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 20, 20);
		g.setStroke(new java.awt.BasicStroke(1));

		// Texte
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(20f));
		String text = "❤️ SE SOIGNER";
		g.drawString(text, buttonX + 30, buttonY + 35);

		g.setFont(g.getFont().deriveFont(14f));
		String cost = healerRoom.getHealCost() + " 💰 → +" + healerRoom.getHealAmount() + " HP";
		g.drawString(cost, buttonX + 40, buttonY + 60);

		// Message si impossible
		if (!canHeal) {
			g.setFont(g.getFont().deriveFont(16f));
			g.setColor(Color.YELLOW);
			String msg = !hero.hasEnoughGold(healerRoom.getHealCost()) ? "Pas assez d'or" : "HP au maximum";
			g.drawString(msg, buttonX + 40, buttonY + buttonHeight + 30);
		}
	}

	// ===================== ITEM RENDERING =====================
	private void drawItem(Graphics2D g, Item item, int cellX, int cellY) {
		int itemW = item.width() * (CELL_SIZE + PADDING) - PADDING;
		int itemH = item.height() * (CELL_SIZE + PADDING) - PADDING;

		// Pour les malédictions dans le sac, on garde l'ancien rendu (rectangle simple)
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
		} else if (name.contains("hachette")) {
			key = item.isRotated() ? "hache90" : "hache";
		} else if (name.contains("gold")) {
			key = "gold";
		} else if (name.contains("heal")) {
			key = "heal";
		} else if (name.contains("ration")) {
			key = "ration";
		}

		if (key != null && !key.equals("shield") && !key.equals("gold") && !key.equals("heal") && !key.equals("ration")) {
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

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

		if (item.isMalediction()) {
			drawMaledictionShape(g, item, mouseX, mouseY, itemW, itemH, true);
		} else {
			drawItemImage(g, item, mouseX, mouseY, itemW, itemH);
			drawStackQuantity(g, item, mouseX, mouseY, itemW, Color.YELLOW);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
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

		if (item.isMalediction()) {
			drawMaledictionShape(g, item, x, y, 0, 0, true);
		} else {
			int w = item.width() * (CELL_SIZE + PADDING) - PADDING;
			int h = item.height() * (CELL_SIZE + PADDING) - PADDING;

			g.setColor(Color.CYAN);
			g.fillRect(x, y, w, h);
			drawItemImage(g, item, x, y, w, h);
			g.setColor(Color.BLACK);
			g.drawRect(x, y, w, h);
		}
	}

	// ===================== STATS BARS =====================
	private void drawHeroStats(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();

		int barHeight = 15;
		int sideBarWidth = 300;
		int startX = info.width() - sideBarWidth - 25;
		int currentY = 30;

		drawBar(g, startX, currentY, sideBarWidth, barHeight, hero.hp(), hero.HeroMaxHp(), Color.RED, Color.GREEN, "HERO",
				hero.hp() + " HP");

		int expBarHeight = 20;
		int expY = info.height() - expBarHeight - 10;

//		currentY += 40;

//		drawBar(g, startX, currentY, sideBarWidth, barHeight, hero.mana(), 100, Color.DARK_GRAY, Color.BLUE, "MANA",
//				hero.mana() + " MANA");

		currentY += 40;

		drawBar(g, startX, currentY, sideBarWidth, barHeight, hero.currentStamina(), hero.maxStamina(), Color.DARK_GRAY,
				Color.ORANGE, "STAMINA", hero.currentStamina() + " / " + hero.maxStamina());
		int currentLevel = hero.lvl(hero.exp());

		float startExp = hero.getXpForLevel(currentLevel);

		float endExp;
		if (currentLevel >= 5) {
			endExp = hero.maxExp();
		} else {
			endExp = hero.getXpForLevel(currentLevel + 1);
		}

		float currentExpInLevel = hero.exp() - startExp;
		float totalExpNeededForLevel = endExp - startExp;

		if (totalExpNeededForLevel <= 0)
			totalExpNeededForLevel = 1;

		drawBar(g, 0, expY, info.width(), expBarHeight, currentExpInLevel, // 5
				totalExpNeededForLevel, // 20
				Color.BLACK, new Color(255, 215, 0), null,
				"Level : " + currentLevel + " | XP: " + (int) currentExpInLevel + " / " + (int) totalExpNeededForLevel);

	}

	private void drawBar(Graphics2D g, int x, int y, int width, int height, double current, double max, Color bgColor,
			Color fillColor, String label, String valueText) {

		double percent = Math.max(0, Math.min(1.0, current / max));
		int fillWidth = (int) (percent * width);

		if (label != null) {
			g.setColor(Color.WHITE);
			g.drawString(label, x, y - 5);
		}

		g.setColor(bgColor);
		g.fillRect(x, y, width, height);

		g.setColor(fillColor);
		g.fillRect(x, y, fillWidth, height);

		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);

		if (valueText != null) {
			g.setColor(Color.WHITE);
			int textWidth = g.getFontMetrics().stringWidth(valueText);
			int textX = x + (width - textWidth) / 2;
			int textY = y + height - 2;

			g.setColor(Color.BLACK);
			g.drawString(valueText, textX + 1, textY + 1);

			g.setColor(Color.WHITE);
			g.drawString(valueText, textX, textY);
		}
	}

	private void drawEnemyHealthBars(Graphics2D g, List<Enemy> enemies) {
		if (enemies == null || enemies.isEmpty())
			return;

		var info = context.getScreenInfo();
		int startX = info.width() - 250;
		int startY = 200;
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

	private void drawEnemyActionBubbles(Graphics2D g, List<Battle.EnemyAction> actions, List<Enemy> enemies) {
		if (actions == null || actions.isEmpty()) {
			return;
		}

		var info = context.getScreenInfo();
		int bubbleWidth = 100;
		int bubbleHeight = 70;
		int startY = 100;
		int padding = 10;

		// Calculer l'espacement horizontal
		int totalWidth = Math.min(actions.size(), 3) * (bubbleWidth + 20);
		int startX = (info.width() - totalWidth) / 2;

		for (int i = 0; i < Math.min(actions.size(), enemies.size()); i++) {
			Enemy enemy = enemies.get(i);
			if (enemy.isAlive()) {
				Battle.EnemyAction action = actions.get(i);
				int bubbleX = startX + i * (bubbleWidth + 20);

				// Bulle
				g.setColor(new Color(255, 255, 255, 230));
				g.fillRoundRect(bubbleX, startY, bubbleWidth, bubbleHeight, 15, 15);

				// Bordure
				Color borderColor = switch (action) {
				case ATTACK -> Color.RED;
				case DEFEND -> Color.BLUE;
				case MALEDICTION -> new Color(150, 50, 200);
				};
				g.setColor(borderColor);
				g.setStroke(new java.awt.BasicStroke(2));
				g.drawRoundRect(bubbleX, startY, bubbleWidth, bubbleHeight, 15, 15);
				g.setStroke(new java.awt.BasicStroke(1));

				// Icône
				g.setFont(g.getFont().deriveFont(28f));
				String icon = switch (action) {
				case ATTACK -> "⚔️"; // faire des images ici
				case DEFEND -> "🛡️";
				case MALEDICTION -> "☠️";
				};
				g.drawString(icon, bubbleX + padding, startY + 30);

				// Texte
				g.setFont(g.getFont().deriveFont(14f));
				g.setColor(Color.BLACK);
				String text = switch (action) {
				case ATTACK -> "ATK";
				case DEFEND -> "DEF";
				case MALEDICTION -> "CURSE";
				};
				g.drawString(text, bubbleX + 50, startY + 25);

				// Dégâts si attaque
				if (action == Battle.EnemyAction.ATTACK) {
					String dmgText = enemy.attackDamage() + " DMG";
					g.setFont(g.getFont().deriveFont(12f));
					g.setColor(Color.DARK_GRAY);
					g.drawString(dmgText, bubbleX + 50, startY + 45);
				}
			}
		}
	}

	private void drawMaledictionShape(Graphics2D g, Item item, int x, int y, int w, int h, boolean isFloating) {
		if (!item.isMalediction()) {
			drawItemImage(g, item, x, y, w, h);
			return;
		}

		Malediction malediction = (Malediction) item;

		int cellSize = isFloating ? CELL_SIZE : CELL_SIZE;
		int spacing = isFloating ? PADDING : 0;

		Color maledictionColor = isFloating ? new Color(150, 50, 200, 180) : new Color(120, 30, 180);

		for (int dy = 0; dy < item.height(); dy++) {
			for (int dx = 0; dx < item.width(); dx++) {
				if (malediction.occupies(dx, dy)) {
					int cellX = x + dx * (cellSize + spacing);
					int cellY = y + dy * (cellSize + spacing);

					g.setColor(maledictionColor);
					g.fillRect(cellX, cellY, cellSize, cellSize);

					g.setColor(new Color(200, 100, 255));
					g.setStroke(new java.awt.BasicStroke(2));
					g.drawRect(cellX, cellY, cellSize, cellSize);
					g.setStroke(new java.awt.BasicStroke(1));

					if (dx == 1 && dy == 0) {
						g.setColor(Color.WHITE);
						g.setFont(g.getFont().deriveFont(28f));
						g.drawString("☠️", cellX + 8, cellY + 38);
						g.setFont(g.getFont().deriveFont(12f));
					}
				}
			}
		}
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