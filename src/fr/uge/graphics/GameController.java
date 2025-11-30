package fr.uge.graphics;

import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.MapDungeon;

/**
 * Controls the game logic and handles user interactions.
 */
public class GameController {

    private final ApplicationContext context;
    private final GameView view;
    private final MapDungeon floor;

    public GameController(ApplicationContext context, GameView view, MapDungeon floor) {
        this.context = Objects.requireNonNull(context);
        this.view = Objects.requireNonNull(view);
        this.floor = Objects.requireNonNull(floor);
    }

    /** Called every frame to check player input */
    public void update() {
      var event = context.pollOrWaitEvent(10);

      switch (event) {
          case null -> {
       
          }

          case KeyboardEvent ke -> { // pour quiter
              
              if (ke.key() == KeyboardEvent.Key.Q) {
                  System.exit(0);
              }
          }

          case PointerEvent pe -> {
              // on vérifie que c’est un VRAI clic
              if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
                  return;
              }

              var pos = pe.location();
              int mouseX = pos.x();
              int mouseY = pos.y();
              System.out.println(mouseX);

              // On déduit quelle salle a été cliquée
              int clickedRoom = roomAt(mouseX, mouseY);
              if (floor.adjacentRooms().contains(clickedRoom)) {
                // Déplacement du joueur
                floor.setPlayerIndex(clickedRoom);
                System.out.println("Player moved to room " + clickedRoom);
            }
              
              

              
          }
      }
  }
    
    
    public int roomAt(int mouseX, int mouseY) {
      int cols = 4;
      int cellSize = 120;
      int padding = 10;

      for (int i = 0; i < floor.rooms().size(); i++) {
          int row = i / cols;
          int col = i % cols;

          int x = padding + col * (cellSize + padding);
          int y = padding + row * (cellSize + padding);

          if (mouseX >= x && mouseX <= x + cellSize &&
              mouseY >= y && mouseY <= y + cellSize) {
              return i;
          }
      }
      return 0;
  }

}
