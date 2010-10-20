package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * A dialog that is permits the selection of the bean properties that will
 * be visible.
 */
public class DialogChooseVisibleBeanProperties extends JDialog {
    // INICIO: Visual Components
	BorderLayout borderLayout1 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    JPanel _panelRoot = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel _panelCancelarOK = new JPanel();
    JButton _botaoOK = new JButton();
    JButton _botaoCancelar = new JButton();
    JSplitPane _splitPane = new JSplitPane();
    JPanel _panelToolBarPropriedadesNaoVisiveis = new JPanel();
    JScrollPane _scrollPanePropriedadesVisiveis = new JScrollPane();
    Border border1;
    TitledBorder titledBorder1;
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel _panelToolBar = new JPanel();
    JScrollPane _scrollPanePropriedadesNaoVisiveis = new JScrollPane();
    Border border2;
    TitledBorder titledBorder2;
    JButton _botaoTornarVisivel = new JButton();
    JButton _botaoTornarNaoVisivel = new JButton();
    Border border3;
    // FIM: Visual Components


	// INICIO: tabelas de propriedades visiveis e nao visiveis

	BeanTable _tablePropriedadesVisiveis = new BeanTable();
	BeanTable _tablePropriedadesNaoVisiveis = new BeanTable();
	MultipleBeanTableModel _modelPropriedadesVisiveis = new MultipleBeanTableModel(BeanProperty.class);
	MultipleBeanTableModel _modelPropriedadesNaoVisiveis = new MultipleBeanTableModel(BeanProperty.class);

	// FIM: tabelas de propriedades visiveis e nao visiveis

	// INICIO: estado

	boolean _state = false;

	// FIM: estado

	/**
	 * All the properties
	 */
	private BeanProperty _properties[];

	/**
	 * Class that will be used
	 */
	private Class _class;

	/**
	 * Null Constructor
	 */
    public DialogChooseVisibleBeanProperties() throws IntrospectionException {
		this(null,null);
	}

	/**
	 * Constructor
	 */
    public DialogChooseVisibleBeanProperties(JFrame frame, Class c) throws IntrospectionException {
		super(frame,"Colunas",true);

		//
		_class = c;

        // get introspection info
        BeanInfo binfo = Introspector.getBeanInfo(_class);
        PropertyDescriptor p[] = binfo.getPropertyDescriptors();

        //
        _properties = new BeanProperty[p.length];

		// INICIO: visual components
        try {
			jbInit();
			_tablePropriedadesVisiveis.registerColumnSorter();
			_tablePropriedadesVisiveis.setDragEnabled(true);
			_tablePropriedadesNaoVisiveis.registerColumnSorter();
			_modelPropriedadesVisiveis.setVisiblePropertySequence(new String[] {"title","descricao"});
			_modelPropriedadesNaoVisiveis.setVisiblePropertySequence(new String[] {"title","descricao"});
			_scrollPanePropriedadesNaoVisiveis.setViewportView(_tablePropriedadesNaoVisiveis);
			_scrollPanePropriedadesVisiveis.setViewportView(_tablePropriedadesVisiveis);
			_tablePropriedadesVisiveis.setToolTipText("Ctrl+Cima ou Ctrl+Baixo para mudar a ordem");
			_tablePropriedadesVisiveis.registerSortCommands();
			/*
			_tablePropriedadesVisiveis.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e)	{
					if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
						if (e.getKeyCode() == KeyEvent.VK_DOWN)
							moveDown();
						else if (e.getKeyCode() == KeyEvent.VK_UP)
							moveUp();
					}
				}
			});
			*/
        }
        catch(Exception e) {
            e.printStackTrace();
        }
		// FIM: visual components

        // add properties to a array
		Vector v = new Vector();
        for (int i=0;i<p.length;i++) {
            _properties[i] = new BeanProperty(p[i]);
			v.add(_properties[i]);
        }
		_modelPropriedadesVisiveis.addObject(v);

		// INICIO: Ligar o Table Models Quando a Janela for aberta

		this.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
				connectModel();
            }
        });

		// FIM: Ligar o Table Models Quando a Janela for aberta

    }

	/**
	 * set property as visible
	 */
	private void setPropertyAsVisible(BeanProperty p) {
		int index = _modelPropriedadesNaoVisiveis.indexOf(p);
		if (index >= 0) {
			_modelPropriedadesNaoVisiveis.removeRow(index);
			_modelPropriedadesVisiveis.addObject(p);
		}
	}

	/**
	 * set property as not visible
	 */
	private void setPropertyAsNotVisible(BeanProperty p) {
		int index = _modelPropriedadesVisiveis.indexOf(p);
		if (index >= 0) {
			_modelPropriedadesVisiveis.removeRow(index);
			_modelPropriedadesNaoVisiveis.addObject(p);
		}
	}

	/**
	 * move up
	 */
	private void moveUp() {
		int row = _tablePropriedadesVisiveis.getSelectedRow();
		if (row > 0) {
			_modelPropriedadesVisiveis.swap(row,row-1);
			_tablePropriedadesVisiveis.getSelectionModel().clearSelection();
			_tablePropriedadesVisiveis.getSelectionModel().setSelectionInterval(row-1,row-1);
			_tablePropriedadesVisiveis.scrollRectToVisible(_tablePropriedadesVisiveis.getCellRect(row-1,0,true));
		}
	}

	/**
	 * move down
	 */
	private void moveDown() {
		int row = _tablePropriedadesVisiveis.getSelectedRow();
		if (row >= 0 && row < _modelPropriedadesVisiveis.getRowCount()-1) {
			_modelPropriedadesVisiveis.swap(row,row+1);
			_tablePropriedadesVisiveis.getSelectionModel().clearSelection();
			_tablePropriedadesVisiveis.getSelectionModel().setSelectionInterval(row+1,row+1);
			_tablePropriedadesVisiveis.scrollRectToVisible(_tablePropriedadesVisiveis.getCellRect(row+1,0,true));
		}
	}

	/**
	 * set visible properties sequence
	 */
	public void setVisibleProperties(int[] indexes) {
		// disconnect model
		this.disconnectModel();

		// do the work
		_modelPropriedadesNaoVisiveis.clear();
		_modelPropriedadesVisiveis.clear();
		for (int i=0;i<_properties.length;i++)
			_modelPropriedadesNaoVisiveis.addObject(_properties[i]);
		for (int i=0;i<indexes.length;i++)
			this.setPropertyAsVisible(_properties[indexes[i]]);

		// connect model
		this.connectModel();
	}

	/**
	 * disconnectModel
	 */
	private void disconnectModel() {
		_tablePropriedadesVisiveis.setModel(new DefaultTableModel());
		_tablePropriedadesNaoVisiveis.setModel(new DefaultTableModel());
	}

	/**
	 * disconnectModel
	 */
	private void connectModel() {
		_tablePropriedadesVisiveis.setModel(_modelPropriedadesVisiveis);
		_tablePropriedadesNaoVisiveis.setModel(_modelPropriedadesNaoVisiveis);
	}

	/**
	 * Init visual components
	 */
	private void jbInit() throws Exception {
        border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Seqüência de Colunas Visíveis");
        border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder2 = new TitledBorder(border2,"Colunas Nao Visíveis");
        border3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Seqüência de Colunas Visíveis");
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
        _scrollPanePropriedadesVisiveis.setBorder(border3);
        _scrollPanePropriedadesVisiveis.setToolTipText("");
        _panelToolBarPropriedadesNaoVisiveis.setLayout(borderLayout3);
        _scrollPanePropriedadesNaoVisiveis.setBorder(titledBorder2);
        _botaoTornarVisivel.setText("Subir");
        _botaoTornarVisivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoTornarVisivel_actionPerformed(e);
            }
        });
        _botaoTornarNaoVisivel.setText("Descer");
        _botaoTornarNaoVisivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoTornarNaoVisivel_actionPerformed(e);
            }
        });
        this.getContentPane().add(_panelRoot, BorderLayout.CENTER);
        _panelRoot.add(_panelCancelarOK, BorderLayout.SOUTH);
        _panelCancelarOK.add(_botaoOK, null);
        _panelCancelarOK.add(_botaoCancelar, null);
        _panelRoot.add(_splitPane, BorderLayout.CENTER);
        _panelToolBarPropriedadesNaoVisiveis.add(_panelToolBar, BorderLayout.NORTH);
        _panelToolBar.add(_botaoTornarVisivel, null);
        _panelToolBar.add(_botaoTornarNaoVisivel, null);
        _panelToolBarPropriedadesNaoVisiveis.add(_scrollPanePropriedadesNaoVisiveis, BorderLayout.CENTER);
        // splitPane
		_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        _splitPane.add(_scrollPanePropriedadesVisiveis, JSplitPane.TOP);
        _splitPane.add(_panelToolBarPropriedadesNaoVisiveis, JSplitPane.BOTTOM);
        _splitPane.setDividerLocation(120);
    }

    /**
	 * Set selected Properties as not visible...
	 * @param e
	 */
	void _botaoTornarNaoVisivel_actionPerformed(ActionEvent e) {
		// get...
		int[] rows = _tablePropriedadesVisiveis.getSelectedRows();
		Vector v = new Vector();
		for (int i=0;i<rows.length;i++)
			v.add(_modelPropriedadesVisiveis.getObject(rows[i]));

		// start expensive update
		disconnectModel();

		// do the hard work
		for (int i=0;i<v.size();i++)
			this.setPropertyAsNotVisible((BeanProperty) v.get(i));

		// finish expensive update
		connectModel();

		// select
		if (v.size() > 0) {
			int i0 = _modelPropriedadesNaoVisiveis.getRowCount()-v.size();
			int i1 = _modelPropriedadesNaoVisiveis.getRowCount()-1;
			_tablePropriedadesNaoVisiveis.getSelectionModel().setSelectionInterval(i0,i1);
			_tablePropriedadesNaoVisiveis.scrollRectToVisible(_tablePropriedadesNaoVisiveis.getCellRect(i1,0,true));
		}

		// request focus to the non visible table
		_tablePropriedadesNaoVisiveis.requestFocus();
    }

    /**
	 * Set selected Properties as visible...
	 * @param e
	 */
    void _botaoTornarVisivel_actionPerformed(ActionEvent e) {
		// get selected rows
		int[] rows = _tablePropriedadesNaoVisiveis.getSelectedRows();
		Vector v = new Vector();
		for (int i=0;i<rows.length;i++)
			v.add(_modelPropriedadesNaoVisiveis.getObject(rows[i]));

		// start expensive update
		disconnectModel();

		// do the hard work
		for (int i=0;i<v.size();i++)
			this.setPropertyAsVisible((BeanProperty) v.get(i));

		// finish expensive update
		connectModel();

		// select
		if (v.size() > 0) {
			int i0 = _modelPropriedadesVisiveis.getRowCount()-v.size();
			int i1 = _modelPropriedadesVisiveis.getRowCount()-1;
			_tablePropriedadesVisiveis.getSelectionModel().setSelectionInterval(i0,i1);
			_tablePropriedadesVisiveis.scrollRectToVisible(_tablePropriedadesVisiveis.getCellRect(i1,0,true));
		}

		// request focus to the visible table
		_tablePropriedadesVisiveis.requestFocus();
    }


	/**
	 * Id sequence of properties...
	 */
	private int indexOf(BeanProperty p) {
		for (int i=0;i<_properties.length;i++)
			if (_properties[i] == p)
				return i;
		return -1;
	}

	/**
	 * Id sequence of properties...
	 */
	public int[] getSequenceOfVisibleProperties() {
		int result[] = new int[_modelPropriedadesVisiveis.getRowCount()];
		for (int i=0;i<result.length;i++)
			result[i] = indexOf((BeanProperty)_modelPropriedadesVisiveis.getObject(i));
		return result;
	}

	/**
	 * true if it was ok
	 * @return
	 */
	public boolean getState() {
		return _state;
	}

    /**
	 * OK
	 * @param e
	 */
	void _botaoOK_actionPerformed(ActionEvent e) {
		_state = true;
		this.setVisible(false);
    }

    /**
	 * Cancelar
	 * @param e
	 */
    void _botaoCancelar_actionPerformed(ActionEvent e) {
		_state = false;
		this.setVisible(false);
    }
}