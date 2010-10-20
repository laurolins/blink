package blink;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;

/**
 * Smith
 */
public class Smith {
    private MatrixBI _A; // smith normal form
    private MatrixBI _S; // smith normal form
    private MatrixBI _R; // smith normal form
    private MatrixBI _C; // smith normal form
    public Smith(MatrixBI A) {
        _A = A.copy();
        _S = A.copy();
        _R = new MatrixBI(A.getNumRows());
        _C = new MatrixBI(A.getNumColumns());
        this.smith();
    }

    public MatrixBI getSmithNormalForm() {
        return _S;
    }

    public MatrixBI getA() {
        return _A;
    }

    public MatrixBI getR() {
        return _R;
    }

    public MatrixBI getC() {
        return _C;
    }

    public int getCurrentNunZeroDiagonalElements() {
        int k=0;
        for (int i=0;i<Math.min(_S.getNumRows(),_S.getNumColumns());i++)
            if (_S.get(i,i).signum() != 0)
                k++;
        return k;
    }

    /**
     * Dynamically change Smith.
     */
    public void addRow(int[] row) {
        int n = _C.getNumRows();

        // row
        if (row.length != n)
            throw new RuntimeException("Problema");

        // calculate B . C
        BigInteger brow[] = new BigInteger[n];
        for (int j=0;j<n;j++) {
            BigInteger x = BigInteger.ZERO;
            for (int k=0;k<n;k++) {
                x = x.add(BigInteger.valueOf(row[k]).multiply(_C.get(k,j)));
            }
            brow[j] = x;
        }

        // matrix A
        _A.addRow(row);

        // matrix S
        _S.addRow(brow);

        // matrix R
        _R.addRowAndColumn();
        _R.set(_R.getNumRows() - 1, _R.getNumColumns() - 1, BigInteger.ONE);
    }


    /**
     * Dynamically change Smith.
     */
    public boolean isAnIntegralCombination(int[] row) {
        int n = row.length;

        // calculate B . C
        BigInteger brow[] = new BigInteger[n];
        for (int j=0;j<n;j++) {
            BigInteger x = BigInteger.ZERO;
            for (int k=0;k<n;k++) {
                x = x.add(BigInteger.valueOf(row[k]).multiply(_C.get(k,j)));
            }
            brow[j] = x;
        }

        //
        int j=0;
        for (;j<Math.min(n,_S.getNumRows());j++) {
            if (brow[j].compareTo(BigInteger.ZERO) == 0) //
                continue;
            BigInteger s_jj = _S.get(j,j);
            if (s_jj.compareTo(BigInteger.ZERO) == 0)
                return false;
            if (brow[j].remainder(s_jj).compareTo(BigInteger.ZERO) != 0) {
                return false;
            }
        }

        for (;j<brow.length;j++) {
            if (brow[j].compareTo(BigInteger.ZERO) != 0) //
                return false;
        }

        return true;
    }

    public void smith() {
        int t = 0;
        //System.out.println("(A-rows) "+_S.getNumRows()+" ");
        while (t < Math.min(_S.getNumColumns(),_S.getNumRows())) {
            //System.out.print(String.format("# Smith (%d,%d)... ",t,t));
            //System.out.print(t+" ");
            //if (t % 50 == 0)
            //    System.out.println("");


            /* if (t == 244) {
                try {
                    PrintWriter pw = new PrintWriter(new File("c:/workspace/permanent/res/log.txt"));
                    pw.println("t = " + t);
                    _S.print(pw);
                    pw.flush();
                    pw.close();
                } catch (FileNotFoundException ex) {
                }
            } */

            int[] xx = _S.findMinimumNonZeroAbsoluteValue(t);
            int rr = xx[0];
            int cc = xx[1];

            // no columns with non-zero entry after line t? then finished!
            if (cc < 0 || rr < 0)
                break; // finished

            // swap lines make (t,t) become (rr,cc)

            _S.swapColumns(t,cc);
            _C.swapColumns(t,cc);

            _S.swapRows(t,rr);
            _R.swapRows(t,rr);

            // LOG
            /*
            System.out.println(String.format("# Starting (%d,%d)... ",t,t));
            System.out.println(String.format("# Minimum Entry (%d,%d)... ",rr,cc));
            System.out.println("# -");
            System.out.println(_S.toString());
            */

            boolean cleanColumnAndCleanRow = true;
            while (true) {

                // LOG
                // System.out.println(String.format("# Cleaning column %d... ",t));
                // System.out.println(String.format("# Pivot %s... ",_S.get(t,t).toString()));

                // pivot is negative? make it positive
                if (_S.get(t,t).compareTo(BigInteger.ZERO) < 0) {
                    _S.multiplyRowByScalar(t, -1);
                    _R.multiplyRowByScalar(t, -1);
                }

                // clean column t (always possible)
                for (int i = t + 1; i < _S.getNumRows(); i++) {
                    BigInteger s_tt = _S.get(t, t);
                    BigInteger s_it = _S.get(i, t);

                    // pivot is negative? make it positive
                    if (s_it.compareTo(BigInteger.ZERO) < 0) {
                        _S.multiplyRowByScalar(i, -1);
                        _R.multiplyRowByScalar(i, -1);

                        // LOG
                        // System.out.println(_S.toString());

                        s_it = s_it.negate();
                    }

                    //
                    BigInteger q = s_it.divide(s_tt);
                    BigInteger r = s_it.remainder(s_tt);
                    if (r.compareTo(BigInteger.ZERO) == 0) { // it is not a disior
                        _S.sumScaledRowsAndReplace(i, BigInteger.ONE, t, q.negate());
                        _R.sumScaledRowsAndReplace(i, BigInteger.ONE, t, q.negate());

                        // LOG
                        // System.out.println(_S.toString());

                    } else {
                        _S.sumScaledRowsAndReplace(i, BigInteger.ONE, t, q.negate());
                        _R.sumScaledRowsAndReplace(i, BigInteger.ONE, t, q.negate());

                        // LOG
                        // System.out.println(_S.toString());

                        _S.swapRows(t, i);
                        _R.swapRows(t, i);

                        // LOG
                        // System.out.println(_S.toString());

                        i--;
                    }
                }

                // clean row t (not always possible)

                // LOG
                // System.out.println(String.format("# Cleaning row %d... ",t));

                for (int j = t + 1; j < _S.getNumColumns(); j++) {
                    BigInteger s_tt = _S.get(t, t);
                    BigInteger s_tj = _S.get(t, j);

                    // pivot is negative? make it positive
                    if (s_tj.compareTo(BigInteger.ZERO) < 0) {
                        _S.multiplyColumnByScalar(j, -1);
                        _C.multiplyColumnByScalar(j, -1);

                        // LOG
                        // System.out.println(_S.toString());

                        s_tj = s_tj.negate();
                    }

                    //
                    BigInteger q = s_tj.divide(s_tt);
                    BigInteger r = s_tj.remainder(s_tt);
                    if (r.compareTo(BigInteger.ZERO) == 0) { // it is not a disior
                        _S.sumScaledColumnAndReplace(j, BigInteger.ONE, t, q.negate());
                        _C.sumScaledColumnAndReplace(j, BigInteger.ONE, t, q.negate());

                        // LOG
                        // System.out.println(_S.toString());

                    } else {
                        _S.sumScaledColumnAndReplace(j, BigInteger.ONE, t, q.negate());
                        _C.sumScaledColumnAndReplace(j, BigInteger.ONE, t, q.negate());

                        // LOG
                        // System.out.println(_S.toString());
                        _S.swapColumns(t, j);
                        _C.swapColumns(t, j);

                        // LOG
                        // System.out.println(_S.toString());

                        cleanColumnAndCleanRow = false;
                        break;
                    }
                }

                // find someone that is not divided by the pivot
                if (cleanColumnAndCleanRow) {
                    BigInteger s_tt = _S.get(t, t);
                    if (s_tt.compareTo(BigInteger.ZERO) != 0) {
                        for (int i = t + 1; i < _S.getNumRows(); i++) {
                            for (int j = t + 1; j < _S.getNumColumns(); j++) {
                                BigInteger s_ij = _S.get(i, j);
                                if (s_ij.compareTo(BigInteger.ZERO) == 0)
                                    continue;
                                if (s_ij.remainder(s_tt).compareTo(BigInteger.ZERO) != 0) {
                                    _S.sumScaledRowsAndReplace(t,BigInteger.ONE, i, BigInteger.ONE);
                                    _R.sumScaledRowsAndReplace(t,BigInteger.ONE, i, BigInteger.ONE);

                                    cleanColumnAndCleanRow = false;

                                    // LOG
                                    // System.out.println(_S.toString());

                                    break;
                                }
                            }

                            if (!cleanColumnAndCleanRow)
                                break;
                        }
                    }
                } // find someone that is not divided by the pivot


                if (cleanColumnAndCleanRow) { // cleaned?
                    break;
                } else cleanColumnAndCleanRow = true;

            }



            t++;
        }

    }

    public static void main2(String[] args) throws Exception {
        PrintWriter pw = new PrintWriter(new File("res/smith.txt"));
        // PrintWriter pw = new PrintWriter(System.out);
        // MatrixBI m = new MatrixBI(new int[][] {{33, 88, -2, -90, 36, -69, 38, -19, 94, -10, -82, 78, 97, 61, -2, 98, 85, 52, -53, -51}, {-18, -15, 14, 60, -29, 42, -93, 48, 55, -86, 3, -50, 30, 75, 88, 86, -24, -68, -25, 30}, {-78, -23, 70, 13, 6, 64, 78, 62, -89, 67, -72, 1, -44, -22, -7, -85, 71, -51, 94, 38}, {68, -36, -59, -67, 68, -9, 9, 43, 39, 38, -83, 76, -58, 3, -86, -53, 96, -8, -36, 12}, {-39, 94, 0, -81, 61, -79, 95, -70, -66, -92, -15, -11, -18, -6, 58, 56, -73, 47, 15, 91}, {67, -95, 67, -80, -7, -3, 9, 86, 96, -92, -91, 74, 73, -49, 54, -70, -17, 55, 57, 9}, {-92, -93, -14, -41, -72, 20, -82, -24, 36, 80, -19, 39, -55, 40, -27, -22, -32, 14, -96, -83}, {-56, -76, -55, 70, 89, 96, 44, 45, -66, 21, -83, 17, 27, 57, 67, 80, -2, -5, 63, 73}, {-13, 73, -74, -64, 87, -37, 66, -76, 66, 31, 56, 26, 94, 97, -47, 32, -50, 43, 29, 40}, {-21, -32, 87, 88, -6, 73, 31, 15, -74, 25, 25, -47, 42, 96, 56, 29, -91, 24, 65, 16}, {-14, -81, -15, -80, -55, -83, 56, -32, -98, -96, 78, -22, -33, 81, 64, 28, -30, 85, -39, -33}, {-83, -35, 40, 24, -12, 56, -99, 47, 97, -27, -59, -2, -86, -75, -29, -93, -66, -89, 98, -27}, {-25, -19, 70, -7, -66, 83, -83, -11, 46, 12, -70, -46, -94, -10, -51, -69, 57, -88, 90, 69}, {-37, -85, -56, 23, -79, 97, -49, -93, 81, 87, -91, 78, 93, -20, -40, 37, 45, -42, -77, -92}, {70, -6, 86, -10, -70, -65, -44, -23, -19, -64, 94, -34, 74, -86, -99, 60, -52, -97, 97, 89}, {-63, -16, 53, 23, 88, -84, 5, -1, 70, -42, 46, -70, -82, -20, 63, -85, -53, -83, 71, 47}, {-26, 15, 53, 17, 1, 42, -9, 53, -24, 84, 32, -76, 33, 47, 20, 44, -11, -97, 98, 14}, {-1, 80, -42, 34, 20, -41, -25, 62, 11, -29, 38, -20, 0, 61, 70, 43, -57, 85, -79, 21}, {-33, -24, -40, -13, 34, -84, 16, 69, 96, 77, -31, -69, -91, 37, 33, 17, 91, -39, 54, -91}, {-28, -79, 93, -48, 14, 74, 38, 8, 60, -59, 93, 41, -69, 86, 82, 57, 63, 89, 66, -48}});
        MatrixBI m = new MatrixBI(new int[][] {
                                  {1, 0, 0, 0},
                                  {0, 2, 0, 0},
                                  {0, 0, 1, 0}});

        // pw.println("# A\n"+m.toString2());

        long t0 = System.currentTimeMillis();

        Smith g = new Smith(m);

        long t = System.currentTimeMillis() - t0;
        pw.println(g.getR().toString("R"));
        pw.println(g.getA().toString("A"));
        pw.println(g.getC().toString("C"));
        pw.println(g.getSmithNormalForm().toString("S"));

        MatrixBI mbi = g.getSmithNormalForm();
        // pw.println("# S\n"+mbi.toString2());

        g.addRow(new int[] { 0, 0, 0, 1 });
        // g.addRow(new int[] {  3,    4, 19,  0 });
        g.smith();

        pw.println(g.getR().toString("R2"));
        pw.println(g.getA().toString("A2"));
        pw.println(g.getC().toString("C2"));
        pw.println(g.getSmithNormalForm().toString("S2"));

        // pw.println("# S\n"+mbi.toString2());

        pw.println(String.format("# matriz: %3d x %3d tempo: %5.4f seg.",m.getNumRows(),m.getNumColumns(),t/1000.0));

        pw.flush(); pw.close();
    }

    public static void main(String[] args) {
        PrintWriter pw = new PrintWriter(System.out);

        MatrixBI m = new MatrixBI(new int[][]
        {{1,0,0,0,0,0,0,0},
        {0,1,0,0,0,0,0,0},
        {0,0,1,0,0,0,0,0},
        {0,0,0,1,0,0,0,0},
        {0,0,0,0,1,0,0,0},
        {0,0,0,0,0,1,0,0},
        {0,0,0,0,0,0,2,0},
        {0,0,0,0,0,0,0,5}});

        // pw.println("# A\n"+m.toString2());

        long t0 = System.currentTimeMillis();

        Smith g = new Smith(m);
        g.smith();

        long t = System.currentTimeMillis() - t0;
        pw.println(g.getR().toString("R"));
        pw.println(g.getA().toString("A"));
        pw.println(g.getC().toString("C"));
        pw.println(g.getSmithNormalForm().toString("S"));

        MatrixBI mbi = g.getSmithNormalForm();
        // pw.println("# S\n"+mbi.toString2());

        pw.println(String.format("# matriz: %3d x %3d tempo: %5.4f seg.",m.getNumRows(),m.getNumColumns(),t/1000.0));

        pw.flush(); pw.close();

    }

}

class MatrixBI {

    private BigInteger[][] _M;

    private int _rowCapacity;
    private int _columnCapacity;

    private int _rows;
    private int _columns;

    public MatrixBI(int m[][]) {
        _rows = m.length;
        _columns = (m.length > 0 ? m[0].length : 0);
        _rowCapacity = 2*_rows;
        _columnCapacity = 2*_columns;
        _M = new BigInteger[_rowCapacity][_columnCapacity];
        for (int i=0;i<_rows;i++)
            for (int j = 0; j < _columns; j++)
                _M[i][j] = BigInteger.valueOf(m[i][j]);
    }

    public MatrixBI(int rows, int columns) {
        _rows = rows;
        _columns = columns;
        _rowCapacity = 2*_rows;
        _columnCapacity = 2*_columns;
        _M = new BigInteger[_rowCapacity][_columnCapacity];
        for (int i=0;i<_rows;i++)
            for (int j = 0; j < _columns; j++)
                _M[i][j] = BigInteger.ZERO;
    }

    public MatrixBI(int dimension) {
        this(dimension, dimension);
        for (int i = 0; i < dimension; i++)
            _M[i][i] = BigInteger.ONE;
    }

    public BigInteger get(int row, int col) {
        BigInteger x = _M[row][col];
        if (x == null)
            return BigInteger.ZERO;
        else
            return x;
    }

    public boolean isZero(int row, int col) {
        return get(row,col).signum() == 0;
    }

    public void set(int row, int col, int value) {
        _M[row][col] = BigInteger.valueOf(value);
    }

    public void set(int row, int col, BigInteger value) {
        _M[row][col] = value;
    }

    public int getNumRows() {
        return _rows;
    }

    public int getNumColumns() {
        return _columns;
    }

    public MatrixBI copy() {
        MatrixBI copy = new MatrixBI(_rows, _columns);
        for (int i = 0; i < _rows; i++)
            for (int j = 0; j < _columns; j++)
                copy.set(i, j, this.get(i, j));
        return copy;
    }

    public int[] findMinimumNonZeroAbsoluteValue(int t) {
        int minRow = -1;
        int minCol = -1;
        BigInteger minimum = null;
        for (int i=t;i<_rows;i++) {
            for (int j = t; j < _columns; j++) {
                if (this.get(i, j).compareTo(BigInteger.ZERO) != 0) {
                    BigInteger abs_ij = this.get(i, j).abs();
                    if (minimum == null) {
                        minimum = abs_ij;
                        minRow = i;
                        minCol = j;
                    }
                    else if (abs_ij.compareTo(minimum) < 0) {
                        minimum = abs_ij;
                        minRow = i;
                        minCol = j;
                    }
                }
            }
        }
        return new int[] {minRow, minCol};
    }

    public void swapRows(int a, int b) {
        if (a == b)
            return;
        for (int j=0;j<_columns;j++) {
            BigInteger aux = this.get(a,j);
            this.set(a,j,this.get(b,j));
            this.set(b,j,aux);
        }
    }

    public void swapColumns(int a, int b) {
        if (a == b)
            return;
        for (int i=0;i<_rows;i++) {
            BigInteger aux = this.get(i,a);
            this.set(i,a,this.get(i,b));
            this.set(i,b,aux);
        }
    }

    public void multiplyRowByScalar(int r, int s) {
        for (int j=0;j<_columns;j++) {
            this.set(r,j,this.get(r,j).multiply(BigInteger.valueOf(s)));
        }
    }

    public void multiplyColumnByScalar(int c, int s) {
        for (int i=0;i<_rows;i++) {
            this.set(i,c,this.get(i,c).multiply(BigInteger.valueOf(s)));
        }
    }

    public void sumScaledRowsAndReplace(int r1, int s1, int r2, int s2) {
        for (int j=0;j<_columns;j++) {
            this.set(r1,j,
                     this.get(r1,j).multiply(BigInteger.valueOf(s1)).add(
                         this.get(r2,j).multiply(BigInteger.valueOf(s2))));
        }
    }

    public void sumScaledRowsAndReplace(int r1, BigInteger s1, int r2, BigInteger s2) {
        for (int j=0;j<_columns;j++) {
            this.set(r1,j,this.get(r1,j).multiply(s1).add(this.get(r2,j).multiply(s2)));
        }
    }

    public void sumScaledColumnAndReplace(int c1, int s1, int c2, int s2) {
        for (int i=0;i<_rows;i++) {
            this.set(i,c1,
                     this.get(i,c1).multiply(BigInteger.valueOf(s1)).add(
                         this.get(i,c2).multiply(BigInteger.valueOf(s2))));
        }
    }

    public void sumScaledColumnAndReplace(int c1, BigInteger s1, int c2, BigInteger s2) {
        for (int i=0;i<_rows;i++) {
            this.set(i,c1,
                     this.get(i,c1).multiply(s1).add(
                         this.get(i,c2).multiply(s2)));
        }
    }

    public String toString2() {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<_rows;i++) {
            for (int j=0;j<_columns;j++) {
                buf.append(String.format("%10s",this.get(i,j).toString()));
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public void print(PrintStream pw) {
        for (int i=0;i<_rows;i++) {
            for (int j=0;j<_columns;j++) {
                pw.print(String.format("%10s",this.get(i,j).toString()));
            }
            pw.print("\n");
        }
    }

    public String toString(String lbl) {
        StringBuffer buf = new StringBuffer();
        buf.append(lbl+":=Matrix([");
        for (int i=0;i<_rows;i++) {
            buf.append("[");
            for (int j=0;j<_columns;j++) {
                buf.append(String.format("%s",this.get(i,j).toString()));
                if (j < _columns-1)
                    buf.append(",");
            }
            buf.append("]");
            if (i < _rows-1)
                buf.append(",");
        }
        buf.append("]);\n#SmithForm("+lbl+");\n");
        return buf.toString();
    }

    private void growCapacity(int deltaRows, int deltaColumns) {
        _rowCapacity += deltaRows;
        _columnCapacity += deltaColumns;

        BigInteger MM[][] = new BigInteger[_rowCapacity][_columnCapacity];
        for (int i = 0; i < _rows; i++)
            for (int j = 0; j < _columns; j++)
                MM[i][j] = _M[i][j];
        _M = null;
        _M = MM;
    }

    public void addRow(BigInteger[] newRow) {
        if (_rows == _rowCapacity) {
            this.growCapacity((int) Math.ceil(0.2*_rowCapacity),0);
        }

        for (int j=0;j<newRow.length;j++)
            _M[_rows][j] = newRow[j];
        _rows++;
    }

    public void addRow(int[] newRow) {
        if (_rows == _rowCapacity) {
            this.growCapacity((int) Math.ceil(0.2*_rowCapacity),0);
        }

        for (int j=0;j<newRow.length;j++)
            _M[_rows][j] = BigInteger.valueOf(newRow[j]);
        _rows++;
    }

    public void addRowAndColumn() {
        if (_rows == _rowCapacity || _columns == _columnCapacity) {
            int deltaRow = (_rows < _rowCapacity ? 0 : (int) Math.ceil(0.2*_rowCapacity));
            int deltaColumn = (_columns < _columnCapacity ? 0 : (int) Math.ceil(0.2*_columnCapacity));
            this.growCapacity(deltaRow,deltaColumn);
        }

        // columns
        for (int j=0;j<_columns;j++)
            _M[_rows][j] = BigInteger.ZERO;
        _rows++;

        // rows
        for (int i=0;i<_rows;i++)
            _M[i][_columns] = BigInteger.ZERO;
        _columns++;
    }
}
