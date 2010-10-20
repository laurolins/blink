package blink;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.visualization.BirdsEyeVisualizationViewer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;

/**
 * Demonstrates the use of: <p>
 * <ul>BirdsEyeGraphDraw
 * <ul>BirdsEyeVisualizationViewer
 * <li>Lens
 * </ul>
 * This demo also shows ToolTips on graph vertices.
 *
 * The birds eye affects the view transform only.
 * You can still affect the layout transform using the
 * mouse.
 *
 * @deprecated See the SatelliteViewDemo for a similar demo with more features
 * @author Tom Nelson - RABA Technologies
 *
 */
public class Lens extends JDialog {

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer _vv;

    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     */
    public Lens(VisualizationViewer vv) {
        super();
        _vv = vv;
        this.setAlwaysOnTop(true);
        this.setPreferredSize(new Dimension(250,400));
        this.setSize(new Dimension(250,400));

        // add my listener for ToolTips
        vv.setToolTipFunction(new DefaultToolTipFunction());
      //  vv.setGraphMouse(new KSGraphMouse(vv));
        vv.setPickSupport(new ShapePickSupport());

        // create a frome to hold the graph
        Container  content = this.getContentPane();

        // create the BirdsEyeView for zoom/pan
        final BirdsEyeVisualizationViewer bird =
            new BirdsEyeVisualizationViewer(vv, 0.25f, 0.25f);

        JButton reset = new JButton("No Zoom");
        // 'reset' unzooms the graph via the Lens
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bird.resetLens();
            }
        });
        final ScalingControl scaler = new ViewScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_vv, 1.1f, _vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_vv, 0.9f, _vv.getCenter());
            }
        });
        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String zoomHelp = "<html><center>Drag the rectangle to pan<p>"+
                "Drag one side of the rectangle to zoom</center></html>";
                // JOptionPane.showMessageDialog(, zoomHelp);
            }
        });
        JPanel controls = new JPanel(new GridLayout(2,2));
        controls.add(plus);
        controls.add(minus);
        controls.add(reset);
        controls.add(help);
        content.add(bird);
        content.add(controls, BorderLayout.SOUTH);
    }
}

