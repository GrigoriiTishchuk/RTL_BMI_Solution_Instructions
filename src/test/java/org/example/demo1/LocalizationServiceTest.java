package org.example.demo1;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LocalizationServiceTest {

    @Test
    void getLocalizedStrings_returnsMapFromResultSet() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("key")).thenReturn("weight", "height");
        when(mockResultSet.getString("value")).thenReturn("Weight (kg)", "Height (m)");

        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Map<String, String> result = LocalizationService.getLocalizedStrings(Locale.of("en", "US"));

            verify(mockStatement).setString(1, "en");
            assertEquals(2, result.size());
            assertEquals("Weight (kg)", result.get("weight"));
            assertEquals("Height (m)", result.get("height"));
        }
    }

    @Test
    void getLocalizedStrings_whenSqlExceptionThrown_returnsEmptyMap() {
        SQLException sqlException = new SQLException("Connection failed");

        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(sqlException);

            Map<String, String> result = LocalizationService.getLocalizedStrings(Locale.of("fa", "IR"));

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void resourceBundles_containExpectedKeysForConfiguredLocales() {
        List<Locale> locales = List.of(
                Locale.of("en", "US"),
                Locale.of("fr", "FR"),
                Locale.of("fa", "IR"),
                Locale.of("ur", "PA"),
                Locale.of("vi", "VI")
        );

        for (Locale locale : locales) {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            assertFalse(bundle.getString("weight").isBlank());
            assertFalse(bundle.getString("height").isBlank());
            assertFalse(bundle.getString("calculate").isBlank());
            assertFalse(bundle.getString("result").isBlank());
            assertFalse(bundle.getString("invalid").isBlank());
            assertFalse(bundle.getString("localTime").isBlank());
        }
    }
}