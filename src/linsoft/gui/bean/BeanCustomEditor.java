package linsoft.gui.bean;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;

import linsoft.gui.table.CustomEditor;
import linsoft.gui.table.CustomEditorListener;

public class BeanCustomEditor implements CustomEditor {
   // gui state
   private JDialog _dialog;
   private BeanPanel _panel;

   // data state
   private Vector _listeners = new Vector();
   private Object _initObject;
   private Object _newObject;

   /**
    * Constructor.
    */
   public BeanCustomEditor(JFrame frame, String title) {
      _dialog = new JDialog(frame,title,true);
      _dialog.setSize(200,250);
      _dialog.setLocation(400,400);


      _panel = new BeanPanel();
      _dialog.setContentPane(_panel);
      _dialog.addWindowListener(new WindowAdapter() {
	 public void windowClosed(WindowEvent e) {
	    fireEditorFinishedEvent();
	 }
      });
      _dialog.setModal(true);
   }

   /**
    * Start the editor with an initialization object.
    */
   public void start(Object obj) {
      if (obj != null) {
	 _initObject = obj;
	 _newObject = obj;
	 //
	 try {
	    _panel.setObject(_newObject);
	    // this is a synchronous call (modal dialog)
	    _dialog.setVisible(true);
	    fireEditorFinishedEvent();
	 }
	 catch (Exception e) {
	    e.printStackTrace();
	    fireEditorFinishedEvent();
	 }
      }
      else fireEditorFinishedEvent();
   }

   /**
    * Get the object that was created
    * it is up to the implementing class decide if getObject()
    * returns the initialization object or another object.
    * At the moment although I think the initialization object
    * and the object returned by this method should be of the same class.
    */
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
	 ((CustomEditorListener) _listeners.get(i)).editionFinished(this,_initObject,_newObject);
      }
   }
}
