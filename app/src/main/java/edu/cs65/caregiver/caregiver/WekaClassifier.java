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

        System.out.println("acceleration p = " + ((Double) i[64]).doubleValue());

        if (i[0] == null) {
            p = 0;
        }
            else if (((Double) i[0]).doubleValue() <= 625.752581) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 625.752581) {
            p = WekaClassifier.N6a1d825e1(i);
        }
        return p;
    }
    static double N6a1d825e1(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 1000.069166) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 1000.069166) {
            p = 2;
        }
        return p;
    }
}
