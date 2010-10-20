package linsoft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class PanelChooseFile extends JPanel {
    public static final boolean OPEN = true;
    public static final boolean SAVE = false;
    private boolean _ok;
    private Input _inputFileName;
    private IHaveProperties _ihp;
    private String _propertyName;
    private String _defaultValue;
    private boolean _openOrSave;
    private String _extensions[];
    public PanelChooseFile(
        String propertyName,
        String defaultValue,
        IHaveProperties ihp,
        boolean openOrSave,
        String[] extensions
        ) {
        _openOrSave = openOrSave;
        _extensions = extensions;
        _propertyName = propertyName;
        _defaultValue = defaultValue;
        _ihp = ihp;

        SpringLayout layout = new SpringLayout();

        this.setLayout(layout);

        _inputFileName = new Input(_ihp,_propertyName,_defaultValue,Input.TF_FILE,200);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        JButton btnVideo = new JButton("...");
        btnVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchVideo();
            }
        });

        this.add(new JLabel("Arquivo"));
        this.add(_inputFileName);
        this.add(btnVideo);

        this.add(btnOk);
        this.add(new JLabel(""));
        this.add(btnCancel);

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                                2, 3, //rows, cols
                                6, 6,        //initX, initY
                                6, 6);       //xPad, yPad
    }

    public void ok() {
        _ok = true;
        this.getTopLevelAncestor().setVisible(false);
    }

    public void cancel() {
        _ok = false;
        this.getTopLevelAncestor().setVisible(false);
    }

    public void run(JFrame parent) {
        JDialog d = new JDialog(parent,"Escolha arquivo",true);
        linsoft.gui.util.Library.resizeAndCenterWindow(d,580,70);
        d.setContentPane(this);
        d.setVisible(true);
    }

    public boolean isOk() {
        return _ok;
    }

    public String getInputFileName() { return _inputFileName.getText(); }

    public void searchVideo() {
        JFileChooser jfc = new JFileChooser();
        jfc.setSelectedFile(new File(this._inputFileName.getText()));
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                StringTokenizer st = new StringTokenizer(f.getName(), ".");
                String last = null;
                while (st.hasMoreTokens()) {
                    last = st.nextToken();
                }
                if (last != null) {
                    last = last.toLowerCase();
                    for (String x: _extensions)
                        if (x.equals(last))
                            return true;
                }
                return false;
            }

            public String getDescription() {
                boolean first = true;
                String result = "(";
                for (String x : _extensions) {
                    if (!first)
                        result += ", ";
                    result += "." + x;
                }
                result += ")";
                return result;
            }
        });
        int result;
        if (_openOrSave == OPEN)
            result = jfc.showOpenDialog(this);
        else
            result = jfc.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            this._inputFileName.setTextAndSave(jfc.getSelectedFile().getAbsolutePath());
        }
    }
}
