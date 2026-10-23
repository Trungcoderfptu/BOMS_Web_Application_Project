package utility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class to forward flash messages from HttpSession to HttpServletRequest
 * and clean up the session attributes (PRG pattern support).
 */
public final class FlashMessageUtil {

    private FlashMessageUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Forwards SUCCESS_MESSAGE and ERROR_MESSAGE from session to request,
     * then removes them from session to avoid duplicate display upon refresh.
     *
     * @param request current HttpServletRequest
     * @param session current HttpSession (can be null)
     */
    public static void forwardFlashMessages(HttpServletRequest request, HttpSession session) {
        if (session == null || request == null) {
            return;
        }

        Object successMessage = session.getAttribute(Constants.SUCCESS_MESSAGE);
        if (successMessage != null) {
            request.setAttribute(Constants.SUCCESS_MESSAGE, successMessage);
            session.removeAttribute(Constants.SUCCESS_MESSAGE);
        }

        Object errorMessage = session.getAttribute(Constants.ERROR_MESSAGE);
        if (errorMessage != null) {
            request.setAttribute(Constants.ERROR_MESSAGE, errorMessage);
            session.removeAttribute(Constants.ERROR_MESSAGE);
        }

        Object activeTab = session.getAttribute(Constants.ACTIVE_TAB);
        if (activeTab != null) {
            request.setAttribute(Constants.ACTIVE_TAB, activeTab);
            session.removeAttribute(Constants.ACTIVE_TAB);
        }
    }
}
