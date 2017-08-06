package com.example.archismansarkar.accelerometer_implementation;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.RealMatrixImpl;

public class SGFilter {

    private static int nl;

    private static int nr;

    public interface DataFilter {

        double[] filter(double[] data);
    }

    public interface Preprocessor {

        /**
         * Data processing method. Called on Preprocessor instance when its
         * processing is needed
         *
         * @param data
         */
        void apply(double[] data);
    }

    private static final List<DataFilter> dataFilters = new ArrayList<DataFilter>();

    private static final List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();

    /////////////////////////////////////////////////////////////////////////////////////
    public static double[] computeSGCoefficients(int nl, int nr, int degree) {
        if (nl < 0 || nr < 0 || nl + nr < degree)
            throw new IllegalArgumentException("Bad arguments");
        RealMatrixImpl matrix = new RealMatrixImpl(degree + 1, degree + 1);
        double[][] a = matrix.getDataRef();
        double sum;
        for (int i = 0; i <= degree; i++) {
            for (int j = 0; j <= degree; j++) {
                sum = (i == 0 && j == 0) ? 1 : 0;
                for (int k = 1; k <= nr; k++)
                    sum += pow(k, i + j);
                for (int k = 1; k <= nl; k++)
                    sum += pow(-k, i + j);
                a[i][j] = sum;
            }
        }
        double[] b = new double[degree + 1];
        b[0] = 1;
        b = matrix.solve(b);
        double[] coeffs = new double[nl + nr + 1];
        for (int n = -nl; n <= nr; n++) {
            sum = b[0];
            for (int m = 1; m <= degree; m++)
                sum += b[m] * pow(n, m);
            coeffs[n + nl] = sum;
        }
        return coeffs;
    }

    public SGFilter(int nl, int nr) {
        if (nl < 0 || nr < 0)
            throw new IllegalArgumentException("Bad arguments");
        this.nl = nl;
        this.nr = nr;
    }

    public static double[] smooth(double[] data, double[] coeffs) {
        return smooth(data, 0, data.length, coeffs);
    }


    public static double[] smooth(double[] data, double[] leftPad, double[] rightPad,
                           int bias, double[][] coeffs) {
        if (bias < 0 || bias > nr || bias > nl)
            throw new IllegalArgumentException(
                    "bias < 0 or bias > nr or bias > nl");
        for (DataFilter dataFilter : dataFilters) {
            data = dataFilter.filter(data);
        }
        int dataLength = data.length;
        if (dataLength == 0)
            return data;
        int n = dataLength + nl + nr;
        double[] dataCopy = new double[n];
        // copy left pad reversed
        int leftPadOffset = nl - leftPad.length;
        if (leftPadOffset >= 0)
            for (int i = 0; i < leftPad.length; i++) {
                dataCopy[leftPadOffset + i] = leftPad[i];
            }
        else
            for (int i = 0; i < nl; i++) {
                dataCopy[i] = leftPad[i - leftPadOffset];
            }
        // copy actual data
        for (int i = 0; i < dataLength; i++) {
            dataCopy[i + nl] = data[i];
        }
        // copy right pad
        int rightPadOffset = nr - rightPad.length;
        if (rightPadOffset >= 0)
            for (int i = 0; i < rightPad.length; i++) {
                dataCopy[i + dataLength + nl] = rightPad[i];
            }
        else
            for (int i = 0; i < nr; i++) {
                dataCopy[i + dataLength + nl] = rightPad[i];
            }
        for (Preprocessor p : preprocessors) {
            p.apply(dataCopy);
        }
        // convolution (with savitzky-golay coefficients)
        double[] sdata = new double[dataLength];
        double[] sg;
        for (int b = bias; b > 0; b--) {
            sg = coeffs[coeffs.length - b];
            int x = (nl + bias) - b;
            double sum = 0;
            for (int i = -nl + b; i <= nr; i++) {
                sum += dataCopy[x + i] * sg[nl - b + i];
            }
            sdata[x - nl] = sum;
        }
        sg = coeffs[0];
        for (int x = nl + bias; x < n - nr - bias; x++) {
            double sum = 0;
            for (int i = -nl; i <= nr; i++) {
                sum += dataCopy[x + i] * sg[nl + i];
            }
            sdata[x - nl] = sum;
        }
        for (int b = 1; b <= bias; b++) {
            sg = coeffs[b];
            int x = (n - nr - bias) + (b - 1);
            double sum = 0;
            for (int i = -nl; i <= nr - b; i++) {
                sum += dataCopy[x + i] * sg[nl + i];
            }
            sdata[x - nl] = sum;
        }
        return sdata;
    }


    public static double[] smooth(double[] data, int from, int to, double[] coeffs) {
        return smooth(data, from, to, 0, new double[][] { coeffs });
    }


    public static double[] smooth(double[] data, int from, int to, int bias,
                           double[][] coeffs) {
        double[] leftPad = Arrays.copyOfRange(data, 0, from);
        double[] rightPad = Arrays.copyOfRange(data, to, data.length);
        double[] dataCopy = Arrays.copyOfRange(data, from, to);
        return smooth(dataCopy, leftPad, rightPad, bias, coeffs);
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /*
    private static void convertDoubleArrayToFloat(double[] in, float[] out) {
        for (int i = 0; i < in.length; i++)
            out[i] = (float) in[i];
    }

    private static void convertFloatArrayToDouble(float[] in, double[] out) {
        for (int i = 0; i < in.length; i++)
            out[i] = in[i];
    }
*/

    /*
    public void appendDataFilter(DataFilter dataFilter) {
        dataFilters.add(dataFilter);
    }

    public void appendPreprocessor(Preprocessor p) {
        preprocessors.add(p);
    }

    public int getNl() {
        return nl;
    }

    public int getNr() {
        return nr;
    }

    public void insertDataFilter(DataFilter dataFilter, int index) {
        dataFilters.add(index, dataFilter);
    }

    public void insertPreprocessor(Preprocessor p, int index) {
        preprocessors.add(index, p);
    }

    public boolean removeDataFilter(DataFilter dataFilter) {
        return dataFilters.remove(dataFilter);
    }

    public DataFilter removeDataFilter(int index) {
        return dataFilters.remove(index);
    }

    public Preprocessor removePreprocessor(int index) {
        return preprocessors.remove(index);
    }

    public boolean removePreprocessor(Preprocessor p) {
        return preprocessors.remove(p);
    }

    public void setNl(int nl) {
        if (nl < 0)
            throw new IllegalArgumentException("nl < 0");
        this.nl = nl;
    }

    public void setNr(int nr) {
        if (nr < 0)
            throw new IllegalArgumentException("nr < 0");
        this.nr = nr;
    }

    */

    /*
    public static double[] smooth(double[] data, double[] leftPad, double[] rightPad,
                           double[] coeffs) {
        return smooth(data, leftPad, rightPad, 0, new double[][] { coeffs });
    }

    public static float[] smooth(float[] data, float[] leftPad, float[] rightPad,
                          int bias, double[][] coeffs) {
        double[] dataAsDouble = new double[data.length];
        double[] leftPadAsDouble = new double[leftPad.length];
        double[] rightPadAsDouble = new double[rightPad.length];
        convertFloatArrayToDouble(data, dataAsDouble);
        convertFloatArrayToDouble(leftPad, leftPadAsDouble);
        convertFloatArrayToDouble(rightPad, rightPadAsDouble);
        double[] results = smooth(dataAsDouble, leftPadAsDouble,
                rightPadAsDouble, bias, coeffs);
        float[] resultsAsFloat = new float[results.length];
        convertDoubleArrayToFloat(results, resultsAsFloat);
        return resultsAsFloat;
    }


    public static float[] smooth(float[] data, int from, int to, double[] coeffs) {
        return smooth(data, from, to, 0, new double[][] { coeffs });
    }


    public static float[] smooth(float[] data, int from, int to, int bias,
                          double[][] coeffs) {
        float[] leftPad = Arrays.copyOfRange(data, 0, from);
        float[] rightPad = Arrays.copyOfRange(data, to, data.length);
        float[] dataCopy = Arrays.copyOfRange(data, from, to);
        return smooth(dataCopy, leftPad, rightPad, bias, coeffs);
    }


    public static float[] smooth(float[] data, double[] coeffs) {
        return smooth(data, 0, data.length, coeffs);
    }


    public static float[] smooth(float[] data, float[] leftPad, float[] rightPad,
                          double[] coeffs) {
        return smooth(data, leftPad, rightPad, 0, new double[][] { coeffs });
    }
*/

}
