package linsoft.gui.table;

/**
 * Edit Long cell.
 */
public class LongCellEditor extends TextFieldCellEditor {
   /**
    * Long cell editor constructor.
    */
   public LongCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
         result = new Long(Long.parseLong(_text.getText()));
      }
      catch (NumberFormatException nfe) {
         result = _value;
      }
      return result;
   }
}
