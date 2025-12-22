package fr.uge.implement;

import java.util.Random;

public class ItemFactory {

    public static Item randomItem() {
        // Création d'une instance locale pour éviter la variable statique
        Random random = new Random();
        
        int choice = random.nextInt(3); 
        int power = 5 + random.nextInt(16);

        return switch (choice) {
            case 0 -> {
                // On récupère le tableau des types localement
                ItemType[] weaponTypes = ItemType.values();
                ItemType randomWeapon = weaponTypes[random.nextInt(weaponTypes.length)];
                yield new Sword(randomWeapon, power, 1, 1, 2,3);
            }
            case 1 -> new Gold("Gold", power);
            default -> new Shield("Shield+" + power, power, 2, 2,3);
        };
    }

    public static Item randomItemMarchant() {
        Random random = new Random();
        
        int type = random.nextInt(2);
        int power = 5 + random.nextInt(16);

        return switch (type) {
            case 0 -> {
              // On récupère le tableau des types localement
              ItemType[] weaponTypes = ItemType.values();
              ItemType randomWeapon = weaponTypes[random.nextInt(weaponTypes.length)];
              yield new Sword(randomWeapon, power, 1, 1, 2,3);
          }
            default -> new Shield("Shield+" + power, power, 2, 2,3);
        };
    }
}