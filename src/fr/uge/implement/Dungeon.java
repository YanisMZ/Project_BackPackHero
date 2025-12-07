package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;

public class Dungeon {

  private final List<MapDungeon> floors = new ArrayList<>();
  private final BackPack backpack = new BackPack();

  public Dungeon() {
    floors.add(createFirstFloor());
    floors.add(createSecondFloor());
    floors.add(createThirdFloor());
  }

  public MapDungeon getFloor(int index) {
    return floors.get(index);
  }

  public BackPack backpack() {
    return backpack;
  }

  private MapDungeon createFloor(String prefix) {
    MapDungeon floor = new MapDungeon();

    floor.add(new Room(prefix + " Corridor 1", Room.Type.CORRIDOR));
    floor.add(new Room(prefix + " Corridor 2", Room.Type.CORRIDOR));

    floor.add(new Room(prefix + " Enemy Room A", Room.Type.ENEMY));
    floor.add(new Room(prefix + " Enemy Room B", Room.Type.ENEMY));
    floor.add(new Room(prefix + " Enemy Room C", Room.Type.ENEMY));

    floor.add(new Room(prefix + " Merchant Room", Room.Type.MERCHANT));

    floor.add(new Room(prefix + " Healer Room", Room.Type.HEALER));

    floor.add(new Room(prefix + " Treasure Room 1", Room.Type.TREASURE));
    floor.add(new Room(prefix + " Treasure Room 2", Room.Type.TREASURE));

    floor.add(new Room(prefix + " Corridor 3", Room.Type.CORRIDOR));
    floor.add(new Room(prefix + " Corridor 4", Room.Type.CORRIDOR));

    floor.add(new Room(prefix + " Exit Door", Room.Type.EXIT));

    floor.add(new Room(prefix + " Corridor 5", Room.Type.CORRIDOR));
    floor.add(new Room(prefix + " Corridor 6", Room.Type.CORRIDOR));
    floor.add(new Room(prefix + " Corridor 7", Room.Type.CORRIDOR));
    floor.add(new Room(prefix + " Corridor 8", Room.Type.CORRIDOR));

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
