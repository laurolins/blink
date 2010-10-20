package linsoft.gui.table;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Edit Double cell.
 */
public class DoubleCellEditor extends TextFieldCellEditor {
   /**
    * Double cell editor constructor.
    */
   public DoubleCellEditor() {
      super();
      _text.setBackground(java.awt.Color.yellow);
   }

   /**
    * Editor Value.
    */
   public Object getCellEditorValue() {
      Object result;
      try {
	 NumberFormat nf = NumberFormat.getInstance();
	 Number n = nf.parse(_text.getText());
         result = new Double(n.doubleValue());
      }
      catch (ParseException pe) {
         result = _value;
      }
      return result;
   }
}
