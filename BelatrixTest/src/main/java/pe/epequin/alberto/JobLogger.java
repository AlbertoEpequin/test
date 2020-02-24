package pe.epequin.alberto;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
    private static boolean logToFile;
    private static boolean logToConsole;
    private static boolean logMessage;
    private static boolean logWarning;
    private static boolean logError;
    private static boolean logToDatabase;
    private static Map dbParams;
    private static Logger logger;

    //Consttructor para poder instanciar a la clase JobLogger
    public JobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
                     boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
        logger = Logger.getLogger("MyLog");
        logError = logErrorParam;
        logMessage = logMessageParam;
        logWarning = logWarningParam;
        logToDatabase = logToDatabaseParam;
        logToFile = logToFileParam;
        logToConsole = logToConsoleParam;
        dbParams = dbParamsMap;
    }

    public static void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {
        //Se quitan los espacios en blanco
        messageText.trim();
        //se verifica que el mensaje a logear tenga contenido
        if (messageText == null || messageText.length() == 0) {
            return;
        }
        //Se verifica que tengamos una accion asignada para el logeo ya sea en consola en archivo o BD
        if (!logToConsole && !logToFile && !logToDatabase) {
            throw new Exception("Invalid configuration");
        }

        //Se verifica que tengamos un level para el logeo
        if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
            throw new Exception("Error or Warning or Message must be specified");
        }

        //seteamos los valores para poder conectarnos a la BD
        Connection connection = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", dbParams.get("userName"));
        connectionProps.put("password", dbParams.get("password"));
        connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
                + ":" + dbParams.get("portNumber") + "/", connectionProps);

        //Categorizamos el tipo de mensaje a logear
        int t = 0;
        if (message && logMessage) {
            t = 1;
        }

        if (error && logError) {
            t = 2;
        }

        if (warning && logWarning) {
            t = 3;
        }

        //Creamos el Statement para conexion a la BD
        Statement stmt = connection.createStatement();

        String l = null;
        //instanciamos el archivo logFile
        File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
        //verificamos si existe el archivo logFile de no ser asi lo creamos
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
        ConsoleHandler ch = new ConsoleHandler();

        //si el error  y logError son true concatenamos la informacion en la variable l que contiene el valor
        // actual de l mas error con la fecha mas el texto enviado (messageText)
        if (error && logError) {
            l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        //si el warning y logWarning son true concatenamos la informacion en la variable l que contiene el valor
        // actual de l mas warning con la fecha mas el texto enviado (messageText)
        if (warning && logWarning) {
            l = l + "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        //si el message y logMessage son true concatenamos la informacion en la variable l que contiene el valor
        // actual de l mas message con la fecha mas el texto enviado (messageText)
        if (message && logMessage) {
            l = l + "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        //agregamos el tipo de registrador y logeamos messageText con el level informacion en eñ archivo logFile.txt
        if(logToFile) {
            logger.addHandler(fh);
            logger.log(Level.INFO, messageText);
        }

        //agregamos el tipo de registrador y logeamos messageText con el level informacion en consola
        if(logToConsole) {
            logger.addHandler(ch);
            logger.log(Level.INFO, messageText);
        }

        //Ejecuta la instruccion SQL para almacenar en BD el mensaje mas el tipo de logeo
        if(logToDatabase) {
            stmt.executeUpdate("insert into Log_Values('" + message + "', " + String.valueOf(t) + ")");
        }
    }
}
