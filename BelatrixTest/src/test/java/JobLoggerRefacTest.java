import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import pe.epequin.alberto.JobLoggerRefac;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class JobLoggerRefacTest {

    @Mock private Connection mockConnection;
    @Mock private Statement mockStatement;
    Connection connection;
    Map map;

    @Test
    public void logearArchivo() throws Exception {
        Map map = new HashMap<String, String>();
        map.put("logFileFolder","D:/data c/Documents/log");
        JobLoggerRefac jobLoggerRefac = new JobLoggerRefac(
        true, false, false,
        false, false, false, map);
        jobLoggerRefac.LogMessage("test archivo", false, false, false);
    }

    @Test
    public void logearConsola() throws Exception {
        JobLoggerRefac jobLoggerRefac = new JobLoggerRefac(
                false, true, false,
                false, false, false, null);
        jobLoggerRefac.LogMessage("test consola", false, false, false);
    }

    @Before
    public void setUp() throws SQLException {
        map = new HashMap<String, String>();
        map.put("userName","root");
        map.put("password","root");
        map.put("dbms","mysql");
        map.put("serverName","localhost");
        map.put("portNumber","3306");
        connection = null;
        MockitoAnnotations.initMocks(this);
        final Statement statement = Mockito.mock(Statement.class);
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.createStatement()).thenReturn(statement);
        PowerMockito.mockStatic(DriverManager.class);
    }

    @Test
    public void logearBdMessage() throws Exception {
        JobLoggerRefac jobLoggerRefac = new JobLoggerRefac(
                false, false, true,
                true, false, false, map);
        jobLoggerRefac.LogMessage("test logear BD Message", true, false, false);
    }

    @Test
    public void logearBdWarning() throws Exception {
        JobLoggerRefac jobLoggerRefac = new JobLoggerRefac(
                false, false, true,
                false, true, false, map);
        jobLoggerRefac.LogMessage("test logear BD warning", false, true, false);
    }

    @Test
    public void logearBdError() throws Exception {
        JobLoggerRefac jobLoggerRefac = new JobLoggerRefac(
                false, false, true,
                false, false, true, map);
        jobLoggerRefac.LogMessage("test logear BD error", false, false, true);
    }
}
