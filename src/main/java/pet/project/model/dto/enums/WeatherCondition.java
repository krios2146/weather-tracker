package pet.project.model.dto.enums;

public enum WeatherCondition {
    THUNDERSTORM,
    DRIZZLE,
    RAIN,
    SNOW,
    ATMOSPHERE,
    CLEAR,
    CLOUDS,
    NOT_DEFINED;

    public static WeatherCondition getWeatherConditionForCode(Integer code) {
        String codeStr = String.valueOf(code);

        if (codeStr.startsWith("2")) {
            return THUNDERSTORM;
        }
        if (codeStr.startsWith("3")) {
            return DRIZZLE;
        }
        if (codeStr.startsWith("5")) {
            return RAIN;
        }
        if (codeStr.startsWith("6")) {
            return SNOW;
        }
        if (codeStr.startsWith("7")) {
            return ATMOSPHERE;
        }
        if (codeStr.equals("800")) {
            return CLEAR;
        }
        if (codeStr.startsWith("8")) {
            return CLOUDS;
        }
        return NOT_DEFINED;
    }
}
