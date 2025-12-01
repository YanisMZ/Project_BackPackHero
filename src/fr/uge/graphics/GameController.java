package fr.uge.graphics;

import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.BackPack;
import fr.uge.implement.Combat;
import fr.uge.implement.Hero;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.SmallWolfRat;
import fr.uge.implement.WolfRat;

/**a
 * Controls the game logic and handles user interactions.
 */
public class GameController {

<<<<<<< Updated upstream
    private final ApplicationContext context;
    private final GameView view;
    private final MapDungeon floor;
    private final BackPack backpack;
    private Combat fight; 
    Boolean combat;
    Boolean corridor;
    Boolean treasure;
    private boolean inCombat = false;
    private final Hero hero;

    public GameController(ApplicationContext context, GameView view, MapDungeon floor,BackPack backpack) {
    	this.combat = false;
    	this.corridor = false;
    	this.treasure = false;
        this.context = Objects.requireNonNull(context);
        this.view = Objects.requireNonNull(view);
        this.floor = Objects.requireNonNull(floor);
        this.backpack = Objects.requireNonNull(backpack);
        this.hero = new Hero(40, 0);
       
        
    }
=======
	private final ApplicationContext context;
	private final GameView view;
	private final MapDungeon floor;
	private final BackPack backpack;
	private Combat fight;
	Boolean combat;
	Boolean corridor;
	Boolean treasure;
	private boolean inCombat = false;

	public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack) {
		this.combat = false;
		this.corridor = false;
		this.treasure = false;
		this.context = Objects.requireNonNull(context);
		this.view = Objects.requireNonNull(view);
		this.floor = Objects.requireNonNull(floor);
		this.backpack = Objects.requireNonNull(backpack);
>>>>>>> Stashed changes

	}

	/** Called every frame to check player input */
	public void update() {
		var event = context.pollOrWaitEvent(10);

		switch (event) {

		case null -> {
		}

		// ----------------------------------
		// 1. Touche de sortie
		// ----------------------------------
		case KeyboardEvent ke -> {

			if (ke.key() == KeyboardEvent.Key.Q) {
				System.exit(0);
			}

<<<<<<< Updated upstream
                      case A -> {
                          System.out.println("🎯 ACTION → Le héros attaque !");
                          fight.attackEnemy();
                          fight.enemyTurn();
                          checkCombatEnd();
                      }

                      case D -> {
                          System.out.println("🛡️ ACTION → Le héros se défend !");
                          fight.defendHero();
                          fight.enemyTurn();
                          checkCombatEnd();
                      }

                      default -> {}
                  }
              }
          }
            
        
    
          case PointerEvent pe -> {
          	
          		if (inCombat) return;
              // on vérifie que c’est un VRAI clic
              if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
                  return;
              }
=======
			// ----------------------------------
			// COMBAT : touches A et D
			// ----------------------------------
			if (inCombat) {
				switch (ke.key()) {
				case A -> {
					System.out.println("🎯 ACTION → Le héros attaque !");
					fight.attackEnemy();
					fight.enemyTurn();
					// checkCombatEnd();
				}

				case D -> {
					System.out.println("🛡️ ACTION → Le héros se défend !");
					fight.defendHero();
					fight.enemyTurn();
					// checkCombatEnd();
				}

				default -> {
				}
				}
			}
		}
>>>>>>> Stashed changes

		case PointerEvent pe -> {
			// on vérifie que c’est un VRAI clic
			if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
				return;
			}

<<<<<<< Updated upstream
              // On déduit quelle salle a été cliquée
              int clickedRoom = roomAt(mouseX, mouseY);
              if (floor.adjacentRooms().contains(clickedRoom)) {
                // Déplacement du joueur
                floor.setPlayerIndex(clickedRoom);
                System.out.println("Player moved to room " + clickedRoom);
                this.combat = false;
                this.corridor = true;
                this.treasure = false;
            }
              
              if (floor.playerOnEnemyRoom()) {
                System.out.println("⚠ Combat déclenché !");
                this.combat = true;
                this.treasure = false;
                this.corridor = false;
                startCombat();
               

            }
              
              if (floor.playerOnCorridor()) {
                  this.corridor = true;
                  this.treasure = false;
                  this.combat = false;
              }
              if (floor.playerOnTreasureRoom()) {
            	  this.treasure = true;
            	  this.combat = false;
            	  this.corridor = false;
              }
          }
      }
  }
    
    
    
    
    
    
    private void startCombat() {
      // Initialisation des ennemis
      List<fr.uge.implement.Enemy> enemies = List.of(
          new SmallWolfRat(),
          new WolfRat()
      );

      fight = new Combat(hero, enemies);
      inCombat = true;

      System.out.println("=== MODE COMBAT ===");
      System.out.println("Appuie sur A = Attaquer | D = Défendre");
  }

  // ==================================
  //     VÉRIFIER FIN DE COMBAT
  // ==================================
  private void checkCombatEnd() {
      if (fight == null) return;

      if (!fight.isRunning()) {
          inCombat = false;
          System.out.println("✨ Combat terminé !");
          // Optionnel : nettoyer les ennemis de la salle
          //floor.clearEnemiesInRoom();
      }
  }
    
    
    public int roomAt(int mouseX, int mouseY) {
      int cols = 4;
      int cellSize = 120;
      int padding = 10;
=======
			var pos = pe.location();
			int mouseX = pos.x();
			int mouseY = pos.y();
			System.out.println(mouseX);
>>>>>>> Stashed changes

			// On déduit quelle salle a été cliquée
			int clickedRoom = roomAt(mouseX, mouseY);
			if (floor.adjacentRooms().contains(clickedRoom)) {
				// Déplacement du joueur
				floor.setPlayerIndex(clickedRoom);
				System.out.println("Player moved to room " + clickedRoom);
				this.combat = false;
				this.corridor = true;
				this.treasure = false;
			}

			if (floor.playerOnEnemyRoom()) {
				System.out.println("⚠ Combat déclenché !");
				this.combat = true;
				this.treasure = false;
				this.corridor = false;
			}

			if (floor.playerOnCorridor()) {
				this.corridor = true;
				this.treasure = false;
				this.combat = false;
			}
			if (floor.playerOnTreasureRoom()) {
				this.treasure = true;
				this.combat = false;
				this.corridor = false;
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

			if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize) {
				return i;
			}
		}
		return 0;
	}
}
