package fr.uge.backpack;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère l'expansion du sac à dos en débloquant des cases adjacentes aux bords.
 */
public class BackpackExpansionSystem {
    
    private final BackPack backpack;
    private int pendingUnlocks = 0;
    private List<Point> availableExpansions = new ArrayList<>();
    
    public BackpackExpansionSystem(BackPack backpack) {
        this.backpack = backpack;
        updateAvailableExpansions();
    }
    
    /**
     * Ajoute des déblocages en attente (appelé quand un ennemi est vaincu)
     */
    public void addPendingUnlocks(int count) {
        pendingUnlocks += count;
        updateAvailableExpansions();
    }
    
    public int getPendingUnlocks() {
        return pendingUnlocks;
    }
    
    public boolean hasPendingUnlocks() {
        return pendingUnlocks > 0;
    }
    
    /**
     * Retourne la liste des cases disponibles pour l'expansion
     */
    public List<Point> getAvailableExpansions() {
        return new ArrayList<>(availableExpansions);
    }
    
    /**
     * Vérifie si une case est disponible pour l'expansion
     */
    public boolean isExpansionAvailable(int x, int y) {
        return availableExpansions.stream()
            .anyMatch(p -> p.x == x && p.y == y);
    }
    
    /**
     * Débloque une case et consomme un unlock en attente
     */
    public boolean unlockCell(int x, int y) {
        if (pendingUnlocks <= 0) return false;
        if (!isExpansionAvailable(x, y)) return false;
        
        backpack.unlockCell(x, y);
        pendingUnlocks--;
        updateAvailableExpansions();
        return true;
    }
    
    /**
     * Met à jour la liste des cases disponibles pour l'expansion.
     * Une case est disponible si elle est sur le périmètre extérieur de la zone débloquée.
     * C'est-à-dire : adjacente à une case débloquée ET sur le bord de cette zone.
     */
    private void updateAvailableExpansions() {
        availableExpansions.clear();
        
        int width = backpack.width();
        int height = backpack.height();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
  
                if (backpack.isUnlocked(x, y)) continue;
                
           
                if (isAdjacentToUnlocked(x, y) && isOnUnlockedPerimeter(x, y)) {
                    availableExpansions.add(new Point(x, y));
                }
            }
        }
    }
    
    /**
     * Vérifie si une case est adjacente à au moins une case débloquée
     */
    private boolean isAdjacentToUnlocked(int x, int y) {
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < backpack.width() && 
                ny >= 0 && ny < backpack.height() && 
                backpack.isUnlocked(nx, ny)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si une case verrouillée est sur le périmètre de la zone débloquée.
     * Une case est sur le périmètre si elle a au moins une direction (haut/bas/gauche/droite)
     * qui ne mène PAS vers une case débloquée (soit hors limites, soit verrouillée).
     */
    private boolean isOnUnlockedPerimeter(int x, int y) {
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            
            if (nx < 0 || nx >= backpack.width() || 
                ny < 0 || ny >= backpack.height()) {
                return true;
            }
            
            
            if (!backpack.isUnlocked(nx, ny)) {
                return true;
            }
        }
        
       
        return false;
    }
}