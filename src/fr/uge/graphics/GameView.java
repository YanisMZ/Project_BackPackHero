package fr.uge.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
public record GameView(ApplicationContext context,MapDungeon floor,BackPack backpack){
	
	
	private static final BufferedImage heroImage = loadHero();
    private static BufferedImage loadHero() {
        try {
            return ImageIO.read(
                Path.of("./data/hero.png").toFile()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
	public void render() {
		context.renderFrame(g -> {
	    drawGrid(g);
	    drawBackPack(g);
	});

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
            graphics.setColor(color.GREEN);
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
            if (name.length() > 8) name = name.substring(0, 7) + "...";
            graphics.drawString(name, x + 5, y + cellSize / 2);
        }
    }
}


	
	
	private void clearScreen(Graphics2D graphics) {
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
	}
	
	
	
	
	
	



}