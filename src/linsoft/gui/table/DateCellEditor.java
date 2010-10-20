package linsoft.gui.table;

import java.awt.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JTable;

public class DateCellEditor  extends TextFieldCellEditor {
    public DateCellEditor() {
		super();
		setMascara("##/##/####");
    }

	private String fillLeftZeros(String st, int totalLength) {
		int k = totalLength-st.length();
		if (k < 0) {
			throw new RuntimeException("total length icompatible");
		}
		StringBuffer result = new StringBuffer(totalLength);
		for (int i=0;i<k;i++)  {
			result.append('0');
		}
		result.append(st);
		return result.toString();
	}

	/**
	 * Integer cell editor constructor.
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Component c = super.getTableCellEditorComponent(table,value,isSelected,row,column);
		if (value instanceof Date) {
			Date date = (Date) value;
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH)+1;
			int year = cal.get(Calendar.YEAR);
			_text.setText(""+
						  fillLeftZeros(""+day,2)+"/"+
						  fillLeftZeros(""+month,2)+"/"+
						  fillLeftZeros(""+year,4));
		}
		return c;
	}

	/**
	 * Editor Value.
	 */
	public Object getCellEditorValue() {
	   Object result;
	   try {
		  String date = _text.getText();
		  if (date.length() != 10) {
			  throw new RuntimeException("");
		  }
		  int day = Integer.parseInt(date.substring(0,2));
		  int month = Integer.parseInt(date.substring(3,5))-1;
		  int year = Integer.parseInt(date.substring(6,10));
		  GregorianCalendar cal = new GregorianCalendar(year,month,day);
		  result = cal.getTime();
	   }
	   catch (Exception nfe) {
		  result = _value;
	   }
	   return result;
	}
}