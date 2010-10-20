package blink;

import java.sql.SQLException;

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
public class UpdateBlinkCatalogNumberOnDB {
    public UpdateBlinkCatalogNumberOnDB() {
    }
    public static void main(String[] args) throws SQLException {
        BlinkCatalog B = new BlinkCatalog();
        App.getRepositorio().updateBlinksCatalogNumber(B.getBlinks());
        System.exit(0);
    }
}
