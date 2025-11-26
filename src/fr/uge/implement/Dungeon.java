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

        floor.add(new Room(prefix + " Enemy Room A"));
        floor.add(new Room(prefix + " Enemy Room B"));
        floor.add(new Room(prefix + " Enemy Room C"));


        floor.add(new Room(prefix + " Merchant Room"));
        
        floor.add(new Room(prefix + " Corridor 1"));
        floor.add(new Room(prefix + " Corridor 2"));


        floor.add(new Room(prefix + " Healer Room"));


        floor.add(new Room(prefix + " Treasure Room 1"));
        floor.add(new Room(prefix + " Treasure Room 2"));
        
        floor.add(new Room(prefix + " Corridor 3"));
        floor.add(new Room(prefix + " Corridor 4"));

 
        floor.add(new Room(prefix + " Exit Door"));

  
        floor.add(new Room(prefix + " Corridor 5"));
        floor.add(new Room(prefix + " Corridor 6"));
        floor.add(new Room(prefix + " Corridor 7"));
        floor.add(new Room(prefix + " Corridor 8"));

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
