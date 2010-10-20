package linsoft.gui.table;

/**
 * Edit Short cell.
 */
public class ShortCellEditor extends TextFieldCellEditor {
   /**
    * Short cell editor constructor.
    */
   public ShortCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
         result = new Short(Short.parseShort(_text.getText()));
      }
      catch (NumberFormatException nfe) {
         result = _value;
      }
      return result;
   }
}
