package com.thirdparty.ticketing.global.security;

import com.thirdparty.ticketing.domain.common.TicketingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationContext authenticationContext;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasParameterAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean hasLongParameterType = parameter.getParameterType().isAssignableFrom(String.class);
        return hasParameterAnnotation && hasLongParameterType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = authenticationContext.getAuthentication();
        checkAuthenticated(authentication);
        return authentication.getPrincipal();
    }

    private void checkAuthenticated(Authentication authentication) {
        if (authentication != null) {
            return;
        }
        throw new AuthenticationException("인증되지 않은 사용자 요청입니다.");
    }
}
