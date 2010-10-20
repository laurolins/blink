package linsoft.gui.table;

/**
 * Edit integer cell.
 */
public class IntegerCellEditor extends TextFieldCellEditor {
   /**
    * Integer cell editor constructor.
    */
   public IntegerCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
         result = new Integer(Integer.parseInt(_text.getText()));
      }
      catch (NumberFormatException nfe) {
         result = _value;
      }
      return result;
   }
}
