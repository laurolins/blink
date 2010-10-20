package linsoft.gui.table;

/**
 * Interface of objects capable of formatting a string form
 * an Object and a formatId string
 */
public interface FormatadorDeTexto {
    /**
     * Interface of an object capable of formatting a string form
     * an Object and a formatId
     *
     * @param value
     * @param formatId
     * @return formatted string from value
     */
    String format(Object value, String formatId);
}