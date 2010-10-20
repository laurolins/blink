package linsoft.gui.table;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Edit Float cell.
 */
public class FloatCellEditor extends TextFieldCellEditor {
   /**
    * Float cell editor constructor.
    */
   public FloatCellEditor() {
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
         result = new Float(n.floatValue());
      }
      catch (ParseException pe) {
         result = _value;
      }
      return result;
   }
}
