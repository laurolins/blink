package linsoft.gui.table;

import java.awt.Component;
import java.awt.Font;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class ConfigurableCellRendererText extends ConfigurableCellRendererDefault {

    /**
     * The component that will be used to show on gui.
     */
    private static JLabel _defaultLabel = new JLabel();

    /**
     * Font
     */
    private static Font _defaultFont = new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 11);

    // INICIO: atributos configuráveis do cell renderer
    private String _formatId;
    private Font _font;
    private JLabel _label;
    // FIM: atributos configuráveis do cell renderer

    public ConfigurableCellRendererText() {
        // formatter
        _formatId = null;

        // set font
        _font = _defaultFont;

        // set label
        _label = _defaultLabel;

        //_font = new javax.swing.plaf.FontUIResource("Courier New", Font.PLAIN, 11);
        //new Font("Dialog",Font.PLAIN,11);
        //FontUIResource dialogPlain12 =
        //
        //_label = new JLabel();
        //_label.setBorder(null);
        //_label.setOpaque(true);
        //_label.setHorizontalAlignment(JTextField.LEFT);
        //_label.setFont(_font);
    }

    // INICIO: implementando interface TableCellRenderer
    // private static final BorderTLBR _selectedBorder = new BorderTLBR(new Color(8, 36, 107), 2, false, false, false, false);

    public Component getTableCellRendererComponent
        (JTable table,
         Object value,
         boolean isSelected,
         boolean hasFocus,
         int row,
         int column) {

        _label.setBorder(null);
        _label.setOpaque(true);
        _label.setHorizontalAlignment(JTextField.LEFT);
        _label.setFont(_font);

        //////////////////////////////////////////////////
        // Configure border and color using default way
        this.configureBorderAndColors(_label, table, value, isSelected, hasFocus, row, column);
        // Configure border and color using default way
        //////////////////////////////////////////////////

        // text format
        String text = CentralDeFormatadorDeTexto.getFormatadorDeTexto().format(value, _formatId);

        // alignment
        switch (this.get_alignment()) {
            case ALIGN_LEFT:
                _label.setHorizontalAlignment(JTextField.LEFT);
                _label.setText(" " + text);
                break;
            case ALIGN_CENTER:
                _label.setHorizontalAlignment(JTextField.CENTER);
                _label.setText(text);
                break;
            case ALIGN_RIGHT:
                _label.setHorizontalAlignment(JTextField.RIGHT);
                _label.setText(text + " ");
                break;
        }

        //
        return _label;
    }

    // FIM: implementando interface TableCellRenderer

    // INICIO: query methods

    public String getFormat() {
        return _formatId;
    }

    public Font getFont() {
        return _font;
    }

    // FIM: query methods

    // INICIO: methods for specific set properties

    public void set_format(String formatId) {
        _formatId = formatId;
    }

    public void set_font(Font font) {
        _font = font;
        _label.setFont(font);
    }

    public void addProperty(String property, String value) {
        /////////////////////////////////////////////////////////
        // INICIO: specific properties for text cell renderers...

        if (property.equals("format")) {
            // format
            try {
                if (value != null)
                    this.set_format(value.trim());
                else
                    this.set_format(null);
            }
            catch (Exception e) {
                System.out.println("Log: Could not create format");
            }
        }
        else if (property.equals("font")) {
            // font
            try {
                if (value != null) {
                    this.set_font(this.getFontFromString(value));
                }
            }
            catch (Exception e) {
                System.out.println("Log: Could not create font");
            }
        } else {
            // tenta adicionar a propriedade atual na classe pai
            super.addProperty(property, value);
        }

        // FIM: specific properties for text cell renderers...
        /////////////////////////////////////////////////////////
    }

    // FIM: methods for specific set properties

    /**
     * Create Color from Comma Separated String of color components.
     * 255,255,255 = white
     */
    private Font getFontFromString(String st) throws Exception {
        StringTokenizer t = new StringTokenizer(st, ",");
        String name = t.nextToken();
        String typeStr = t.nextToken();
        int size = Integer.parseInt(t.nextToken().trim());

        //
        int type = Font.PLAIN;
        if (typeStr.equals("plain"))
            type = Font.PLAIN;
        else if (typeStr.equals("bold"))
            type = Font.BOLD;
        else if (typeStr.equals("italic"))
            type = Font.ITALIC;
        else if (typeStr.equals("bold_italic"))
            type = Font.BOLD + Font.ITALIC;

            //
        return new Font(name, type, size);
    }
}