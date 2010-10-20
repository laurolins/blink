package linsoft.gui.bean;

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>Title: PersistentScrollPane </p>
 * <p>Description: </p>
 * An extension of a JScrollPane that given a identifier it
 * is able to keep storing the position of the viewport in
 * the registry.
 *
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Lauro Didier Lins
 * @version 1.0
 */
public class PersistentScrollPane extends JScrollPane {
	private boolean _isSaving = false;
	private boolean _userRoot;

	// e.g. conjug/tables/
	private String _savePath;

	// e.g. conjug/tables/
	private String _saveName;

	private ChangeListener _changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			savePosition();
		}
	};

	/**
	 * save
	 */
	public void savePosition() {
		// nothing to save
		if(_savePath == null || _saveName == null)
			return;

		// point
		Point p = this.getViewport().getViewPosition();

		//
		Preferences pref = (_userRoot ? Preferences.userRoot().node(_savePath): Preferences.systemRoot().node(_savePath));
		pref.putInt(_saveName+".x",(int)p.getX());
		pref.putInt(_saveName+".y",(int)p.getY());

		// System.out.println("saving ("+(int)p.getX()+","+(int)p.getY()+") for "+_saveName);

		// System.out.println(""+e);
		// System.out.println("position: "+getViewport().getViewPosition());
	}

	/**
	 * save
	 */
	public void readPosition() {
		// nothing to save
		if(_savePath == null || _saveName == null)
			return;

		// get preferences node
		Preferences pref = (_userRoot ? Preferences.userRoot().node(_savePath): Preferences.systemRoot().node(_savePath));
		int x = pref.getInt(_saveName+".x",0);
		int y = pref.getInt(_saveName+".y",0);

		// System.out.println("reading ("+x+","+y+") for "+_saveName);

		// set new position
		this.getViewport().setViewPosition(new Point(x,y));
		this.getViewport().repaint();

		// System.out.println(""+e);
		// System.out.println("position: "+getViewport().getViewPosition());
	}

	/**
	 * setup store data.
	 */
	public void setupStoreData(String savePath, String saveName, boolean userRoot) {
		_savePath = savePath;
		_saveName = saveName;
		_userRoot = userRoot;
   }

	/**
	 * start saving...
	 */
	public void startSaving() {
		if (_isSaving)
			return;
		//System.out.println("startSaving");
		_isSaving = true;
		getViewport().addChangeListener(_changeListener);
	}

	/**
	 * stop saving...
	 */
	public void stopSaving() {
		if (!_isSaving)
			return;
		//System.out.println("stopSaving");
		_isSaving = false;
		getViewport().removeChangeListener(_changeListener);
	}

	public String getSaveName() {
		return _saveName;
}
}
