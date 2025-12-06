package fr.uge.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.github.forax.zen.ApplicationContext;

import fr.uge.implement.BackPack;
import fr.uge.implement.Item;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.Room;

/**
 * Represents the visual representation of the game, handling rendering.
 */
public record GameView(ApplicationContext context, MapDungeon floor, BackPack backpack) {

  private static final BufferedImage corridorImage = loadImage("corridor2.png");
  private static final BufferedImage treasureRoomImage = loadImage("treasureroom.png");
  private static final BufferedImage treasureImage = loadImage("treasure.png");
  private static final BufferedImage heroImage = loadImage("hero.png");
  private static final BufferedImage heroImage2 = loadImage("hero2.png");
  private static final BufferedImage enemyImage = loadImage("enemy.png");
  private static final BufferedImage enemyImage2 = loadImage("enemy2miror.png");
  private static final BufferedImage enemyRoomImage0 = loadImage("fight0.png");
  private static final BufferedImage enemyRoomImage1 = loadImage("fight1.png");
  private static final BufferedImage enemyRoomImage2 = loadImage("fight2.png");
  private static final BufferedImage enemyRoomImage3 = loadImage("fight3.png");
  private static final BufferedImage attackOrDefenseBanner = loadImage("attackdefend.png");
  private static final BufferedImage attackBanner = loadImage("attack.png");
  private static final BufferedImage defendBanner = loadImage("defend.png");

  private static BufferedImage loadImage(String fileName) {
    try {
      File file = Path.of("./data", fileName).toFile();
      BufferedImage result = ImageIO.read(file);
      if (result == null) {
        throw new IllegalArgumentException("Error when loading image file" + file);
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException("Unable to load data file: " + fileName, e);
    }
  }

  public void render() {
    context.renderFrame(g -> {
      clearScreen(g);
      drawGrid(g);
      drawBackPack(g);
    });
  }

  public void combatDisplay(int nb_enemies, int status) {
    context.renderFrame(g -> {
      clearScreen(g);
      drawCombat(g, nb_enemies, status);
      drawGrid(g);
      drawBackPack(g);
    });
  }

  public void corridorDisplay() {
    context.renderFrame(g -> {
      clearScreen(g);
      drawCorridor(g);
      drawHero(g);
      drawGrid(g);
    });
  }

  public void treasureDisplay() {
    context.renderFrame(g -> {
      clearScreen(g);
      drawTreasure(g);
      drawGrid(g);
    });
  }

  public void emptyRoomDisplay() {
    context.renderFrame(g -> {
      clearScreen(g);
      drawEmptyRoom(g);
      drawHero(g);
      drawGrid(g);
    });
  }

  private void drawEmptyRoom(Graphics2D g) {
    var info = context.getScreenInfo();
    int width = info.width();
    int height = info.height();

    g.drawImage(enemyRoomImage0, 0, 0, width, height, null);
  }

  private void drawTreasure(Graphics2D g) {
    var info = context.getScreenInfo();
    int width = info.width();
    int height = info.height();

    g.drawImage(treasureRoomImage, 0, 0, width, height, null);
    g.drawImage(treasureImage, width / 2, height / 2, width / 2, height / 2, null);
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
    default -> throw new IllegalArgumentException("nb_enemies must be 1–3, got " + nb_enemies);
    }, 0, 0, width, height, null);

    int bannerWidth = width / 5;
    int bannerHeight = height / 5;

    int x = (width - bannerWidth) / 2;

    int marginBottom = height / 50;
    int y = height - bannerHeight - marginBottom;
    switch (status) {
    case 0 -> g.drawImage(attackOrDefenseBanner, x, y, bannerWidth, bannerHeight, null);
    case 1 -> g.drawImage(attackBanner, x, y, bannerWidth, bannerHeight, null);
    case 2 -> g.drawImage(defendBanner, x, y, bannerWidth, bannerHeight, null);
    default -> throw new IllegalArgumentException("Unexpected value: " + status);
    }
  }

  private void drawHero(Graphics2D g) {
    var info = context.getScreenInfo();
    int width = info.width();
    int height = info.height();

    g.drawImage(heroImage2, width / 4, height / 4, width, height, null);
  }

  private void drawCorridor(Graphics2D g) {
    var info = context.getScreenInfo();
    int width = info.width();
    int height = info.height();

    g.drawImage(corridorImage, 0, 0, width, height, null);
  }

  /**
   * Draws the game grid.
   *
   * @param graphics the graphics context
   */
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

      // couleur normale selon le type...
      Color color = switch (room.name()) {
      case String s when s.contains("Enemy") -> new Color(200, 80, 80);
      case String s when s.contains("Merchant") -> new Color(80, 180, 250);
      case String s when s.contains("Healer") -> new Color(100, 220, 100);
      case String s when s.contains("Treasure") -> new Color(250, 220, 80);
      case String s when s.contains("Exit") -> new Color(180, 80, 250);
      default -> new Color(180, 180, 180);
      };

      // si case adjacente la couleur est plus clair
      if (adjacents.contains(i)) {
        graphics.setColor(Color.GREEN);
      } else {
        graphics.setColor(color);
      }
      graphics.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));

      // bordure
      graphics.setColor(Color.BLACK);
      graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));

      // player
      if (i == floor.playerIndex()) {
        graphics.setColor(Color.RED);

        int imgSize = cellSize / 2;
        int cx = x + (cellSize - imgSize) / 2;
        int cy = y + (cellSize - imgSize) / 2;

        graphics.drawImage(heroImage, cx, cy, imgSize, imgSize, null);

      }

      // texte
      graphics.setColor(Color.BLACK);
      graphics.drawString(room.name(), x + 8, y + cellSize / 2);
    }
  }

  private void drawBackPack(Graphics2D graphics) {

    int originX = 20;
    int originY = 550;

    int cols = 5;
    int cellSize = 60;
    int padding = 8;

    Item[] slots = new Item[15];
    backpack.items().forEach((item, index) -> {
      if (index < slots.length) {
        slots[index] = item;
      }
    });

    graphics.setColor(Color.BLACK);
    graphics.drawString("Backpack :", originX, originY - 10);

    for (int i = 0; i < slots.length; i++) {

      int row = i / cols;
      int col = i % cols;

      int x = originX + col * (cellSize + padding);
      int y = originY + row * (cellSize + padding);

      // fond
      if (slots[i] == null) {
        graphics.setColor(Color.YELLOW);
      } else {
        graphics.setColor(Color.CYAN);
      }
      graphics.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));

      // bordure
      graphics.setColor(Color.BLACK);
      graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));

      // nom de l’objet
      if (slots[i] != null) {
        graphics.setColor(Color.BLACK);
        String name = slots[i].name();
        if (name.length() > 8)
          name = name.substring(0, 7) + "...";
        graphics.drawString(name, x + 5, y + cellSize / 2);
      }
    }
  }

  private void clearScreen(Graphics2D graphics) {
    graphics.setColor(Color.BLACK);
    graphics.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
  }

}