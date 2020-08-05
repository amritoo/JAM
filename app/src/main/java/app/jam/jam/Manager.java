package app.jam.jam;

import android.content.Context;
import android.content.res.Resources;
import android.util.Patterns;

import java.util.regex.Pattern;

public class Manager {

    /**
     * Checks for validity of an email address
     *
     * @param target the email to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Checks for validity of given password
     *
     * @param context The application context to access resource files
     * @param target the password to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidPassword(Context context, CharSequence target) {
        if (target == null)
            return false;

        Resources res = context.getResources();
        if (target.length() < res.getInteger(R.integer.password_min_length) || target.length() > res.getInteger(R.integer.password_max_length)) {
            return false;
        }
//        return Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]", target);   // Checking if target contains punctuation
        Pattern textPattern = Pattern.compile(context.getString(R.string.password_pattern_check));   // Checking if target contains Upper case and digit
        return textPattern.matcher(target).matches();
    }

    /**
     * Checks for validity of given username
     *
     * @param context The application context to access resource files
     * @param target the username to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidUsername(Context context, CharSequence target) {
        if (target == null)
            return false;
        Resources res = context.getResources();
        return target.length() >= res.getInteger(R.integer.username_min_length) && target.length() <= res.getInteger(R.integer.username_max_length);
    }

}
