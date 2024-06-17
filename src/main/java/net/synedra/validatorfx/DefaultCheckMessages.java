package net.synedra.validatorfx;

import javafx.beans.value.ObservableValue;

import java.util.Objects;
import java.util.function.Function;

public interface DefaultCheckMessages {
    /**
     * Will be called when a {@link DefaultChecks#createNonNullCheck(ObservableValue, String, Severity)} fails
     * @return the message that will be attached to the validation result
     */
    default String notNullMessage(String fieldName) {
        return String.format("%s mustn't be null", fieldName);
    }

    /**
     * Will be called when a {@link DefaultChecks#createNonBlankCheck(ObservableValue, String, Severity)} fails
     * @return the message that will be attached to the validation result
     */
    default String notBlankMessage(String fieldName) {
        return String.format("%s is required to not be blank", fieldName);
    }

    /**
     * Will be called when a {@link DefaultChecks#createMinimumLengthCheck(ObservableValue, String, Severity, int)} fails
     * @return the message that will be attached to the validation result
     */
    default String minimumLengthMessage(String fieldName, String currentValue, int minimumLength) {
        return String.format("%s with value of ['%s'] should be at least %d characters long", fieldName, currentValue, minimumLength);
    }

    /**
     * Will be called when a {@link DefaultChecks#createMaximumLengthCheck(ObservableValue, String, Severity, int)} fails
     * @return the message that will be attached to the validation result
     */
    default String maximumLengthMessage(String fieldName, String currentValue, int maximumLength) {
        return String.format("%s with value of ['%s'] should be at most %d characters long", fieldName, currentValue, maximumLength);
    }

    /**
     * Will be called when a {@link DefaultChecks#createIsMappableToCheck(ObservableValue, String, Severity, Function)} fails
     * @return the message that will be attached to the validation result
     */
    default String isAssignableMessage(String fieldName) {
        return String.format("%s is not assignable", fieldName);
    }

    /**
     * Will be called when a {@link DefaultChecks#createIsNumberCheck(ObservableValue, String, Severity)} fails
     * @return the message that will be attached to the validation result
     */
    default String isNumberMessage(String fieldName) {
        return String.format("%s is not a number", fieldName);
    }

    /**
     * Will be called when a {@link DefaultChecks#createIsNumberWithinBoundsCheck(ObservableValue, String, Severity, double, double)} fails
     * @return the message that will be attached to the validation result
     */
    default String isNumberWithinBoundsMessage(String fieldName, String currentValue, double minimum, double maximum) {
        return String.format("%s['%s'] is not between %.2f and %.2f", fieldName, currentValue, minimum, maximum);
    }

    /**
     * Will be called when a {@link DefaultChecks#createMatchesRegexCheck(ObservableValue, String, Severity, String)} fails
     * @return the message that will be attached to the validation result
     */
    default String matchesRegexMessage(String fieldName, String currentValue, String regex) {
        return String.format("%s['%s'] does not match the regex ['%s']", fieldName, currentValue, regex);
    }

    class CurrentCheckMessagesFactory {
        private static DefaultCheckMessages INSTANCE = new DefaultCheckMessages(){};

        public static DefaultCheckMessages getDefaultCheckMessagesFactory() {
            return INSTANCE;
        }

        public static void setDefaultCheckMessagesFactory(DefaultCheckMessages INSTANCE) {
            CurrentCheckMessagesFactory.INSTANCE = Objects.requireNonNull(INSTANCE);
        }
    }
}