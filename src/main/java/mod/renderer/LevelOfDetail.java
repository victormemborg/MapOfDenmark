package mod.renderer;

/**
 * How many detail should be shown on the map.
 * The lower a level, the more detail.
 */
public enum LevelOfDetail {
    Level0, Level1, Level2, Level3, Level4, Level5, Level6, Level7, Level8;

    public static LevelOfDetail from(int code) {
        return switch (code) {
            case 0 -> Level0;
            case 1 -> Level1;
            case 2 -> Level2;
            case 3 -> Level3;
            case 4 -> Level4;
            case 5 -> Level5;
            case 6 -> Level6;
            case 7 -> Level7;
            case 8 -> Level8;
            default -> null;
        };
    }

    public int into() {
        return switch (this) {
            case Level0 -> 0;
            case Level1 -> 1;
            case Level2 -> 2;
            case Level3 -> 3;
            case Level4 -> 4;
            case Level5 -> 5;
            case Level6 -> 6;
            case Level7 -> 7;
            case Level8 -> 8;
        };
    }
}
