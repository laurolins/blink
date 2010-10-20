package blink;

import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

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
public class ValidateBlinkToGem {



    public ValidateBlinkToGem(ArrayList<BlinkEntry> B) throws ClassNotFoundException, IOException, SQLException {
        for (BlinkEntry be: B) {
            Gem gem = be.getBlink().getGem();
            gem.goToCodeLabel();
            GemEntry gemOnBlinkEntry = App.getRepositorio().getGemById(be.get_gem());

            boolean equals = true;
            try {
                be.loadPath();
                Path path = be.getPath();
                Gem resultingGem = path.getResultWhenAppliedTo(gem);
                equals = resultingGem.equals(gemOnBlinkEntry.getGem());
            } catch (Exception ex) {
                equals = false;
            }

            if (!equals) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("PROBLEM Gem not verified on blink "+be.get_id());
            }
            else {
                System.out.println("OK blink "+be.get_id()+" goes to gem "+be.get_gem());
            }
        }
    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        ArrayList<BlinkEntry> entries = App.getRepositorio().getBlinks(9,9);
        new ValidateBlinkToGem(entries);
        System.exit(0);
    }


}
