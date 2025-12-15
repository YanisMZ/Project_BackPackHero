package fr.uge.implement;

public class DisplayedItem {

    private final Item item; // l'item réel
    private int screenX;
    private int screenY;
    private boolean onScreen;

    public DisplayedItem(Item item, int x, int y) {
        this.item = item;
        this.screenX = x;
        this.screenY = y;
        this.onScreen = true;
    }

    public Item getItem() {
        return item;
    }

    public int getScreenX() {
        return screenX;
    }

    public void setScreenX(int screenX) {
        this.screenX = screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public void setScreenY(int screenY) {
        this.screenY = screenY;
    }

    public boolean isOnScreen() {
        return onScreen;
    }

    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
    }

    public int width() {
        return item.width();
    }

    public int height() {
        return item.height();
    }

    public String name() {
        return item.name();
    }
}
