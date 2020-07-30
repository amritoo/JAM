package app.jam.jam;

import android.text.TextUtils;

public class Manager {

    /**
     * Checks for validity of an email address
     *
     * @param target the email to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Checks for validity of given password
     *
     * @param target the password to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidPassword(CharSequence target) {
        return target != null && target.length() >= 6;
    }

    /**
     * Checks for validity of given username
     *
     * @param target the username to check
     * @return true if target is valid, false otherwise
     */
    public static boolean isValidUsername(CharSequence target) {
        return target != null && target.length() > 2 ;
    }

}
