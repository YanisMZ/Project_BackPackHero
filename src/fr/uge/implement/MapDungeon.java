package fr.uge.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapDungeon {
  private final ArrayList<Room> rooms;
  private int playerIndex = 0;
  private final ArrayList<Integer> visited = new ArrayList<>();

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

    // left
    if (col > 0)
      adj.add(playerIndex - 1);
    // right
    if (col < cols - 1 && playerIndex + 1 <= maxIndex)
      adj.add(playerIndex + 1);
    // up
    if (row > 0)
      adj.add(playerIndex - cols);
    // down
    if (row < maxRow && playerIndex + cols <= maxIndex)
      adj.add(playerIndex + cols);

    return adj;
  }

  public void show() {
    System.out.println("=== Floor ===");
    for (Room r : rooms) {
      System.out.println(" - " + r.name());
    }
    System.out.println();
  }

  public void setPlayerIndex(int index) {
    this.playerIndex = index;
  }

  public boolean playerOnEnemyRoom() {
    return rooms.get(playerIndex).name().contains("Enemy");
  }

  public boolean playerOnTreasureRoom() {
    return rooms.get(playerIndex).name().contains("Treasure");
  }

  public boolean playerOnCorridor() {
    return rooms.get(playerIndex).name().contains("Corridor");
  }

  public boolean isVisited(int index) {
    return visited.contains(index);
  }

  public void markVisited(int index) {
    if (!visited.contains(index)) {
      visited.add(index);
    }
  }

  public List<Integer> visitedRooms() {
    return List.copyOf(visited);
  }

}
