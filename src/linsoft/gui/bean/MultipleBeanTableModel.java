package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Font;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import linsoft.gui.table.ConfigurableCellRendererText;
import linsoft.gui.table.FormatadorDeTexto;

/**
 * Table model for properties...
 */
public class MultipleBeanTableModel extends AbstractTableModel {
    public static final String ROW_NUMBER_COLUMN_ID = "_rowNumberColumn";

    /////////////////////////////////////////////////
    // Specific MBTM events listener infrastructure
    private Vector _specificListeners;
    public void addMBTMListener(MBTMListener listener) {
        if (_specificListeners == null) {
            _specificListeners = new Vector(1);
        }
        else if (_specificListeners.contains(listener)) {
            return;
        }
        _specificListeners.add(listener);
    }
    public void removerMBTMListener(MBTMListener listener) {
        if (_specificListeners == null)
            return;
        else
            _specificListeners.remove(listener);
    }
    public void fireMBTMEvent(MBTMEvent event) {
        if (_specificListeners != null) {
            for (int i=0;i<_specificListeners.size();i++) {
                ((MBTMListener) _specificListeners.get(i)).modelEvent(event);
            }
        }
    }
    // Specific MBTM events listener infrastructure
    /////////////////////////////////////////////////

	/**
	 * Classe do comparador usado para ordenar os registros no table model.
	 */
	class Comparador
		implements Comparator {
		private int _coluna;
		private int _currentColumn;
		private boolean _crescente;
		private Object _registro1;
		private Object _registro2;

		public void setColuna(int c) {
			_coluna = c;
		}

		public void setCrescente(boolean c) {
			_crescente = c;
		}

		public int compare(Object registro1, Object registro2) {
			_currentColumn = _coluna;
			_registro1 = registro1;
			_registro2 = registro2;
			return treeCompare();
		}

		private int treeCompare() {
			int resultado = 0;
			Object o1 = getVisibleProperty(_currentColumn).getValue(_registro1);
			Object o2 = getVisibleProperty(_currentColumn).getValue(_registro2);

			if (o1 != null && o2 != null) {
				if (o1 instanceof Comparable) {
					resultado = ( (Comparable) o1).compareTo(o2);
				}
				else {
					String s1 = o1.toString();
					String s2 = o2.toString();
					resultado = s1.compareTo(s2);
				}
			}

			// os nulls vem depois
			else if (o1 != null && o2 == null) {
				resultado = -1;
			}

			// os nulls vem depois
			else if (o1 == null && o2 != null) {
				resultado = 1;
			}

			// desempatar com coluna anterior
			if (resultado == 0 && _currentColumn > 0) {
				_currentColumn--;
				resultado = this.treeCompare();
			}

			// se o resultado foi obtido entao aplicar inversão
			else if (!_crescente) {
				resultado = -resultado;
			}


			return resultado;
		}

		public boolean equals(Object obj) {
			boolean igual = false;
			if (obj instanceof Comparador) {
				Comparador c = (Comparador) obj;
				if (c._coluna == _coluna)
					igual = true;
			}
			return igual;
		}
	}

	/** Comparador */
	private Comparador _comparador;

	/** class that will have objects in this table model */
	private Class _class = null;

	/** properties vector */
	private Vector _objects;

        /** Number column property*/
        private BeanProperty _rowNumberColumnPropery;

	/** properties vector */
	private BeanProperty[] _properties;

	/** visible property array */
	private int _numberOfVisibleProperties;
	private int _visibleProperties[];

	/** editable */
	private boolean _editable;

	/** Use this formatter for rendering. */
	private FormatadorDeTexto _formatter;

	/** contructor */
	public MultipleBeanTableModel(Class c) throws IntrospectionException {

		// set object
		_class = c;

		//
		_objects = new Vector();
		_comparador = new Comparador();

		//
		_editable = true;

		//
		if (c != null) {
			// get introspection info
			PropertyDescriptor p[] = CacheClassBeanInfo.getInstance().getPropertiesDescriptors(c);

			//
			_properties = new BeanProperty[p.length + 1];
			_visibleProperties = new int[p.length + 1];
			_numberOfVisibleProperties = p.length + 1;

                        // build special row number property and add, by default,
                        // add it to the class properties
                        _rowNumberColumnPropery = mountNumberColumnBeanProperty();
                        _rowNumberColumnPropery.set_index(0);


                        // add properties to a array
                        _properties[0] = _rowNumberColumnPropery;
                        for (int i = 0; i < p.length; i++) {
                            _properties[i + 1] = new BeanProperty(p[i], _formatter);
                            _properties[i + 1].set_index(i + 1);
                            _visibleProperties[i + 1] = i;
                        }
			// rebuild all
			// this.updateAllCells();
		}
	}

	/**
	 * Special BeanProperty to column number.
	 */
	private BeanProperty mountNumberColumnBeanProperty() throws IntrospectionException {
        PropertyDescriptor numberColumnPropertyDescriptor = new PropertyDescriptor(ROW_NUMBER_COLUMN_ID, null, null);
        numberColumnPropertyDescriptor.setValue("title", "#");
        numberColumnPropertyDescriptor.setValue("width", "35");
        numberColumnPropertyDescriptor.setValue("alignment", "2");
        numberColumnPropertyDescriptor.setValue("font", "Tahoma,bold,11");
        numberColumnPropertyDescriptor.setValue("fgColor", "0,0,0");
        BeanProperty bp = new BeanProperty(numberColumnPropertyDescriptor);
        return bp;
	}

	/**
	 * Enable/Disenable edition on this model (only the fields allowed)
	 */
	public void setEditable(boolean editable) {
		_editable = editable;
	}

	/**
	 * Return the title of the column
	 */
	public String getColumnName(int columnIndex) {
		return this.getVisibleProperty(columnIndex).getTitle();
	}

	/**
	 * Return the width of the column
	 */
	public int getColumnWidth(int columnIndex) {
		return this.getVisibleProperty(columnIndex).getWidth();
	}

        public String getPropertyName(int i) {
                return _properties[i].getName();
        }

	/** class of columns */
	public Class getColumnClass(int columnIndex) {
		Class c = this.getVisibleProperty(columnIndex).getPropertyClass();
		return c;
	}

	/** add object */
	public synchronized void addObject(Object object) {
		// not null
		if (object == null)
			throw new RuntimeException("Not null objects in this table model");

		// from a different class
		if (object.getClass() != _class)
			throw new RuntimeException(
				"This object is not compatible with this table model (different class)");

		//
		_objects.add(object);
		this.fireTableDataChanged();
	}

	/** add objects not yet contained */
	public synchronized void makeUnion(java.util.List v) {
		boolean change = false;
		for (int i=0;i<v.size();i++) {
			if (!_objects.contains(v.get(i))) {
				// from a different class
				if (v.get(i).getClass() != _class)
					throw new RuntimeException(
						"This object is not compatible with this table model (different class)");
				_objects.add(v.get(i));
				change = true;
			}
		}
		if (change)
			this.fireTableDataChanged();
	}

	/**
	 * Insert object from at the given index.
	 */
	public synchronized void insertObject(Object object, int index) {
		// not null
		if (object == null)
			throw new RuntimeException("Not null objects in this table model");

		// from a different class
		if (object.getClass() != _class)
			throw new RuntimeException(
				"This object is not compatible with this table model (different class)");

		//
		_objects.insertElementAt(object, index);
		this.fireTableDataChanged();
	}

    /**
     * set object from at the given index.
     */
    public synchronized void setObject(Object object, int index) {
        // not null
        if (object == null)
            throw new RuntimeException("Not null objects in this table model");

        // from a different class
        if (object.getClass() != _class)
            throw new RuntimeException(
                "This object is not compatible with this table model (different class)");

        //
        _objects.setElementAt(object, index);
        this.updateAllCells();
    }

    /**
     * set all objects from a given index.
     */
    public synchronized void changeObjects(Vector objects, int startRow) {
        // not null
        if (objects == null || startRow + objects.size() > this.getRowCount())
            throw new RuntimeException("changeObjects");

        //
        for (int i=0;i<objects.size();i++) {
            _objects.setElementAt(objects.get(i), i);
        }

        //
        this.updateAllCells();
    }

	/**
	 * Add all object from vector. They must match the same class
	 * as the _class state variable.
	 */
	public synchronized void addObject(java.util.List v) {
		_objects.addAll(v);
		this.fireTableDataChanged();
	}

	/**
	 * Remove all object from vector. They must match the same class
	 * as the _class state variable.
	 */
	public synchronized void removeObject(java.util.List v) {
		_objects.removeAll(v);
		this.fireTableDataChanged();
	}

    /**
     * Closed interval of indexes
     */
    public synchronized void removeInterval(int beginIndex, int endIndex) {
        for (int i=endIndex;i>=beginIndex;i--) {
            _objects.remove(i);
        }
        this.fireTableDataChanged();
    }

	/** remove object */
	public synchronized void removeObject(Object object) {
		// not null
		if (object == null)
			throw new RuntimeException("Not null objects in this table model");

		// from a different class
		if (object.getClass() != _class)
			throw new RuntimeException(
				"This object is not compatible with this table model (different class)");

		//
		_objects.remove(object);
		this.fireTableDataChanged();
	}

	/** remove object */
	public synchronized void removeRow(int row) {
		_objects.remove(row);
		this.fireTableDataChanged();
	}

	/** clear */
	public void clear() {
		int n = this.getRowCount();
		_objects.removeAllElements();
		this.fireTableDataChanged();
	}

	/** get visible property */
	public BeanProperty getVisibleProperty(int index) {
		return _properties[_visibleProperties[index]];
	}

    /**
     * Get absolute number of properties.
     */
    public int getNumProperties() {
        return _properties.length;
    }

    /**
     * Get absolute property.
     */
    public BeanProperty getProperty(int index) {
        return _properties[index];
    }

	/**
     * Set object
     */
	public Object getObject(int index) {
		return _objects.get(index);
	}

    /** set object */
    public Vector getObjects(int[] indexes) {
        Vector retorno = new Vector();

        for (int i=0; i<indexes.length; i++) {
            retorno.add(_objects.get(indexes[i]));
        }

        return retorno;
    }

	/** set object */
	public Vector getObjects() {
		return (Vector) _objects.clone();
	}

	/** one column for the names and another column for the values */
    public int getColumnCount() {
        return _numberOfVisibleProperties;
    }

	/** number of properties */
	public int getRowCount() {
		return _objects.size();
	}

	/**
     * index of object [0..n-1]
     * -1 if objects is not on model
     */
	public int indexOf(Object o) {
		return _objects.indexOf(o);
	}

    /**
     * Returns flag indicating if the model contains object
     */
    public boolean contains(Object o) {
        return _objects.contains(o);
    }

    /** get object */
    public Object getValueAt(int row, int column) {
        BeanProperty bp = this.getVisibleProperty(column);
        if (bp == _rowNumberColumnPropery) {
            return new Integer(row + 1);
        } else {
            return getVisibleProperty(column).getValue(getObject(row));
        }
    }
	/** swap two rows */
	public void swap(int row1, int row2) {
		Object o1 = _objects.get(row1);
		Object o2 = _objects.get(row2);
		_objects.setElementAt(o1, row2);
		_objects.setElementAt(o2, row1);
		this.fireTableRowsUpdated(row1, row1);
		this.fireTableRowsUpdated(row2, row2);
	}

	/** is cell editable */
	public boolean isCellEditable(int row, int column) {
		if (_editable)
			return this.getVisibleProperty(column).isEditable();
		else
			return false;
	}

	/** get number of properties */
	public int getNumberOfProperties() {
		return _properties.length;
	}

	/** get number of visible properties */
	public int getNumberOfVisibleProperties() {
		return _numberOfVisibleProperties;
	}

	/**
	 * @param propertyIdSequence the id of a property is the index on BeanInfo PropertyDescriptor array
	 */
	public void setVisiblePropertySequence(int[] propertyIdSequence) {
		_numberOfVisibleProperties = propertyIdSequence.length;
		for (int i = 0; i < _numberOfVisibleProperties; i++)
			_visibleProperties[i] = propertyIdSequence[i];
		this.fireTableStructureChanged();
	}

    /**
     * This must be a sequence of bean properties that
     * are all inside the array.
     */
    public void setVisiblePropertySequence(BeanProperty[] bpSequence) {
        for (int i = 0; i < bpSequence.length; i++)
            _visibleProperties[i] = bpSequence[i].get_index();
        _numberOfVisibleProperties = bpSequence.length;
        this.fireTableStructureChanged();
    }

	/**
	 * find property
	 */
	protected int findPropertyIndex(String propertyName) {
		for (int i = 0; i < _properties.length; i++) {
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
		for (int i = 0; i < propertyNameSequence.length; i++) {
			int index = this.findPropertyIndex(propertyNameSequence[i]);
			if (index >= 0) {
				_visibleProperties[count++] = index;
			}
		}
		_numberOfVisibleProperties = count;
		this.fireTableStructureChanged();
	}

	/** get visible sequence */
	public int[] getVisiblePropertySequence() {
		int[] sequence = new int[_numberOfVisibleProperties];
		for (int i = 0; i < sequence.length; i++)
			sequence[i] = _visibleProperties[i];
		return sequence;
	}

	/** set value */
	public void setValueAt(Object value, int row, int column) {
		BeanProperty p = this.getVisibleProperty(column);
		try {
			p.updateValue(getObject(row), value);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// not very efficient!!! it should be by means of event signaling...
		this.updateAllCells();
	}

	public void ordenar(int col, boolean crescente) {
        // query listeners if the sort procedure is enabled
        try {
            this.fireMBTMEvent(new MBTMEvent(this, MBTMEvent.SORT_IS_ENABLED_QUERY));
        } catch (Exception x) {
            // not allowed
            return;
        }

        // make the sort
		_comparador.setColuna(col);
		_comparador.setCrescente(crescente);
		Collections.sort(_objects, _comparador);

        // signal listeners
        this.fireMBTMEvent(new MBTMEvent(this,MBTMEvent.ROWS_WERE_SORTED));
	}

    public void permutar(Vector perm) {
        // check size
        if (_objects.size() != perm.size()) {
            throw new RuntimeException("Permutacao Invalida: Numero de Elementos Diferente do Esperado");
        }

        // check content
        HashSet set = new HashSet();
        for (int i=0;i<_objects.size();i++) {
            if (_objects.indexOf(perm.get(i)) != -1) {
                set.add(perm.get(i));
            }
        }

        //
        if (set.size() != _objects.size()) {
            throw new RuntimeException("Permutacao Invalida: Elementos Inv'alidos");
        }

        //
        Vector newObjects = ((Vector) perm.clone());
        _objects = newObjects;

        //
        this.updateAllCells();

        // signal listeners
        this.fireMBTMEvent(new MBTMEvent(this,MBTMEvent.ROWS_WERE_SORTED));
    }

	/**
	 *
	 * <p>Title: </p>
	 * <p>Description: </p>
	 * <p>Copyright: Copyright (c) 2002</p>
	 * <p>Company: </p>
	 * @author unascribed
	 * @version 1.0
	 */
	public TableCellRenderer getRenderer(int row, int column) {
		BeanProperty p = this.getVisibleProperty(column);
		return p.getRenderer();
	}

	/**
	 * Find next row "findRow" that contains the substring at column "column" such that
		 * "findRow" > "row" if direction is forward ("forward"==true), "findRow" < "row"
		 * if direction is backward ("forward"==false). If no such row exists return -1.
	 *
	 * @param substring text to find as a substring on the same color and at a different row
	 * @param row initial row
	 * @param column search column
	 * @param forward direction (forward if true, backward if false).
	 * @return -1 if no such row exists row index otherwise.
	 */
	public int find(String substring, int row, int column, boolean forward) {
		// initialize normalization variables for the
		// forward and backward search
		int startIndex;
		int endIndex;
		int increment;
		if (forward) {
			startIndex = row + 1;
			endIndex = this.getRowCount() - 1;
			increment = 1;
		}
		else {
			startIndex = row - 1;
			endIndex = 0;
			increment = -1;
		}

		// test
		if (column < 0 ||
			column >= this.getColumnCount() ||
			startIndex < 0 ||
			startIndex >= this.getRowCount() ||
			endIndex < 0 ||
			endIndex >= this.getRowCount())
			return -1;

		// get bean property
		BeanProperty p = this.getVisibleProperty(column);
		String formatId = null;
		if (p.getRenderer()instanceof ConfigurableCellRendererText) {
			formatId = ( (ConfigurableCellRendererText) p.getRenderer()).
				getFormat();
		}

		// do the search
		int result = -1;
		for (int i = startIndex; ; i += increment) {

			// value <- table_visible[i,column]
			String value;
			if (_formatter != null) {
				value = _formatter.format(this.getValueAt(i, column), formatId);
			}
			else {
				value = this.getValueAt(i, column).toString();
			}

			//
			if (value.indexOf(substring) >= 0) {
				result = i;
				break;
			}

			//
			if (i == endIndex)
				break;
		}
		return result;
	}

	/**
	 * Make only these properties editable
	 */
	public void makeOnlyThesePropertiesEditable(String propertyNameSequence[]) {
		for (int i = 0; i < _properties.length; i++)
			_properties[i].setContextEditable(false);
		for (int i = 0; i < propertyNameSequence.length; i++) {
			int index = this.findPropertyIndex(propertyNameSequence[i]);
			_properties[index].setContextEditable(true);
		}
	}

	/**
	 * get formatted cell text.
	 */
	public String getFormattedCellText(int row, int column) {

		// get bean property
		BeanProperty p = this.getVisibleProperty(column);
		String formatId = null;
		if (p.getRenderer()instanceof ConfigurableCellRendererText) {
			formatId = ( (ConfigurableCellRendererText) p.getRenderer()).
				getFormat();
		}

		// value <- table_visible[i,column]
		String value;
		if (_formatter != null) {
			value = _formatter.format(this.getValueAt(row, column), formatId);
		}
		else {
			value = this.getValueAt(row, column).toString();
		}
		return value;
	}

	/**
	 * Update all cells without destroying selection
	 */
	public void updateAllCells() {
		this.fireTableRowsUpdated(0, this.getRowCount() - 1);
	}

	/**
	 * Set fg color
	 */
	public void setNumberColumnFgColor(Color fgColor) {
		((ConfigurableCellRendererText) _rowNumberColumnPropery.getRenderer()).set_fgColor(fgColor);
	}

	/**
	 * Set bg color
	 */
	public void setNumberColumnBgColor(Color bgColor) {
		((ConfigurableCellRendererText) _rowNumberColumnPropery.getRenderer()).set_bgColor(bgColor);
	}

	/**
	 * Set bg color
	 */
	public void setNumberColumnFont(Font font) {
		((ConfigurableCellRendererText) _rowNumberColumnPropery.getRenderer()).set_font(font);
	}

	/**
	 * Set width
	 */
	public void setNumberColumnWidth(int width) {
		_rowNumberColumnPropery.setWidth(width);
	}

    /**
     * Set width on BeanProperty.
     */
    public boolean setVisibleColumnWidth(int index, int width) {
        BeanProperty  bp = this.getVisibleProperty(index);
        if (bp != null) {
            if (bp.getWidth() != width) {
                bp.setWidth(width);
                return true;
            }
        }
        return false;
    }
}
