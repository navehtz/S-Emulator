package util.support;

import com.google.gson.Gson;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.lineSeparator();
    public final static String TEMP_NAME = "<Anonymous>";
    public final static String USERNAME_PREFIX = "Hello, ";
    public final static int REFRESH_RATE = 500;

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/ui/main/components/s-emulator-app-main.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/ui/login/login.fxml";
    public final static String DASHBOARD_FXML_RESOURCE_LOCATION = "/ui/dashboard/components/main/dashboard.fxml";
    public final static String EXECUTION_PAGE_FXML_RESOURCE_LOCATION = "/ui/execution/components/main/executionPage.fxml";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    public final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    public final static String CONTEXT_PATH = "/S-Emulator_App_Web";
    public final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public final static String USER_HISTORY = FULL_SERVER_PATH + "/userHistory";

    // Debug endpoints
    public final static String DEBUG_START = FULL_SERVER_PATH + "/debug/start";
    public final static String DEBUG_STEP = FULL_SERVER_PATH + "/debug/step";
    public final static String DEBUG_RESUME = FULL_SERVER_PATH + "/debug/resume";
    public final static String DEBUG_RESUME_STATUS = FULL_SERVER_PATH + "/debug/resumeStatus";
    public final static String DEBUG_RESUME_RESULT = FULL_SERVER_PATH + "/debug/resumeResult";
    public final static String AVERAGE_CYCLES = FULL_SERVER_PATH + "/averageCycles";
    public final static String ARCHITECTURE_COST = FULL_SERVER_PATH + "/architectureCost";

    //Query parameters
    public static final String PROGRAM_NAME_QUERY_PARAM = "programName";
    public static final String DEGREE_QUERY_PARAM = "degree";
    public static final String INPUTS_VALUES_QUERY_PARAM = "inputsValues";
    public static final String CHOSEN_ARCHITECTURE_STR_QUERY_PARAM = "architecture";
    public static final String CREDITS_AMOUNT_QUERY_PARAM = "creditsAmount";
    public static final String RUN_ID_QUERY_PARAM = "runId";
    public static final String USER_NAME_QUERY_PARAM = "userName";

    public static final Gson GSON_INSTANCE = new Gson();
}
