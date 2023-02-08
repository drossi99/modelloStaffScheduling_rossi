package utility;

import java.util.Random;

public class EstrazioniCasuali {
    private static Random rand = new Random();

    public static int estraiIntero(int min, int max) {
        int range = max + 1 - min;
        int casual = rand.nextInt(range);
        return casual + min;
    }

    public static double estraiDecimale(double min, double max) {
        double range = max + 1 - min;
        double casual = rand.nextDouble(range);
        return casual + min;
    }
}
