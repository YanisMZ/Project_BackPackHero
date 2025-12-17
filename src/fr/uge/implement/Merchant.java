package fr.uge.implement;

public class Merchant {
    private final Grid stock;

    public Merchant(int rows, int cols) {
        this.stock = new Grid(rows, cols);
        generateStock();
    }

    public Grid getStock() { 
        return stock; 
    }

    public void generateStock() {
        stock.clear();
        // Générez des items avec des prix définis
        stock.generateRandomItems(3, 7);
    }

    // ACHAT (joueur achète au marchand)
    public boolean buyItem(Item item, Hero hero) {
        int price = item.price();
        
        if (!hero.hasEnoughGold(price)) {
            System.out.println("Pas assez d'or ! Prix: " + price + ", Or: " + hero.gold());
            return false;
        }
        
        if (!hero.getBackpack().autoAdd(item)) {
            System.out.println("Pas de place dans le sac !");
            return false;
        }

        hero.removeGold(price);
        stock.removeItem(item);
        System.out.println("Achat réussi : " + item.name() + " pour " + price + " or");
        return true;
    }

    // VENTE (joueur vend au marchand)
    public boolean sellItem(Item item, Hero hero) {
        int sellPrice = item.price() / 2; // Revente à 50%
        
        hero.getBackpack().remove(item);
        hero.addGold(sellPrice);
        
        // Optionnel : ajouter l'item au stock du marchand
        // stock.autoAdd(item);
        
        System.out.println("Vente réussie : " + item.name() + " pour " + sellPrice + " or");
        return true;
    }
}