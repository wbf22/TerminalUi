package util;

import java.util.Random;

public class RandomPlus {
    
    public static Random random = new Random();

    public static void setRandom(Random random) {
        RandomPlus.random = random;
    }

    public static double weighted(double min, double max, double average, double weight) {
        if (weight > 1.0 || weight < 0.0) throw new IllegalArgumentException("Weight must be 0.0 to 1.0");

        double first = RandomPlus.random.nextDouble() * (max - min) + min;
        double second = RandomPlus.random.nextDouble() * (max - min) + min;
        double firstWeight = Math.abs(first - average) < Math.abs(second - average) ? weight : 1 - weight;
        double secondWeight = 1 - firstWeight;

        return first * firstWeight + second * secondWeight;
    }
}
