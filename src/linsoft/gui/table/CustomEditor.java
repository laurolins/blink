package linsoft.gui.table;

/**
 * Defines an Object editor.
 */
public interface CustomEditor {
   /**
    * Start the editor with an initialization object.
    */
   void start(Object obj);

   /**
    * Get the object that was created
    * it is up to the implementing class decide if getObject()
    * returns the initialization object or another object.
    * At the moment although I think the initialization object
    * and the object returned by this method should be of the same class.
    */
   Object getObject();

   /**
    * Add CustomEditorListener
    */
   void addCustomEditorListener(CustomEditorListener listener);

   /**
    * Remove CustomEditorListener
    */
   void removeCustomEditorListener(CustomEditorListener listener);
}