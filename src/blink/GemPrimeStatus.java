package blink;

public enum GemPrimeStatus {
    POTENTIALLY_PRIME,
    POTENTIALLY_COMPOSITE,
    UNDEFINED,
    PRIME,
    COMPOSITE_FROM_HANDLE,
    COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP,
    COMPOSITE_FROM_NON_DISCONNECTING_QUARTET;

    public static GemPrimeStatus fromNumber(int i) {
        switch (i) {
        case 0 : return UNDEFINED;
        case -1 : return POTENTIALLY_PRIME;
        case -2 : return POTENTIALLY_COMPOSITE;
        case 1 : return PRIME;
        case 2 : return COMPOSITE_FROM_HANDLE;
        case 4 : return COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP;
        case 6 : return COMPOSITE_FROM_NON_DISCONNECTING_QUARTET;
        default: return UNDEFINED;
        }
    }
    public String getSmallDescription() {
        switch (this.getNumber()) {
        case 0 : return "undefined";
        case -1 : return "pot.prime";
        case -2 : return "pot.composite";
        case 1 : return "prime";
        case 2 : return "composite.hnd";
        case 4 : return "composite.discquart";
        case 6 : return "composite.nondquart";
        default: return "undefined";
        }
    }

    public int getNumber() {
        if (this == UNDEFINED) return 0;
        else if (this == POTENTIALLY_PRIME) return -1;
        else if (this == POTENTIALLY_COMPOSITE) return -2;
        else if (this == PRIME) return 1;
        else if (this == COMPOSITE_FROM_HANDLE) return 2;
        else if (this == COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP) return 4;
        else if (this == COMPOSITE_FROM_NON_DISCONNECTING_QUARTET) return 6;
        return 0;
    }
    public String toString() {
        return super.toString()+" ("+this.getNumber()+")";
    }

    /**
     * The tendency is to be prime?
     */
    public boolean isPrimeBiased() {
        return (this == PRIME || this == POTENTIALLY_PRIME);
    }

}
