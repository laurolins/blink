package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * View to choose objects
 */
public class ViewSelecao extends JDialog {
    // INICIO: Visual Components
	BorderLayout borderLayout1 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    JPanel _panelRoot = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel _panelCancelarOK = new JPanel();
    JButton _botaoOK = new JButton();
    JButton _botaoCancelar = new JButton();
    TitledBorder _borderRegistros;
    // FIM: Visual Components


	// INICIO: estado

	boolean _status = false;

	// FIM: estado

	/**
	 * All objects
	 */
	private Vector _allObjects;

	/**
	 * Class that will be used
	 */
	private Class _class;
    JScrollPane _scrollPanePropriedadesVisiveis = new JScrollPane();
    JTextArea _textArea = new JTextArea();

	/**
	 * Constructor
	 */
    public ViewSelecao(JFrame frame) {
		super(frame,"Seleção de Registros",true);

		//
		_class = Object.class;

		// INICIO: visual components
        try {
			jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
		// FIM: visual components

		// INICIO: Ligar o Table Models Quando a Janela for aberta

		this.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
				_status = false;
            }
        });

		// FIM: Ligar o Table Models Quando a Janela for aberta
    }

	/**
	 * true if it was ok
	 * @return
	 */
	public Vector getTokens() {
		StringTokenizer t = new StringTokenizer(_textArea.getText(),";, \n");
		Vector result = new Vector();
		while (t.hasMoreTokens())
			result.add(t.nextToken());
		return result;
	}

	/**
	 * true if it was ok
	 * @return
	 */
	public boolean getStatus() {
		return _status;
	}

    /**
	 * OK
	 * @param e
	 */
	void _botaoOK_actionPerformed(ActionEvent e) {
		_status = true;
		this.setVisible(false);
    }

    /**
	 * Cancelar
	 * @param e
	 */
    void _botaoCancelar_actionPerformed(ActionEvent e) {
		_status = false;
		this.setVisible(false);
    }

	/**
	 * Init visual components
	 */
	private void jbInit() throws Exception {
        _borderRegistros = new TitledBorder(BorderFactory.createLineBorder(Color.black,1),"Registros");
        _panelRoot.setLayout(borderLayout2);
        _botaoOK.setText("OK");
        _botaoOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoOK_actionPerformed(e);
            }
        });
        _botaoCancelar.setText("Cancelar");
        _botaoCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoCancelar_actionPerformed(e);
            }
        });
        _scrollPanePropriedadesVisiveis.setBorder(_borderRegistros);
        _scrollPanePropriedadesVisiveis.setToolTipText("");
        _scrollPanePropriedadesVisiveis.setViewportView(_textArea);
        this.getContentPane().add(_panelRoot, BorderLayout.CENTER);
        _panelRoot.add(_panelCancelarOK, BorderLayout.SOUTH);
        _panelCancelarOK.add(_botaoOK, null);
        _panelCancelarOK.add(_botaoCancelar, null);
        _panelRoot.add(_scrollPanePropriedadesVisiveis,  BorderLayout.CENTER);
        // splitPane
    }
}