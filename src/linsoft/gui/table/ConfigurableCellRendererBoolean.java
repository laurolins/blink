package linsoft.gui.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;

public class ConfigurableCellRendererBoolean extends ConfigurableCellRendererDefault {
	/**
	 * Check Box
	 */
	private JCheckBox _checkBox;

	/**
	 * Configurable Cell Renderer for Boolean values
	 */
	public ConfigurableCellRendererBoolean() {
		super();

		//
		_checkBox = new JCheckBox();
        _checkBox.setBorderPainted(true);

		// init default properties for this type of renderer
		this.set_alignment(ALIGN_CENTER);
    }


	// INICIO: implementando interface TableCellRenderer

	public Component getTableCellRendererComponent
		(   JTable table,
			Object value,
			boolean isSelected,
			boolean  hasFocus,
			int row,
			int column  )
	{
        //////////////////////////////////////////////////
        // Configure border and color using default way
        this.configureBorderAndColors(_checkBox,table,value,isSelected,hasFocus,row,column);
        // Configure border and color using default way
        //////////////////////////////////////////////////

        /*
		//
		if (isSelected) {
			_checkBox.setBackground(this.get_selectedBgColor());
			_checkBox.setForeground(this.get_selectedFgColor());
		}
		else {
			_checkBox.setBackground(this.get_bgColor());
			_checkBox.setForeground(this.get_fgColor());
		}

		// focus
		if (hasFocus)
			_checkBox.setBorder(this.get_border());
		else
			_checkBox.setBorder(null);
        */

		// alignment
		switch (this.get_alignment()) {
			case ALIGN_LEFT: _checkBox.setHorizontalAlignment(JTextField.LEFT); break;
			case ALIGN_CENTER: _checkBox.setHorizontalAlignment(JTextField.CENTER); break;
			case ALIGN_RIGHT: _checkBox.setHorizontalAlignment(JTextField.RIGHT); break;
		}

		// set value
		if (value instanceof Boolean) {
			_checkBox.setSelected(((Boolean) value).booleanValue());
		}

        // _checkBox.setBorder(new javax.swing.border.LineBorder(Color.black,2));

		//
		return _checkBox;
	}

	// FIM: implementando interface TableCellRenderer
}