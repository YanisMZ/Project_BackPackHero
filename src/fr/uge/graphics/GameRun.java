package fr.uge.graphics;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.Application;

import fr.uge.implement.Dungeon;




/**
 * Runs the game application and manages player turns.
 */
public class GameRun {
	public GameRun() {
	}

	/**
	 * Starts the game application and handles the main game loop.
	 */
	public void run() {

    Application.run(Color.WHITE, context -> {

        Dungeon dungeon = new Dungeon();
        var floor0 = dungeon.getFloor(0);

        GameView view = new GameView(context, floor0);
        GameController controller = new GameController(context, view, floor0);

        boolean running = true;

        while (running) {

            controller.update();  

            view.render();        
        }
    });
}

	

	
}