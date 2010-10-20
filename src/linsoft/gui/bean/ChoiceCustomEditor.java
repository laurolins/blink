package linsoft.gui.bean;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.JFrame;

import linsoft.gui.table.CustomEditor;
import linsoft.gui.table.CustomEditorListener;

/**
 * Editor de células que chama uma janela de escolha de objetos.
 */
public class ChoiceCustomEditor implements CustomEditor {

    private Object _initObject;
    private Object _newObject;

    // gui state
    private ChoiceDialog _dialog;

    // data state
    private Vector _listeners = new Vector();


    /**
     * Constructor.
     */
    public ChoiceCustomEditor(JFrame frame, String title) {
        _dialog = new ChoiceDialog();
        _dialog.setSize(200, 250);
        _dialog.setLocation(400, 400);
        _dialog.setSize(
            (int) (_dialog.getInsets().left + _dialog.getInsets().right + 320),
            (int) (_dialog.getInsets().top + _dialog.getInsets().bottom + 240)
        );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        _dialog.setLocation((int) (d.getWidth() - _dialog.getWidth()) / 2, (int) (d.getHeight() - _dialog.getHeight()) / 2);
    }


    /**
     * Start the editor with an initialization object.
     */
    public void start(Object obj) {
        if (obj != null) {
            Vector objs = (Vector) obj;
            _initObject = obj;
            try {
                _dialog.inicializar("Escolha", objs);
                _dialog.setVisible(true);
                _newObject = _dialog.getObject();
                if(_newObject != null)
                    fireEditorFinishedEvent();
            }
            catch (Exception e) {
                e.printStackTrace();
                _newObject = null;
                fireEditorFinishedEvent();
            }
        }
        else fireEditorFinishedEvent();
    }



    public Object getObject() {
        return _newObject;
    }


    /**
     * Add CustomEditorListener
     */
    public void addCustomEditorListener(CustomEditorListener listener) {
        if (_listeners.indexOf(listener) == -1) {
            _listeners.add(listener);
        }
    }


    /**
     * Remove CustomEditorListener
     */
    public void removeCustomEditorListener(CustomEditorListener listener) {
        _listeners.remove(listener);
    }


    /**
     * Fire event
     */
    private void fireEditorFinishedEvent() {
        for (int i=0;i<_listeners.size();i++) {
            ((CustomEditorListener) _listeners.get(i)).editionFinished(this, _initObject, _newObject);
        }
    }
}
