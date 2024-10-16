package com.example.proiectlicenta.data.model;

public class BodyFatModel {
    // coeficientii obtinuti din modelul de invatare automata
    private static final double INTERCEPT = -44.59543335161164;
    private static final double COEF_ABDOMEN = 0.9662478361374708;
    private static final double COEF_WEIGHT = -0.14369853877218286;

    public double calculateBodyFat(double abdomen, double weightKg) {
        // convertim greutatea din kilograme in lbs
        double weightLbs = weightKg * 2.20462;
        // utilizam coeficientii obtinuti din modelul de invatare automata
        return INTERCEPT + (COEF_ABDOMEN * abdomen) + (COEF_WEIGHT * weightLbs);
    }
}

