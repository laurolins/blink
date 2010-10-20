package blink.cli;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import blink.DrawPanel;
import blink.GBlink;
import blink.Gem;
import blink.MapD;
import blink.PanelBlinkViewer;
import blink.PanelGemViewer;
import blink.PanelMapViewer;

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
public class FunctionDraw extends Function {
    public FunctionDraw() {
        super("draw","Draw g-blink or gem");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    public Object hardwork(ArrayList params, DataMap localMap) throws EvaluationException, Exception {
        if (params.get(0) instanceof GBlink) {
            GBlink G = (GBlink) params.get(0);
            JFrame f = new JFrame("G-Blink Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            JTabbedPane tp = new JTabbedPane();
            tp.add("Coin", new DrawPanel(new MapD(G)));
            tp.add("GBlink", new PanelMapViewer(G));
            tp.add("Blink", new PanelBlinkViewer(G));
            f.setContentPane(tp);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        }
        else if (params.get(0) instanceof Gem) {
            Gem G = (Gem) params.get(0);
            JFrame f = new JFrame("Gem Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            f.setContentPane(new PanelGemViewer(G));
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        }
        return null;
    }
}
