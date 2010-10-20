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
public class ValidateGemPathEntry {

    public ValidateGemPathEntry() throws ClassNotFoundException, IOException, SQLException {

        ArrayList<GemPathEntry> list = App.getRepositorio().getGemPaths();

        for (GemPathEntry gpe: list) {
            GemEntry gemSourceEntry = App.getRepositorio().getGemById(gpe.getSource());
            GemEntry gemTargetEntry = App.getRepositorio().getGemById(gpe.getTarget());
            Gem gemSource = gemSourceEntry.getGem();
            Gem gemTarget = gemTargetEntry.getGem();

            boolean equals = true;
            try {
                gpe.loadPath();
                Path path = gpe.getPath();
                Gem resultingGem = path.getResultWhenAppliedTo(gemSource);
                equals = resultingGem.equals(gemTarget);
            } catch (Exception ex) {
                equals = false;
            }

            if (!equals) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("PROBLEM Gem Path Entry "+gpe.getId()+" verified on gems "+gemSourceEntry.getId()+" -> "+gemTargetEntry.getId());
            }
            else {
                System.out.println("OK Gem Path Entry "+gpe.getId()+" verified on gems "+gemSourceEntry.getId()+" -> "+gemTargetEntry.getId());
            }
        }
    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        new ValidateGemPathEntry();
        System.exit(0);
    }


}
