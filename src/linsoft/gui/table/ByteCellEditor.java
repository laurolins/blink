package linsoft.gui.table;

/**
 * Edit Byte cell.
 */
public class ByteCellEditor extends TextFieldCellEditor {
   /**
    * Byte cell editor constructor.
    */
   public ByteCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
         result = new Byte(Byte.parseByte(_text.getText()));
      }
      catch (NumberFormatException nfe) {
         result = _value;
      }
      return result;
   }
}
