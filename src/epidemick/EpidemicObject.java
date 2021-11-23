package epidemick;

/**
 * Represents a square on the game grid.
 */
public class EpidemicObject {
    public String state = "normal";
    public int[] position;
    public int tickWhenInfected;

    EpidemicObject(int i, int j) {
        this.position = new int[] {i, j};
    }

    public void infect(int ticks) {
        state = "infected";
        tickWhenInfected = ticks; // store to determine when this should recover.
    }

    public void recover() {
        state = "recovered";
    }

    public void kill() {
        state = "deceased";
    }

    public void vaccinate() {
        state = "vaccinated";
    }

    public void makeWall() {
        state = "wall";
    }

}
