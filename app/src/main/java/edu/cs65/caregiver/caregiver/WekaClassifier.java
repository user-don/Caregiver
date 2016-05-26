package edu.cs65.caregiver.caregiver;


public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N79cbdcde0(i);
        return p;
    }
    static double N79cbdcde0(Object []i) {
        double p = Double.NaN;

        if (i[64] == null) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() <= 90.752581) {
            p = 0;
        } else if (((Double) i[64]).doubleValue() > 90.752581) {
            p = 1;
        }
        return p;
    }

}
