package ui.execution.run;

import com.google.gson.JsonObject;
import dto.execution.ProgramExecutorDTO;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.support.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static util.http.HttpClientUtil.HTTP_CLIENT;
import static util.support.Constants.*;

public final class RunApi {

    public record SubmitResponse(String runId, String state) { }

    public static SubmitResponse submitRun(String programName, String architecture, int degree, List<Long> inputs)
            throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty(PROGRAM_NAME_QUERY_PARAM, programName);
        body.addProperty(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, architecture);
        body.addProperty(DEGREE_QUERY_PARAM, degree);
        body.add(INPUTS_VALUES_QUERY_PARAM, GSON_INSTANCE.toJsonTree(inputs));

        Request req = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH + "/runProgram")
                .post(RequestBody.create(GSON_INSTANCE.toJson(body), MediaType.parse("application/json")))
                .build();

        try (Response resp = HTTP_CLIENT.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            String json = resp.body() != null ? resp.body().string() : "{}";
            Map<?,?> map = GSON_INSTANCE.fromJson(json, Map.class);
            return new SubmitResponse((String) map.get("runId"), (String) map.get("state"));
        }
    }

    public static JsonObject getStatus(String runId) throws IOException {
        Request req = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH + "/runStatus?runId=" + runId)
                .get()
                .build();
        try (Response resp = HTTP_CLIENT.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            String json = resp.body() != null ? resp.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, JsonObject.class);
        }
    }

    public static ProgramExecutorDTO fetchLatestExecution(String programName) throws IOException {
        Request req = new Request.Builder()
                .url(Constants.FULL_SERVER_PATH + "/latestExecution?programName=" + programName)
                .get()
                .build();
        try (Response resp = HTTP_CLIENT.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            String json = resp.body() != null ? resp.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, ProgramExecutorDTO.class);
        }
    }
}
