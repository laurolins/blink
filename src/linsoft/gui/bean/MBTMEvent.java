package linsoft.gui.bean;

/**
 * Multiple Bean Table Model specific events:
 *
 * Rows were Sorted
 */
public class MBTMEvent {
    public static final int ROWS_WERE_SORTED = 1;
    public static final int SORT_IS_ENABLED_QUERY = 2;

    private MultipleBeanTableModel _source;
    private int _eventType;

    public MBTMEvent(MultipleBeanTableModel source, int eventType) {
        _source = source;
        _eventType = eventType;
    }
    public int getEventType() {
        return _eventType;
    }
    public MultipleBeanTableModel getSource() {
        return _source;
    }
}