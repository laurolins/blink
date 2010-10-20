package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.table.TableCellRenderer;

import linsoft.gui.table.CentralFCTCR;
import linsoft.gui.table.ConfigurableCellRendererDefault;
import linsoft.gui.table.ConfigurableCellRendererText;
import linsoft.gui.table.FormatadorDeTexto;

/**
 * Keep a set of useful auxiliar data for a bean property.
 */
public class BeanProperty {

    /**
     * Descritor de propriedade
     */
    private PropertyDescriptor _descriptor;

    /**
     * Index
     */
    private int _index;

    /**
     * Renderer
     */
    private ConfigurableCellRendererDefault _renderer;

    /**
     * Title Renderer
     */
    private static ConfigurableCellRendererText _titleRenderer = new ConfigurableCellRendererText();

    /**
     * Largura inicialmente igual a original, mas que
     * pode mudar com o decorrer do tempo.
     */
    private int _width;

    /**
     * Largura informada p/ a propriedade original
     */
    private int _originalWidth;

    /**
     * this property is contextEditable.
     * if this flag is true yet it is needed that the property
     * descriptor have the write method.
     */
    private boolean _contextEditable;

    /**
     * Título informado p/ a coluna da propriedade.
     */
    private String _title;

    /**
     * Descriçao da propriedade.
     */
    private String _descricao;

    /**
     * Use this formatter for rendering.
     */
    private FormatadorDeTexto _formatter;
    public void set_index(int _index) {
        this._index = _index;
    }

    public int get_index() {
        return _index;
    }

    public int get_originalWidth() {
        return _originalWidth;
    }

    /**
     * Constructor
     */
    public BeanProperty(PropertyDescriptor descriptor) {
        this(descriptor, null);
    }

    /**
     * Constructor
     */
    public BeanProperty(PropertyDescriptor descriptor, FormatadorDeTexto formatter) {
        _descriptor = descriptor;

        //
        _contextEditable = true;

        //
        _formatter = formatter;

        //
        // presentation renderer characteristics
        this.mountCellRendererFormDecriptor();

        //
        //_titleRenderer = new ConfigurableCellRendererText();
        // _titleRenderer.set_font(new Font("Tahoma",Font.BOLD + Font.ITALIC,10));

        // get title
        _title = _descriptor.getName();
        try {
            String value = (String) _descriptor.getValue("title");
            if (value != null)
                _title = value;
        }
        catch (Exception e) {
            System.out.print("Could not get title");
        }

        // get descrição
        _descricao = "";
        try {
            String value = (String) _descriptor.getValue("descricao");
            if (value != null)
                _descricao = value;
        }
        catch (Exception e) {
            System.out.print("Could not get descricao");
        }

        // get width
        _width = 100;
        try {
            String value = (String) _descriptor.getValue("width");
            if (value != null)
                _width = Integer.parseInt(value.trim());
        }
        catch (Exception e) {
            System.out.print("Could not get width");
        }
        _originalWidth = _width;
    }

    /**
     * Class of the property
     */
    public Class getPropertyClass() {
        Class c = _descriptor.getPropertyType();
        if (c == int.class)
            c = Integer.class;
        else if (c == short.class)
            c = Short.class;
        else if (c == byte.class)
            c = Byte.class;
        else if (c == boolean.class)
            c = Boolean.class;
        else if (c == long.class)
            c = Long.class;
        else if (c == float.class)
            c = Float.class;
        else if (c == char.class)
            c = Character.class;
        else if (c == double.class)
            c = Double.class;
        return c;
    }

    public String getName() {
        return _descriptor.getName();
    }

    public String getDisplayName() {
        return _descriptor.getDisplayName();
    }

    public Class getCustomEditorClass() {
        return _descriptor.getPropertyEditorClass();
    }

    /**
     * Property value on the given instance.
     */
    public Object getValue(Object instance) {
        try {
            return _descriptor.getReadMethod().invoke(instance);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * is Editable.
     */
    public boolean isEditable() {
        return (_descriptor.getWriteMethod() != null) && getContextEditable();
    }

    /**
     * Try updating this property value on Bean.
     */
    public void updateValue(Object instance, Object newValue) throws
        InvocationTargetException, IllegalAccessException {
        Method w = _descriptor.getWriteMethod();
        w.invoke(instance, new Object[] {newValue});
    }

    /**
     * Return cell renderer for this property
     */
    public TableCellRenderer getRenderer() {
        return _renderer;
    }

    /**
     * Title Renderer
     */
    public TableCellRenderer getTitleRenderer() {
        _titleRenderer.set_bgColor(new Color(190, 190, 190));
        _titleRenderer.set_fgColor(new Color(0, 0, 100));
        _titleRenderer.set_font(new Font("Tahoma", Font.BOLD, 10));
        _titleRenderer.set_alignment(ConfigurableCellRendererText.ALIGN_RIGHT);
        return _titleRenderer;
    }

    /**
     * Width property
     */
    public void setWidth(int width) {
        _width = width;
    }

    /**
     * Width property
     */
    public int getWidth() {
        return _width;
    }

    /**
     * Title property
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Descricao property
     */
    public String getDescricao() {
        return _descricao;
    }

    /**
     * set formatter
     */
    public void setFormatter(FormatadorDeTexto formatter) {
        _formatter = formatter;
    }

    /**
     * setContextEditable
     */
    public void setContextEditable(boolean contextEditable) {
        _contextEditable = contextEditable;
    }

    /**
     * getContextEditable
     */
    public boolean getContextEditable() {
        return _contextEditable;
    }

    /**
     * Create Configurable Cell Renderer from Property Descriptor values.
     */
    private void mountCellRendererFormDecriptor() {
        // pegar o renderizador da central de fornecimento
        _renderer = CentralFCTCR.getFornecedorCTCR().getCTCR(this.getPropertyClass(),
           (String) _descriptor.getValue("rendererKey"));

        /////////////////////////////////////////////////////////
        // INICIO: specific properties for text cell renderers...
        // format
        _renderer.addProperty("format", (String) _descriptor.getValue("format"));
        // font
        _renderer.addProperty("font", (String) _descriptor.getValue("font"));
        // FIM: specific properties for text cell renderers...
        /////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////
        // adicionando propriedades gerais de todos os _renderers
        _renderer.addProperty("fgColor", (String) _descriptor.getValue("fgColor"));
        _renderer.addProperty("bgColor", (String) _descriptor.getValue("bgColor"));
        _renderer.addProperty("selectedFgColor", (String) _descriptor.getValue("selectedFgColor"));
        _renderer.addProperty("selectedBgColor", (String) _descriptor.getValue("selectedBgColor"));
        _renderer.addProperty("focusedFgColor", (String) _descriptor.getValue("focusedFgColor"));
        _renderer.addProperty("focusedBgColor", (String) _descriptor.getValue("focusedBgColor"));
        _renderer.addProperty("alignment", (String) _descriptor.getValue("alignment"));
        // adicionando propriedades gerais de todos os _renderers
        /////////////////////////////////////////////////////////
    }
}
