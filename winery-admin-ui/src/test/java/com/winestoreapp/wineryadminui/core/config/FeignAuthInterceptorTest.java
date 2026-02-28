package com.winestoreapp.wineryadminui.core.config;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FeignAuthInterceptorTest {

    private SessionTokenStorage storage;
    private SpanTagger spanTagger;
    private FeignAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        storage = mock(SessionTokenStorage.class);
        spanTagger = mock(SpanTagger.class);

        interceptor = new FeignAuthInterceptor();
        interceptor.setStorage(storage);
        interceptor.setSpanTagger(spanTagger);
        interceptor.setSkipUrls(List.of("/health"));
    }

    @Test
    void shouldAddAuthorizationHeader() {
        HttpSession session = mock(HttpSession.class);
        when(storage.get(session)).thenReturn("my-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);

        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            interceptor.apply(template);

            assertTrue(template.headers().containsKey("Authorization"));
            assertEquals("Bearer my-token", template.headers().get("Authorization").iterator().next());
            verify(spanTagger).tag("auth.header.present", true);
        }
    }

    @Test
    void shouldNotAddHeaderForHealthCheck() {
        HttpSession session = mock(HttpSession.class);
        when(storage.get(session)).thenReturn("my-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);

        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            RequestTemplate template = new RequestTemplate().target("http://localhost/health/check");

            interceptor.apply(template);

            assertFalse(template.headers().containsKey("Authorization"));
            verifyNoInteractions(spanTagger);
        }
    }

    @Test
    void shouldHandleNoSessionOrRequestAttributesGracefully() {
        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            assertDoesNotThrow(() -> interceptor.apply(template));
            assertFalse(template.headers().containsKey("Authorization"));
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);

        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);
            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            assertDoesNotThrow(() -> interceptor.apply(template));
            assertFalse(template.headers().containsKey("Authorization"));
        }
    }
}
