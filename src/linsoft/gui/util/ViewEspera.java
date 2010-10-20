package linsoft.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class ViewEspera
    extends JDialog {

    private boolean _isWorking;
    private IWorker _workImpl;
    private SwingWorker _worker;

    private JPanel _panel = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JProgressBar _progressBar = new JProgressBar();
    private Border border1;
    private JLabel _lblAtividade = new JLabel();
    Border border2;

    public ViewEspera(JFrame parent) {
        super(parent, "Espera", true);
        try {
            jbInit();
            userInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ViewEspera() {
        try {
            jbInit();
            userInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        border2 = BorderFactory.createLineBorder(Color.black, 1);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        _isWorking = false;
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 2), BorderFactory.createEmptyBorder(4, 4, 4, 4));
        _panel.setBorder(border1);
        _progressBar.setBorder(border2);
        _progressBar.setIndeterminate(true);
        this.getContentPane().setLayout(borderLayout1);
        _panel.setLayout(borderLayout2);
        _lblAtividade.setFont(new java.awt.Font("Dialog", 1, 13));
        _lblAtividade.setHorizontalAlignment(SwingConstants.CENTER);
        _lblAtividade.setHorizontalTextPosition(SwingConstants.CENTER);
        _lblAtividade.setText("Atividade");
        this.getContentPane().add(_panel, BorderLayout.CENTER);
        _panel.add(_progressBar, BorderLayout.SOUTH);
        _panel.add(_lblAtividade, BorderLayout.CENTER);
    }

    private void userInit() {
        // when the view opens start job
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                if (!isWorking()) {
                    _isWorking = true;
                    _worker.start();
                }
            }
        });
    }

    public boolean isWorking() {
        return _isWorking;
    }

    public Object doWork(IWorker workImpl, String message, int width, int height) {
        return doWork(workImpl, message, width, height, null);
    }

    public Object doWork(IWorker workImpl, String message, int width, int height, Icon icon) {
        if (!isWorking()) {
            _workImpl = workImpl;
            _worker = new SwingWorker() {
                public Object construct() {
                    return _workImpl.doWork();
                }

                public void finished() {
                    _workImpl = null; // release reference
                    setVisible(false);
                    _isWorking = false;
                }
            };
            _lblAtividade.setText(message);
            _lblAtividade.setIcon(icon);

            //
            Library.resizeAndCenterWindow(this, width, height);
            this.setVisible(true);

            //
            Object value = _worker.get();
            _worker = null;

            //
            return value;
        }
        else {
            return null;
        }
    }
}
