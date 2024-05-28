package net.synedra.validatorfx;

import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.synedra.validatorfx.extended.Typed;
import net.synedra.validatorfx.extended.TypedCheck;
import net.synedra.validatorfx.extended.TypedValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class TypedValidatorTest extends TestBase {
    private enum TestType implements Typed<TestType> {
        TYPE_1, TYPE_2;

        @Override
        public TestType getType() {
            return this;
        }
    }

    private VBox root;

    @Start
    private void setupScene(Stage stage) {
        root = new VBox();
        stage.setScene(new Scene(root, 640, 400));
        stage.show();
    }

    @BeforeEach
    public void resetRoot() {
        fx(root.getChildren()::clear);
    }

    @Test
    void testTextFieldWithTwoChecks(FxRobot robot) {
        TextField textfield = new TextField();
        fx(() -> root.getChildren().add(textfield));

        TypedValidator<TestType> validator = new TypedValidator<>();
        Check c1 = validator.createTypedCheck(TestType.TYPE_1)
                .withMethod(this::maxSize)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield)
                .immediate();
        Check c2 = validator.createTypedCheck(TestType.TYPE_2)
                .withMethod(this::noVowels)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield)
                .immediate();

        WaitForAsyncUtils.waitForFxEvents(); // .immediate() will call the initial update delayed, so we have to wait 
        assertEquals(0, validator.getValidationResult().getMessages().size());
        assertFalse(validator.containsWarnings());
        assertFalse(validator.containsErrors());

        robot.clickOn(".text-field");
        robot.type(KeyCode.A, 1);
        checkMessage(validator, Severity.ERROR, "Txt cntns vwls");
        assertFalse(validator.containsWarnings());
        assertTrue(validator.containsErrors());

        Optional<TypedCheck<TestType>> optionalType1Check = validator.getFor(TestType.TYPE_1);
        assertTrue(optionalType1Check.isPresent());
        Assertions.assertInstanceOf(TypedCheck.class, optionalType1Check.get());
        TypedCheck<TestType> typedCheckType1 = optionalType1Check.get();
        Assertions.assertEquals(c1, typedCheckType1);

        Optional<TypedCheck<TestType>> optionalType2Check = validator.getFor(TestType.TYPE_2);
        assertTrue(optionalType2Check.isPresent());
        Assertions.assertInstanceOf(TypedCheck.class, optionalType1Check.get());
        TypedCheck<TestType> typedCheckType2 = optionalType2Check.get();
        Assertions.assertEquals(c2, typedCheckType2);

        robot.type(KeyCode.A, 6);
        checkMessage(validator, Severity.WARNING, "Too long", Severity.ERROR, "Txt cntns vwls");
        assertTrue(validator.containsWarnings());
        assertTrue(validator.containsErrors());

        validator.remove(c1);
        checkMessage(validator, Severity.ERROR, "Txt cntns vwls");
        assertFalse(validator.containsWarnings());
        assertTrue(validator.containsErrors());

        validator.remove(c2);
        assertEquals(0, validator.getValidationResult().getMessages().size());
        assertFalse(validator.containsWarnings());
        assertFalse(validator.containsErrors());
    }

    @Test
    void testExplicitValidate(FxRobot robot) {
        TextField textfield = new TextField();
        fx(() -> root.getChildren().add(textfield));

        TypedValidator<TestType> validator = new TypedValidator<>();
        validator.createTypedCheck(TestType.TYPE_1)
                .withMethod(this::noVowels)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield);

        assertEquals(0, validator.getValidationResult().getMessages().size());
        assertFalse(validator.containsWarnings());
        assertFalse(validator.containsErrors());

        robot.clickOn(".text-field");
        robot.type(KeyCode.A, 1);
        // although we violate the check, nothing should happen until ...
        assertEquals(0, validator.getValidationResult().getMessages().size());
        assertFalse(validator.containsWarnings());
        assertFalse(validator.containsErrors());

        // ... we validate explicitly
        assertFalse(fx(validator::validate));
        checkMessage(validator, Severity.ERROR, "Txt cntns vwls");
        assertFalse(validator.containsWarnings());
        assertTrue(validator.containsErrors());

        Optional<TypedCheck<TestType>> optionalType1Check = validator.getFor(TestType.TYPE_1);
        assertTrue(optionalType1Check.isPresent());
        Assertions.assertInstanceOf(TypedCheck.class, optionalType1Check.get());
        TypedCheck<TestType> typedCheckType1 = optionalType1Check.get();
        List<ValidationMessage> messages = typedCheckType1.getValidationResult().getMessages();
        Assertions.assertEquals(1, messages.size());
        ValidationMessage validationMessage = messages.get(0);
        Assertions.assertEquals(Severity.ERROR, validationMessage.getSeverity());
        Assertions.assertEquals("Txt cntns vwls", validationMessage.getText());
    }

    @Test
    void testModeChange(FxRobot robot) {
        TextField textfield = new TextField();
        fx(() -> root.getChildren().add(textfield));

        TypedValidator<TestType> validator = new TypedValidator<>();
        validator.createTypedCheck(TestType.TYPE_1)
                .withMethod(this::noVowels)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield)
        ;

        robot.clickOn(".text-field");
        robot.type(KeyCode.A, 1);
        // Default mode is explicit ...
        assertEquals(0, validator.getValidationResult().getMessages().size());

        // Immediate will show the violating vowel
        fx(validator::immediate);
        WaitForAsyncUtils.waitForFxEvents(); // .immediate() will call the initial update delayed, so we have to wait 
        assertEquals(1, validator.getValidationResult().getMessages().size());

        // If we clear no errors are there anymore
        fx(validator::clear);
        assertEquals(0, validator.getValidationResult().getMessages().size());

        // Typing will immediately show the error again
        robot.type(KeyCode.B, 1);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, validator.getValidationResult().getMessages().size());

        // Switching to immediateClear will not change anything per se ...
        fx(validator::immediateClear);
        // But typing will clear the error
        robot.type(KeyCode.B, 1);
        assertEquals(0, validator.getValidationResult().getMessages().size());
        // ... until we explicitly validate 
        fx(validator::validate);
        assertEquals(1, validator.getValidationResult().getMessages().size());

        // Clearing errors & switching to explicit mode ...
        fx(validator::clear);
        fx(validator::explicit);
        robot.type(KeyCode.B, 1);
        assertEquals(0, validator.getValidationResult().getMessages().size());
        fx(validator::validate);
        assertEquals(1, validator.getValidationResult().getMessages().size());
        // Typing no longer make errors disappear ...
        robot.type(KeyCode.C, 1);
        assertEquals(1, validator.getValidationResult().getMessages().size());
    }


    @Test
    void testStringProperty(FxRobot robot) {
        TextField textfield = new TextField();
        fx(() -> root.getChildren().add(textfield));

        TypedValidator<TestType> validator = new TypedValidator<>();
        validator.createTypedCheck(TestType.TYPE_1)
                .withMethod(this::maxSize)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield)
                .immediate()
        ;
        validator.createTypedCheck(TestType.TYPE_2)
                .withMethod(this::noVowels)
                .dependsOn("content", textfield.textProperty())
                .decorates(textfield)
                .immediate()
        ;

        StringBinding all = validator.createStringBinding("* ", "\n", Severity.WARNING, Severity.ERROR);
        StringBinding errors = validator.createStringBinding("* ", "\n", Severity.ERROR);
        StringBinding errors2 = validator.createStringBinding("* ", "\n");
        StringBinding warnings = validator.createStringBinding("* ", "\n", Severity.WARNING);
        StringBinding defaultBinding = validator.createStringBinding();

        assertEquals("", all.get());
        assertEquals("", errors.get());
        assertEquals("", errors2.get());
        assertEquals("", warnings.get());
        assertEquals("", defaultBinding.get());

        WaitForAsyncUtils.waitForFxEvents(); // .immediate() will call the initial update delayed, so we have to wait 

        robot.clickOn(".text-field");
        robot.type(KeyCode.A, 1);

        assertEquals("* Txt cntns vwls", all.get());
        assertEquals("* Txt cntns vwls", errors.get());
        assertEquals("* Txt cntns vwls", errors2.get());
        assertEquals("", warnings.get());
        assertEquals("• Txt cntns vwls", defaultBinding.get());


        robot.type(KeyCode.A, 6);
        assertEquals("* Too long\n* Txt cntns vwls", all.get());
        assertEquals("* Txt cntns vwls", errors.get());
        assertEquals("* Txt cntns vwls", errors2.get());
        assertEquals("* Too long", warnings.get());
        assertEquals("• Txt cntns vwls", defaultBinding.get());
    }

    private void maxSize(Check.Context c) {
        String text = c.get("content");
        if (text.length() > 5) {
            c.warn("Too long");
        }
    }

    private void noVowels(Check.Context c) {
        String text = c.get("content");
        if (text.matches(".*[aeiouAEIOU].*")) {
            c.error("Txt cntns vwls");
        }
    }

    private void checkMessage(TypedValidator<TestType> validator, Severity severity, String text) {
        List<ValidationMessage> messages = validator.getValidationResult().getMessages();
        assertEquals(1, messages.size());
        assertEquals(text, messages.get(0).getText());
        assertEquals(severity, messages.get(0).getSeverity());
    }

    private void checkMessage(TypedValidator<TestType> validator, Severity severity, String text, Severity severity2, String text2) {
        List<ValidationMessage> messages = validator.getValidationResult().getMessages();
        assertEquals(2, messages.size());
        assertEquals(text, messages.get(0).getText());
        assertEquals(severity, messages.get(0).getSeverity());
        assertEquals(text2, messages.get(1).getText());
        assertEquals(severity2, messages.get(1).getSeverity());
    }
}
