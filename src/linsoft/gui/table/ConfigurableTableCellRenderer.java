package linsoft.gui.table;

import javax.swing.table.TableCellRenderer;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ConfigurableTableCellRenderer extends TableCellRenderer {
    public abstract void addProperty(String property, String value);
}
