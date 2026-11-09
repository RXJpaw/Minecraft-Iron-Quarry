package pw.rxj.iron_quarry.expansion;

public class MiningPatternExpansion extends AbstractExpansion {
    private MiningPatternExpansion() { }
    public static MiningPatternExpansion of() {
        return new MiningPatternExpansion();
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
