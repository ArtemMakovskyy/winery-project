package com.winestoreapp.wineryadminui.features.user.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FieldMatchValidatorTest {

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    private FieldMatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FieldMatchValidator();

        FieldMatch fieldMatch = mock(FieldMatch.class);
        when(fieldMatch.field()).thenReturn("field");
        when(fieldMatch.fieldMatch()).thenReturn("fieldMatch");

        validator.initialize(fieldMatch);
    }

    @Test
    void shouldReturnTrueWhenBothFieldsAreEqual() {
        TestObject obj = new TestObject("password123", "password123");
        assertThat(validator.isValid(obj, context)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFieldsAreDifferent() {
        TestObject obj = new TestObject("password123", "otherPassword");
        assertThat(validator.isValid(obj, context)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            ", value",
            "value, ",
            ", "
    })
    void shouldReturnFalseWhenAnyFieldIsNull(String f1, String f2) {
        TestObject obj = new TestObject(f1, f2);
        assertThat(validator.isValid(obj, context)).isFalse();
    }

    @Test
    void shouldWorkWithNumericValues() {
        NumericObject obj = new NumericObject(100, 100);

        FieldMatch fieldMatch = mock(FieldMatch.class);
        when(fieldMatch.field()).thenReturn("num1");
        when(fieldMatch.fieldMatch()).thenReturn("num2");
        validator.initialize(fieldMatch);

        assertThat(validator.isValid(obj, context)).isTrue();
    }

    private record TestObject(String field, String fieldMatch) {
    }

    private record NumericObject(Integer num1, Integer num2) {
    }
}
