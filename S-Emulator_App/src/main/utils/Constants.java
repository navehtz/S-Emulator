package main.utils;

import com.google.gson.Gson;

public class Constants {
    public static final String USERNAME = "username";
    public static final String USER_NAME_ERROR = "username_error";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;

    public static final Gson GSON_INSTANCE = new Gson();

    // Query parameters
    public static final String PROGRAM_NAME_QUERY_PARAM = "programName";
    public static final String DEGREE_QUERY_PARAM = "degree";
    public static final String INPUTS_VALUES_QUERY_PARAM = "inputsValues";
    public static final String CHOSEN_ARCHITECTURE_STR_QUERY_PARAM = "architecture";
    public static final String CREDITS_AMOUNT_QUERY_PARAM = "creditsAmount";
    public static final String CHOSEN_ARCHITECTURE_QUERY_PARAM = "chosenArchitecture";
    public static final String RUN_ID_QUERY_PARAM = "runId";
    public static final String RUN_STATE_QUERY_PARAM = "runState";
    public static final String PROGRESS_PERCENT_QUERY_PARAM = "progressPercent";
    public static final String MESSAGE_QUERY_PARAM = "message";
    public static final String RUN_STATE_ERROR = "runState_error";
    public static final String PROGRESS_PERCENT_ERROR = "progressPercent_error";
    public static final String MESSAGE_ERROR = "message_error";
    public static final String CHOSEN_ARCHITECTURE_ERROR = "chosenArchitecture_error";
    public static final String CHOSEN_ARCHITECTURE_STR_ERROR = "chosenArchitectureStr_error";


    public static final String ERROR = "error";


}
