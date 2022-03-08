
package org.telescope.javel.framework.helper;

import javax.servlet.http.HttpServletRequest;

public final class ServletHelper {

    private ServletHelper() {
    }

    public static String retrieveRemoteAddress(HttpServletRequest request) {

        if (request != null) {
            String remoteAddress = request.getHeader("X-FORWARDED-FOR");

            if (remoteAddress != null && !remoteAddress.isEmpty()) {
                int separatorIndex = remoteAddress.indexOf(",");
                if (separatorIndex > 0) {
                    return remoteAddress.substring(0, separatorIndex); // remove the additional data
                } else {
                    return remoteAddress;
                }
            } else {
                return request.getRemoteAddr();
            }
        } else {
            return null;
        }
    }

}
