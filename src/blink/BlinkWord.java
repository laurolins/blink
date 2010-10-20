package blink;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Considers the red edges also
 */
public class BlinkWord extends MapWord implements Comparable {

    private int[] _reds;

    public BlinkWord(int code[], int reds[]) {
        super(code);
        _reds = (int[])reds.clone();
    }

    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    public int compareTo(Object o) {
        //
        BlinkWord other = (BlinkWord) o;

        // compare map word
        int r = super.compareTo(o);

        if (r == 0) {

            // the one with more red edges has
            // by this definition a larger code
            r = _reds.length - other._reds.length;

            // untie on the red edges
            int i = 0;
            while (r == 0 && i < _reds.length) {
                r = _reds[i]-other._reds[i];
                i++;
            }

        }

        // return result
        return r;
    }

    public String toString() {
        String reds = "";
        boolean first = true;
        for (int r: _reds) {
            if (!first) reds+=",";
            reds+=r;
            first = false;
        }
        return super.toString()+" "+reds;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        FileInputStream f1 = new FileInputStream("c:/temp/k1.wmv");
        FileInputStream f2 = new FileInputStream("c:/temp/k2.wmv");
        FileOutputStream fout = new FileOutputStream("c:/temp/k.wmv");
        byte buffer[] = new byte[4096];
        int r;
        while ((r = f1.read(buffer)) != -1) {
            fout.write(buffer,0,r);
        }
        f1.close();
        while ((r = f2.read(buffer)) != -1) {
            fout.write(buffer,0,r);
        }
        f2.close();
        fout.flush();
        fout.close();





    }

}
