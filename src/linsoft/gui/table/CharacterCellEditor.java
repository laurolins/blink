package linsoft.gui.table;

/**
 * Edit Character cell.
 */
public class CharacterCellEditor extends TextFieldCellEditor {
   /**
    * Character cell editor constructor.
    */
   public CharacterCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
         result = new Character(_text.getText().charAt(0));
      }
      catch (NumberFormatException nfe) {
         result = _value;
      }
      return result;
   }
}
