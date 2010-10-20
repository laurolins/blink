package linsoft.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Library {
	private static DecimalFormat _decimalPointParser = new DecimalFormat();
	private static DecimalFormat _decimalCommaParser = new DecimalFormat();
	static {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		dfs.setGroupingSeparator(',');
		_decimalPointParser.setDecimalFormatSymbols(dfs);
		dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		dfs.setGroupingSeparator('.');
		_decimalCommaParser.setDecimalFormatSymbols(dfs);
	}

	/**
	 * Resize and center windows (JFrame, JDialog, ...).
	 * @param f
	 * @param width
	 * @param height
	 */
	public static void resizeAndCenterWindow(Window w, int width, int height) {
		w.pack();
		w.setSize(
			(int) (w.getInsets().left + w.getInsets().right + width),
			(int) (w.getInsets().top + w.getInsets().bottom + height)
		);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation((int) (d.getWidth() - w.getWidth()) / 2, (int) (d.getHeight() - w.getHeight()) / 2);
	}

	/**
	 * Read double of the form 1.19
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static double parsePointSeparatedDecimal(String s) throws ParseException {
		return _decimalPointParser.parse(s).doubleValue();
	}

	/**
	 * Read double of the form 1.19
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static int parsePointSeparatedInteger(String s) throws ParseException {
		return _decimalPointParser.parse(s).intValue();
	}

	/**
	 * Read double of the form 1,19
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static double parseCommaSeparatedDecimal(String s) throws ParseException {
		return _decimalCommaParser.parse(s).doubleValue();
	}

	/**
	 * Read double of the form 1.19
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static int parseCommaSeparatedInteger(String s) throws ParseException {
		return _decimalCommaParser.parse(s).intValue();
	}

	/**
	 * Format double
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static String formatCommaSeparatedDouble(double d, int casasDecimais) {
		_decimalCommaParser.setMinimumFractionDigits(casasDecimais);
		_decimalCommaParser.setMaximumFractionDigits(casasDecimais);
		return _decimalCommaParser.format(d);
	}

	/**
	 * Robust to double
	 */
	public static double toDouble(String s) {
		double d = 0;
		try {
			d = Library.parseCommaSeparatedDecimal(s);
		}
		catch (Exception e1) {
			try {
				d = Library.parsePointSeparatedDecimal(s);
			}
			catch (Exception e2) {
			}
		}
		System.out.println(s+" -> "+d);
		return d;
	}

	/**
	 * Find first file of the list that exists and returns
	 * it. null otherwise.
	 */
	public static String findFirstFileThatExists(String[] fileList) {
		File f;
		String result = null;
		for (int i=0;i<fileList.length;i++) {
			f = new File(fileList[i]);
			if (f.exists() && f.isFile()) {
				result=fileList[i];
				break;
			}
		}
		return result;
	}

	/**
	 * parse integer list.
	 */
	public static int[] parseIntegerList(String list) {
		StringTokenizer st = new StringTokenizer(list,",");
		int[] result = new int[st.countTokens()];
		for (int i=0;i<result.length;i++)
			result[i] = Integer.parseInt(st.nextToken());
		return result;
	}

	/**
	 * mount string of integer list.
	 */
	public static String mountStringOfIntegerList(int list[]) {
		StringBuffer buffer = new StringBuffer(100);
		for (int i=0;i<list.length;i++) {
			buffer.append(list[i]);
			if (i < list.length-1)
				buffer.append(",");
		}
		return buffer.toString();
	}

	public static JComponent createRectangle(Color c, int w, int h) {
		JLabel l = new JLabel("");
		l.setOpaque(true);
		l.setBackground(c);
		l.setSize(new Dimension(w,h));
		l.setPreferredSize(new Dimension(w,h));
		return l;
	}

    public static JComponent createTransparentRectangle(int w, int h) {
        JLabel l = new JLabel("");
        l.setSize(new Dimension(w,h));
        l.setPreferredSize(new Dimension(w,h));
        return l;
    }

    public static JPanel prepareListPanelOnDialog(String topMessage, String lines) {
        JButton btns[] = {new JButton("OK")};
        btns[0].setMargin(new Insets(2,2,2,2));
        btns[0].setPreferredSize(new Dimension(80,23));
        JPanel bottomPanel = new JPanel();
        FlowLayout f = new FlowLayout();
        f.setVgap(2);
        bottomPanel.add(btns[0]);

        JLabel lbl = new JLabel();
        lbl.setText(topMessage);
        lbl.setFont(new Font("Tahoma",Font.BOLD,11));
        lbl.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

        JTextArea ta = new JTextArea();
        ta.setBackground(btns[0].getBackground());
        ta.setEditable(false);
        ta.setLineWrap(false);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Tahoma",Font.PLAIN,11));
        ta.setText(lines);

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JButton source = (JButton) actionEvent.getSource();
                if ("OK".equals(source.getActionCommand())) {
                }
                JDialog d = (JDialog) source.getRootPane().getParent();
                d.setVisible(false);
            }
        };

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        // p.add(lbl,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
        // p.add(new JScrollPane(ta),new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
        // p.add(bottomPanel,new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
        p.add(lbl,BorderLayout.NORTH);
        p.add(new JScrollPane(ta),BorderLayout.CENTER);
        p.add(bottomPanel,BorderLayout.SOUTH);
        btns[0].addActionListener(al);
        return p;
    }
}