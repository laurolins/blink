package blink;

public enum GemColor {
    yellow, blue, red, green;
    private int _index;
    public void setIndex(int i) {_index = i; }
    public int getIndex() { return _index; }
    public int getNumber() {
        if (this == GemColor.yellow) return 0;
        else if (this == GemColor.blue) return 1;
        else if (this == GemColor.red) return 2;
        else if (this == GemColor.green) return 3;
        throw new RuntimeException();
    }
    public static GemColor getByNumber(int c) {
        if (c == 0) return GemColor.yellow;
        else if (c == 1) return GemColor.blue;
        else if (c == 2) return GemColor.red;
        else if (c == 3) return GemColor.green;
        throw new RuntimeException();
    }

    // permutations
    public static final GemColor[][] PERMUTATIONS = {
                                                     {GemColor.yellow,GemColor.blue,GemColor.red,GemColor.green},
                                                     {GemColor.yellow,GemColor.blue,GemColor.green,GemColor.red},
                                                     {GemColor.yellow,GemColor.red,GemColor.blue,GemColor.green},
                                                     {GemColor.yellow,GemColor.red,GemColor.green,GemColor.blue},
                                                     {GemColor.yellow,GemColor.green,GemColor.blue,GemColor.red},
                                                     {GemColor.yellow,GemColor.green,GemColor.red,GemColor.blue},

                                                     {GemColor.blue,GemColor.yellow,GemColor.red,GemColor.green},
                                                     {GemColor.blue,GemColor.yellow,GemColor.green,GemColor.red},
                                                     {GemColor.blue,GemColor.red,GemColor.yellow,GemColor.green},
                                                     {GemColor.blue,GemColor.red,GemColor.green,GemColor.yellow},
                                                     {GemColor.blue,GemColor.green,GemColor.yellow,GemColor.red},
                                                     {GemColor.blue,GemColor.green,GemColor.red,GemColor.yellow},

                                                     {GemColor.red,GemColor.blue,GemColor.yellow,GemColor.green},
                                                     {GemColor.red,GemColor.blue,GemColor.green,GemColor.yellow},
                                                     {GemColor.red,GemColor.yellow,GemColor.blue,GemColor.green},
                                                     {GemColor.red,GemColor.yellow,GemColor.green,GemColor.blue},
                                                     {GemColor.red,GemColor.green,GemColor.blue,GemColor.yellow},
                                                     {GemColor.red,GemColor.green,GemColor.yellow,GemColor.blue},

                                                     {GemColor.green,GemColor.blue,GemColor.red,GemColor.yellow},
                                                     {GemColor.green,GemColor.blue,GemColor.yellow,GemColor.red},
                                                     {GemColor.green,GemColor.red,GemColor.blue,GemColor.yellow},
                                                     {GemColor.green,GemColor.red,GemColor.yellow,GemColor.blue},
                                                     {GemColor.green,GemColor.yellow,GemColor.blue,GemColor.red},
                                                     {GemColor.green,GemColor.yellow,GemColor.red,GemColor.blue}};




    // ------------------------------
    // -- Component information ---------------
    public static final int COLORSET_ALL_COLORS = 0x00;
    public static final int COLORSET_NO_YELLOW = 0x01;
    public static final int COLORSET_NO_BLUE = 0x02;
    public static final int COLORSET_NO_RED = 0x04;
    public static final int COLORSET_NO_GREEN = 0x08;

    public static final int COLORSET_NO_COLOR = 0x0F;
    public static final int COLORSET_YELLOW = 0x0E;
    public static final int COLORSET_BLUE = 0x0D;
    public static final int COLORSET_RED = 0x0B;
    public static final int COLORSET_GREEN = 0x07;

    public static final int[] COLOR_SET_WITH_1_OR_2_OR_3_COLORS = {
                                                              COLORSET_NO_YELLOW,
                                                              COLORSET_NO_BLUE,
                                                              COLORSET_NO_RED,
                                                              COLORSET_NO_GREEN,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_BLUE,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_RED,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_GREEN,
                                                              COLORSET_NO_BLUE + COLORSET_NO_RED,
                                                              COLORSET_NO_BLUE + COLORSET_NO_GREEN,
                                                              COLORSET_NO_RED + COLORSET_NO_GREEN,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_BLUE + COLORSET_NO_RED,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_BLUE + COLORSET_NO_GREEN,
                                                              COLORSET_NO_YELLOW + COLORSET_NO_RED + COLORSET_NO_GREEN,
                                                              COLORSET_NO_BLUE + COLORSET_NO_RED + COLORSET_NO_GREEN};
    /**
     * Return the component value of a component of complementing colors.
     * @param cs GemColor[]
     * @return int
     */
    public static int getComplementColorSet(GemColor ... cs) {
        int component = 0;
        for (GemColor c: cs) {
            if (c == GemColor.yellow)
                component |= COLORSET_NO_YELLOW;
            else if (c == GemColor.blue)
                component |= COLORSET_NO_BLUE;
            else if (c == GemColor.red)
                component |= COLORSET_NO_RED;
            else if (c == GemColor.green)
                component |= COLORSET_NO_GREEN;
        }
        return component;
    }

    /**
     * Return the component value of a component of complementing colors.
     * @param cs GemColor[]
     * @return int
     */
    public static int getComplementColorSet(int colorSet) {
        return GemColor.getComplementColorSet(GemColor.getColorsOfColorSet(colorSet));
    }

    public static GemColor[] getComplementColors(int colorSet) {
        return GemColor.getColorsOfColorSet(GemColor.getComplementColorSet(GemColor.getColorsOfColorSet(colorSet)));
    }

    public static int removeColor(int colorSet, GemColor c) {
        if (c == GemColor.yellow)
            colorSet |= COLORSET_NO_YELLOW;
        else if (c == GemColor.blue)
            colorSet |= COLORSET_NO_BLUE;
        else if (c == GemColor.red)
            colorSet |= COLORSET_NO_RED;
        else if (c == GemColor.green)
            colorSet |= COLORSET_NO_GREEN;
        return colorSet;
    }

    public static int union(int c1, int c2) {
        int result = 0;
        for (int i=0;i<4;i++) {
            int c1i = ((c1 >> i) & 0x01);
            int c2i = ((c2 >> i) & 0x01);
            if (c1i == 1 && c2i == 1)
                result |= (0x1 << i);
        }
        return result;
    }

    public static int intersection(int c1, int c2) {
        int result = 0;
        for (int i=0;i<4;i++) {
            int c1i = ((c1 >> i) & 0x01);
            int c2i = ((c2 >> i) & 0x01);
            if (c1i == 1 || c2i == 1)
                result |= (0x1 << i);
        }
        return result;
    }

    public static int difference(int c1, int c2) {
        int result = 0;
        for (int i=0;i<4;i++) {
            int c1i = ((c1 >> i) & 0x01);
            int c2i = ((c2 >> i) & 0x01);
            if (c1i == 1 || c2i == 0)
                result |= (0x1 << i);
        }
        return result;
    }

    /**
     * Return the component value of a component of complementing colors.
     * @param cs GemColor[]
     * @return int
     */
    public static int getColorSet(GemColor ... cs) {
        int component = 0;
        for (GemColor c: cs) {
            if (c == GemColor.yellow)
                component += COLORSET_NO_YELLOW;
            else if (c == GemColor.blue)
                component += COLORSET_NO_BLUE;
            else if (c == GemColor.red)
                component += COLORSET_NO_RED;
            else if (c == GemColor.green)
                component += COLORSET_NO_GREEN;
        }
        return 15-component;
    }

    /**
     * Return the component value of a component of complementing colors.
     * @param cs GemColor[]
     * @return int
     */
    public static String getColorSetCompactString(GemColor ... cs) {
        return getColorSetCompactString(GemColor.getColorSet(cs));
    }
    public static String getColorSetCompactString(int colorSet) {
        String result = "";
        for (GemColor c: COLOR_SET[colorSet]) {
            if (c == GemColor.yellow)
                result+="Y";
            else if (c == GemColor.blue)
                result+="B";
            else if (c == GemColor.red)
                result+="R";
            else if (c == GemColor.green)
                result+="G";
        }
        return result;
    }

    public static String getColorSetCompactStringABCD(int colorSet) {
        String result = "";
        for (GemColor c: COLOR_SET[colorSet]) {
            if (c == GemColor.yellow)
                result+="a";
            else if (c == GemColor.blue)
                result+="b";
            else if (c == GemColor.red)
                result+="c";
            else if (c == GemColor.green)
                result+="d";
        }
        return result;
    }

    public static GemColor[] parseColorsCompactString(String colorPermutation) {
        GemColor[] result = new GemColor[colorPermutation.length()];
        for (int i=0;i<colorPermutation.length();i++) {
            char c = colorPermutation.charAt(i);
            result[i] = (c == 'Y' ? GemColor.yellow :
                         (c == 'B' ? GemColor.blue :
                          (c == 'R' ? GemColor.red :
                           (c == 'G' ? GemColor.green : null))));
        }
        return result;
    }

    public static int colorSetFromABCD(String colorPermutation) {
        colorPermutation = colorPermutation.toUpperCase();
        GemColor[] result = new GemColor[colorPermutation.length()];
        for (int i=0;i<colorPermutation.length();i++) {
            char c = colorPermutation.charAt(i);
            result[i] = (c == 'A' ? GemColor.yellow :
                         (c == 'B' ? GemColor.blue :
                          (c == 'C' ? GemColor.red :
                           (c == 'D' ? GemColor.green : null))));
        }
        return GemColor.getColorSet(result);
    }


    public static String getColorsCompactString(GemColor ... colors) {
        String result = "";
        for (GemColor c: colors) {
            if (c == GemColor.yellow)
                result+="Y";
            else if (c == GemColor.blue)
                result+="B";
            else if (c == GemColor.red)
                result+="R";
            else if (c == GemColor.green)
                result+="G";
        }
        return result;
    }

    public static String getColorsCompactStringABCD(GemColor ... colors) {
        String result = "";
        for (GemColor c: colors) {
            if (c == GemColor.yellow)
                result+="A";
            else if (c == GemColor.blue)
                result+="B";
            else if (c == GemColor.red)
                result+="C";
            else if (c == GemColor.green)
                result+="D";
        }
        return result;
    }

    public static GemColor[] getColorsOfColorSet(int colorSet) {
        return COLOR_SET[colorSet];
    }

    public static GemColor[] getComplementColors(GemColor ... colors) {
        return getColorsOfColorSet(getComplementColorSet(colors));
    }

    public static boolean containsColor(GemColor c, int colorSet) {
        GemColor[] colors = COLOR_SET[colorSet];
        for (GemColor cc: colors) {
            if (c == cc)
                return true;
        }
        return false;
    }

    public static final GemColor[][] COLOR_SET = {
        {GemColor.yellow, GemColor.blue, GemColor.red, GemColor.green}, // 0. NO_NOTHING
        {GemColor.blue, GemColor.red, GemColor.green},                  // 1. NO_YELLOW
        {GemColor.yellow, GemColor.red, GemColor.green},                // 2. NO_BLUE
        {GemColor.red, GemColor.green},                                 // 3. NO_YELLOW + NO_BLUE
        {GemColor.yellow, GemColor.blue, GemColor.green},               // 4. NO_RED
        {GemColor.blue, GemColor.green},                                // 5. NO_RED + NO_YELLOW
        {GemColor.yellow, GemColor.green},                              // 6. NO_RED + NO_BLUE
        {GemColor.green},                                               // 7. NO_RED + NO_BLUE + NO_YELLOW
        {GemColor.yellow, GemColor.blue, GemColor.red},                 // 8. NO_GREEN
        {GemColor.blue, GemColor.red},                                  // 9. NO_GREEN + NO_YELLOW
        {GemColor.yellow, GemColor.red},                                //10. NO_GREEN + NO_BLUE
        {GemColor.red},                                                 //11. NO_GREEN + NO_YELLOW + NO_BLUE
        {GemColor.yellow, GemColor.blue},                               //12. NO_GREEN + NO_RED
        {GemColor.blue},                                                //13. NO_GREEN + NO_RED + NO_YELLOW
        {GemColor.yellow},                                              //14. NO_GREEN + NO_RED + NO_BLUE
        {},                                                             //15. NO_GREEN + NO_RED + NO_BLUE + NO_YELLOW
    };
    // ------------------------------------
}

