package com.winestoreapp.wineryadminui.features.dashboard;

import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardUiControllerTest {

    @Mock
    private SpanTagger spanTagger;

    @InjectMocks
    private DashboardUiController dashboardUiController;

    @Test
    void dashboard_ShouldReturnViewAndTag() {
        String viewName = dashboardUiController.dashboard();

        assertThat(viewName).isEqualTo("dashboard/dashboard");
        verify(spanTagger).tag("ui.page", "dashboard");
    }
}
