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
    public static void checkValidPassword(String password, String errorMessage) throws ValidationException {
        //TODO regax password for future
        if (password != null && password.contains(" ")) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void checkEmailFormat(String email, String errorMessage) throws ValidationException {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (email != null && !email.matches(emailRegex)) {
            throw new ValidationException(errorMessage);
        }
    }

    public static void checkPhoneFormat(String phone, String errorMessage) throws ValidationException {
        String phoneRegex = "^0\\d{9,10}$";
        if (phone != null && !phone.matches(phoneRegex)) {
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
