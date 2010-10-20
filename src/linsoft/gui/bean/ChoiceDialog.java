package linsoft.gui.bean;

import java.awt.event.ActionEvent;
import java.util.Vector;


/**
 * Janela que permite escolher um objeto entre vários em uma lista.
 * Cada objeto é visto como uma linha numa tabela.
 */
public class ChoiceDialog extends EditDialog {
    ChoicePanel _panel = new ChoicePanel();
    boolean _cancelado = false;

    public void inicializar(String titulo, Vector objs) {
        this.setEditPanel(_panel);
        _cancelado = false;
        this.setTitle(titulo);
        try {
            _panel.setClass(objs.get(0).getClass());
            for(int i = 0; i < objs.size(); i++)
                _panel.addObject(objs.get(i));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getObject() {
        Object obj = null;
        if(!_cancelado)
            obj = _panel.getObject();
        return obj;
    }

    void _botaoCancelar_actionPerformed(ActionEvent e) {
        _cancelado = true;
        this.setVisible(false);
    }
}