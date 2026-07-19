package com.dating.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

public class DateUtils {

    public static Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) return null;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public static Integer calculateAge(Instant birthDate) {
        if (birthDate == null) return null;
        LocalDate date = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        return calculateAge(date);
    }

    public static boolean isAdult(LocalDate birthDate) {
        Integer age = calculateAge(birthDate);
        return age != null && age >= 18;
    }
}
