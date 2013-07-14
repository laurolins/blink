package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
public class PanelDrawBlinks extends JPanel {

    private ArrayList<BlinkDrawing> _drawings;
    private int _rows;
    private int _cols;

    public PanelDrawBlinks(ArrayList<GBlink> list, int rows, int cols, int smooth) {
        _drawings = new ArrayList<BlinkDrawing>();
        for (GBlink b: list) {
            _drawings.add(new BlinkDrawing(b,smooth,-1));
        }
        _rows = rows;
        _cols = cols;
        
        addGUIComponents();
    }

    public PanelDrawBlinks(ArrayList<BlinkDrawing> drawings, int rows, int cols) {
        _drawings = (ArrayList<BlinkDrawing>) drawings.clone();
        _rows = rows;
        _cols = cols;
        
        addGUIComponents();
    }
    
    private void addGUIComponents() {
    	BlinkPanel blinkPanel = new BlinkPanel();
    	JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JButton(new SavePDF(blinkPanel, null)));
        
        this.setLayout(new BorderLayout());
        this.add(bottomPanel,BorderLayout.SOUTH);
        this.add(blinkPanel, BorderLayout.CENTER);
        this.add(new JLabel(String.format("Blink")),BorderLayout.NORTH);
    }

    class BlinkPanel extends JPanel {
    	
    	public void paint(Graphics g) {
            super.paint(g);

            int width = this.getWidth();
            int height = this.getHeight();

            g.setColor(Color.black);
            g.fillRect(0,0,width+1,height+1);

            double cellWidth = (double) width / _cols;
            double cellHeight = (double) height / _rows;

            int r = 0; int c = 0;
            for (BlinkDrawing ld: _drawings) {
                ld.draw((Graphics2D) g,c*cellWidth,r*cellHeight,cellWidth,cellHeight,0.1);
                c++;
                if (c == _cols) {
                    r++;
                    c=0;
                }
            }
        }
    }
}
