package fr.uge.implement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import fr.uge.implement.Room.Type;

public class MapDungeon {
	private final ArrayList<Room> rooms;
	private int playerIndex = 0;
	private int previousPlayerIndex = 0;
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
	
	public int previousPlayerIndex() {
		return previousPlayerIndex;
	}

	public void movePlayerTo(int newIndex) {		
		if (newIndex >= 0 && newIndex < rooms.size()) {
			previousPlayerIndex = playerIndex;
			playerIndex = newIndex;
		}
	}

	public List<Integer> adjacentRooms() {
    return getAdjacentRooms(this.playerIndex);
}

	public void show() {
		System.out.println("=== Floor ===");
		for (Room r : rooms) {
			System.out.println(" - " + r.name());
		}
		System.out.println();
	}

	public boolean playerOnEnemyRoom() {
		return rooms.get(playerIndex).type() == Type.ENEMY;
	}

	public boolean playerOnTreasureRoom() {
		return rooms.get(playerIndex).type() == Type.TREASURE;
	}

	public boolean playerOnCorridor() {
		return rooms.get(playerIndex).type() == Type.CORRIDOR;
	}

	public boolean playeOnExitRom() {
		return rooms.get(playerIndex).type() == Type.EXIT;

	}

	public boolean playerOnMerchantRoom() {
		return rooms().get(playerIndex()).type() == Room.Type.MERCHANT;

	}

	public boolean playerOnHealerRoom() {
		return rooms.get(playerIndex).type() == Type.HEALER;
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

	public void clearVisited() {
		visited.clear();
	}

	public List<Integer> findPath(int start, int end) {
		if (start == end) {
			return List.of(start);
		}

		Queue<Integer> queue = new LinkedList<>();
		Map<Integer, Integer> parent = new HashMap<>();
		Set<Integer> visited = new HashSet<>();

		queue.add(start);
		visited.add(start);
		parent.put(start, null);

		while (!queue.isEmpty()) {
			int current = queue.poll();

			if (current == end) {
				return reconstructPath(parent, start, end);
			}

			for (int neighbor : getAdjacentRooms(current)) {
				if (!visited.contains(neighbor)) {
					visited.add(neighbor);
					parent.put(neighbor, current);
					queue.add(neighbor);
				}
			}
		}

		return null; 
	}

	
	private List<Integer> reconstructPath(Map<Integer, Integer> parent, int start, int end) {
		List<Integer> path = new ArrayList<>();
		Integer current = end;

		while (current != null) {
			path.add(current);
			current = parent.get(current);
		}

		Collections.reverse(path);
		return path;
	}

	
	private List<Integer> getAdjacentRooms(int index) {
		int cols = 4;
		List<Integer> adj = new ArrayList<>();
		int maxIndex = rooms.size() - 1;
		int row = index / cols;
		int col = index % cols;
		int maxRow = maxIndex / cols;

		if (col > 0)
			adj.add(index - 1);
		if (col < cols - 1 && index + 1 <= maxIndex)
			adj.add(index + 1);
		if (row > 0)
			adj.add(index - cols);
		if (row < maxRow && index + cols <= maxIndex)
			adj.add(index + cols);

		return adj;
	}


	public boolean isRoomAccessible(int roomIndex) {
		
		if (adjacentRooms().contains(roomIndex)) {
			return true;
		}

		
		List<Integer> path = findPath(playerIndex, roomIndex);
		return path != null && !path.isEmpty();
	}

	public boolean isPathClear(List<Integer> path) {
		if (path == null || path.isEmpty()) {
			return false;
		}

	
		for (int i = 1; i < path.size() - 1; i++) {
			int roomIndex = path.get(i);
			Room room = rooms.get(roomIndex);

			
			if (room.type() == Type.CORRIDOR) {
				continue;
			}

		
			if (!isVisited(roomIndex)) {
			
				return false;
			}
		}

		return true;
	}


	public List<Integer> findClearPath(int start, int end) {
    if (start == end) {
        return List.of(start);
    }

    Queue<Integer> queue = new LinkedList<>();
    Map<Integer, Integer> parent = new HashMap<>();
    Set<Integer> visited = new HashSet<>();

    queue.add(start);
    visited.add(start);
    parent.put(start, null);

    while (!queue.isEmpty()) {
        int current = queue.poll();

        if (current == end) {
            return reconstructPath(parent, start, end);
        }

        for (int neighbor : getAdjacentRooms(current)) {

            if (!visited.contains(neighbor) && canPassThrough(neighbor, end)) {
                visited.add(neighbor);
                parent.put(neighbor, current);
                queue.add(neighbor);
            }
        }
    }
    return null; 
}


private boolean canPassThrough(int roomIndex, int destinationIndex) {
    
    if (roomIndex == destinationIndex) return true;
    
    Room room = rooms.get(roomIndex);
   
    return room.type() == Type.CORRIDOR || isVisited(roomIndex);
}

}
