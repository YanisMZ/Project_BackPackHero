package fr.uge.implement;

import java.util.Random;

public class ItemFactory {

    private static final Random RANDOM = new Random();

    /**
     * Generates a random item.
     * 
     * @return a new Item instance
     */
    public static Item randomItem() {
        int type = RANDOM.nextInt(3); // 0=Weapon,1=Armor
        int power = 5 + RANDOM.nextInt(16); // 5..20

        return switch (type) {
        case 0 -> new Sword("Sword+" + power, power,1, 1, 2);
        case 1 -> new Gold("Gold",power);
            default -> new Shield("Shield+" + power, power,2,2);
        };
    }
    
    
    public static Item randomItemMarchant() {
      int type = RANDOM.nextInt(2); // 0=Weapon,1=Armor
      int power = 5 + RANDOM.nextInt(16); // 5..20

      return switch (type) {
      case 0 -> new Sword("Sword+" + power, power,1, 1, 2);
      default -> new Shield("Shield+" + power, power,2,2);
      };
  }
    
    
    
}
