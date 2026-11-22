package org.example.demo1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BMIControllerTest {
    private BMIController controller;
    private VBox rootVBox;
    private Label lblWeight;
    private Label lblHeight;
    private TextField tfWeight;
    private TextField tfHeight;
    private Button btnCalculate;
    private Label lblResult;
    private Label lblLocalTime;

    @BeforeAll
    static void initJavaFxToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit уже запущен
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new BMIController();
        rootVBox = mock(VBox.class);
        lblWeight = mock(Label.class);
        lblHeight = mock(Label.class);
        tfWeight = mock(TextField.class);
        tfHeight = mock(TextField.class);
        btnCalculate = mock(Button.class);
        lblResult = mock(Label.class);
        lblLocalTime = mock(Label.class);
        setField("rootVBox", rootVBox);
        setField("lblWeight", lblWeight);
        setField("lblHeight", lblHeight);
        setField("tfWeight", tfWeight);
        setField("tfHeight", tfHeight);
        setField("btnCalculate", btnCalculate);
        setField("lblResult", lblResult);
        setField("lblLocalTime", lblLocalTime);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = BMIController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void onCalculateClick_validInputs_setsFormattedBmiAndCallsSaveResult() {
        when(tfWeight.getText()).thenReturn("70");
        when(tfHeight.getText()).thenReturn("175");
        try (MockedStatic<LocalizationService> locMock = mockStatic(LocalizationService.class);
             MockedStatic<BMIResultService> saveMock = mockStatic(BMIResultService.class);
             MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            locMock.when(() -> LocalizationService.getLocalizedStrings(any(Locale.class)))
                    .thenReturn(Map.of("result", "BMI:"));
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(invocation -> null);
            controller.onENClick(new ActionEvent());
            controller.onCalculateClick(new ActionEvent());
            verify(lblResult).setText("BMI: 22.86");
            saveMock.verify(() -> BMIResultService.saveResult(
                    eq(70.0), eq(1.75), anyDouble(), eq("en")));
        }
    }

    @Test
    void onCalculateClick_invalidInput_setsInvalidText() {
        when(tfWeight.getText()).thenReturn("abc");
        when(tfHeight.getText()).thenReturn("175");
        try (MockedStatic<LocalizationService> locMock = mockStatic(LocalizationService.class);
             MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            locMock.when(() -> LocalizationService.getLocalizedStrings(any(Locale.class)))
                    .thenReturn(Map.of("invalid", "Bad input"));
            platformMock.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(invocation -> null);
            controller.onENClick(new ActionEvent());
            controller.onCalculateClick(new ActionEvent());
            verify(lblResult).setText("Bad input");
        }
    }
}