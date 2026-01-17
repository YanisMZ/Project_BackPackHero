package fr.uge.map;

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

import fr.uge.room.Room;
import fr.uge.room.Room.Type;

/**
 * Represents a dungeon map made of rooms.
 * The player can move between rooms and paths can be searched.
 */
public class MapDungeon {

	/** List of all rooms in the dungeon */
	private final ArrayList<Room> rooms;

	/** Current player position */
	private int playerIndex = 0;

	/** Previous player position */
	private int previousPlayerIndex = 0;

	/** List of visited room indexes */
	private final ArrayList<Integer> visited = new ArrayList<>();

	/**
	 * Creates an empty dungeon map.
	 */
	public MapDungeon() {
		this.rooms = new ArrayList<>();
	}

	/**
	 * Adds a room to the dungeon.
	 *
	 * @param ele the room to add
	 */
	public void add(Room ele) {
		Objects.requireNonNull(ele);
		rooms.add(ele);
	}

	/**
	 * Returns the list of rooms.
	 *
	 * @return all rooms
	 */
	public List<Room> rooms() {
		return rooms;
	}

	/**
	 * Returns the current player index.
	 *
	 * @return player index
	 */
	public int playerIndex() {
		return playerIndex;
	}

	/**
	 * Returns the previous player index.
	 *
	 * @return previous player index
	 */
	public int previousPlayerIndex() {
		return previousPlayerIndex;
	}

	/**
	 * Moves the player to another room if the index is valid.
	 *
	 * @param newIndex the new room index
	 */
	public void movePlayerTo(int newIndex) {
		if (newIndex >= 0 && newIndex < rooms.size()) {
			previousPlayerIndex = playerIndex;
			playerIndex = newIndex;
		}
	}

	/**
	 * Returns adjacent rooms of the current player position.
	 *
	 * @return list of adjacent room indexes
	 */
	public List<Integer> adjacentRooms() {
		return getAdjacentRooms(this.playerIndex);
	}

	/**
	 * Displays all room names in the dungeon.
	 */
	public void show() {
		System.out.println("=== Floor ===");
		for (Room r : rooms) {
			System.out.println(" - " + r.name());
		}
		System.out.println();
	}

	/**
	 * Checks if the player is on an enemy room.
	 *
	 * @return true if enemy room
	 */
	public boolean playerOnEnemyRoom() {
		return rooms.get(playerIndex).type() == Type.ENEMY;
	}

	/**
	 * Checks if the player is on a treasure room.
	 *
	 * @return true if treasure room
	 */
	public boolean playerOnTreasureRoom() {
		return rooms.get(playerIndex).type() == Type.TREASURE;
	}

	/**
	 * Checks if the player is on a corridor.
	 *
	 * @return true if corridor
	 */
	public boolean playerOnCorridor() {
		return rooms.get(playerIndex).type() == Type.CORRIDOR;
	}

	/**
	 * Checks if the player is on the exit room.
	 *
	 * @return true if exit room
	 */
	public boolean playeOnExitRom() {
		return rooms.get(playerIndex).type() == Type.EXIT;
	}

	/**
	 * Checks if the player is on a merchant room.
	 *
	 * @return true if merchant room
	 */
	public boolean playerOnMerchantRoom() {
		return rooms().get(playerIndex()).type() == Room.Type.MERCHANT;
	}

	/**
	 * Checks if the player is on a healer room.
	 *
	 * @return true if healer room
	 */
	public boolean playerOnHealerRoom() {
		return rooms.get(playerIndex).type() == Type.HEALER;
	}

	/**
	 * Checks if a room was already visited.
	 *
	 * @param index room index
	 * @return true if visited
	 */
	public boolean isVisited(int index) {
		return visited.contains(index);
	}

	/**
	 * Marks a room as visited.
	 *
	 * @param index room index
	 */
	public void markVisited(int index) {
		if (!visited.contains(index)) {
			visited.add(index);
		}
	}

	/**
	 * Returns the list of visited rooms.
	 *
	 * @return visited room indexes
	 */
	public List<Integer> visitedRooms() {
		return List.copyOf(visited);
	}

	/**
	 * Clears all visited rooms.
	 */
	public void clearVisited() {
		visited.clear();
	}

	/**
	 * Finds a path between two rooms using BFS.
	 *
	 * @param start start index
	 * @param end end index
	 * @return path as a list of indexes, or null if not found
	 */
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

	/**
	 * Rebuilds a path from parent links.
	 *
	 * @param parent parent map
	 * @param start start index
	 * @param end end index
	 * @return reconstructed path
	 */
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

	/**
	 * Returns adjacent rooms for a given index (grid of 4 columns).
	 *
	 * @param index room index
	 * @return adjacent room indexes
	 */
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

	/**
	 * Checks if a room can be reached by the player.
	 *
	 * @param roomIndex target room
	 * @return true if accessible
	 */
	public boolean isRoomAccessible(int roomIndex) {
		if (adjacentRooms().contains(roomIndex)) {
			return true;
		}

		List<Integer> path = findPath(playerIndex, roomIndex);
		return path != null && !path.isEmpty();
	}

	/**
	 * Checks if a path only goes through visited rooms or corridors.
	 *
	 * @param path path to check
	 * @return true if path is clear
	 */
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

	/**
	 * Finds a clear path between two rooms.
	 *
	 * @param start start index
	 * @param end end index
	 * @return clear path or null
	 */
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

	/**
	 * Checks if the player can pass through a room.
	 *
	 * @param roomIndex room to check
	 * @param destinationIndex destination room
	 * @return true if passable
	 */
	private boolean canPassThrough(int roomIndex, int destinationIndex) {
		if (roomIndex == destinationIndex)
			return true;

		Room room = rooms.get(roomIndex);
		return room.type() == Type.CORRIDOR || isVisited(roomIndex);
	}
}
