package linsoft.gui.bean;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.table.TableCellEditor;

/**
 * Janela para edição de um objeto bean.
 */
public class BeanPanelDialog extends EditDialog {

    BeanPanel _beanPanel = new BeanPanel();
    Object _object = null;

    public BeanPanelDialog(JFrame frame) {
        super(frame);
    }

    public void inicializar(String titulo, Object object) {
        this.setEditPanel(_beanPanel);
        _object = object;
        this.setTitle(titulo);
        try {
            _beanPanel.setObject(object);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Object getObject() {
        return _object;
    }

    void _botaoCancelar_actionPerformed(ActionEvent e) {
        _object = null;
        this.setVisible(false);
    }

    /**
     * Seta o editor da tabela que há em _beanPanel.
     */
    public void setEditor(Class c, TableCellEditor edt) {
        _beanPanel.setEditor(c, edt);
    }
}