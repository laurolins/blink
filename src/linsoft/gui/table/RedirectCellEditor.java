package linsoft.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;


public class RedirectCellEditor implements TableCellEditor, CustomEditorListener {

    // basic state
    private EventListenerList _listenerList = new EventListenerList();
    private ChangeEvent _event = new ChangeEvent(this);
    protected Object _value;

    // gui state
    protected JPanel _panel = new JPanel();
    protected JButton _btnRedirect = new JButton();
    protected JTextField _text = new JTextField();

    // client editor
    protected CustomEditor _editor = null;

    /**
     * Constructor
     */
    public RedirectCellEditor() {
        try {
            init();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set custom editor.
     */
    public void setCustomEditor(CustomEditor editor) {
        _editor = editor;
        _editor.addCustomEditorListener(this);
    }

    /**
     * Open Editor.
     */
    public void openEditor() {
        if (_editor != null) {
            _editor.start(_value);
        }
        else System.out.println("Open Editor");
    }

    /**
     * init components of Redirect Cell Editor GUI.
     * @throws Exception
     */
    private void init() throws Exception {
        BorderLayout borderLayout = new BorderLayout();
        Border border = BorderFactory.createLineBorder(SystemColor.controlText, 1);
        _btnRedirect.setBorder(border);
        _btnRedirect.setText("...");
        _btnRedirect.setFocusPainted(false);
        _btnRedirect.setPreferredSize(new Dimension(18, 17));
        _btnRedirect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEditor();
            }
        });
        _text.setBorder(null);
        _text.setEditable(false);
        _text.setBackground(Color.white);
        _panel.setLayout(borderLayout);
        _panel.add(_btnRedirect, BorderLayout.EAST);
        _panel.add(_text, BorderLayout.CENTER);
    }

    /**
     * Integer cell editor constructor.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        _value = value;
        _text.setText("" + (value != null ? value : ""));
        return _panel;
    }

    /**
     * Editor Value.
     */
    public Object getCellEditorValue() {
        return _value;
    }

    /**
     */
    public boolean isCellEditable(EventObject anEvent) {
        if(anEvent instanceof MouseEvent) {
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
     */
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    /**
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
    protected void fireEditingStopped() {
        // essa lista deve ser feita de pares, o primeiro objeto
        // sendo um Class e o segundo um Listener
        Object[] listeners = _listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            ((CellEditorListener)listeners[i + 1]).editingStopped(_event);
    }

    /**
     * signal listeners of stop editing.
     */
    protected void fireEditingCanceled() {
        Object[] listeners = _listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            ((CellEditorListener)listeners[i + 1]).editingCanceled(_event);
    }


    /**
     * Signal that edition was completed.
     * @param source is the editor.
     * @param initObject was the object passed on the initialization of the editor.
     * @param editedObject the resulting object from edition.
     */
    public void editionFinished(CustomEditor source, Object initObject, Object editedObject) {
        _value = editedObject;
        stopCellEditing();
    }
}
