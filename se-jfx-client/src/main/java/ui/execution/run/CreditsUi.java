package ui.execution.run;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import okhttp3.Request;
import okhttp3.Response;
import util.support.Constants;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

import static util.http.HttpClientUtil.HTTP_CLIENT;
import static util.support.Constants.GSON_INSTANCE;

public final class CreditsUi {
    private static BiConsumer<Long, Long> creditsUpdater;

    public static void bindUpdater(BiConsumer<Long, Long> uiUpdater) {
        creditsUpdater = uiUpdater;
    }

    public static void refreshCreditsAsync() {
        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url(Constants.FULL_SERVER_PATH + "/credits-balance")
                        .get()
                        .build();
                try (Response resp = HTTP_CLIENT.newCall(req).execute()) {
                    if (!resp.isSuccessful()) return;
                    String json = resp.body() != null ? resp.body().string() : "{}";
                    JsonObject jsonObject = GSON_INSTANCE.fromJson(json, JsonObject.class);
                    long currentCredits = jsonObject.has("currentCredits") ? jsonObject.get("currentCredits").getAsLong() : 0L;
                    long usedCredits = jsonObject.has("usedCredits") ? jsonObject.get("usedCredits").getAsLong() : 0L;
                    if (creditsUpdater != null) {
                        Platform.runLater(() -> creditsUpdater.accept(currentCredits, usedCredits));
                    }
                }
            } catch (IOException ignore) {}
        }, "credits-refresh").start();
    }
}
