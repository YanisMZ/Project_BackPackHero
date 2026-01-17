package fr.uge.items;

import java.awt.Point;

public class FloatingItem {
	public Item item;
	public Point position;

	public FloatingItem(Item item, Point position) {
		this.item = item;
		this.position = position;
	}

	public Item item() {
		return item;
	}
}
