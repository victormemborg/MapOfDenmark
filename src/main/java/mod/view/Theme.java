package mod.view;

public enum Theme {
    DEFAULT, DARK, COLORBLIND;

    @Override
    public String toString() {
        return switch (this) {
            case DEFAULT -> "Default mode";
            case DARK -> "Dark mode";
            case COLORBLIND -> "Color blind mode";
        };
    }
}
