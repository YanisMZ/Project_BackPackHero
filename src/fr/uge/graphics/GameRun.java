package fr.uge.graphics;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.Application;

import fr.uge.implement.BackPack;
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

    	BackPack backpack = dungeon.backpack();  // le sac global
    	BackPack.fillBackPackForTest(backpack);  // on remplit le seul vrai sac

    	System.out.println(backpack.BackPackDisplay());

    	var floor0 = dungeon.getFloor(0);

    	GameView view = new GameView(context, floor0, backpack);
    	GameController controller = new GameController(context, view, floor0, backpack);


        boolean running = true;

        while (running) {

            controller.update();  

            view.render();        
        }
    });
}

	

	
}