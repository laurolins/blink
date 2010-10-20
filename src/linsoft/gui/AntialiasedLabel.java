package linsoft.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class AntialiasedLabel extends JLabel
{
    public AntialiasedLabel(String st) {
        super(st);
    }
    public void paint(Graphics g)
    {
         if (g instanceof Graphics2D)
         {
             Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(
               RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         }

         // Don't forget this step
         super.paint(g);
    }
}
