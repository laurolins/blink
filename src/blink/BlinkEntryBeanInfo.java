package blink;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
public class BlinkEntryBeanInfo extends SimpleBeanInfo {
	Class beanClass = BlinkEntry.class;

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor _colors = new PropertyDescriptor("_colors", beanClass, "get_colors", null);
			_colors.setValue("width", "null");
			_colors.setValue("title", "Cores");

			PropertyDescriptor _comment = new PropertyDescriptor("_comment", beanClass, "get_comment", "set_comment");

			PropertyDescriptor _hg = new PropertyDescriptor("_hg", beanClass, "get_hg", "set_hg");
			_hg.setValue("width", "75");
			_hg.setValue("title", "HG");
			_hg.setValue("alignment", "0");

			PropertyDescriptor _id = new PropertyDescriptor("_id", beanClass, "get_id", "set_id");
			_id.setValue("width", "55");
			_id.setValue("title", "ID");
			_id.setValue("alignment", "1");

			PropertyDescriptor _mapCode = new PropertyDescriptor("_mapCode", beanClass, "get_mapCode", null);
			_mapCode.setValue("width", "100");
			_mapCode.setValue("title", "Code");
			_mapCode.setValue("alignment", "0");

			PropertyDescriptor _numEdges = new PropertyDescriptor("_numEdges", beanClass, "get_numEdges", null);
			_numEdges.setValue("width", "24");
			_numEdges.setValue("title", "#E");
			_numEdges.setValue("alignment", "1");

			PropertyDescriptor _qi = new PropertyDescriptor("_qi", beanClass, "get_qi", null);
			_qi.setValue("width", "50");
			_qi.setValue("title", "QI");
			_qi.setValue("alignment", "1");

                        PropertyDescriptor _gem = new PropertyDescriptor("_gem", beanClass, "get_gem", null);
                        _gem.setValue("width", "50");
                        _gem.setValue("title", "Gem");
                        _gem.setValue("alignment", "1");

                        PropertyDescriptor _mingem = new PropertyDescriptor("_mingem", beanClass, "getMinGem", null);
                        _mingem.setValue("width", "50");
                        _mingem.setValue("title", "MinGem");
                        _mingem.setValue("alignment", "1");

			PropertyDescriptor[] pds = new PropertyDescriptor[] { _colors, _comment, _hg, _id, _mapCode, _numEdges, _qi, _gem, _mingem };
			return pds;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
