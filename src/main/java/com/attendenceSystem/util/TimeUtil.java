package com.attendenceSystem.util;

import java.time.format.DateTimeFormatter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


}
