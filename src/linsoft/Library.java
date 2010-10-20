package linsoft;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

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
public class Library {

    public Library() {
    }

    public static String intArrayToString(String sep, int ... array) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<array.length;i++) {
            sb.append(array[i]);
            if (i < array.length-1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static int[] subarray(int input[], int index, int length) {
        int[] result = new int[length];
        System.arraycopy(input,index,result,0,length);
        return result;
    }

    public static int[] stringToIntArray(String s, String sep) {
        StringTokenizer st = new StringTokenizer(s,sep);
        int[] result = new int[st.countTokens()];
        int i=0;
        while (st.hasMoreTokens()) {
            result[i++] =  Integer.parseInt(st.nextToken());
        }
        return result;
    }

    /**
     * Takes a 001000111000
     * @param colorsSt String
     * @return int[]
     */
    public static int[] setOfIntegersFromSetBinaryString(String colorsSt) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < colorsSt.length(); i++) {
            int d = Integer.parseInt("" + colorsSt.charAt(i));
            if (d == 1)
                list.add(i+1);
        }
        int[] elements = new int[list.size()];
        int k = 0;
        for (int i: list)
            elements[k++] = i;

        return elements;
    }


    public static String collectionToString(Collection list, char separator) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object o: list) {
            if (!first)
                sb.append(separator);
            sb.append(o);
            first=false;
        }
        return sb.toString();
    }

    /**
     * Create
     */
    public static String fillStringWithChar(String input, int length, char fillChar, boolean leftAlignment) {
        String fill = "";
        for (int i=0;i<length-input.length();i++)
            fill+=""+fillChar;
        if (leftAlignment)
            return fill+input;
        else
            return input+fill;
    }

    public static void playSound(String fileName, long waitPeriod) {
        try {
            InputStream in = new FileInputStream(fileName);
            AudioStream as = new AudioStream(in);
            AudioPlayer.player.start(as);
            Thread.sleep(waitPeriod);
            AudioPlayer.player.stop(as);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        playSound("solucao.wav",2000);
    }

}
