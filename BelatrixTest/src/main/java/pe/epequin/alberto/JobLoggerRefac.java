package pe.epequin.alberto;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

public class JobLoggerRefac {

    private static boolean logToFile;
    private static boolean logToConsole;
    private static boolean logMessage;
    private static boolean logWarning;
    private static boolean logError;
    private static boolean logToDatabase;
    private static Map dbParams;
    private static Logger logger;

    //Consttructor para poder instanciar a la clase JobLogger
    public JobLoggerRefac(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
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

        //agregamos el tipo de registrador y logeamos messageText con el level informacion en eñ archivo logFile.txt
        if(logToFile) {
            //instanciamos el archivo logFile
            File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
            //verificamos si existe el archivo logFile de no ser asi lo creamos
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
            JobLoggerRefac.logear(fh, messageText);
        }

        //agregamos el tipo de registrador y logeamos messageText con el level informacion en consola
        if(logToConsole) {
            ConsoleHandler ch = new ConsoleHandler();
            JobLoggerRefac.logear(ch, messageText);
        }

        //Ejecuta la instruccion SQL para almacenar en BD el mensaje mas el tipo de logeo
        if(logToDatabase) {
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
            //Creamos el Statement para conexion a la BD
            Statement stmt = connection.createStatement();
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
            stmt.executeUpdate("insert into Log_Values('" + message + "', " + t + ")");
        }
    }

    public static void logear(Handler handler, String msn){
        logger.addHandler(handler);
        logger.log(Level.INFO, msn);
    }

}

