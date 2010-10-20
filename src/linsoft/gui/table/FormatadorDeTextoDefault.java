package linsoft.gui.table;

public class FormatadorDeTextoDefault implements FormatadorDeTexto {
    public FormatadorDeTextoDefault() {
    }
    public String format(Object value, String formatId) {
        return value.toString();
    }
}