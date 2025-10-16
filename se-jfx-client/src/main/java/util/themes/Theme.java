package util.themes;

public enum Theme {
    LIGHT("Light", "/ui/styles/light.css"),
    DARK("Dark", "/ui/styles/dark.css"),
    YELLOW_BLUE("Yellow-Blue", "/ui/styles/yellowblue.css");

    final String label;
    public final String css;
    Theme(String label, String css) { this.label = label; this.css = css; }
    @Override public String toString() { return label; }
}
