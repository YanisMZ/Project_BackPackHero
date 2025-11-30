package fr.uge.implement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class BackPack {
	private HashMap<Item,Integer> items;
	private int counter = 0;
	private int maxBackPack = 15;

	public BackPack() {
		this.items = new HashMap<>();
		
	}
	
	
	public int add(Item ele) {
    Objects.requireNonNull(ele);
    if (counter <= maxBackPack) {
        items.put(ele, counter++);
        return 1;
    }
    return 0;
}
	
	public Map<Item, Integer> items() {
    return items;
}


	
	public String BackPackInfo() {
    return items.entrySet()
                .stream()
                .map(e -> e.getKey() + " : " + e.getValue())
                .collect(Collectors.joining("\n"));
}
	
	
	public static void fillBackPackForTest(BackPack backPack) {
    for (int i = 1; i <= 5; i++) {
        String name = "Sword_" + i;    
        int damage = 10 * i;           
        backPack.add(new Sword(name, damage));
    }
}
	
	
	public String BackPackDisplay() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== BACKPACK (3 x 5) ===\n");


    Item[] slots = new Item[maxBackPack];

    items.forEach((item, index) -> {
        if (index < maxBackPack) {
            slots[index] = item;
        }
    });

    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 5; col++) {
            int index = row * 5 + col;
            if (slots[index] != null) {
                sb.append(String.format("[%s]", slots[index].name()));
            } else {
                sb.append("[ EMPTY ]");
            }
            sb.append(" ");
        }
        sb.append("\n");
    }

    return sb.toString();
}



}
	


