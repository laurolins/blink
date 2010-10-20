package blink;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class App {
    public App() {
    }

    public static final int MAX_EDGES = 16;

    ///////////////////////////////////
    // INICIO: Configuracao
    private static final Configuracao _config = new Configuracao("blink");

    public static Configuracao getConfiguracao() {
        return App._config;
    }

    public static String getProperty(String name) {
        return _config.getProperty(name);
    }

    public static void setProperty(String name, String value) {
        _config.setProperty(name, value);
    }

    public static String getProperty(String name, String defaultValue) {
        String x = _config.getProperty(name);
        if ("".equals(x) || x == null)
            return defaultValue;
        else
            return x;
    }

    public static void setProperty(String nodeName, String name, String value) {
        _config.setProperty(nodeName, name, value);
    }
    // FIM: Configuracao
    ///////////////////////////////////


    //////////////////////////////////////
    // INICIO: Program name and version
    public static final String NAME = "Blink";
    public static final String VERSION = "v0.01";
    public static final String DATE = "17/02/2006";

    public static String getVersion() { return VERSION; }
    public static String getName() { return NAME; }
    public static String getDate() { return DATE; }


    public static final String DB_NAME_PROPERTY = "dbname";
    public static final String DB_NAME_DEFAULT = "blink";

    //public static String DATA_DIR = "data/";
    //public static String DB_NAME = "mixnfix";

    static {
        String x = App.getProperty(DB_NAME_PROPERTY);
        if (x == null || x.length() == 0) {
            App.setProperty(DB_NAME_PROPERTY,DB_NAME_DEFAULT);
        }
    }

    // FIM: Program name and version

    //////////////////////////////////////

    //////////////////////////////////////
    // INICIO: Repositorio
    private static BlinkDB      _repositorio;

    public static BlinkDB getRepositorio() {
        if (_repositorio == null) {
            try {
                _repositorio = new BlinkDB();
            }
            catch (Exception ex) {
                return null;
            }
        }
        return _repositorio;
    }

    // FIM: Repositorio
    //////////////////////////////////////

//    public static JDCConnectionDriver DRIVER = null;
//    public static void restartDbDriver() {
//        try {
//            DriverManager.deregisterDriver(DRIVER);
//            try {
//                DRIVER = new JDCConnectionDriver(
//                        "com.mysql.jdbc.Driver",
//                        "jdbc:mysql://localhost/" + App.getProperty(DB_NAME_PROPERTY),
//                        "root",
//                        ""
//                         );
//            } catch (SQLException ex1) {
//                ex1.printStackTrace();
//            } catch (IllegalAccessException ex1) {
//                ex1.printStackTrace();
//            } catch (InstantiationException ex1) {
//                ex1.printStackTrace();
//            } catch (ClassNotFoundException ex1) {
//                ex1.printStackTrace();
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//
//    }
//
//    static {
//
//        //
//        // linsoft.gui.table.CentralDeFormatadorDeTexto.setFormatadorDeTexto(new FormatText());
//
//
//        ///////////////////////////////////////////////////////
//        // inicializacao do driver
////        try {
////            System.out.println("Registering database connection");
////            DRIVER = new JDCConnectionDriver(
////                "com.mysql.jdbc.Driver",
////                "jdbc:mysql://localhost/"+App.getProperty(DB_NAME_PROPERTY),
////                "root",
////                ""
////                );
////        }
////        catch (SQLException ex) {
////            ex.printStackTrace();
////        }
////        catch (IllegalAccessException ex) {
////            ex.printStackTrace();
////        }
////        catch (InstantiationException ex) {
////            ex.printStackTrace();
////        }
////        catch (ClassNotFoundException ex) {
////            ex.printStackTrace();
////        }
////    }
}
