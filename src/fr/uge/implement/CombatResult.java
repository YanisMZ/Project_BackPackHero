package fr.uge.implement;

/**
 * Résultat d'un calcul de combat avec effets
 * 
 * @param damage    Dégâts finaux infligés (après modifications)
 * @param lifeBonus Points de vie récupérés (gemme de cœur, etc.)
 */
public record CombatResult(int damage, int lifeBonus) {
    
    /**
     * Constructeur compact avec validation
     */
    public CombatResult {
        if (damage < 0) {
            throw new IllegalArgumentException("Les dégâts ne peuvent pas être négatifs");
        }
        if (lifeBonus < 0) {
            throw new IllegalArgumentException("Le bonus de vie ne peut pas être négatif");
        }
    }
    
    /**
     * Constructeur pour un résultat sans bonus de vie
     */
    public CombatResult(int damage) {
        this(damage, 0);
    }
    
    /**
     * Vérifie si ce résultat contient un bonus de vie
     */
    public boolean hasLifeBonus() {
        return lifeBonus > 0;
    }
    
    /**
     * Vérifie si des dégâts ont été infligés
     */
    public boolean hasDamage() {
        return damage > 0;
    }
}