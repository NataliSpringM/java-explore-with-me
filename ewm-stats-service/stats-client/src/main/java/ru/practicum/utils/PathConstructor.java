package ru.practicum.utils;

import lombok.experimental.UtilityClass;

import static ru.practicum.utils.Constants.*;

@UtilityClass
public class PathConstructor {
    /**
     * construct path with parameters for GET-request
     *
     * @return path with list of parameters as name=value pairs
     */
    public static String getParameterPath(String start, String end, String uris, Boolean unique) {
        return "?"
                + constructParamPair(START_PARAMETER_NAME, start)
                + "&"
                + constructParamPair(END_PARAMETER_NAME, end)
                + "&"
                + constructParamPair(URIS_PARAMETER_NAME, uris)
                + "&"
                + constructParamPair(UNIQUE_PARAMETER_NAME, unique);
    }

    /**
     * construct String with name={value} pair
     */
    private String constructParamPair(String name, Object value) {
        return name
                + "="
                + value;
    }
}
