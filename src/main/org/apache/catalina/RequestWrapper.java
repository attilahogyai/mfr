package org.apache.catalina;

import javax.servlet.http.HttpServletRequest;

public class RequestWrapper {
    public static ThreadLocal<HttpServletRequest> httpRequest = new ThreadLocal<HttpServletRequest>() {
    };
}
