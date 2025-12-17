package fr.uge.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.forax.zen.ApplicationContext;

import fr.uge.implement.BackPack;
import fr.uge.implement.BackpackExpansionSystem;
import fr.uge.implement.Enemy;
import fr.uge.implement.FloatingItem;
import fr.uge.implement.Hero;
import fr.uge.implement.Item;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.Room;

public record GameView(ApplicationContext context, MapDungeon floor, BackPack backpack) {

	private static final List<BufferedImage> loadingAnimation = loadFrames(161);
	private static BufferedImage corridorImage;
	private static BufferedImage treasureRoomImage;
	private static BufferedImage treasureImage;
	private static BufferedImage heroImage;
	private static BufferedImage heroImage2;
	private static BufferedImage enemyRoomImage0;
	private static BufferedImage enemyRoomImage1;
	private static BufferedImage enemyRoomImage2;
	private static BufferedImage enemyRoomImage3;
	private static BufferedImage enemyRoomImage4;
	private static BufferedImage attackOrDefenseBanner;
	private static BufferedImage attackBanner;
	private static BufferedImage defendBanner;
	private static BufferedImage sword90;
	private static BufferedImage sword180;
	private static BufferedImage shield;
	private static BufferedImage injuredEnemy;
	private static BufferedImage swordv;
	private static BufferedImage swordj;

	// Listes d'animations de combat (on les initialise plus tard aussi)
	private static List<BufferedImage> fightingAnnimation1;
	private static List<BufferedImage> fightingAnnimation2;
	private static List<BufferedImage> fightingAnnimation3;

	private static BufferedImage loadImage(String fileName) {
		try {
			File file = Path.of("./data", fileName).toFile();
			BufferedImage result = ImageIO.read(file);
			if (result == null) {
				throw new IllegalArgumentException("Error when loading image file: " + fileName);
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data file: " + fileName, e);
		}
	}

	public static void loadGameAssets() {
		System.out.println("Chargement des assets du jeu...");
		if (corridorImage != null)
			return; // Déjà chargé

		corridorImage = loadImage("corridor2.png");
		treasureRoomImage = loadImage("treasureroom.png");
		treasureImage = loadImage("treasure.png");
		heroImage = loadImage("hero.png");
		heroImage2 = loadImage("hero2.png");
		enemyRoomImage0 = loadImage("fight0.png");
		enemyRoomImage1 = loadImage("fight1.png");
		enemyRoomImage2 = loadImage("fight2.png");
		enemyRoomImage3 = loadImage("fight3.png");
		enemyRoomImage4 = loadImage("fight3.png");
		attackOrDefenseBanner = loadImage("attackdefend.png");
		attackBanner = loadImage("attack.png");
		defendBanner = loadImage("defend.png");
		sword90 = loadImage("./weapons/sword90.png");
		sword180 = loadImage("./weapons/sword180.png");
		shield = loadImage("./weapons/shield.png");
		injuredEnemy = loadImage("./injuredRat.jpg");
		swordv = loadImage("./weapons/sword90v.png");
		swordj = loadImage("./weapons/sword90o.png");

		// Chargement des animations lourdes
		fightingAnnimation1 = loadAttackFrames(1, 157);
		fightingAnnimation2 = loadAttackFrames(2, 78);
		fightingAnnimation3 = loadAttackFrames(3, 78);

		System.out.println("Assets chargés !");
	}

	public void loadingDisplay(long startTime) {
		int duration = 8000;
		context.renderFrame(g -> {
			clearScreen(g);
			long currentTime = System.currentTimeMillis();
			long totalElapsed = currentTime - startTime;
			long loopElapsed = totalElapsed % duration;
			long loopStartTime = currentTime - loopElapsed;

			drawAnimation(g, loopStartTime, duration, loadingAnimation);
		});
	}

	private static List<BufferedImage> loadFrames(int nbFrames) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames; i++) {
			frames.add(loadImage("./loadingscreen" + "/loading(" + i + ").jpg"));
		}
		return frames;
	}

	private static List<BufferedImage> loadAttackFrames(int nbEnemies, int nbFrames) {
		List<BufferedImage> frames = new ArrayList<>();
		for (int i = 1; i < nbFrames; i++) {
			frames.add(loadImage("./fighting" + nbEnemies + "/hit_(" + i + ").png"));
		}
		return frames;
	}

	public void loadingDisplay() {
		int totalTime = 8000;
		context.renderFrame(g -> {
			clearScreen(g);
			drawAnimation(g, 0, totalTime, loadingAnimation);
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

	public void combatDisplay(int nb_enemies, int status, List<Integer> selectedSlots, Hero hero, List<Enemy> enemies,
			boolean isDragging, Item draggedItem, int dragOffsetX, int dragOffsetY, long lastAttackTime) {
		context.renderFrame(g -> {
			clearScreen(g);
			int totalTime = 4500;
			long timeElapsed = System.currentTimeMillis() - lastAttackTime;
			if (timeElapsed < totalTime) {
				drawAnimation(g, lastAttackTime, totalTime, switch (nb_enemies) {
				case 1 -> fightingAnnimation1;
				case 2 -> fightingAnnimation2;
				case 3 -> fightingAnnimation3;
				default -> fightingAnnimation1;
				});
				drawHeroManaBar(g, hero);
				drawHeroHealthBar(g, hero);
				drawHeroStaminaBar(g, hero);
				drawEnemyHealthBars(g, enemies);
				drawHeroHealthBar(g, hero);
				drawGrid(g);
				drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			} else {
				if (nb_enemies > 0) {
					if (enemies.get(0).hp() < enemies.get(0).maxHp()) {
						drawInjuredRat(g, nb_enemies, status);
					} else {
						drawCombat(g, nb_enemies, status);
					}
				}
				drawHeroManaBar(g, hero);
				drawHeroHealthBar(g, hero);
				drawHeroStaminaBar(g, hero);
				drawEnemyHealthBars(g, enemies);
				drawHeroHealthBar(g, hero);
				drawGrid(g);
				drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			}
		});
	}

	public void expansionDisplay(List<Integer> selectedSlots, Hero hero, BackpackExpansionSystem expansionSystem) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawEmptyRoom(g);
			drawHeroStaminaBar(g, hero);
			drawHeroManaBar(g, hero);
			drawHeroHealthBar(g, hero);
			drawHero(g);
			drawGrid(g);
			drawBackPackWithExpansion(g, selectedSlots, expansionSystem);
		});
	}

	public void corridorDisplay(List<Integer> selectedSlots, Hero hero, boolean isDragging, Item draggedItem,
			int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawCorridor(g);
			drawHeroStaminaBar(g, hero);
			drawHeroManaBar(g, hero);
			drawHeroHealthBar(g, hero);
			drawHero(g);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	public void treasureDisplay(List<Integer> selectedSlots, Item[][] treasureGrid, Hero hero, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY, List<FloatingItem> floatingItems) {
		context.renderFrame(g -> {
			clearScreen(g);
			drawHeroStaminaBar(g, hero);
			drawHeroManaBar(g, hero);
			drawHeroHealthBar(g, hero);
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
			drawHeroStaminaBar(g, hero);
			drawHeroManaBar(g, hero);
			drawHeroHealthBar(g, hero);
			drawHero(g);
			drawGrid(g);
			drawBackPack(g, selectedSlots, isDragging, draggedItem, dragOffsetX, dragOffsetY);
			drawFloatingItems(g, floatingItems);
		});
	}

	private void drawEmptyRoom(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(enemyRoomImage0, 0, 0, info.width(), info.height(), null);
	}

	private void drawTreasure(Graphics2D g) {
		var info = context.getScreenInfo();
		int width = info.width();
		int height = info.height();
		g.drawImage(treasureRoomImage, 0, 0, width, height, null);
		g.drawImage(treasureImage, width / 2, height / 2, width / 2, height / 2, null);
	}

	private void drawInjuredRat(Graphics2D g, int nb_enemies, int status) {
		var info = context.getScreenInfo();
		int width = info.width();
		int height = info.height();

		g.drawImage(switch (nb_enemies) {
		case 0 -> enemyRoomImage0;
		case 1 -> injuredEnemy;
		case 2 -> enemyRoomImage2;
		case 3 -> enemyRoomImage3;
		default -> enemyRoomImage3;
		}, 0, 0, width, height, null);

		int bannerWidth = width / 5;
		int bannerHeight = height / 5;
		int x = (width - bannerWidth) / 2;
		int y = height - bannerHeight - height / 50;

		switch (status) {
		case 0 -> g.drawImage(attackOrDefenseBanner, x, y, bannerWidth, bannerHeight, null);
		case 1 -> g.drawImage(attackBanner, x, y, bannerWidth, bannerHeight, null);
		case 2 -> g.drawImage(defendBanner, x, y, bannerWidth, bannerHeight, null);
		default -> throw new IllegalArgumentException("Unexpected status: " + status);
		}
	}

	private void drawCombat(Graphics2D g, int nb_enemies, int status) {
		var info = context.getScreenInfo();
		int width = info.width();
		int height = info.height();

		g.drawImage(switch (nb_enemies) {
		case 0 -> enemyRoomImage0;
		case 1 -> enemyRoomImage1;
		case 2 -> enemyRoomImage2;
		case 3 -> enemyRoomImage3;
		default -> enemyRoomImage3;
		}, 0, 0, width, height, null);

		int bannerWidth = width / 5;
		int bannerHeight = height / 5;
		int x = (width - bannerWidth) / 2;
		int y = height - bannerHeight - height / 50;

		switch (status) {
		case 0 -> g.drawImage(attackOrDefenseBanner, x, y, bannerWidth, bannerHeight, null);
		case 1 -> g.drawImage(attackBanner, x, y, bannerWidth, bannerHeight, null);
		case 2 -> g.drawImage(defendBanner, x, y, bannerWidth, bannerHeight, null);
		default -> throw new IllegalArgumentException("Unexpected status: " + status);
		}
	}

	private void drawBackPackWithExpansion(Graphics2D graphics, List<Integer> selectedSlots,
			BackpackExpansionSystem expansionSystem) {
		int originX = 20;
		int originY = 550;
		int cols = backpack.width();
		int cellSize = 60;
		int padding = 8;
		Item[][] grid = backpack.grid();

		var availableExpansions = expansionSystem.getAvailableExpansions();

		graphics.setColor(Color.WHITE);
		graphics.drawString("EXPANSION DU SAC - " + expansionSystem.getPendingUnlocks() + " case(s) à débloquer", originX,
				originY - 30);
		graphics.drawString("Cliquez sur une case verte pour la débloquer", originX, originY - 10);
		graphics.setColor(Color.YELLOW);
		graphics.drawString("(ESPACE pour passer)", originX, originY + (backpack.height() + 1) * (cellSize + padding));

		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				int cellX = originX + x * (cellSize + padding);
				int cellY = originY + y * (cellSize + padding);

				boolean isUnlocked = backpack.isUnlocked(x, y);
				boolean isExpandable = expansionSystem.isExpansionAvailable(x, y);
				Item item = grid[y][x];

				if (isExpandable) {
					graphics.setColor(new Color(0, 255, 0, 150));
				} else if (isUnlocked) {
					graphics.setColor(item == null ? Color.YELLOW : Color.BLACK);
				} else {
					graphics.setColor(new Color(80, 80, 80));
				}
				graphics.fill(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));

				if (isExpandable) {
					graphics.setColor(Color.GREEN);
					graphics.setStroke(new java.awt.BasicStroke(3));
				} else {
					graphics.setColor(Color.BLACK);
					graphics.setStroke(new java.awt.BasicStroke(1));
				}
				graphics.draw(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));
				graphics.setStroke(new java.awt.BasicStroke(1));

				if (!isUnlocked && !isExpandable) {
					graphics.setColor(Color.DARK_GRAY);
					graphics.fillRect(cellX + cellSize / 3, cellY + cellSize / 3, cellSize / 3, cellSize / 3);
				}
			}
		}

		for (int y = 0; y < backpack.height(); y++) {
			for (int x = 0; x < backpack.width(); x++) {
				Item item = grid[y][x];
				if (item == null || !backpack.isUnlocked(x, y))
					continue;

				boolean isTopLeft = true;
				if (x > 0 && grid[y][x - 1] == item)
					isTopLeft = false;
				if (y > 0 && grid[y - 1][x] == item)
					isTopLeft = false;

				if (isTopLeft) {
					int cellX = originX + x * (cellSize + padding);
					int cellY = originY + y * (cellSize + padding);
					int finalWidth = item.width() * (cellSize + padding) - padding;
					int finalHeight = item.height() * (cellSize + padding) - padding;

					if (item.name().contains("Sword") || item.name().contains("Epee")) {
						BufferedImage img = item.isRotated() ? sword180 : sword90;
						graphics.drawImage(img, cellX, cellY, finalWidth, finalHeight, null);
					} else if (item.name().contains("Shield") || item.name().contains("Bouclier")) {
						graphics.drawImage(shield, cellX, cellY, finalWidth, finalHeight, null);
					} else {
						graphics.setColor(Color.BLACK);
						String name = item.name();
						if (name.length() > 8)
							name = name.substring(0, 7) + "...";
						graphics.drawString(name, cellX + 5, cellY + cellSize / 2);
					}
				}
			}
		}
	}

	private void drawAnimation(Graphics2D g, long startTime, int duration, List<BufferedImage> name) {
		long currentTime = System.currentTimeMillis();
		long timeElapsed = currentTime - startTime;

		if (timeElapsed > duration) {
			return;
		}

		int totalFrames = name.size();
		int frameIndex = (int) ((timeElapsed * totalFrames) / duration);

		if (frameIndex >= totalFrames) {
			frameIndex = totalFrames - 1;
		}

		var info = context.getScreenInfo();
		g.drawImage(name.get(frameIndex), 0, 0, info.width(), info.height(), null);
	}

	private void drawHero(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(heroImage2, info.width() / 4, info.height() / 4, info.width(), info.height(), null);
	}

	private void drawCorridor(Graphics2D g) {
		var info = context.getScreenInfo();
		g.drawImage(corridorImage, 0, 0, info.width(), info.height(), null);
	}

	private void drawGrid(Graphics2D graphics) {
		int cols = 4;
		int cellSize = 120;
		int padding = 10;
		var adjacents = floor.adjacentRooms();

		for (int i = 0; i < floor.rooms().size(); i++) {
			Room room = floor.rooms().get(i);
			int row = i / cols;
			int col = i % cols;
			int x = padding + col * (cellSize + padding);
			int y = padding + row * (cellSize + padding);

			Color color = switch (room.type()) {
			case ENEMY -> new Color(200, 80, 80);
			case TREASURE -> new Color(250, 220, 80);
			case MERCHANT -> new Color(80, 180, 250);
			case HEALER -> new Color(100, 220, 100);
			case EXIT -> new Color(180, 80, 250);
			default -> new Color(180, 180, 180);
			};

			if (adjacents.contains(i))
				graphics.setColor(Color.GREEN);
			else
				graphics.setColor(color);
			graphics.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));

			graphics.setColor(Color.BLACK);
			graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));

			if (i == floor.playerIndex()) {
				graphics.setColor(Color.RED);
				int imgSize = cellSize / 2;
				graphics.drawImage(heroImage, x + (cellSize - imgSize) / 2, y + (cellSize - imgSize) / 2, imgSize, imgSize,
						null);
			}

			graphics.setColor(Color.BLACK);
			graphics.drawString(room.name(), x + 8, y + cellSize / 2);
		}
	}

	private void drawBackPack(Graphics2D graphics, List<Integer> selectedSlots, boolean isDragging, Item draggedItem,
      int dragOffsetX, int dragOffsetY) {

  int originX = 20;
  int originY = 550;
  int cols = backpack.width();
  int cellSize = 60;
  int padding = 8;
  Item[][] grid = backpack.grid();

  graphics.setColor(Color.BLACK);
  graphics.drawString("Backpack :", originX, originY - 10);

  // =======================
  // DESSIN DES CASES
  // =======================
  for (int y = 0; y < backpack.height(); y++) {
      for (int x = 0; x < backpack.width(); x++) {

          int cellX = originX + x * (cellSize + padding);
          int cellY = originY + y * (cellSize + padding);
          Item item = grid[y][x];
          boolean isUnlocked = backpack.isUnlocked(x, y);

          if (!isUnlocked) {
              graphics.setColor(new Color(60, 60, 60));
          } else if (isDragging && item == draggedItem) {
              graphics.setColor(Color.YELLOW);
          } else if (item == null) {
              graphics.setColor(Color.YELLOW);
          } else {
              graphics.setColor(Color.BLACK);
          }

          graphics.fill(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));

          graphics.setColor(item != null && item != draggedItem ? Color.GRAY : Color.BLACK);
          graphics.draw(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));

          if (!isUnlocked) {
              graphics.setColor(Color.DARK_GRAY);
              int lockSize = cellSize / 3;
              graphics.fillRect(cellX + lockSize, cellY + lockSize, lockSize, lockSize);
          }

          int slot = y * cols + x;
          if (item == null && selectedSlots != null && selectedSlots.contains(slot) && isUnlocked) {
              graphics.setColor(Color.RED);
              graphics.drawRect(cellX - 2, cellY - 2, cellSize + 4, cellSize + 4);
          }
      }
  }

  // =======================
  // DESSIN DES ITEMS
  // =======================
  for (int y = 0; y < backpack.height(); y++) {
      for (int x = 0; x < backpack.width(); x++) {

          Item item = grid[y][x];
          if (item == null || (isDragging && item == draggedItem) || !backpack.isUnlocked(x, y))
              continue;

          boolean isTopLeft = true;
          if (x > 0 && grid[y][x - 1] == item) isTopLeft = false;
          if (y > 0 && grid[y - 1][x] == item) isTopLeft = false;

          if (!isTopLeft) continue;

          int cellX = originX + x * (cellSize + padding);
          int cellY = originY + y * (cellSize + padding);
          int finalWidth = item.width() * (cellSize + padding) - padding;
          int finalHeight = item.height() * (cellSize + padding) - padding;

          if (item.name().contains("Sword") || item.name().contains("Epee")) {
              BufferedImage img = item.isRotated() ? sword180 : sword90;
              graphics.drawImage(img, cellX, cellY, finalWidth, finalHeight, null);
          } else if (item.name().contains("Shield") || item.name().contains("Bouclier")) {
              graphics.drawImage(shield, cellX, cellY, finalWidth, finalHeight, null);
          } else {
              graphics.setColor(Color.BLACK);
              graphics.drawRect(cellX, cellY, finalWidth, finalHeight);
              graphics.drawString(item.name(), cellX + 5, cellY + cellSize / 2);
          }

          // 🔥 STACK GOLD
          if (item.isStackable() && item.quantity() > 1) {
              graphics.setColor(Color.BLACK);
              graphics.fillRect(cellX + finalWidth - 26, cellY + finalHeight - 18, 24, 16);

              graphics.setColor(Color.YELLOW);
              graphics.drawString(
                  String.valueOf(item.quantity()),
                  cellX + finalWidth - 22,
                  cellY + finalHeight - 6
              );
          }
      }
  }

  // =======================
  // DRAG
  // =======================
  if (isDragging && draggedItem != null) {

      int mouseX = dragOffsetX;
      int mouseY = dragOffsetY;
      int itemPixelWidth = draggedItem.width() * (cellSize + padding) - padding;
      int itemPixelHeight = draggedItem.height() * (cellSize + padding) - padding;

      if (draggedItem.name().contains("Sword")) {
          BufferedImage img = draggedItem.isRotated() ? sword180 : sword90;
          graphics.drawImage(img, mouseX, mouseY, itemPixelWidth, itemPixelHeight, null);
      } else {
          graphics.setColor(new Color(100, 200, 255));
          graphics.fillRect(mouseX, mouseY, itemPixelWidth, itemPixelHeight);
          graphics.setColor(Color.BLACK);
          graphics.drawString(draggedItem.name(), mouseX + 5, mouseY + cellSize / 2);
      }

      // 🔥 STACK EN DRAG
      if (draggedItem.isStackable() && draggedItem.quantity() > 1) {
          graphics.setColor(Color.BLACK);
          graphics.fillRect(mouseX + itemPixelWidth - 26, mouseY + itemPixelHeight - 18, 24, 16);
          graphics.setColor(Color.YELLOW);
          graphics.drawString(
              String.valueOf(draggedItem.quantity()),
              mouseX + itemPixelWidth - 22,
              mouseY + itemPixelHeight - 6
          );
      }

      graphics.setColor(Color.BLACK);
      graphics.drawRect(mouseX, mouseY, itemPixelWidth, itemPixelHeight);
  }
}


	public void drawTreasureChest(Graphics2D g, Item[][] treasureGrid, List<Integer> selectedSlots, boolean isDragging,
			Item draggedItem, int dragOffsetX, int dragOffsetY) {
		var info = context.getScreenInfo();
		int width = info.width();
		int height = info.height();

		int chestWidth = 200;
		int chestHeight = 150;
		int chestX = width / 2 - chestWidth / 2;
		int chestY = height / 3 - chestHeight / 2;

		int treasureRows = treasureGrid.length;
		int treasureCols = treasureGrid[0].length;
		int cellSize = 60;
		int padding = 8;
		int startX = chestX;
		int startY = chestY + chestHeight + 20;

		g.setColor(Color.BLACK);
		g.drawString("Coffre au trésor :", startX, startY - 10);

		// 1. Dessin des cellules vides et des bordures
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				int cellX = startX + x * (cellSize + padding);
				int cellY = startY + y * (cellSize + padding);

				Item item = treasureGrid[y][x];

				if (isDragging && item == draggedItem) {
					g.setColor(Color.ORANGE);
				} else {
					g.setColor(item == null ? Color.ORANGE : new Color(255, 200, 100));
				}
				g.fill(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));

				g.setColor(Color.BLACK);
				g.draw(new Rectangle2D.Float(cellX, cellY, cellSize, cellSize));
			}
		}

		// 2. Dessin des objets (images ou texte)
		for (int y = 0; y < treasureRows; y++) {
			for (int x = 0; x < treasureCols; x++) {
				Item item = treasureGrid[y][x];

				if (item == null || (isDragging && item == draggedItem))
					continue;

				// Vérifie si c'est le coin supérieur gauche de l'objet
				boolean isTopLeft = true;
				if (x > 0 && treasureGrid[y][x - 1] == item)
					isTopLeft = false;
				if (y > 0 && treasureGrid[y - 1][x] == item)
					isTopLeft = false;

				if (isTopLeft) {
					int cellX = startX + x * (cellSize + padding);
					int cellY = startY + y * (cellSize + padding);
					int itemWidth = item.width() * (cellSize + padding) - padding;
					int itemHeight = item.height() * (cellSize + padding) - padding;

					if (item.name().contains("Sword")) {
						BufferedImage img = item.isRotated() ? sword180 : sword90;
						g.drawImage(img, cellX, cellY, itemWidth, itemHeight, null);
					} else if (item.name().contains("Shield")) {
						g.drawImage(shield, cellX, cellY, itemWidth, itemHeight, null);
					} else {
						// Affichage par défaut (texte) pour les autres objets
						String name = item.name();
						if (name.length() > 8)
							name = name.substring(0, 7) + "...";
						g.setColor(Color.BLACK);
						g.drawString(name, cellX + 5, cellY + cellSize / 2);

						g.setColor(new Color(0, 0, 0, 150));
						g.drawRect(cellX, cellY, itemWidth, itemHeight);
					}
				}
			}
		}

		// 3. Dessin de l'objet glissé (draggedItem)
		if (isDragging && draggedItem != null) {
			int mouseX = dragOffsetX;
			int mouseY = dragOffsetY;

			int itemPixelWidth = draggedItem.width() * (cellSize + padding) - padding;
			int itemPixelHeight = draggedItem.height() * (cellSize + padding) - padding;

			g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.7f));

			if (draggedItem.name().contains("Sword")) {
				BufferedImage img = draggedItem.isRotated() ? sword180 : sword90;
				g.drawImage(img, mouseX, mouseY, itemPixelWidth, itemPixelHeight, null);
			} else if (draggedItem.name().contains("Shield")) {
				g.drawImage(shield, mouseX, mouseY, itemPixelWidth, itemPixelHeight, null);
			} else {
				g.setColor(new Color(100, 200, 255));
				g.fillRect(mouseX, mouseY, itemPixelWidth, itemPixelHeight);

				g.setColor(Color.BLACK);
				String name = draggedItem.name();
				if (name.length() > 8)
					name = name.substring(0, 7) + "...";
				g.drawString(name, mouseX + 5, mouseY + cellSize / 2);
			}

			g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
			g.setColor(Color.BLACK);
			g.drawRect(mouseX, mouseY, itemPixelWidth, itemPixelHeight);
		}
		
	}

	private void drawHeroManaBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int width = info.width();
		int startX = width - 250;
		int startY = 50;
		int barWidth = 200;

		int heroMaxHp = hero.HeroMaxHp();
		double hpPercent = hero.hp() / (double) heroMaxHp;

		g.setColor(Color.WHITE);
		g.drawString(" HERO", startX, startY + 15);

		int heroHpBarWidth = (int) (hpPercent * barWidth);

		g.setColor(Color.RED);
		g.fillRect(startX, startY + 20, barWidth, 15);

		g.setColor(Color.BLUE);
		g.fillRect(startX, startY + 20, Math.max(0, heroHpBarWidth), 15);

		g.setColor(Color.BLACK);
		g.drawRect(startX, startY + 20, barWidth, 15);
		g.drawString(hero.mana() + " MANA", startX + barWidth / 2 - 20, startY + 32);
	}

	private void drawHeroHealthBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int width = info.width();
		int startX = width - 250;
		int startY = 10;
		int barWidth = 200;

		int heroMaxHp = hero.HeroMaxHp();
		double hpPercent = hero.hp() / (double) heroMaxHp;

		g.setColor(Color.WHITE);
		g.drawString(" HERO", startX, startY + 15);

		int heroHpBarWidth = (int) (hpPercent * barWidth);

		g.setColor(Color.RED);
		g.fillRect(startX, startY + 20, barWidth, 15);

		g.setColor(Color.GREEN);
		g.fillRect(startX, startY + 20, Math.max(0, heroHpBarWidth), 15);

		g.setColor(Color.BLACK);
		g.drawRect(startX, startY + 20, barWidth, 15);
		g.drawString(hero.hp() + " HP", startX + barWidth / 2 - 20, startY + 32);
	}

	private void drawHeroStaminaBar(Graphics2D g, Hero hero) {
		var info = context.getScreenInfo();
		int width = info.width();

		int startX = width - 250;
		int startY = 50; // juste sous la barre HP
		int barWidth = 200;
		int barHeight = 15;

		double staminaPercent = hero.currentStamina() / (double) hero.maxStamina();
		int staminaWidth = (int) (staminaPercent * barWidth);

		g.setColor(Color.WHITE);
		g.drawString("STAMINA", startX, startY - 5);

		// fond
		g.setColor(Color.DARK_GRAY);
		g.fillRect(startX, startY, barWidth, barHeight);

		// barre stamina
		g.setColor(Color.ORANGE);
		g.fillRect(startX, startY, staminaWidth, barHeight);

		// contour
		g.setColor(Color.BLACK);
		g.drawRect(startX, startY, barWidth, barHeight);

		g.drawString(hero.currentStamina() + " / " + hero.maxStamina(), startX + barWidth / 2 - 15, startY + 12);
	}

	private void drawEnemyHealthBars(Graphics2D g, List<Enemy> enemies) {
		if (enemies == null || enemies.isEmpty())
			return;

		var info = context.getScreenInfo();
		int width = info.width();
		int startX = width - 250;
		int startY = 90;
		int barWidth = 200;
		int barHeight = 30;
		int spacing = 10;

		int currentY = startY;

		for (int i = 0; i < enemies.size(); i++) {
			Enemy enemy = enemies.get(i);
			if (enemy.isAlive()) {

				g.setColor(Color.WHITE);
				g.drawString(enemy.name() + (i + 1), startX, currentY + 15);

				int maxHp = enemy.maxHp();
				double enemyPercent = enemy.hp() / (double) maxHp;
				int enemyHpBarWidth = (int) (enemyPercent * barWidth);

				g.setColor(Color.RED);
				g.fillRect(startX, currentY + 20, barWidth, 15);

				g.setColor(Color.MAGENTA);
				g.fillRect(startX, currentY + 20, Math.max(0, enemyHpBarWidth), 15);

				g.setColor(Color.WHITE);
				g.drawRect(startX, currentY + 20, barWidth, 15);
				g.drawString(enemy.hp() + " HP", startX + barWidth / 2 - 20, currentY + 32);

				currentY += barHeight + spacing;
			}
		}
	}

	private void drawFloatingItems(Graphics2D g, List<FloatingItem> floatingItems) {
		int cellSize = 60;
		int padding = 8;

		for (FloatingItem fItem : floatingItems) {
			Item item = fItem.item();
			int x = fItem.position.x;
			int y = fItem.position.y;

			int itemPixelWidth = item.width() * (cellSize + padding) - padding;
			int itemPixelHeight = item.height() * (cellSize + padding) - padding;

			// Dessin du fond
			g.setColor(Color.CYAN);
			g.fillRect(x, y, itemPixelWidth, itemPixelHeight);

			// Dessin de l'image ou du texte
			if (item.name().contains("Sword")) {
				BufferedImage img = item.isRotated() ? sword180 : sword90;
				g.drawImage(img, x, y, itemPixelWidth, itemPixelHeight, null);
			} else if (item.name().contains("Shield")) {
				g.drawImage(shield, x, y, itemPixelWidth, itemPixelHeight, null);
			} else {
				g.setColor(Color.BLACK);
				String name = item.name();
				if (name.length() > 8)
					name = name.substring(0, 7) + "...";
				g.drawString(name, x + 5, y + cellSize / 2);
			}

			// Bordure
			g.setColor(Color.BLACK);
			g.drawRect(x, y, itemPixelWidth, itemPixelHeight);
		}
	}

	private void clearScreen(Graphics2D graphics) {
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
	}
}