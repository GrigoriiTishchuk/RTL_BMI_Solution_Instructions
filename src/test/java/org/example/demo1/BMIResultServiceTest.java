package org.example.demo1;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BMIResultServiceTest {

    @Test
    void saveResult_insertsParametersAndExecutesUpdate() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            BMIResultService.saveResult(72.5, 1.75, 23.67, "en");

            verify(mockStatement).setDouble(1, 72.5);
            verify(mockStatement).setDouble(2, 1.75);
            verify(mockStatement).setDouble(3, 23.67);
            verify(mockStatement).setString(4, "en");
            verify(mockStatement).executeUpdate();
        }
    }

    @Test
    void saveResult_whenSqlExceptionThrown_doesNotPropagate() {
        SQLException sqlException = new SQLException("DB unavailable");

        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(sqlException);

            assertDoesNotThrow(() -> BMIResultService.saveResult(60.0, 1.65, 22.04, "fa"));
        }
    }
}