package com.geekbank.bank.models;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CosineSimilarity {

    /**
     * Calcula la similitud de coseno entre dos vectores de frecuencias de términos.
     *
     * @param leftVector  Mapa de frecuencia de términos del primer documento
     * @param rightVector Mapa de frecuencia de términos del segundo documento
     * @return Similitud de coseno entre los dos vectores
     */
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
            // Uno de los vectores es un vector cero
            return 0.0;
        } else {
            return dotProduct / (normLeft * normRight);
        }
    }

    /**
     * Obtiene el conjunto de claves comunes entre dos mapas.
     *
     * @param leftVector  Primer mapa vectorial
     * @param rightVector Segundo mapa vectorial
     * @return Conjunto de claves comunes
     */
    private static Set<String> getIntersection(final Map<String, Integer> leftVector,
                                               final Map<String, Integer> rightVector) {
        Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Calcula el producto punto de dos vectores para las claves comunes.
     *
     * @param leftVector  Primer mapa vectorial
     * @param rightVector Segundo mapa vectorial
     * @param intersection Conjunto de claves comunes
     * @return Producto punto
     */
    private static double dot(final Map<String, Integer> leftVector,
                              final Map<String, Integer> rightVector,
                              final Set<String> intersection) {
        double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    /**
     * Calcula la norma Euclidiana de un vector.
     *
     * @param vector El mapa vectorial
     * @return Norma del vector
     */
    private static double norm(final Map<String, Integer> vector) {
        double sum = 0.0;
        for (Integer value : vector.values()) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }
}
