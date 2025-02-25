//package com.ehb.connected.config;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class AjaxAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws ServletException, IOException {
//        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
//            // For AJAX requests, return a 200 OK status without redirection
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().write("{\"status\":\"success\"}");
//            response.getWriter().flush();
//        } else {
//            super.onAuthenticationSuccess(request, response, authentication);
//        }
//    }
//}

