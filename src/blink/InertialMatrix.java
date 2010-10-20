package blink;

import java.util.Random;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class InertialMatrix {
    private int _n;
    private double _M[][];

    public static double[][] copy(int[][] M) {
        double Md[][] = new double[M.length][M.length];
        for (int i=0;i<M.length;i++)
            for (int j=0;j<M.length;j++) {
                Md[i][j] = M[i][j];
            }
        return Md;
    }

    public InertialMatrix(int M[][]) {
        this(copy(M));
    }

    public InertialMatrix(double M[][]) {
        _M = M;
        _n = _M.length;

        // this.print("Matriz Inicial");

        int k=0;
        while (k < _n) {
            if (Math.abs(_M[k][k]) > 1.0e-5) {

                // convert position k,k to 1 or -1
                double s = 1/Math.sqrt(Math.abs(_M[k][k]));
                scaleLine(k,s);
                scaleColumn(k,s);

                // this.print(String.format("Transformando valor nao nulo em %d %d em 1 ou -1",k,k));

                // convert all lines different of 1 or -1 to 1 or -1
                for (int i=k+1;i<_n;i++) {
                    if (Math.abs(_M[i][k])  > 1.0e-5) {
                        double si = - _M[k][k]/_M[i][k];
                        scaleLine(i,si);
                        scaleColumn(i,si);

                        // this.print(String.format("Transformando valor nao nulo em %d %d e em %d %d em -M[%d,%d]",i,k,k,i,k,k));

                        sumRow(i,k);
                        sumColumn(i,k);

                        // this.print(String.format("Somando linha e coluna %d com linha e coluns %d",i,k));

                    }
                }
                k++;
            }
            else {
                // find someone on the column
                // that is different from zero
                int i=k+1;
                for (;i<_n;i++) {
                    if (Math.abs(M[i][k]) > 1.0e-5)
                        break;
                }

                if (i == _n) {
                    k++;
                    continue;
                }
                else {
                    // swap line i with line i+k AND
                    // swap column i with column k
                    sumRow(k,i);
                    sumColumn(k,i);

                    // this.print(String.format("Somar linha e coluna %d com linha e coluns %d",i,k));

                }
            }
        }

        // this.print(String.format("Resultado Final"));

    }

    public int getDiagonalSum() {
        double sum = 0;
        for (int i=0;i<_n;i++)
            sum+=_M[i][i];
        return (int)Math.round(sum);
    }

    private void sumColumn(int j1, int j2) {
        for (int i=0;i<_n;i++) {
            _M[i][j1] = _M[i][j1] + _M[i][j2];
        }
    }

    private void sumRow(int i1, int i2) {
        for (int j=0;j<_n;j++) {
            _M[i1][j] = _M[i1][j] + _M[i2][j];
        }
    }

    private void scaleLine(int i, double s) {
        for (int j=0;j<_n;j++) {
            _M[i][j] = s * _M[i][j];
        }
    }

    private void scaleColumn(int j, double s) {
        for (int i=0;i<_n;i++) {
            _M[i][j] = s * _M[i][j];
        }
    }

    public void inercialMatrix(int M[][]) {
        int n = M.length;
        int i=0;
        while (i < n) {
            if (M[i][i] == 0) {
            }
        }
    }

    public void print(String title) {
        if (title != null)
            System.out.println(title);
        for (int i=0;i<_n;i++) {
            for (int j = 0; j < _n; j++)
                System.out.print(String.format("%10.4f ", _M[i][j]));
            System.out.println();
        }
    }

    public static double[][] random(int n) {
        Random r = new Random();
        double[][] result = new double[n][n];
        for (int i=0;i<n;i++)
            for (int j=i;j<n;j++) {
                int k = r.nextInt(20);
                if (r.nextBoolean())
                    k = -k;
                result[i][j] = k;
                result[j][i] = k;
            }
        return result;
    }

    public static void main(String[] args) {
        new InertialMatrix(new double[][] {{0,0,-1},{0,0,0},{-1,0,1}});
    }

}
