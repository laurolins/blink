package blink;

public class BlinkNativeLib {
    public native static boolean intersects(
        double ax1, double ay1, double az1,
        double ax2, double ay2, double az2,
        double ax3, double ay3, double az3,
        double ax4, double ay4, double az4,
        double bx1, double by1, double bz1,
        double bx2, double by2, double bz2,
        double bx3, double by3, double bz3,
        double bx4, double by4, double bz4
    );
    static {
        System.loadLibrary("blinkNativeLib");
    }    
}