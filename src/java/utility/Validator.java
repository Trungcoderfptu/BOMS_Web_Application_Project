/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author AD
 */
public class Validator {

    public static void checkEmpty(String value, String errorMessage) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void checkMinLength(String value, int minLength, String errorMessage) throws ValidationException {
        if (value != null && value.trim().length() < minLength) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void checkValidUsername(String username, String errorMessage) throws ValidationException {
        if (username != null && (username.contains(" ") || username.contains("'"))) {
            throw new ValidationException(errorMessage);
        }
    }

    public static int checkInteger(String value, String errorMessage) throws ValidationException {
        checkEmpty(value, errorMessage);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(errorMessage);
        }
    }
}
