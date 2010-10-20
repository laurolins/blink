package linsoft.gui.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import linsoft.gui.table.FormatadorDeTexto;

/**
 * Table model for properties...
 */
public class BeanTableModel2 extends AbstractTableModel {

    // some constants
	public static final int PROPERTY_NAME_COLUMN = 0;
    public static final int PROPERTY_VALUE_COLUMN = 1;

    /** class that will have objects in this table model */
    private Class _class = null;

    /** properties vector */
    private Vector _objects;

    /** set of values */
    private HashSet _auxiliarSet = new HashSet();

    /** properties vector */
    private BeanProperty[] _properties;

    /** visible property array */
    private int _numberOfVisibleProperties;
    private int _visibleProperties[];

	/** editable */
    private boolean _editable;

	/** labels column width */
	private int _columnWidthLabels;

	/** values column width */
	private int _columnWidthValues;

    /**
     * Use this formatter for rendering.
     */
    private FormatadorDeTexto _formatter;

    /** contructor */
    public BeanTableModel2(Class c) throws IntrospectionException {
        this(c,null);
    }

    /** contructor */
    public BeanTableModel2(Class c, FormatadorDeTexto formatter) throws IntrospectionException {

        // set object
        _class = c;

        // formatter
        _formatter = formatter;

        //
        _objects = new Vector();

		//
		_editable = true;

		//
		_columnWidthLabels = 57;

		//
		_columnWidthValues = 70;

        //
        if (c != null) {
            // get introspection info
            BeanInfo binfo = Introspector.getBeanInfo(_class);
            PropertyDescriptor p[] = binfo.getPropertyDescriptors();

            //
            _properties = new BeanProperty[p.length];
            _visibleProperties = new int[p.length];
            _numberOfVisibleProperties = p.length;

            // add properties to a array
            for (int i=0;i<p.length;i++) {
                _properties[i] = new BeanProperty(p[i],_formatter);
                _visibleProperties[i] = i;
            }

            // rebuild all
            // this.fireTableStructureChanged();
        }
    }

	public void setColumnWidths(int columnWidthLabels, int columnWidthValues) {
		_columnWidthLabels = columnWidthLabels;
		_columnWidthValues = columnWidthValues;
	}

	/////////////////////////////////////////////////////
	// INICIO: sobrepondo métodos de AbstractTableModel

    /**
	 * getValueAt
	 */
    public Object getValueAt(int row, int column) {
		Object result = null;

		// get property
		BeanProperty property = getVisibleProperty(row);

		// title column
		if (column == PROPERTY_NAME_COLUMN) {
			result = property.getTitle();
		}

		// value column
		if (column == PROPERTY_VALUE_COLUMN) {
			_auxiliarSet.clear();
			for (int i=0;i<_objects.size();i++) {
				try {
				    _auxiliarSet.add(property.getValue(getObject(i)));
				} catch (Exception e) { e.printStackTrace(); }
			}
			if (_auxiliarSet.size() == 1) {
				result = _auxiliarSet.iterator().next();
			}
			else result = "?";
		}

		return result;
    }

    /** set value */
    public void setValueAt(Object value, int row, int column) {

		// check
		if (column != PROPERTY_VALUE_COLUMN) {
			return;
		}

        // it is a valid cell
		BeanProperty property = this.getVisibleProperty(row);
		for (int i=0;i<_objects.size();i++) {
			try {
				property.updateValue(getObject(i),value);
			} catch (Exception e) { e.printStackTrace(); }
		}

        // not very efficient!!! it should be by means of event signaling...
        updateAllCells();

    }

    /**
	 * one column for the names and another column for the values
	 */
    public int getColumnCount() {
		return 2;
	}

    /**
	 * number of properties
	 */
    public int getRowCount() {
		return _numberOfVisibleProperties;
	}

    /**
	 * class of column
	 */
    public Class getColumnClass(int columnIndex) {
        return Object.class;
    }

	/*
	public int getRowCount();
	public int getColumnCount();
	public Object getValueAt(int row, int column);
	void addTableModelListener(TableModelListener l)
	Adds a listener to the list that is notified each time a change to the data model occurs.
	Class getColumnClass(int columnIndex)
          Returns the most specific superclass for all the cell values in the column.
 int getColumnCount()
          Returns the number of columns in the model.
 String getColumnName(int columnIndex)
          Returns the name of the column at columnIndex.
 int getRowCount()
          Returns the number of rows in the model.
 Object getValueAt(int rowIndex, int columnIndex)
          Returns the value for the cell at columnIndex and rowIndex.
 boolean isCellEditable(int rowIndex, int columnIndex)
          Returns true if the cell at rowIndex and columnIndex is editable.
 void removeTableModelListener(TableModelListener l)
          Removes a listener from the list that is notified each time a change to the data model occurs.
 void setValueAt(Object aValue, int rowIndex, int columnIndex)
          Sets the value in the cell at columnIndex and rowIndex to aValue.
	*/



	// FIM: sobrepondo métodos de AbstractTableModel
	///////////////////////////////////////////////////

    /**
	 * Make everything editable or not
	 */
    public void setEditable(boolean editable) {
		_editable = editable;
    }

    /**
	 * Return the title of the column
	 */
    public String getColumnName(int columnIndex) {
        String nome = null;
        if(columnIndex == PROPERTY_NAME_COLUMN)
            nome = "Nome";
        else if(columnIndex == PROPERTY_VALUE_COLUMN)
            nome = "Valor";
        return nome;
    }

    /**
	  * Return the width of the column
	  */
    public int getColumnWidth(int columnIndex) {
		if (columnIndex == PROPERTY_NAME_COLUMN) {
			return _columnWidthLabels;
		}
		else if (columnIndex == PROPERTY_VALUE_COLUMN) {
			return _columnWidthValues;
		}
		else return 100;
    }

    public String getPropertyName(int i) {
        return _properties[i].getDisplayName();
    }

	/** class of columns */
	public Class getCellClass(int rowIndex, int columnIndex) {
		Class result = Object.class;
		if (columnIndex == PROPERTY_NAME_COLUMN)
			result = String.class;
		else if (columnIndex == PROPERTY_VALUE_COLUMN)
			result = this.getVisibleProperty(rowIndex).getPropertyClass();
		return result;
	}

    /** add object */
    public synchronized void addObject(Object object) {
        // not null
        if (object == null)
        throw new RuntimeException("Not null objects in this table model");

        // from a different class
        if (object.getClass() != _class)
        throw new RuntimeException("This object is not compatible with this table model (different class)");

        //
        _objects.add(object);

        //
        updateAllCells();
    }

    /**
	 * Add all object from vector. They must match the same class
	 * as the _class state variable.
	 */
    public synchronized void addObject(Vector v) {
		_objects.addAll(v);

        updateAllCells();
    }

    /**
	 * Remove all object from vector. They must match the same class
	 * as the _class state variable.
	 */
    public synchronized void removeObject(Vector v) {
		_objects.removeAll(v);

        updateAllCells();
    }

	/**
	 * Remove all objects.
	 */
	public synchronized void clear() {
		_objects.clear();

        updateAllCells();
	}

    /** remove object */
    public synchronized void removeObject(Object object) {
        // not null
        if (object == null)
        throw new RuntimeException("Not null objects in this table model");

        // from a different class
        if (object.getClass() != _class)
        throw new RuntimeException("This object is not compatible with this table model (different class)");

        //
        _objects.remove(object);
        this.updateAllCells();
    }

    /** get property */
    private BeanProperty getProperty(int index) { return _properties[index]; }

    /** get visible property */
    private BeanProperty getVisibleProperty(int index) { return _properties[_visibleProperties[index]]; }

    /** set object */
    public Object getObject(int index) { return _objects.get(index); }

    /** set object */
    public Vector getObjects() { return (Vector)_objects.clone(); }

    /** number of properties */
    public int indexOf(Object o) { return _objects.indexOf(o); }

    /** is cell editable */
    public boolean isCellEditable(int row, int column) {
		if (column == PROPERTY_VALUE_COLUMN && _editable)
            return this.getVisibleProperty(row).isEditable();
		else
			return false;
    }

    /** get number of properties */
    public int getNumberOfProperties() { return _properties.length; }

    /** get number of visible properties */
    public int getNumberOfVisibleProperties() { return _numberOfVisibleProperties; }

    /**
     * @param propertyIdSequence the id of a property is the index on BeanInfo PropertyDescriptor array
     */
    public void setVisiblePropertySequence(int[] propertyIdSequence) {
        _numberOfVisibleProperties = propertyIdSequence.length;
        for(int i = 0; i < _numberOfVisibleProperties; i++)
            _visibleProperties[i] = propertyIdSequence[i];
        this.fireTableStructureChanged();
    }

    /**
     * find property
     */
    private int findPropertyIndex(String propertyName) {
		for (int i=0;i<_properties.length;i++) {
		    if (_properties[i].getName().equals(propertyName))
				return i;
		}
		return -1;
    }


    /**
     * @param propertyNameSequence the names of a property is the index on BeanInfo PropertyDescriptor array
     */
    public void setVisiblePropertySequence(String[] propertyNameSequence) {
		int count = 0;
		for (int i=0;i<propertyNameSequence.length;i++) {
			int index = this.findPropertyIndex(propertyNameSequence[i]);
			if (index >= 0) {
                _visibleProperties[count++] = index;
			}
		}
		_numberOfVisibleProperties=count;
        this.fireTableStructureChanged();
    }

    /**
	 * Make only these properties editable
	 */
    public void makeOnlyThesePropertiesEditable(String propertyNameSequence[]) {
		for (int i=0;i<_properties.length;i++)
			_properties[i].setContextEditable(false);
		for (int i=0;i<propertyNameSequence.length;i++) {
			int index = this.findPropertyIndex(propertyNameSequence[i]);
			_properties[index].setContextEditable(true);
		}
    }

	/** get visible sequence */
    public int[] getVisiblePropertySequence() {
        int[] sequence = new int[_numberOfVisibleProperties];
        for(int i = 0; i < sequence.length; i++)
            sequence[i] = _visibleProperties[i];
        return sequence;
    }

	/**
	 * get renderer
	 */
	public TableCellRenderer getRenderer(int row, int column) {
		BeanProperty p = this.getVisibleProperty(row);
		if (column == PROPERTY_NAME_COLUMN) {
			return p.getTitleRenderer();
		}
		else if (column == PROPERTY_VALUE_COLUMN) {
		    return p.getRenderer();
		}
		else return null;
	}

    /**
     * Update all cells without destroying selection
     */
    public void updateAllCells() {
        this.fireTableRowsUpdated(0,this.getRowCount()-1);
    }
}