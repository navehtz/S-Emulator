package ui.execution.debug;

import com.google.gson.JsonObject;
import dto.execution.*;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

import static util.http.HttpClientUtil.HTTP_CLIENT;
import static util.support.Constants.*;

public class HttpDebugGateway implements DebugGateway {

    private HttpUrl baseUrl(String path) {
        return HttpUrl.parse(FULL_SERVER_PATH + path);
    }

    @Override
    public DebugResponseDTO startDebug(String programName, String architecture, int degree, List<Long> inputs) throws IOException {
        DebugStartRequestDTO requestDTO = new DebugStartRequestDTO(programName, architecture, degree, inputs);
        String body = GSON_INSTANCE.toJson(requestDTO);

        Request request = new Request.Builder()
                .url(DEBUG_START)
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() == 402) throw new DebugOutOfCreditsException("Out of credits");
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " +
                    (response.body() != null ? response.body().string() : ""));
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, DebugResponseDTO.class);
        }
    }

    @Override
    public DebugResponseDTO stepOver(String sessionId) throws IOException {
        HttpUrl url = baseUrl("/debug/step").newBuilder()
                .addQueryParameter("sessionId", sessionId)
                .addQueryParameter("action", "over")
                .build();
        Request request = new Request.Builder().url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() == 402) throw new DebugOutOfCreditsException("Out of credits");
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, DebugResponseDTO.class);
        }
    }

    @Override
    public DebugResponseDTO stepBack(String sessionId) throws IOException {
        HttpUrl url = baseUrl("/debug/step").newBuilder()
                .addQueryParameter("sessionId", sessionId)
                .addQueryParameter("action", "back")
                .build();
        Request request = new Request.Builder().url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() == 402) throw new DebugOutOfCreditsException("Out of credits");
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, DebugResponseDTO.class);
        }
    }

    @Override
    public DebugResponseDTO stop(String sessionId) throws IOException {
        HttpUrl url = baseUrl("/debug/step").newBuilder()
                .addQueryParameter("sessionId", sessionId)
                .addQueryParameter("action", "stop")
                .build();
        Request request = new Request.Builder().url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, DebugResponseDTO.class);
        }
    }

    @Override
    public String submitResume(String sessionId, List<Boolean> breakpoints) throws IOException {
        HttpUrl url = baseUrl("/debug/resume").newBuilder()
                .addQueryParameter("sessionId", sessionId)
                .build();
        String body = GSON_INSTANCE.toJson(breakpoints);

        Request request = new Request.Builder().url(url)
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            JsonObject obj = GSON_INSTANCE.fromJson(json, JsonObject.class);
            return obj.get("resumeId").getAsString();
        }
    }

    @Override
    public ExecutionStatusDTO getResumeStatus(String resumeId) throws IOException {
        HttpUrl url = baseUrl("/debug/resumeStatus").newBuilder()
                .addQueryParameter("resumeId", resumeId)
                .build();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, ExecutionStatusDTO.class);
        }
    }

    @Override
    public DebugResponseDTO getResumeResult(String resumeId) throws IOException {
        HttpUrl url = baseUrl("/debug/resumeResult").newBuilder()
                .addQueryParameter("resumeId", resumeId)
                .build();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, DebugResponseDTO.class);
        }
    }

    @Override
    public double getAverageCycles(String programName, int degree) throws IOException {
        HttpUrl url = baseUrl("/averageCycles").newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
                .addQueryParameter(DEGREE_QUERY_PARAM, String.valueOf(degree))
                .build();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String body = response.body() != null ? response.body().string() : "0";
            return Double.parseDouble(body.trim());
        }
    }

    @Override
    public int getArchitectureCost(String architecture) throws IOException {
        HttpUrl url = baseUrl("/architectureCost").newBuilder()
                .addQueryParameter(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, architecture)
                .build();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String body = response.body() != null ? response.body().string() : "0";
            return Integer.parseInt(body.trim());
        }
    }

    @Override
    public long getCreditsBalance() throws IOException {
        HttpUrl url = baseUrl("/credits/balance").newBuilder().build();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            JsonObject obj = GSON_INSTANCE.fromJson(json, JsonObject.class);
            return obj.has("currentCredits") ? obj.get("currentCredits").getAsLong() : 0L;
        }
    }

    @Override
    public List<String> fetchRequiredInputs(String programName, int degree) throws IOException {
        HttpUrl url = baseUrl("/program-dto").newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
                .addQueryParameter(DEGREE_QUERY_PARAM, String.valueOf(degree))
                .build();
        Request req = new Request.Builder().url(url).get().build();
        try (Response response = HTTP_CLIENT.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "";
            ProgramDTO programDTO = GSON_INSTANCE.fromJson(json, ProgramDTO.class);
            return programDTO.inputVariables();
        }
    }
}
