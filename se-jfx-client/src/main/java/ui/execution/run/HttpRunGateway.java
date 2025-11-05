package ui.execution.run;

import com.google.gson.JsonObject;
import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramDTO;
import dto.execution.ProgramExecutorDTO;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

import static util.http.HttpClientUtil.HTTP_CLIENT;
import static util.support.Constants.*;

public class HttpRunGateway implements RunGateway {

    private HttpUrl baseUrl(String path) {
        return HttpUrl.parse(FULL_SERVER_PATH + path);
    }

    @Override
    public ProgramDTO fetchExpanded(String programName, int degree) throws IOException {
        HttpUrl url = baseUrl("/program-dto").newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
                .addQueryParameter(DEGREE_QUERY_PARAM, String.valueOf(degree))
                .build();
        Request req = new Request.Builder().url(url).get().build();
        try (Response response = HTTP_CLIENT.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "";
            return GSON_INSTANCE.fromJson(json, ProgramDTO.class);
        }
    }

    @Override
    public List<String> fetchRequiredInputs(String programName, int degree) throws IOException {
        ProgramDTO programDTO = fetchExpanded(programName, degree);
        return programDTO.inputVariables();
    }

    @Override
    public String submitRun(String programName, String architecture, int degree, List<Long> inputs) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty(PROGRAM_NAME_QUERY_PARAM, programName);
        body.addProperty(DEGREE_QUERY_PARAM, degree);
        body.addProperty(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, architecture);
        body.add(INPUTS_VALUES_QUERY_PARAM, GSON_INSTANCE.toJsonTree(inputs));

        Request req = new Request.Builder()
                .url(FULL_SERVER_PATH + "/runProgram")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = HTTP_CLIENT.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            JsonObject responseJson = GSON_INSTANCE.fromJson(response.body() != null ? response.body().string() : "{}", JsonObject.class);
            return responseJson.get("runId").getAsString();
        }
    }

    @Override
    public ExecutionStatusDTO getStatus(String runId) throws IOException {
        HttpUrl url = baseUrl("/runStatus").newBuilder()
                .addQueryParameter(RUN_ID_QUERY_PARAM, runId)
                .build();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            return GSON_INSTANCE.fromJson(response.body() != null ? response.body().string() : "", ExecutionStatusDTO.class);
        }
    }

    @Override
    public ProgramExecutorDTO fetchResult(String programName) throws IOException {
        HttpUrl url = baseUrl("/runResult").newBuilder()
                .addQueryParameter(PROGRAM_NAME_QUERY_PARAM, programName)
                .build();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            String json = response.body() != null ? response.body().string() : "{}";
            return GSON_INSTANCE.fromJson(json, ProgramExecutorDTO.class);
        }
    }

//    @Override
//    public CreditsBalance fetchCredits() throws IOException {
//        HttpUrl url = HttpUrl.parse(FULL_SERVER_PATH + "/credits/balance").newBuilder().build();
//        try (Response resp = HTTP_CLIENT.newCall(new Request.Builder().url(url).build()).execute()) {
//            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
//            String json = resp.body() != null ? resp.body().string() : "{}";
//            var obj = GSON_INSTANCE.fromJson(json, JsonObject.class);
//            long cur  = obj.has("currentCredits") ? obj.get("currentCredits").getAsLong() : 0L;
//            long used = obj.has("usedCredits")    ? obj.get("usedCredits").getAsLong()    : 0L;
//            return new CreditsBalance(cur, used);
//        }
//    }
}
