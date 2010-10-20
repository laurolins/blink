package linsoft.gui.table;

/**
 * Defines a Custom Editor Listener.
 */
public interface CustomEditorListener {
   /**
    * Signal that edition was completed.
    * @param source is the editor.
    * @param initObject was the object passed on the initialization of the editor.
    * @param editedObject the resulting object from edition.
    */
   void editionFinished(CustomEditor source, Object initObject, Object editedObject);
}