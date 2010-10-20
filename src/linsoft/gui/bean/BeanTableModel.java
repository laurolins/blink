package linsoft.gui.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for properties...
 */
public class BeanTableModel extends AbstractTableModel {

    /** */
    public static final int PROPERTY_NAME_COLUMN = 0;
    public static final int PROPERTY_VALUE_COLUMN = 1;

    /** the table can be null */
    private JTable _table = null;

    /** the object that is introspected */
    private Object _object = null;

    /** properties vector */
    private Vector _properties = new Vector();

    /** Property Descriptor */
    private PropertyDescriptor[] _propertiesDescriptor;

    /** contructor */
    public BeanTableModel() {
    }

    /** table */
    public void setTable(JTable table) {
        _table = table;
    }

    /** get last selected cell */
    public Class getColumnClass(int columnIndex) {
        if (_table != null) {
            try {
                int row = _table.getSelectedRow();
                int col = _table.getSelectedColumn();
                return this.getValueAt(row, col).getClass();
            }
            catch (Exception e) {
                return Object.class;
            }
        }
        else return Object.class;
    }

	/**
	 * Knows what class is the object shown at cell at (row,col)
	 * @param row
	 * @param col
	 * @return the class of the object shown.
	 */
    public Class getClassAt(int row, int col) {
        Class c = null;
        if(col == PROPERTY_NAME_COLUMN)
            c = String.class;
        else if(col == PROPERTY_VALUE_COLUMN)
            c = this.getProperty(row).getPropertyType();
        return c;
    }

   /**
    * What is the mask of the i-th cell editor.
    * @param row
    * @return
    */
   public String getMascara(int row, int col) {
	  if (col == 1)
	     return this.getProperty(row).getMascara();
	  else
		 return null;
   }

    /** set object */
    public void setObject(Object object) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        // set object
        _object = object;

        // remove all elements
        _properties.removeAllElements();

        // get introspection info
        BeanInfo binfo = Introspector.getBeanInfo(_object.getClass());
        _propertiesDescriptor = binfo.getPropertyDescriptors();

        // add properties to a vector
        for (int i = 0; i < _propertiesDescriptor.length; i++)
            _properties.add(new Property(_propertiesDescriptor[i]));

        // rebuild all
        this.fireTableDataChanged();
    }

    /** public get index of parameter by name */
    private int indexOf(String propertyName) {
        for (int i = 0; i < _propertiesDescriptor.length; i++)
        if (this.getProperty(i).getName().equals(propertyName))
            return i;
        return -1;
    }

    /** clear */
    public void clear() {
        int n = this.getRowCount();
        _properties.removeAllElements();
        this.fireTableRowsDeleted(0, n - 1);
    }

    /** one column for the names and another column for the values */
    public int getColumnCount() {
        return 2;
    }

    /** number of properties */
    public int getRowCount() {
        return _properties.size();
    }

    /** get object */
    public Object getValueAt(int row, int column) {
        if (column == PROPERTY_NAME_COLUMN)
            return this.getProperty(row).getDisplayName();
        else if (column == PROPERTY_VALUE_COLUMN)
            return this.getProperty(row).getValue();
        else
            return null;
    }

    /** is cell editable */
    public boolean isCellEditable(int row, int column) {
        if (column == PROPERTY_VALUE_COLUMN)
            return this.getProperty(row).isEditable();
        else
            return false;
    }

    /** get property */
    private Property getProperty(int index) {
        return (Property) _properties.get(index);
    }

    /** set value */
    public void setValueAt(Object value, int row, int column) {
        Property p = this.getProperty(row);
        try {
        p.updateValue(value);
        } catch (Exception e) { e.printStackTrace(); }

        // not very efficient!!! it should be by means of event signaling...
        this.fireTableDataChanged();
    }

    /** set value */
    public Class getCustomEditorClass(int row, int col) {
        Property p = this.getProperty(row);
        return p.getCustomEditorClass();
    }

    public String getColumnName(int column) {
        String nome = null;
        if(column == 0)
            nome = "Propriedade";
        else if(column == 1)
            nome = "Valor";
        return nome;
    }

    /** internal class */
    class Property {
        private PropertyDescriptor _descriptor;
        private Object _value;
        public Property(PropertyDescriptor descriptor) throws InvocationTargetException, IllegalAccessException {
            //
            _descriptor = descriptor;

            // cache values for faster access...
            _value = _descriptor.getReadMethod().invoke(_object);
        }
        public Class getPropertyType() { return _descriptor.getPropertyType(); }
        public String getName() { return _descriptor.getName(); }
        public String getDisplayName() { return _descriptor.getDisplayName(); }
        public Object getValue() { return _value; }
        public Class getCustomEditorClass() { return _descriptor.getPropertyEditorClass(); }
        public boolean isEditable() { return (_descriptor.getWriteMethod() != null); }
        public void updateValue(Object newValue) throws InvocationTargetException, IllegalAccessException {
            _descriptor.getWriteMethod().invoke(_object,new Object[] {newValue});
            _value = _descriptor.getReadMethod().invoke(_object);
        }
        public String getMascara() {
            return (String) _descriptor.getValue("mascara");
        }
    }
}
