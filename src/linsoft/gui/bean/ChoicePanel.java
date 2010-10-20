package linsoft.gui.bean;

import java.beans.IntrospectionException;

/**
 * Painel com uma BeanTable cujo modelo é um MultipleBeanTableModel.
 * Usado no editor de uma propriedade que é uma referencia para um
 * objeto qualquer.
 */
public class ChoicePanel extends BeanTablePanel {
    private MultipleBeanTableModel _tableModel;


    public ChoicePanel() {
        super();

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
   }


    public void setClass(Class c) throws IntrospectionException {
        _tableModel = new MultipleBeanTableModel(c);
        setModel(_tableModel);
    }


    public void addObject(Object obj) {
        _tableModel.addObject(obj);
    }


    /**
     * Retorna o objeto escolhido.
     */
    public Object getObject() {
        int i = _table.getSelectedRow();
        Object obj = null;
        if(i != -1)
            obj = _tableModel.getObject(i);
        return obj;
    }
}