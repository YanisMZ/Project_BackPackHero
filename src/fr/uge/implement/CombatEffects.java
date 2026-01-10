package fr.uge.implement;

import java.util.HashSet;
import java.util.Set;

/**
 * Système de calcul des effets de combat avec interactions entre équipements
 */
public class CombatEffects {
    
    private final BackPack backpack;
    private final Hero hero;

    public CombatEffects(BackPack backpack, Hero hero) {
        this.backpack = backpack;
        this.hero = hero;
    }
    
    /**
     * Calcule les dégâts finaux d'une arme en tenant compte des interactions
     */
    public int calculateDamage(Item weapon, int x, int y) {
        int baseDamage = weapon.attackValue();
        
        // Cas spécial : Hachette
        if (weapon.name().equalsIgnoreCase("Hachette")) {
            if (hero.protection() > 0) {
                return 1;
            } else {
                return baseDamage;
            }
        }
        
        return baseDamage;
    }
    
    /**
     * Calcule le bonus de vie gagné lors de l'utilisation d'une arme
     */
    public int calculateLifeBonus(Item weapon, int x, int y) {
        int lifeBonus = 0;
        
        // Vérifier si l'arme est adjacente à une gemme de cœur
        if (isAdjacentToHeartGem(x, y)) {
            lifeBonus += 1;
        }
        
        return lifeBonus;
    }
    
    /**
     * Vérifie si une position est adjacente à une gemme de cœur
     */
    private boolean isAdjacentToHeartGem(int x, int y) {
        Item item = backpack.grid()[y][x];
        if (item == null) return false;
        
        Set<int[]> itemPositions = getItemPositions(item);
        
        // Pour chaque position de l'item, vérifier les 8 cases adjacentes
        for (int[] pos : itemPositions) {
            if (hasHeartGemAround(pos[0], pos[1])) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie si une case spécifique a une gemme de cœur autour
     */
    private boolean hasHeartGemAround(int x, int y) {
        int[][] directions = {
            {-1, -1}, {0, -1}, {1, -1},  // haut-gauche, haut, haut-droite
            {-1, 0},           {1, 0},    // gauche, droite
            {-1, 1},  {0, 1},  {1, 1}     // bas-gauche, bas, bas-droite
        };
        
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            
            if (newX >= 0 && newX < backpack.width() && 
                newY >= 0 && newY < backpack.height()) {
                
                Item neighbor = backpack.grid()[newY][newX];
                if (neighbor != null && isHeartGem(neighbor)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Récupère toutes les positions occupées par un item
     */
    private Set<int[]> getItemPositions(Item item) {
        Set<int[]> positions = new HashSet<>();
        Item[][] grid = backpack.grid();
        
        for (int y = 0; y < backpack.height(); y++) {
            for (int x = 0; x < backpack.width(); x++) {
                if (grid[y][x] == item) {
                    positions.add(new int[]{x, y});
                }
            }
        }
        
        return positions;
    }
    
    /**
     * Vérifie si un item est une gemme de cœur
     */
    private boolean isHeartGem(Item item) {
        return item.name().equalsIgnoreCase("Heal");
    }
    
    /**
     * Applique tous les effets d'un item utilisé en combat
     */
    public CombatResult applyItemEffects(Item item, int x, int y, Hero hero) {
        int damage = calculateDamage(item, x, y);
        int lifeBonus = calculateLifeBonus(item, x, y);
        
        // Appliquer le bonus de vie si applicable
        if (lifeBonus > 0) {
            hero.heal(lifeBonus);
            System.out.println("💚 Gemme de cœur activée ! +" + lifeBonus + " HP");
        }
        
        return new CombatResult(damage, lifeBonus);
    }
}