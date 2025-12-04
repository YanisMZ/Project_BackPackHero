package fr.uge.graphics;

import java.util.List;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.implement.BackPack;
import fr.uge.implement.Combat;
import fr.uge.implement.Enemy;
import fr.uge.implement.Hero;
import fr.uge.implement.MapDungeon;
import fr.uge.implement.SmallWolfRat;
import fr.uge.implement.WolfRat;

/**
 * Contrôle la logique du jeu et gère les interactions utilisateur.
 */
public class GameController {

    private final ApplicationContext context;
    private final GameView view;
    private final MapDungeon floor;
    private final BackPack backpack;
    private final Hero hero;
    private Combat fight;
    private boolean inCorridor = true;
	private boolean inTreasure = false;
    private boolean inCombat = false;
    
    
    public GameController(ApplicationContext context, GameView view, MapDungeon floor, BackPack backpack) {
    	this.context = Objects.requireNonNull(context);
    	this.view = Objects.requireNonNull(view);
    	this.floor = Objects.requireNonNull(floor);
    	this.backpack = Objects.requireNonNull(backpack);
    	this.hero = new Hero(40, 0);
    }
    
    public boolean isInCorridor() {
		return inCorridor;
	}

	public boolean isInTreasure() {
		return inTreasure;
	}
    
    public boolean isInCombat() {
    	return this.inCombat;
    }


    public void update() {
        var event = context.pollOrWaitEvent(10);
        

        if (event == null) {
            return;
        }

        switch (event) {
            // ==============================
            // ÉVÈNEMENTS CLAVIER
            // ==============================
            case KeyboardEvent ke -> {
                // Touche de sortie
                if (ke.key() == KeyboardEvent.Key.Q) {
                    System.exit(0);
                }

                // Si on est en combat, on gère A (attaque) et D (défense)
                if (inCombat) {
                    switch (ke.key()) {
                        case A -> {
                            System.out.println("🎯 ACTION → Le héros attaque !");
                            // AFFICHAGE A EN COULEUR
                            fight.attackEnemy();
                            fight.enemyTurn();
                            checkCombatEnd();
                        }
                        case D -> {
                            System.out.println("🛡️ ACTION → Le héros se défend !");
                            // AFFICHAGE B EN COULEUR
                            fight.defendHero();
                            fight.enemyTurn();
                            checkCombatEnd();
                        }
                        default -> {
                            // autres touches ignorées en combat
                        }
                    }
                }
            }

            // ==============================
            // ÉVÈNEMENTS SOURIS
            // ==============================
            case PointerEvent pe -> {
                // Si on est en combat, on ignore les clics
                if (inCombat) {
                    return;
                }

                // On vérifie que c’est un vrai clic (pression)
                if (pe.action() != PointerEvent.Action.POINTER_DOWN) {
                    return;
                }

                var pos = pe.location();
                int mouseX = pos.x();
                int mouseY = pos.y();
                System.out.println("Clic à : " + mouseX + ", " + mouseY);

                // On déduit quelle salle a été cliquée
                int clickedRoom = roomAt(mouseX, mouseY);
                System.out.println("Room détectée : " + clickedRoom);

                // Déplacement si la salle est adjacente
                if (floor.adjacentRooms().contains(clickedRoom)) {
                    floor.setPlayerIndex(clickedRoom);
                    System.out.println("Player moved to room " + clickedRoom);
                    this.inCombat = false;
                    this.inCorridor = true;
                    this.inTreasure = false;
                }

                // On vérifie sur quel type de salle le joueur se trouve
                if (floor.playerOnEnemyRoom()) {
                    System.out.println("⚠ Combat déclenché !");
                    this.inCombat = true;
                    this.inTreasure = false;
                    this.inCorridor = false;
                    startCombat();
                } else if (floor.playerOnCorridor()) {
                    this.inCorridor = true;
                    this.inTreasure = false;
                    this.inCombat = false;
                } else if (floor.playerOnTreasureRoom()) {
                    this.inTreasure = true;
                    this.inCombat = false;
                    this.inCorridor = false;
                }
            }

            default -> {
                // autres types d’évènements ignorés
            }
        }
    }

    // ==============================
    //   DÉBUT DE COMBAT
    // ==============================
    private void startCombat() {
        // Initialisation des ennemis
        List<Enemy> enemies = List.of(
            new SmallWolfRat(),
            new WolfRat()
        );

        fight = new Combat(hero, enemies);
        inCombat = true;

        System.out.println("=== MODE COMBAT ===");
        System.out.println("Appuie sur A = Attaquer | D = Défendre");
    }

    // ==============================
    //   VÉRIFICATION FIN DE COMBAT
    // ==============================
    private void checkCombatEnd() {
        if (fight == null) {
            return;
        }

        if (!fight.isRunning()) {
            inCombat = false;
            System.out.println("✨ Combat terminé !");
            // Éventuellement : supprimer les ennemis de la salle actuelle
            // floor.clearEnemiesInRoom();
        }
    }

    /**
     * Calcule l’index de la salle cliquée à partir de la position de la souris.
     *
     * @param mouseX position X de la souris
     * @param mouseY position Y de la souris
     * @return index de la salle, ou -1 si aucune salle ne correspond
     */
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
        return -1;
    }
}
