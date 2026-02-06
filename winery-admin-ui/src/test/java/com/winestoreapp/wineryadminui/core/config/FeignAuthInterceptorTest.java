package com.winestoreapp.wineryadminui.core.config;

import com.winestoreapp.wineryadminui.core.security.SessionTokenStorage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeignAuthInterceptorTest {

    private SessionTokenStorage storage;
    private Tracer tracer;
    private FeignAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        storage = mock(SessionTokenStorage.class);
        tracer = mock(Tracer.class);
        interceptor = new FeignAuthInterceptor(storage, tracer);
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

            interceptor.authRequestInterceptor().apply(template);

            assertTrue(template.headers().containsKey("Authorization"));
            assertEquals("Bearer my-token", template.headers().get("Authorization").iterator().next());
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

            interceptor.authRequestInterceptor().apply(template);

            assertFalse(template.headers().containsKey("Authorization"));
        }
    }

    @Test
    void shouldHandleNoSessionOrRequestAttributesGracefully() {
        // no session
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);

        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            assertDoesNotThrow(() -> interceptor.authRequestInterceptor().apply(template));
            assertFalse(template.headers().containsKey("Authorization"));
        }

        // no RequestAttributes
        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            assertDoesNotThrow(() -> interceptor.authRequestInterceptor().apply(template));
            assertFalse(template.headers().containsKey("Authorization"));
        }
    }

    @Test
    void shouldTagSpanIfTracerPresent() {
        HttpSession session = mock(HttpSession.class);
        // Додано: налаштовуємо ID сесії
        when(session.getId()).thenReturn("test-session-123");
        when(storage.get(session)).thenReturn("my-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
        when(attrs.getRequest()).thenReturn(request);

        Span span = mock(Span.class);
        when(tracer.currentSpan()).thenReturn(span);

        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            RequestTemplate template = new RequestTemplate().target("http://localhost/api/data");

            interceptor.authRequestInterceptor().apply(template);

            verify(span).tag("session.id", "test-session-123");
            assertEquals("Bearer my-token", template.headers().get("Authorization").iterator().next());
        }
    }
}
