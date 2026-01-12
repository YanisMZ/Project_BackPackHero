package fr.uge.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.uge.room.Room;

public class Dungeon {

  private final List<MapDungeon> floors = new ArrayList<>();
  private final Random random = new Random();

  private static final int MIN_ROOMS = 16;

  public Dungeon() {
    floors.add(createRandomFloor("Floor 1 -"));
    floors.add(createRandomFloor("Floor 2 -"));
    floors.add(createRandomFloor("Floor 3 -"));
  }

  public MapDungeon getFloor(int index) {
    return floors.get(index);
  }

  private MapDungeon createRandomFloor(String prefix) {
    MapDungeon floor = new MapDungeon();
    List<Room> rooms = new ArrayList<>();

  
    addRooms(rooms, prefix, Room.Type.ENEMY, 3);
    addRooms(rooms, prefix, Room.Type.TREASURE, 2);
    addRooms(rooms, prefix, Room.Type.MERCHANT, 1);
    addRooms(rooms, prefix, Room.Type.HEALER, 1);

  
    int currentCount = rooms.size() + 1; // +1 pour EXIT
    int corridorsToAdd = Math.max(0, MIN_ROOMS - currentCount);

    addRooms(rooms, prefix, Room.Type.CORRIDOR, corridorsToAdd);


    Collections.shuffle(rooms, random);

 
    forceFirstCorridor(rooms, prefix);

 
    rooms.add(new Room(prefix + " Exit Door", Room.Type.EXIT));


    for (Room room : rooms) {
      floor.add(room);
    }

    return floor;
  }



  private void addRooms(List<Room> rooms, String prefix, Room.Type type, int count) {
    for (int i = 1; i <= count; i++) {
      rooms.add(new Room(prefix + " " + type.name() + " Room " + i, type));
    }
  }

  private void forceFirstCorridor(List<Room> rooms, String prefix) {
    if (rooms.isEmpty())
      return;

    if (rooms.get(0).type() == Room.Type.CORRIDOR)
      return;


    for (int i = 1; i < rooms.size(); i++) {
      if (rooms.get(i).type() == Room.Type.CORRIDOR) {
        Collections.swap(rooms, 0, i);
        return;
      }
    }

    
    rooms.add(0, new Room(prefix + " Corridor Start", Room.Type.CORRIDOR));
  }
}
