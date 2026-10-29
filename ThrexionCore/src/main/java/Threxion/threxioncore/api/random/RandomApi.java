package Threxion.threxioncore.api.random;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 统一 Random API（规格 random_api）：Core 提供统一随机系统，
 * 各 Mod 无需独立实现。用途：random_add / random_multiply / probability /
 * random_recipe / random_failure。
 */
public final class RandomApi {

    private RandomApi() {
    }

    /** 返回 [min, max] 闭区间内的随机 double。min > max 时自动交换。 */
    public static double random(double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max)) {
            return min;
        }
        if (min > max) {
            double t = min;
            min = max;
            max = t;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /** 返回 [min, max] 闭区间内的随机 int（含两端）。 */
    public static int randomInt(int min, int max) {
        if (min > max) {
            int t = min;
            min = max;
            max = t;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static long randomLong(long min, long max) {
        if (min > max) {
            long t = min;
            min = max;
            max = t;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /** 以 probability(0~1) 概率返回 true。 */
    public static boolean chance(double probability) {
        if (probability <= 0.0) {
            return false;
        }
        if (probability >= 1.0) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    public static <T> T pick(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[randomInt(0, array.length - 1)];
    }

    public static <T> T pick(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(randomInt(0, list.size() - 1));
    }

    /** 返回一个新的非确定性种子；可用于随机配方 / 随机失败判定等。 */
    public static long nextSeed() {
        return ThreadLocalRandom.current().nextLong();
    }
}
