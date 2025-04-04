package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

public interface IX01DartBotAccuracyCalculator {
    double createOffsetR(double targetOneDartAvg, double currentOneDartAvg);

    double createOffsetTheta(double targetOneDartAvg, double currentOneDartAvg);
}