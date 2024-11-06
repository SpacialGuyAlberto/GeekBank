package com.geekbank.bank.models;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CosineSimilarity {

    public static double cosineSimilarity(final Map<String, Integer> leftVector,
                                          final Map<String, Integer> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Los vectores de entrada no deben ser nulos");
        }

        Set<String> intersection = getIntersection(leftVector, rightVector);

        double dotProduct = dot(leftVector, rightVector, intersection);
        double normLeft = norm(leftVector);
        double normRight = norm(rightVector);

        if (normLeft == 0.0 || normRight == 0.0) {
            return 0.0;
        } else {
            return dotProduct / (normLeft * normRight);
        }
    }

    private static Set<String> getIntersection(final Map<String, Integer> leftVector,
                                               final Map<String, Integer> rightVector) {
        Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    private static double dot(final Map<String, Integer> leftVector,
                              final Map<String, Integer> rightVector,
                              final Set<String> intersection) {
        double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    private static double norm(final Map<String, Integer> vector) {
        double sum = 0.0;
        for (Integer value : vector.values()) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}
