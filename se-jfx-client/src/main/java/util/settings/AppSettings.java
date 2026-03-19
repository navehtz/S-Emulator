package util.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import util.themes.Theme;

import java.util.prefs.Preferences;

public class AppSettings {

    private static final Preferences PREFS = Preferences.userNodeForPackage(Theme.class);
    private static final String PREF_KEY_ANIMATIONS = "app.animations";

    public static final BooleanProperty animationsEnabled = new SimpleBooleanProperty(
            PREFS.getBoolean(PREF_KEY_ANIMATIONS, true)
    );

    static {
        animationsEnabled.addListener((obs, was, isNow) ->
                PREFS.putBoolean(PREF_KEY_ANIMATIONS, isNow));
    }

    private AppSettings() {}
}
