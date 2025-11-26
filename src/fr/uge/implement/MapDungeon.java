package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapDungeon {
	private ArrayList<Room> rooms;
	public MapDungeon() {
		this.rooms = new ArrayList<>();
	}
	
	
	public void add(Room ele) {
		Objects.requireNonNull(ele);
		rooms.add(ele);
	}
	
	
	public void show() {
    System.out.println("=== Floor ===");
    for (Room r : rooms) {
        System.out.println(" - " + r.name());
    }
    System.out.println();
}
	
	
	public List<Room> rooms() {
    return rooms;
}


	
	
	

}
