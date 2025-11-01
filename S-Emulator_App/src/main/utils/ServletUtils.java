package main.utils;

import engine.Engine;
import engine.EngineImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import static main.utils.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String ENGINE_ATTRIBUTE_NAME = "engine";

    private static final Object engineLock = new Object();

    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new EngineImpl());
            }
        }
        return (EngineImpl) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }


    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
                return INT_PARAMETER_ERROR;
            }
        }

        return INT_PARAMETER_ERROR;
    }
}
