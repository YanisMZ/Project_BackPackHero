package fr.uge.implement;

public class Merchant {
    private final Grid stock;

    public Merchant(int rows, int cols) {
        this.stock = new Grid(rows, cols);
        generateStock();
    }

    public Grid getStock() { return stock; }

    public void generateStock() {
        stock.clear();
        stock.generateRandomItemsMarchant(3, 7);
    }

    // ACHAT (joueur achète au marchand)
    public boolean buyItem(Item item, Hero hero) {
        int price = item.price();

        if (!hero.hasEnoughGold(price)) {
            System.out.println("Pas assez d'or ! Prix: " + price + ", Or: " + hero.gold());
            return false;
        }

        // Retirer l'or avant d'ajouter l'item
        hero.removeGold(price);

        // Si l'item est de l'or, fusionner avec le stack existant
        if (item.isStackable() && item.name().equals("Gold")) {
            hero.addGold(item.quantity());
        } else {
            if (!hero.getBackpack().autoAdd(item)) {
                System.out.println("Pas de place dans le sac !");
                // Rembourser l'or si achat échoue
                hero.addGold(price);
                return false;
            }
        }

        stock.removeItem(item);
        System.out.println("Achat réussi : " + item.name() + " pour " + price + " or");
        return true;
    }

 
    public boolean sellItem(Item item, Hero hero) {
        int sellPrice = item.price() / 2; // Revente à 50%

    
        hero.addGold(sellPrice);

        hero.getBackpack().remove(item);

        System.out.println("Vente réussie : " + item.name() + " pour " + sellPrice + " or");
        return true;
    }
}
