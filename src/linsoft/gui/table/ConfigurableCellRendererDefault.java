package linsoft.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;

import linsoft.gui.border.BorderTLBR;

/**
 * Classe que implementa um TableCellRenderer configurável.
 * Atributos configuráveis:
 *
 * fgColor - cor foreground quando célula não selecionada
 * bgColor - cor background quando célula não selecionada
 * selectedFgColor - cor foreground quando célula selecionada
 * selectedBgColor - cor foreground quando célula selecionada
 * alignment - alinhamento horizontal do texto {0 = LEFT, 1 = CENTER, 2 = RIGHT}
 */
public abstract class ConfigurableCellRendererDefault
    implements ConfigurableTableCellRenderer, Cloneable {

    ///////////////////////////////////////////////////////
    // INICIO: static attributes
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    // FIM: static attributes
    ///////////////////////////////////////////////////////

    // private constants
    private static final BorderTLBR _selectedBorder = new BorderTLBR(DefaultColors.DEFAULT_BORDER_COLOR,1,false,false,false,false);

    ///////////////////////////////////////////////////////
    // INICIO: atributos configuráveis do cell renderer
    private Color _fgColor;
    private Color _bgColor;
    private Color _selectedBgColor;
    private Color _selectedFgColor;
    private Color _focusedBgColor;
    private Color _focusedFgColor;
    private Color _selectionBorderColor;
    private int _alignment;
    // FIM: atributos configuráveis do cell renderer
    ///////////////////////////////////////////////////////

    /**
     * The component that will be used
     */
    public ConfigurableCellRendererDefault() {
        _bgColor = DefaultColors.DEFAULT_BG_COLOR;
        _fgColor = DefaultColors.DEFAULT_FG_COLOR;
        _selectedBgColor = DefaultColors.DEFAULT_SELECTED_BG_COLOR;
        _selectedFgColor = DefaultColors.DEFAULT_SELECTED_FG_COLOR;
        _focusedBgColor = DefaultColors.DEFAULT_FOCUSED_BG_COLOR;
        _focusedFgColor = DefaultColors.DEFAULT_FOCUSED_FG_COLOR;
        _alignment = ALIGN_LEFT;
    }

    // INICIO: implementando interface TableCellRenderer

    public abstract Component getTableCellRendererComponent
        (JTable table,
         Object value,
         boolean isSelected,
         boolean hasFocus,
         int row,
         int column);

    // FIM: implementando interface TableCellRenderer

    // INICIO: get methods

    // FIM: get methods

    ////////////////////////////////////
    // INICIO: set methods
    public int get_alignment() { return _alignment; }
    public Color get_bgColor() { return _bgColor; }
    public Color get_fgColor() { return _fgColor; }
    public Color get_selectedBgColor() { return _selectedBgColor; }
    public Color get_selectedFgColor() { return _selectedFgColor; }
    public Color get_focusedBgColor() { return _focusedBgColor; }
    public Color get_focusedFgColor() { return _focusedFgColor; }
    public Color get_selectionBorderColor() { return _selectionBorderColor; }
    public void set_bgColor(Color bgColor) { this._bgColor = bgColor; }
    public void set_fgColor(Color fgColor) { this._fgColor = fgColor; }
    public void set_selectedBgColor(Color selectedBgColor) { this._selectedBgColor = selectedBgColor; }
    public void set_selectedFgColor(Color selectedFgColor) { this._selectedFgColor = selectedFgColor; }
    public void set_focusedBgColor(Color focusedBgColor) { this._focusedBgColor = focusedBgColor; }
    public void set_focusedFgColor(Color focusedFgColor) { this._focusedFgColor = focusedFgColor; }
    public void set_selectionBorderColor(Color selectionBorderColor) { this._selectionBorderColor = selectionBorderColor; _selectedBorder.setColor(_selectionBorderColor);}
    public void set_alignment(int _alignment) { this._alignment = _alignment; }
    // FIM: set methods
    ////////////////////////////////////

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Set the color and border of the cell...
     */
    protected void configureBorderAndColors(JComponent component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ////////////////////////////////////////////////
        // backgorund and foreground color
        if (hasFocus) {
            component.setBackground(get_focusedBgColor());
            component.setForeground(get_focusedFgColor());
        }
        else if (isSelected) {
            component.setBackground(get_selectedBgColor());
            component.setForeground(get_selectedFgColor());
        }
        else {
            component.setBackground(get_bgColor());
            component.setForeground(get_fgColor());
        }
        // backgorund and foreground color
        ////////////////////////////////////////////////

        ///////////////////////////////////////////////
        // border
        if (isSelected) {
            component.setBorder(_selectedBorder);
            boolean l = (column == 0);
            boolean r = (column == table.getColumnCount()-1);
            boolean t = row > 0 ? !table.getSelectionModel().isSelectedIndex(row-1) : true;
            boolean b = row < table.getRowCount()-1 ? !table.getSelectionModel().isSelectedIndex(row+1) : true;
            _selectedBorder.setVisibleBorderLines(l,r,t,b);

        }
        else {
            component.setBorder(null);
        }
        // border
        ///////////////////////////////////////////////
    }

    public Border calculateBorder(JTable table, int row, int column) {
        boolean l = (column == 0);
        boolean r = (column == table.getColumnCount()-1);
        boolean t = row > 0 ? !table.getSelectionModel().isSelectedIndex(row-1) : true;
        boolean b = row < table.getRowCount()-1 ? !table.getSelectionModel().isSelectedIndex(row+1) : true;
        _selectedBorder.setVisibleBorderLines(l,r,t,b);
        return _selectedBorder;
    }

    public void addProperty(String property, String value) {
        //////////////////////////////////////////////////////////////////
      // INICIO: global properties for all configurable cell renderers...
      //  fgColor
      if (property.equals("fgColor")) {
          try {
              if (value != null) {
                  this.set_fgColor(getColorFromString(value));
              }
          }
          catch (Exception e) {
              System.out.println("Log: Could not create fgColor");
          }
      } else if (property.equals("bgColor")) {
          // bgColor
          try {
              if (value != null) {
                  this.set_bgColor(getColorFromString(value));
              }
          }
          catch (Exception e) {
              System.out.println("Log: Could not create bgColor");
          }
      } else if (property.equals("selectedFgColor")) {
          // selectedFgColor
          try {
              if (value != null)
                  this.set_selectedFgColor(getColorFromString(value));
          }
          catch (Exception e) {
              System.out.println("Log: Could not create selectedFgColor");
          }
      } else if (property.equals("selectedBgColor")) {
          // selectedFgColor
          try {
              if (value != null)
                  this.set_selectedBgColor(getColorFromString(value));
          }
          catch (Exception e) {
              System.out.println("Log: Could not create selectedBgColor");
          }
      } else if (property.equals("focusedFgColor")) {
          // focusedFgColor
          try {
              if (value != null)
                  this.set_focusedFgColor(getColorFromString(value));
          }
          catch (Exception e) {
              System.out.println("Log: Could not create focusedFgColor");
          }
      } else if (property.equals("focusedBgColor")) {
          // focusedBgColor
          try {
              if (value != null)
                  this.set_focusedBgColor(getColorFromString(value));
          }
          catch (Exception e) {
              System.out.println("Log: Could not create focusedBgColor");
          }
      } else if (property.equals("alignment")) {
          // alignment
          try {
              if (value != null)
                  this.set_alignment(Integer.parseInt(value.trim()));
          }
          catch (Exception e) {
              System.out.println("Log: Could not create alignment");
          }
      }
      // FIM: global properties for all configurable cell renderers...
      //////////////////////////////////////////////////////////////////
    }

    /**
     * Create Color from Comma Separated String of color components.
     * 255,255,255 = white
     */
    private Color getColorFromString(String st) throws Exception {
        StringTokenizer t = new StringTokenizer(st, ",");
        int r = Integer.parseInt(t.nextToken().trim());
        int g = Integer.parseInt(t.nextToken().trim());
        int b = Integer.parseInt(t.nextToken().trim());
        return new Color(r / 255.0f, g / 255.0f, b / 255.0f);
    }

}