package linsoft.gui.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MaskFormatter;

/**
 * Edit integer cell.
 */
public class TextFieldCellEditor implements TableCellEditor {

    /** integer cell state */
    private EventListenerList _listenerList = new EventListenerList();
    private ChangeEvent _event = new ChangeEvent(this);
    protected JFormattedTextField _text = new JFormattedTextField();
    protected Object _value;

    /**
     * Formatador para a edição com uma máscara.
     */
    private MaskFormatter _formatter = new MaskFormatter();

    /**
     * Integer cell editor constructor.
     */
    public TextFieldCellEditor() {
        _text.setBorder(null);
		_text.setHorizontalAlignment((int) JFormattedTextField.RIGHT);
		_text.setFont(new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 11));

        ///////////////////////////////////////////////////////////////
        // When Focus is lost try to submit change
        _text.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                stopCellEditing();
            }
        });
        // When Focus is lost try to submit change
        ///////////////////////////////////////////////////////////////

        ///////////////////////////////////////////////////////////////
        // When Enter is pressed actionPerformed event is dispatched
		_text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
			}
		});
        // When Enter is pressed actionPerformed event is dispatched
        ///////////////////////////////////////////////////////////////

        ///////////////////////////////////////////////////////////
        // Bind Escape Key when in focus to cancel editing
        _text.getActionMap().put("cancelEditing",new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelCellEditing();
            }
        });
        _text.getActionMap().put("submitEdition",new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println(""+e.getSource());
                //boolean wasEditingWithFocus = table.isEditing() && table.getEditorComponent().isFocusOwner()
                //if (wasEditingWithFocus) {
                    stopCellEditing();
                //}
            }
        });
        _text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancelEditing");
        _text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0),"submitEdition");
        // Bind Escape Key when in focus to cancel editing
        ///////////////////////////////////////////////////////////


        _text.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                System.out.println("kb focus mg"+KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
                System.out.println("focus mg"+FocusManager.getCurrentManager().getFocusOwner());
            }
        });


    }

    /**
     * Integer cell editor constructor.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //throw new RuntimeException();
        _value = value;
        TableCellRenderer cr = table.getCellRenderer(row,column);
        if (cr instanceof ConfigurableCellRendererDefault) {
            ConfigurableCellRendererDefault renderer = (ConfigurableCellRendererDefault) cr;
            _text.setBorder(renderer.calculateBorder(table,row,column));
            _text.setBackground(renderer.get_focusedBgColor());
            _text.setForeground(renderer.get_focusedFgColor());
        }
        _text.setText(""+value);
		_text.setCaretPosition(0);
        _text.selectAll();
        return _text;
    }

    /**
     * Editor Value.
     */
    public Object getCellEditorValue() {
        return _text.getText();
    }

    /**
     * It is always true.
     */
    public boolean isCellEditable(EventObject anEvent) {
        /*
		System.out.println("Event "+anEvent);
        if (anEvent instanceof KeyEvent) {
            return ((KeyEvent)anEvent).getKeyCode() == KeyEvent.VK_SPACE;
        }
        return false;
		*/
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent)anEvent).getClickCount() >= 2;
        }
        // return true;
        return true;
    }

    /**
     * It is always true.
     */
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * It is always true.
     */
    public boolean stopCellEditing() {
        System.out.println("Stop Edition");
        fireEditingStopped();
        return true;
    }

    /**
     * It is always true.
     */
    public void cancelCellEditing() {
        //throw new RuntimeException();
        System.out.println("Cancel Edition");
        fireEditingCanceled();
    }

    /**
     * Add listener.
     */
    public void addCellEditorListener(CellEditorListener l) {
        _listenerList.add(CellEditorListener.class, l);
    }

    /**
     * Remove listener.
     */
    public void removeCellEditorListener(CellEditorListener l) {
        _listenerList.remove(CellEditorListener.class, l);
    }

    /**
     * signal listeners of stop editing.
     */
    protected void fireEditingStopped() {
        Object[] listeners = _listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            ((CellEditorListener)listeners[i+1]).editingStopped(_event);
    }

    /**
     * signal listeners of stop editing.
     */
    protected void fireEditingCanceled() {
        Object[] listeners = _listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            ((CellEditorListener)listeners[i+1]).editingCanceled(_event);
    }


    /**
     * Coloca uma máscara de edição no text field. Caso ele não seja
     * da classe JFormattedTextField, constrói um novo objeto e então
     * coloca a máscara nele. Precisa do JDK 1.4.
     */
    public void setMascara(String mascara) {
        /*
		if(!(_text instanceof JFormattedTextField)) {
            _text = new JFormattedTextField();
            _text.setBorder(null);
            _text.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    stopCellEditing();
                }
            });
        }
		*/

		//
        if(_formatter == null) {
            // _formatter = new MaskFormatter();
			// _formatter.install(_text);
			_formatter.uninstall();
			// .
        }

		//
        try {
            if(mascara != null && mascara != "") {
                _formatter.setMask(mascara);
				_formatter.setPlaceholderCharacter('_');
                _formatter.install(_text);
            }
			else {
			   _formatter.uninstall();
			}
        }
        catch(java.text.ParseException e) {
            e.printStackTrace();
        }
    }
}
