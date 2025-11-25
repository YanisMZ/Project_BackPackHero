package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;

public class Dungeon {

    private final List<MapDungeon> floors = new ArrayList<>();

    public Dungeon() {
        floors.add(createFirstFloor());
        floors.add(createSecondFloor());
        floors.add(createThirdFloor());
    }

    public MapDungeon getFloor(int index) {
        return floors.get(index);
    }

    private MapDungeon createFloor(String prefix) {
        MapDungeon floor = new MapDungeon();

        // 3 salles ennemies
        floor.add(new Room(prefix + " Enemy Room A"));
        floor.add(new Room(prefix + " Enemy Room B"));
        floor.add(new Room(prefix + " Enemy Room C"));

        // 1 marchand
        floor.add(new Room(prefix + " Merchant Room"));

        // 1 guérisseur
        floor.add(new Room(prefix + " Healer Room"));

        // 2 trésors
        floor.add(new Room(prefix + " Treasure Room 1"));
        floor.add(new Room(prefix + " Treasure Room 2"));

        // 1 sortie
        floor.add(new Room(prefix + " Exit Door"));

        // Quelques couloirs
        floor.add(new Room(prefix + " Corridor 1"));
        floor.add(new Room(prefix + " Corridor 2"));
        floor.add(new Room(prefix + " Corridor 3"));

        return floor;
    }

    private MapDungeon createFirstFloor() {
        return createFloor("Floor 1 -");
    }

    private MapDungeon createSecondFloor() {
        return createFloor("Floor 2 -");
    }

    private MapDungeon createThirdFloor() {
        return createFloor("Floor 3 -");
    }
}
