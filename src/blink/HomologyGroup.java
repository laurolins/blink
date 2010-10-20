package blink;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HomologyGroup implements Comparable {
    HashMap<BigInteger,Integer> _map = new HashMap<BigInteger,Integer>();
    public void add(BigInteger x) {
        Integer i = _map.get(x);
        if (i == null)
            _map.put(x,1);
        else
            _map.put(x,i+1);
    }
    public String toString() {
        StringBuffer result = new StringBuffer();
        ArrayList<BigInteger> list = new ArrayList<BigInteger>(_map.keySet());
        if (list.size() > 0) {
            Collections.sort(list);
            boolean first = true;
            for (BigInteger i : list) {
                if (!first)
                    result.append(" ");
                result.append(i + "^" + _map.get(i));
                first = false;
            }
        }
        else {
            result.append("1^1");
        }
        return result.toString();
    }
    /**
     * betti number plus numbers and their multiplicitites
     * @return int[]
     */
    public ArrayList<Integer> getNumbers() {
        ArrayList<Integer> numbers = new ArrayList<Integer>();

        numbers.add(this.getBettiNumber());

        ArrayList<BigInteger> list = new ArrayList<BigInteger>(_map.keySet());
        list.remove(BigInteger.ZERO);
        if (list.size() > 0) {
            Collections.sort(list);
            for (BigInteger i : list) {
                int base = Integer.parseInt(""+i);
                int exp = Integer.parseInt(""+_map.get(i));
                numbers.add(base);
                numbers.add(exp);
            }
        }
        return numbers;
    }

    public int getBettiNumber() {
        Integer v = _map.get(BigInteger.ZERO);
        if (v == null) return 0;
        return v;
    }

    public int compareTo(Object o) {
        HomologyGroup x = (HomologyGroup) o;
        return this.toString().compareTo(x.toString());
    }
    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

}
