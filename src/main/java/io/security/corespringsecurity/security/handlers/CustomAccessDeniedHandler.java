package io.security.corespringsecurity.security.handlers;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("accessDeniedHandler")
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    //즉, 인가 예외 발생 시 해당 handle 메서드가 실행하여 /denied 로 리다이렉트
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       //인가 예외
                       AccessDeniedException e) throws IOException, ServletException {
        String deniedUrl= "/denied" + "?exception=" + e.getMessage();
        response.sendRedirect(deniedUrl);
    }

}
