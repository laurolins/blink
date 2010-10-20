package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import linsoft.gui.table.ByteCellEditor;
import linsoft.gui.table.CharacterCellEditor;
import linsoft.gui.table.CheckBoxCellEditor;
import linsoft.gui.table.DateCellEditor;
import linsoft.gui.table.DoubleCellEditor;
import linsoft.gui.table.FloatCellEditor;
import linsoft.gui.table.IntegerCellEditor;
import linsoft.gui.table.LongCellEditor;
import linsoft.gui.table.RedirectCellEditor;
import linsoft.gui.table.ShortCellEditor;
import linsoft.gui.table.StringCellEditor;
import linsoft.gui.table.TextFieldCellEditor;

/**
 * Override getCellEditor and getCellRenderer.
 */
public class BeanTable
    extends JTable {

    // default editors
    private static final IntegerCellEditor _defaultIntegerCellEditor = new IntegerCellEditor();
    private static final StringCellEditor _defaultStringCellEditor = new StringCellEditor();
    private static final FloatCellEditor _defaultFloatCellEditor = new FloatCellEditor();
    private static final CheckBoxCellEditor _defaultCheckBoxCellEditor = new CheckBoxCellEditor();
    private static final CharacterCellEditor _defaultCharacterCellEditor = new CharacterCellEditor();
    private static final LongCellEditor _defaultLongCellEditor = new LongCellEditor();
    private static final DoubleCellEditor _defaultDoubleCellEditor = new DoubleCellEditor();
    private static final ByteCellEditor _defaultByteCellEditor = new ByteCellEditor();
    private static final ShortCellEditor _defaultShortCellEditor = new ShortCellEditor();
    private static final RedirectCellEditor _defaultRedirectCellEditor = new RedirectCellEditor();
    private static final DateCellEditor _defaultDateCellEditor = new DateCellEditor();

    private static TableCellRenderer DEFAULT_HEADER_RENDERER = new DefaultTableCellRenderer();

    private TableCellRenderer _headerRenderer = new SimpleHeaderRenderer();

    public BeanTable() {
        super();
        this.setCellEditor(null);
        this.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setAutoCreateColumnsFromModel(true);
        this.registerInvertSelectionKeyListener();
        this.registerSelectMultipleRowsByIdentifiers();
        this.registerSpaceBarStartEditing();
        this.registerFindTextCapabilities();

        // teste
        // this.registerDescricaoBeanTable();

        this.getTableHeader().setReorderingAllowed(false);

        /////////////////////////////////////////
        // Initialize Default Editors
        /*
        this.setDefaultEditor(Integer.class, new linsoft.gui.table.IntegerCellEditor());
        this.setDefaultEditor(String.class, new linsoft.gui.table.StringCellEditor());
        this.setDefaultEditor(Float.class, new linsoft.gui.table.FloatCellEditor());
        this.setDefaultEditor(Boolean.class, new linsoft.gui.table.CheckBoxCellEditor());
        this.setDefaultEditor(Character.class, new linsoft.gui.table.CharacterCellEditor());
        this.setDefaultEditor(Long.class, new linsoft.gui.table.LongCellEditor());
        this.setDefaultEditor(Double.class, new linsoft.gui.table.DoubleCellEditor());
        this.setDefaultEditor(Byte.class, new linsoft.gui.table.ByteCellEditor());
        this.setDefaultEditor(Short.class, new linsoft.gui.table.ShortCellEditor());
        this.setDefaultEditor(Object.class, new linsoft.gui.table.RedirectCellEditor());
        this.setDefaultEditor(Date.class, new linsoft.gui.table.DateCellEditor());
        */
        this.setDefaultEditor(Integer.class, _defaultIntegerCellEditor);
        this.setDefaultEditor(String.class, _defaultStringCellEditor);
        this.setDefaultEditor(Float.class, _defaultFloatCellEditor);
        this.setDefaultEditor(Boolean.class, _defaultCheckBoxCellEditor);
        this.setDefaultEditor(Character.class, _defaultCharacterCellEditor);
        this.setDefaultEditor(Long.class, _defaultLongCellEditor);
        this.setDefaultEditor(Double.class, _defaultDoubleCellEditor);
        this.setDefaultEditor(Byte.class, _defaultByteCellEditor);
        this.setDefaultEditor(Short.class, _defaultShortCellEditor);
        this.setDefaultEditor(Object.class, _defaultRedirectCellEditor);
        this.setDefaultEditor(Date.class, _defaultDateCellEditor);
        // Initialize Default Editors
        /////////////////////////////////////////

        /////////////////////////////////////////
        // Initialize Default Renderers
        // ...
        // Initialize Default Renderers
        /////////////////////////////////////////

        /////////////////////////////////////////
        // Initialize Default Editors
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (getModel()instanceof MultipleBeanTableModel) {
                    int index0 = Math.max(0, e.getFirstIndex() - 1);
                    int index1 = Math.min(e.getLastIndex() + 1, getRowCount() - 1);
                    ( (MultipleBeanTableModel) getModel()).fireTableRowsUpdated(index0, index1);
                }
            }
        });
        // Initialize Default Editors
        /////////////////////////////////////////

    }

    // override these methods
    public TableCellEditor getCellEditor(int row, int col) {
        TableCellEditor editor = null;
        if (this.getModel() != null) {

            // if the model is a MultipleBeanTableModel
            if (this.getModel()instanceof MultipleBeanTableModel) {
                editor = this.getDefaultEditor(this.getModel().getColumnClass(col));
            }

            // if the model is a BeanTableModel
            else if (this.getModel()instanceof BeanTableModel) {
                BeanTableModel model = (BeanTableModel)this.getModel();
                editor = this.getDefaultEditor(model.getClassAt(row, col));
            }

            // if the model is a BeanTableModel2
            else if (this.getModel()instanceof BeanTableModel2) {
                BeanTableModel2 model = (BeanTableModel2)this.getModel();
                editor = this.getDefaultEditor(model.getCellClass(row, col));
            }

            // else
            else {
                Object obj = this.getModel().getValueAt(row, col);

                // it is a non-null object then it obj's class editor
                if (obj != null)
                    editor = this.getDefaultEditor(obj.getClass());

                    // it is a null object then it is Object.class editor
                else
                    editor = this.getDefaultEditor(Object.class);
            }
        }

        else
            editor = super.getCellEditor(row, col);

            // setando a máscara
        if (this.getModel()instanceof BeanTableModel) {
            BeanTableModel model = (BeanTableModel)this.getModel();
            String mascara = model.getMascara(row, col);
            System.out.println(mascara);
            if (editor instanceof TextFieldCellEditor) {
                ( (TextFieldCellEditor) editor).setMascara(mascara);
            }
        }

        return editor;
    }

    // override these methods
    public TableCellRenderer getCellRenderer(int row, int col) {
        if (this.getModel() != null) {

            // if its is a MultipleBeanTableModel...
            if (this.getModel()instanceof MultipleBeanTableModel) {
                MultipleBeanTableModel m = (MultipleBeanTableModel)this.getModel();
                return m.getRenderer(row, col);
            }

            // if the model is a BeanTableModel
            else if (this.getModel()instanceof BeanTableModel2) {
                BeanTableModel2 model = (BeanTableModel2)this.getModel();
                return model.getRenderer(row, col);
            }

            // rendered based on class
            Object obj = this.getModel().getValueAt(row, col);
            if (obj != null)
                return this.getDefaultRenderer(obj.getClass());
            else
                return this.getDefaultRenderer(Object.class);
        }
        else
            return super.getCellRenderer(row, col);
    }

    /**
     * set editor by Class
     */
    public void setEditorByClass(Class c, TableCellEditor editor) {
        this.setDefaultEditor(c, editor);
    }

    /**
     * register desricao bena table...
     */
    public void registerDescricaoBeanTable() {
        new DescricaoBeanTable(this);
    }


    /**
     * Set the cell renderer for all header columns.
     */
    public void setHeaderRenderer(TableCellRenderer headerRenderer) {
        _headerRenderer = headerRenderer;
         TableColumnModel cm = this.getColumnModel();
         for (int i = 0; i < cm.getColumnCount(); i++) {
             cm.getColumn(i).setHeaderRenderer(_headerRenderer);
         }
    }


	private MouseListener _mouseColumnSortListener;
	/**
	 * register column sorter...
	 */
    public void registerColumnSorter() {
		if (_mouseColumnSortListener == null) {
			_mouseColumnSortListener = new java.awt.event.MouseAdapter() {
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() > 1) {
						JTableHeader header = getTableHeader();
						int col = header.columnAtPoint(e.getPoint());
						TableModel model = getModel();
						if (model instanceof MultipleBeanTableModel) {
							MultipleBeanTableModel beanModel = (MultipleBeanTableModel) model;

							// save selected objects
							HashSet set = new HashSet();
							int[] selectedLines = getSelectedRows();
							for (int i = 0; i < selectedLines.length; i++) {
								set.add(beanModel.getObject(selectedLines[i]));
							}

							//
							boolean crescente = true;
							if ( (e.getModifiers() & java.awt.event.MouseEvent.SHIFT_MASK) != 0)
								crescente = false;
							beanModel.ordenar(col, crescente);
							beanModel.fireTableDataChanged();

							// save selected objects
							Iterator it = set.iterator();
							while (it.hasNext()) {
								int indexof = beanModel.indexOf(it.next());
								if (indexof >= 0) {
									getSelectionModel().addSelectionInterval(indexof, indexof);
								}
							}
						}
					}
				}
			};
		}

        // register column sorter routine to table header
        JTableHeader header = getTableHeader();
        header.addMouseListener(_mouseColumnSortListener);
    }

	/**
	 * register column sorter...
	 */
	public void unregisterColumnSorter() {
		if (_mouseColumnSortListener != null) {
			// register column sorter routine to table header
			JTableHeader header = getTableHeader();
			header.removeMouseListener(_mouseColumnSortListener);
		}
	}

    /**
     * Override setModel...
     */
    public void setModel(TableModel model) {
        super.setModel(model);
    }

    /**
     * On Creation of Columns from model...
     * @param model
     */
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        TableModel model = getModel();
        if (model instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel m = (MultipleBeanTableModel) model;
            TableColumnModel cm = this.getColumnModel();
            for (int i = 0; i < cm.getColumnCount(); i++) {
                cm.getColumn(i).setWidth(m.getColumnWidth(i));
                cm.getColumn(i).setPreferredWidth(m.getColumnWidth(i));
                cm.getColumn(i).setHeaderRenderer(_headerRenderer);
            }
        }
        else if (model instanceof BeanTableModel2) {
            this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            BeanTableModel2 m = (BeanTableModel2) model;
            TableColumnModel cm = this.getColumnModel();
            for (int i = 0; i < cm.getColumnCount(); i++) {
                cm.getColumn(i).setPreferredWidth(m.getColumnWidth(i));
            }
        }
    }

    /**
     * changeSelection
     */
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        TableModel model = getModel();

        // proibir que as células na coluna de nomes
        // ganhem o foco repassando a solicitaçao de
        // foco nestas colunas para a coluna de valores
        // se o model for BeanTableModel2
        if (model instanceof BeanTableModel2) {
            if (columnIndex == BeanTableModel2.PROPERTY_NAME_COLUMN) {
                columnIndex = BeanTableModel2.PROPERTY_VALUE_COLUMN;
            }
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }

    /**
     * selectRows
     */
    public void selectRows(int rows[]) {
        Vector intervals = new Vector();
        this.getSelectionModel().clearSelection();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] < this.getRowCount()) {
                this.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
            }
            else {
                break;
            }
        }
    }

    /**
     * selectRows
     */
    public void selectRow(int row) {
        this.getSelectionModel().clearSelection();
        if (row >= 0 && row < this.getRowCount()) {
            this.getSelectionModel().addSelectionInterval(row, row);
        }
    }

    /**
     * selectRows
     */
    public void selectObject(Object obj) {
        this.getSelectionModel().clearSelection();
        TableModel model = getModel();
        if (model instanceof MultipleBeanTableModel || model instanceof BeanTableModel2) {
            MultipleBeanTableModel m = (MultipleBeanTableModel) model;
            int row = m.indexOf(obj);
            if (row >= 0 && row < this.getRowCount()) {
                this.getSelectionModel().addSelectionInterval(row, row);
            }
        }
    }

    /**
     * get selected objects
     */
    public Object getSelectedObject() {
        Object result = null;
        int row = this.getSelectedRow();
        TableModel model = getModel();
        if (row >= 0 && model.getRowCount() > row && model instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel m = (MultipleBeanTableModel) model;
            result = m.getObject(row);
        }
        return result;
    }

    /**
     * Scroll to cell rectangle
     */
    public void scrollToObject(Object obj) {
        TableModel model = getModel();
        if (model instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel m = (MultipleBeanTableModel) model;
            int index = m.indexOf(obj);
            if (index >= 0) {
                this.scrollRectToVisible(this.getCellRect(index, 0, true));
            }
        }
    }

    /**
     * substituir
     */
    public void changeObject(Object oldObj, Object newObj) {
        TableModel model = getModel();
        if (model instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel m = (MultipleBeanTableModel) model;
            int index = m.indexOf(oldObj);
            m.setObject(newObj,index);
        }
    }

    /**
     * Invert selection
     */
    private void registerInvertSelectionKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                    invertSelection();
            }
        });
    }

    /**
     * Invert selection
     */
    private void registerFindTextCapabilities() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                    findText();
            }
        });
    }




    /**
     * Invert selection
     */
    private void registerSelectMultipleRowsByIdentifiers() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_J && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                    selectObjectFromIdentifier();
            }
        });
    }

    /**
     * find and select text
     */
    public void findText() {
        if (this.getModel()instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel model = (MultipleBeanTableModel)this.getModel();

            // text
            String text = JOptionPane.showInputDialog(this.getRootPane(), "Encontrar");
            if (text != null) {
                int index = model.find(text, -1, this.getSelectedColumn(), true);
                if (index >= 0) {
                    this.getSelectionModel().clearSelection();
                    this.getSelectionModel().addSelectionInterval(index, index);
                    scrollRectToVisible(getCellRect(index, getSelectedColumn(), true));
                }
            }
        }
    }

    /**
     * Invert selection
     */
    private void invertSelection() {
        Vector v = new Vector();
        for (int i = 0; i < this.getRowCount(); i++) {
            if (!this.getSelectionModel().isSelectedIndex(i))
                v.add(new Integer(i));
        }
        this.getSelectionModel().clearSelection();
        for (int i = 0; i < v.size(); i++) {
            int j = ( (Integer) v.get(i)).intValue();
            this.getSelectionModel().addSelectionInterval(j, j);
        }
    }

    /**
     * Register spacebar startup edition of cell
     */
    private void registerSpaceBarStartEditing() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_E) {
                    int row = getSelectedRow();
                    int col = getSelectedColumn();
                    System.out.println("col: " + col);
                    System.out.println("row: " + row);
                    if (row >= 0 && col >= 0) {
                        editCellAt(row, col);
                    }
                }
            }
        });
    }

    /**
     * Register the following commands
     *  CTRL+UP to move selected objects up one position
     *  CTRL+DOWN to move selected objects down one position
     *  CTRL+LEFT to move selected objects to the begining
     *  CTRL+RIGHT to move selected objects to the end
     */
    public void registerSortCommands() {
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    if (e.getKeyCode() == KeyEvent.VK_UP)
                        moverRegistros(MOVE_UP);
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                        moverRegistros(MOVE_DOWN);
                    else if (e.getKeyCode() == KeyEvent.VK_LEFT)
                        moverRegistros(MOVE_HOME);
                    else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                        moverRegistros(MOVE_END);
                }
            }
        });
    }

    /**
     * Scroll to cell rectangle
     */
    public void scrollToCell(int row, int column) {
        this.scrollRectToVisible(this.getCellRect(row, column, true));
    }

    // commands of sorting schema
    public static final int MOVE_HOME = 0;
    public static final int MOVE_END = 1;
    public static final int MOVE_UP = 2;
    public static final int MOVE_DOWN = 3;

    /**
     * Rotina para mover os registros selecionados de uma tabela.
     *
     * @param table
     * @param model
     * @param command
     */
    public void moverRegistros(int command) {
        // define table
        BeanTable table = this;

        // define model
        MultipleBeanTableModel model = null;
        if (getModel()instanceof MultipleBeanTableModel)
            model = (MultipleBeanTableModel) getModel();
        else
            return;

        // get selected indexes from table in ascendign order
        int indexes[] = table.getSelectedRows();

        // check if there is some index to be moved
        if (indexes.length > 0) {
            // get total number of elements on the table
            int N = model.getRowCount();

            // get number of selected elements on the table
            int n = indexes.length;

            // get min index from the list
            int minIndex = indexes[0];

            // get max index from the list
            int maxIndex = indexes[n - 1];

            // vector of selected objects
            Vector objects = new Vector();
            for (int i = 0; i < n; i++)
                objects.add(model.getObject(indexes[i]));

                // init delata as zero
            int delta = 0;

            // init scroll index as zero
            int scrollIndex = 0;

            // calculate delata and scrollIndex
            switch (command) {
                case MOVE_HOME:

                    // calculate the delta that will be applied to the objects position
                    delta = -minIndex;
                    scrollIndex = 0;
                    break;
                case MOVE_END:

                    // calculate the delta that will be applied to the objects position
                    delta = N - 1 - maxIndex;
                    scrollIndex = N - 1;
                    break;
                case MOVE_UP:
                    if (minIndex > 0) {
                        delta = -1;
                        scrollIndex = minIndex - 1;
                    }
                    break;
                case MOVE_DOWN:
                    if (maxIndex < N - 1) {
                        delta = 1;
                        scrollIndex = maxIndex + 1;
                    }
                    break;
            }

            // if there is some movement to do then execute it
            if (delta != 0) {
                // remove selected objects
                model.removeObject(objects);

                // insert them at the right place
                for (int i = 0; i < n; i++)
                    model.insertObject(objects.get(i), indexes[i] + delta);

                    // insert them at the right place
                table.clearSelection();
                for (int i = 0; i < n; i++)
                    table.getSelectionModel().addSelectionInterval(indexes[i] + delta, indexes[i] + delta);

                    // scroll
                table.scrollRectToVisible(table.getCellRect(scrollIndex, 0, true));

            }
        }
    }

    /**
     * Add object from a MultipleBeanTableModel to the selected lines
     */
    public void addObjectToSelection(Object object) {
        if (this.getModel()instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel model = (MultipleBeanTableModel)this.getModel();
            int index = model.indexOf(object);
            if (index != -1) {
                this.getSelectionModel().addSelectionInterval(index, index);
            }
        }
    }

    /**
     * Select all objects from....
     */
    public void selectObjectFromIdentifier() {
        if (this.getModel()instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel model = (MultipleBeanTableModel)this.getModel();

            // text
            ViewSelecao view = new ViewSelecao(new JFrame());
            resizeAndCenterWindow(view, 300, 200);
            view.setVisible(true);

            //
            int column = this.getSelectedColumn();

            //
            if (view.getStatus()) {
                Vector v = view.getTokens();
                this.clearSelection();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String text = model.getFormattedCellText(i, column);
                    if (v.contains(text))
                        this.getSelectionModel().addSelectionInterval(i, i);
                }
            }
        }
    }

    /**
     * Resize and center windows (JFrame, JDialog, ...).
     */
    public static void resizeAndCenterWindow(java.awt.Window w, int width, int height) {
        w.pack();
        w.setSize(
            (int) (w.getInsets().left + w.getInsets().right + width),
            (int) (w.getInsets().top + w.getInsets().bottom + height)
            );
        java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        w.setLocation( (int) (d.getWidth() - w.getWidth()) / 2, (int) (d.getHeight() - w.getHeight()) / 2);
    }

}

class SimpleHeaderRenderer
    extends JLabel
    implements TableCellRenderer {
    /***************
     * constructor *
     **************/
    public SimpleHeaderRenderer() {
        super();
        setOpaque(true);
        setBackground(new Color(70,70,130));
        setForeground(Color.white);
        // setBorder(BorderFactory.createLineBorder(Color.black,1));
        //setBackground(new Color(160,160,160));
        //setForeground(new Color(160,0,0));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setHorizontalAlignment(SwingConstants.CENTER);
    } //end constructor

    /*=************
     * set methods *
     **************/

    /*=********************************************
     * getTableCellRendererComponent(): (override) *
     **********************************************/
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        // setFont(table.getFont().deriveFont(Font.BOLD));
        if (value != null)
            setText(value.toString());
        return this;
    } //end getTableCellRendererComponent method
}
