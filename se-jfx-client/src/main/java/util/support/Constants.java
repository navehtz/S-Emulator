package util.support;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String TEMP_NAME = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/ui/main/components/s-emulator-app-main.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/ui/login/login.fxml";
    public final static String DASHBOARD_FXML_RESOURCE_LOCATION = "/ui/dashboard/components/main/dashboard.fxml";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/S-Emulator_App_Web";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
}
