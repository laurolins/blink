package linsoft.gui.bean;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

/**
 * Painel com uma tabela BeanTable em que o modelo é um BeanTableModel.
 */
public class BeanPanel extends BeanTablePanel {
    private Object _object;
    private BeanTableModel _tableModel;


    public BeanPanel() {
        super();
        _tableModel = new BeanTableModel();
        _tableModel.setTable(_table);
        setModel(_tableModel);

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
   }


    public void setObject(Object object) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        _object = object;
        _tableModel.setObject(object);
    }
}
