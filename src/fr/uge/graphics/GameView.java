package fr.uge.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import com.github.forax.zen.ApplicationContext;

import fr.uge.implement.MapDungeon;
import fr.uge.implement.Room;


/**
 * Represents the visual representation of the game, handling rendering.
 */
public record GameView(ApplicationContext context,MapDungeon floor){
	
	public void render() {
    context.renderFrame(graphics -> drawGrid(graphics));
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

          int circleSize = cellSize / 2; 
          int cx = x + (cellSize - circleSize) / 2;
          int cy = y + (cellSize - circleSize) / 2;

          graphics.fill(new Ellipse2D.Float(
              cx,
              cy,
              circleSize,
              circleSize
          ));
      }

        // texte
        graphics.setColor(Color.BLACK);
        graphics.drawString(room.name(), x + 8, y + cellSize / 2);
    }
}
	
	
	
	private void clearScreen(Graphics2D graphics) {
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
	}
	
	
	
	
	
	



}