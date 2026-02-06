package com.winestoreapp.wineryadminui.features.dashboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardUiControllerTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @InjectMocks
    private DashboardUiController dashboardUiController;

    @Test
    void dashboard_ShouldReturnViewAndTagSpan() {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);

        // Act
        String viewName = dashboardUiController.dashboard();

        // Assert
        assertThat(viewName).isEqualTo("dashboard/dashboard");
        verify(span).tag("ui.page", "dashboard");
    }

    @Test
    void dashboard_ShouldReturnViewWhenSpanIsNull() {
        // Arrange
        when(tracer.currentSpan()).thenReturn(null);

        // Act
        String viewName = dashboardUiController.dashboard();

        // Assert
        assertThat(viewName).isEqualTo("dashboard/dashboard");
        verifyNoInteractions(span);
    }
}