package pw.rxj.iron_quarry.types;

public enum ScrollDirection {
    FORWARDS(1.0, "forwards"),
    UNKNOWN(0.0, "unknown"),
    BACKWARDS(-1.0, "backwards");

    private final double signum;
    private final String name;

    private ScrollDirection(double signum, String name) {
        this.signum = signum;
        this.name = name;
    }

    public static ScrollDirection from(double signum) {
        return switch ((int) signum) {
            case 1 -> FORWARDS;
            case -1 -> BACKWARDS;
            default -> UNKNOWN;
        };
    }

    public double getSignum() {
        return signum;
    }
    public String getName() {
        return name;
    }
}
