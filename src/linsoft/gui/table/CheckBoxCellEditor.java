package linsoft.gui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * Edit integer cell.
 */
public class CheckBoxCellEditor implements TableCellEditor {

    // chack box state
    private EventListenerList _listenerList = new EventListenerList();
    private ChangeEvent _event = new ChangeEvent(this);
    protected JCheckBox _check = new JCheckBox();
    protected Object _value;

    /**
     * Integer cell editor constructor.
     */
    public CheckBoxCellEditor() {
        _check.setBorder(null);
        // _check.setBackground(Color.yellow);
        _check.setHorizontalAlignment((int) JCheckBox.CENTER_ALIGNMENT);
        _check.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                stopCellEditing();
            }
        });
        _check.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
    }

    /**
     * Integer cell editor constructor.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        _value = value;
        if (value instanceof Boolean) {
            _check.setSelected(((Boolean) value).booleanValue());
        }
        return _check;
    }

    /**
     * Editor Value.
     */
    public Object getCellEditorValue() {
        return new Boolean(_check.isSelected());
    }

    /**
     * It is always true.
     */
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent)anEvent).getClickCount() >= 1;
        }
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
        fireEditingStopped();
        return true;
    }

    /**
     * It is always true.
     */
    public void cancelCellEditing() {
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
    protected void fireEditingStopped()
            {  Object[] listeners = _listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2)
        ((CellEditorListener)listeners[i+1]).
        editingStopped(_event);
    }

    /**
     * signal listeners of stop editing.
     */
    protected void fireEditingCanceled()
            {  Object[] listeners = _listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2)
        ((CellEditorListener)listeners[i+1]).
        editingCanceled(_event);
    }
}