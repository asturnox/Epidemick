package epidemick;

public class EpidemicObject {
    String state = "normal";
    int[] position;
    int tickWhenInfected;

    EpidemicObject(int i, int j) {
        this.position = new int[] {i, j};
    }

    public void infect() {
        state = "infected";
        tickWhenInfected = ticks;
        arrayToInfect.add(this);
    }

    public void attemptInfect() {
        float x = randomizer.nextFloat();
        if (x <= pValue && state.equals("normal")) {
            this.infect();
        }
    }

    public void recover() {
        state = "recovered";
    }

    public void kill() {
        state = "deceased";
        deaths++;
    }

    public void vaccinate() {
        state = "vaccinated";
    }

    public void makeWall() {
        state = "wall";
    }

}
