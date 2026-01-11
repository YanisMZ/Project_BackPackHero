package fr.uge.items;

public interface Item {
    String name();

    int attackValue();
    int staminaCost();
    int defendValue();

    int width();
    int height();

    Item rotate();
    boolean isRotated();

    // Stack
    default boolean isStackable() {
        return false;
    }

    default int quantity() {
        return 1;
    }

    default Item addQuantity(int amount) {
        throw new UnsupportedOperationException();
    }
    
    int price();
    
    default boolean isSellable() {
      return true; 
  }
    
    default int staminaRegen() {
      return 0; // La plupart des objets ne redonnent pas d'énergie
  }
    
    
    /** @return la durabilité actuelle. -1 si l'objet est indestructible. */
    default int durability() {
      return -1; // Objet indestructible par défaut (ex: Gold)
  }

  default Item decreaseDurability() {
      return this; // Ne fait rien par défaut
  }

  default boolean isBroken() {
      return durability() == 0;
  }
  
  default int healthRegen() {
    return 0; 
}
  
  default boolean occupies(int dx, int dy) {
    return true;
}
  
  default boolean isMalediction() {
    return false;
}


   
}
