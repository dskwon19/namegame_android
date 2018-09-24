package com.willowtreeapps.namegame.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AnalyticsUtil {

    private static final String PREFS = "PREFS";
    private static final String TOTAL_KEY = "TOTAL_KEY";
    private static final String CORRECT_KEY = "CORRECT_KEY";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getTotalAttempts(Context context) {
        return getSharedPreferences(context).getInt(TOTAL_KEY, 0);
    }

    public static int getCorrectAttempts(Context context) {
        return getSharedPreferences(context).getInt(CORRECT_KEY, 0);
    }

    public static void incrementTotalAttempt(Context context) {
        int total = getTotalAttempts(context);
        getSharedPreferences(context).edit().putInt(TOTAL_KEY, total + 1).apply();
    }

    public static void incrementCorrectAttempt(Context context) {
        int correct = getCorrectAttempts(context);
        getSharedPreferences(context).edit().putInt(CORRECT_KEY, correct + 1).apply();
    }

    public static void resetData(Context context) {
        getSharedPreferences(context).edit().putInt(TOTAL_KEY, 0).apply();
        getSharedPreferences(context).edit().putInt(CORRECT_KEY, 0).apply();
    }

}
