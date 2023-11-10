package pw.rxj.iron_quarry.util;

import oshi.util.tuples.Pair;

import java.util.Random;

public class FortuneUtil {
    private static final Random random = new Random();

    private static Pair<Integer, Integer> findFortuneLevels(double targetProbability) {
        double lastProbability = toProbability(0);

        for (int level = 1;; level++) {
            double currentProbability = toProbability(level);

            if(lastProbability <= targetProbability && currentProbability >= targetProbability) {
                return new Pair<>(level - 1, level);
            }

            lastProbability = currentProbability;
        }
    }

    private static double getFortuneWeight(double targetProbability, int level1, int level2) {
        double probability1 = toProbability(level1);
        double probability2 = toProbability(level2);

        return (targetProbability - probability2) / (probability1 - probability2);
    }

    private static int getWeightedFortuneLevel(double targetProbability, int level1, int level2) {
        double weight = getFortuneWeight(targetProbability, level1, level2);

        return random.nextDouble() <= weight ? level1 : level2;
    }

    public static int fromProbability(double fortuneMultiplier) {
        Pair<Integer, Integer> fortuneCandidates = findFortuneLevels(fortuneMultiplier);

        return getWeightedFortuneLevel(fortuneMultiplier, fortuneCandidates.getA(), fortuneCandidates.getB());
    }

    public static double toProbability(int level) {
        return 1.0/(level + 2.0) + (level + 1.0)/2.0;
    }
}
