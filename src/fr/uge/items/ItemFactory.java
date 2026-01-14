package fr.uge.items;

import java.util.Random;

public class ItemFactory {

    public static Item randomItem() {
        
        Random random = new Random();
        
        int choice = random.nextInt(4); 
        int power = 5 + random.nextInt(16);

        return switch (choice) {
            case 0 -> {
               
                ItemType[] weaponTypes = ItemType.values();
                ItemType randomWeapon = weaponTypes[random.nextInt(weaponTypes.length)];
                yield new Sword(randomWeapon, power, 1, 1, 2,5);
            }
            case 1 -> new Gold("Gold", power);
            case 2 -> new Ration("Ration",1,2,1);
            case 3 -> new HealingItem("Heal", 10, 1, 1);
            default -> new Shield("Shield+" + power, power, 2, 2,5);
        };
    }

    public static Item randomItemMarchant() {
        Random random = new Random();
        
        int type = random.nextInt(4);
        int power = 5 + random.nextInt(16);

        return switch (type) {
            case 0 -> {
              
              ItemType[] weaponTypes = ItemType.values();
              ItemType randomWeapon = weaponTypes[random.nextInt(weaponTypes.length)];
              yield new Sword(randomWeapon, power, 1, 1, 2,5);
          }
            case 1 -> new Ration("Ration",1,2,1);
            case 2 -> new HealingItem("Heal", 10, 1, 1);
            default -> new Shield("Shield+" + power, power, 2, 2,5);
        };
    }
}