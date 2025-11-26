package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapDungeon {
    private final ArrayList<Room> rooms;
    private int playerIndex = 0; 

    public MapDungeon() {
        this.rooms = new ArrayList<>();
    }

    public void add(Room ele) {
        Objects.requireNonNull(ele);
        rooms.add(ele);
    }

    public List<Room> rooms() {
        return rooms;
    }

    public int playerIndex() {
        return playerIndex;
    }

    public void movePlayerTo(int newIndex) {
        if (newIndex >= 0 && newIndex < rooms.size()) {
            playerIndex = newIndex;
        }
    }

   
    public List<Integer> adjacentRooms() {
        int cols = 4;
        List<Integer> adj = new ArrayList<>();

        int maxIndex = rooms.size() - 1;
        int row = playerIndex / cols;
        int col = playerIndex % cols;
        int maxRow = maxIndex / cols;

        // gauche
        if (col > 0) adj.add(playerIndex - 1);
        // droite
        if (col < cols - 1 && playerIndex + 1 <= maxIndex) adj.add(playerIndex + 1);
        // haut
        if (row > 0) adj.add(playerIndex - cols);
        // bas
        if (row < maxRow && playerIndex + cols <= maxIndex) adj.add(playerIndex + cols);

        return adj;
    }

    public void show() {
        System.out.println("=== Floor ===");
        for (Room r : rooms) {
            System.out.println(" - " + r.name());
        }
        System.out.println();
    }
}
