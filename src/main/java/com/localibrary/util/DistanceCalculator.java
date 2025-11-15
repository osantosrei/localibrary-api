package com.localibrary.util;

/**
 * Classe utilitária para cálculo de distâncias geográficas.
 * Utiliza a fórmula de Haversine para calcular distância entre dois pontos.
 *
 * A fórmula de Haversine calcula a distância do grande círculo entre dois pontos
 * em uma esfera a partir de suas longitudes e latitudes.
 */
public class DistanceCalculator {

    // Raio médio da Terra em quilômetros
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcula a distância entre duas coordenadas geográficas usando a fórmula de Haversine.
     *
     * @param lat1 Latitude do primeiro ponto (em graus)
     * @param lon1 Longitude do primeiro ponto (em graus)
     * @param lat2 Latitude do segundo ponto (em graus)
     * @param lon2 Longitude do segundo ponto (em graus)
     * @return Distância em quilômetros
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Converter graus para radianos
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Diferenças
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Fórmula de Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distância em quilômetros
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calcula a distância entre duas coordenadas e retorna em metros.
     *
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @return Distância em metros
     */
    public static double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000;
    }

    /**
     * Formata a distância para exibição amigável.
     * Se a distância for menor que 1km, exibe em metros.
     * Caso contrário, exibe em quilômetros com 2 casas decimais.
     *
     * @param distanceKm Distância em quilômetros
     * @return String formatada (ex: "500 m" ou "2.5 km")
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            int meters = (int) Math.round(distanceKm * 1000);
            return meters + " m";
        } else {
            return String.format("%.2f km", distanceKm);
        }
    }

    /**
     * Verifica se um ponto está dentro de um raio específico de outro ponto.
     *
     * @param lat1 Latitude do ponto central
     * @param lon1 Longitude do ponto central
     * @param lat2 Latitude do ponto a verificar
     * @param lon2 Longitude do ponto a verificar
     * @param radiusKm Raio em quilômetros
     * @return true se o ponto está dentro do raio, false caso contrário
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }

    /**
     * Calcula o ponto médio entre duas coordenadas.
     * Útil para centralizar mapas entre múltiplos pontos.
     *
     * @param lat1 Latitude do primeiro ponto
     * @param lon1 Longitude do primeiro ponto
     * @param lat2 Latitude do segundo ponto
     * @param lon2 Longitude do segundo ponto
     * @return Array com [latitude, longitude] do ponto médio
     */
    public static double[] calculateMidpoint(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);

        // Converter para radianos
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double bx = Math.cos(lat2) * Math.cos(dLon);
        double by = Math.cos(lat2) * Math.sin(dLon);

        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2),
                Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by));
        double lon3 = lon1 + Math.atan2(by, Math.cos(lat1) + bx);

        return new double[]{Math.toDegrees(lat3), Math.toDegrees(lon3)};
    }

    // Construtor privado
    private DistanceCalculator() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }
}