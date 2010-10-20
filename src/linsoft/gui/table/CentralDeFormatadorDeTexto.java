package linsoft.gui.table;

public class CentralDeFormatadorDeTexto {
    private static FormatadorDeTexto _formatadorDeTexto = new FormatadorDeTextoDefault();
    public static FormatadorDeTexto getFormatadorDeTexto() {
        return _formatadorDeTexto;
    }
    public static void setFormatadorDeTexto(FormatadorDeTexto formatadorDeTexto) {
        _formatadorDeTexto = formatadorDeTexto;
    }
}