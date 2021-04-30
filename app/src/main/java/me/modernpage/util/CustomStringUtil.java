package me.modernpage.util;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CustomStringUtil {
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Returns a new StringBuilder to be used while producing SQL queries.
     *
     * @return A new or recycled StringBuilder
     */
    public static StringBuilder newStringBuilder() {
        // TODO pool:
        return new StringBuilder();
    }

    /**
     * Adds bind variable placeholders (?) to the given string. Each placeholder is separated
     * by a comma.
     *
     * @param builder The StringBuilder for the query
     * @param count   Number of placeholders
     */
    public static void appendPlaceholders(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append("?");
            if (i < count - 1) {
                builder.append(",");
            }
        }
    }

    /**
     * Splits a comma separated list of integers to integer list.
     * <p>
     * If an input is malformed, it is omitted from the result.
     *
     * @param input Comma separated list of integers.
     * @return A List containing the integers or null if the input is null.
     */
    @Nullable
    public static List<Integer> splitToIntList(@Nullable String input) {
        if (input == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input, ",");
        while (tokenizer.hasMoreElements()) {
            final String item = tokenizer.nextToken();
            try {
                result.add(Integer.parseInt(item));
            } catch (NumberFormatException ex) {
                Log.e("ROOM", "Malformed integer list", ex);
            }
        }
        return result;
    }

    /**
     * Joins the given list of integers into a comma separated list.
     *
     * @param input The list of integers.
     * @return Comma separated string composed of integers in the list. If the list is null, return
     * value is null.
     */
    @Nullable
    public static String joinIntoString(@Nullable List<Integer> input) {
        if (input == null) {
            return null;
        }

        final int size = input.size();
        if (size == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(Integer.toString(input.get(i)));
            if (i < size - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private CustomStringUtil() {
    }

    /**
     * Splits a comma separated list of longs to long list.
     * <p>
     * If an input is malformed, it is omitted from the result.
     *
     * @param input Comma separated list of longs.
     * @return A List containing the longs or null if the input is null.
     */

    @Nullable
    public static List<Long> splitToLongList(@Nullable String input) {
        if (input == null) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input, ",");
        while (tokenizer.hasMoreElements()) {
            final String item = tokenizer.nextToken();
            try {
                result.add(Long.parseLong(item));
            } catch (NumberFormatException ex) {
                Log.e("ROOM", "Malformed long list", ex);
            }
        }
        return result;
    }

    /**
     * Joins the given list of longs into a comma separated list.
     *
     * @param input The list of longs.
     * @return Comma separated string composed of longs in the list. If the list is null, return
     * value is null.
     */
    @Nullable
    public static String joinLongToString(@Nullable List<Long> input) {
        if (input == null) {
            return null;
        }

        final int size = input.size();
        if (size == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(Long.toString(input.get(i)));
            if (i < size - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
