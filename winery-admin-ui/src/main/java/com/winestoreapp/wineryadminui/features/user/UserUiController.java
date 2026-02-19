package com.winestoreapp.wineryadminui.features.user;

import com.winestoreapp.wineryadminui.core.observability.ObservationContextualNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationNames;
import com.winestoreapp.wineryadminui.core.observability.ObservationTags;
import com.winestoreapp.wineryadminui.core.observability.SpanTagger;
import com.winestoreapp.wineryadminui.features.user.dto.UpdateRoleForm;
import com.winestoreapp.wineryadminui.features.user.dto.UserResponseDto;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui/users")
@RequiredArgsConstructor
@Slf4j
public class UserUiController {

    private final UserService userService;
    private final SpanTagger spanTagger;

    @GetMapping("/role")
    @Observed(name = ObservationNames.USER_CONTROLLER,
            contextualName = ObservationContextualNames.ROLE_FORM,
            lowCardinalityKeyValues = {ObservationTags.FORM, ObservationTags.USER_ROLE}
    )
    public String roleForm(Model model) {
        model.addAttribute("form", new UpdateRoleForm());
        return "user/user-role";
    }

    @PostMapping("/role")
    @Observed(name = ObservationNames.USER_CONTROLLER,
            contextualName = ObservationContextualNames.UPDATE_ROLE,
            lowCardinalityKeyValues = {
                    ObservationTags.FORM, ObservationTags.USER_ROLE,
                    ObservationTags.OPERATION, ObservationTags.WRITE}
    )
    public String updateRole(
            @Valid @ModelAttribute("form") UpdateRoleForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            spanTagger.tag(ObservationTags.VALIDATION, "failed");
            return "user/user-role";
        }

        spanTagger.tag(ObservationTags.USER_ID, form.getUserId());
        spanTagger.tag(ObservationTags.USER_ROLE, form.getRole());

        try {
            UserResponseDto updated = userService.updateUserRole(form.getUserId(), form.getRole());
            model.addAttribute(ObservationTags.SUCCESS, true);
            model.addAttribute(ObservationTags.USER_ID, updated.getId());
            spanTagger.tag(ObservationTags.STATUS, ObservationTags.SUCCESS);
        } catch (RuntimeException e) {
            spanTagger.tag(ObservationTags.ERROR_MESSAGE, ObservationTags.BUSINESS_LOGIC);
            spanTagger.error(e);
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user role", e);
            spanTagger.error(e);
            model.addAttribute("error", "An unexpected error occurred");
        }

        return "user/user-role";
    }
}
