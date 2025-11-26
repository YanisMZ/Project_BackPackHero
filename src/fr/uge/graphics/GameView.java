package fr.uge.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    int cols = 4;              // nombre de colonnes
    int rows = (int)Math.ceil(floor.rooms().size() / (double)cols);

    int cellSize = 120;
    int padding = 10;

    for (int i = 0; i < floor.rooms().size(); i++) {
        Room room = floor.rooms().get(i);

        int row = i / cols;
        int col = i % cols;

        int x = padding + col * (cellSize + padding);
        int y = padding + row * (cellSize + padding);

        // Couleur selon le type de salle
        Color color = switch (room.name()) {
            case String s when s.contains("Enemy") -> new Color(200, 80, 80);
            case String s when s.contains("Merchant") -> new Color(80, 180, 250);
            case String s when s.contains("Healer") -> new Color(100, 220, 100);
            case String s when s.contains("Treasure") -> new Color(250, 220, 80);
            case String s when s.contains("Exit") -> new Color(180, 80, 250);
            default -> new Color(180, 180, 180);
        };

        // Dessin de la case
        graphics.setColor(color);
        graphics.fill(new Rectangle2D.Float(x, y, cellSize, cellSize));

        // Bordure
        graphics.setColor(Color.BLACK);
        graphics.draw(new Rectangle2D.Float(x, y, cellSize, cellSize));

        // Texte
        graphics.setColor(Color.BLACK);
        graphics.drawString(room.name(), x + 10, y + cellSize / 2);
    }
}


}