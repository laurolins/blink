package linsoft.gui.table;

/**
 * Edit String cell.
 */
public class StringCellEditor extends TextFieldCellEditor {
    /**
     * String cell editor constructor.
     */
    public StringCellEditor() {
        super();
        _text.setBackground(java.awt.Color.yellow);
        _text.setHorizontalAlignment((int) javax.swing.JTextField.LEFT);
    }
}
