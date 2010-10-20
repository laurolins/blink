package blink;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
public class ClassHGQIBeanInfo extends SimpleBeanInfo {
	Class beanClass = ClassHGQI.class;

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor _hg = new PropertyDescriptor("_hg", beanClass, "get_hg", null);
			_hg.setValue("width", "70");
			_hg.setValue("title", "Hom.Grp.");
			_hg.setValue("alignment", "1");

			PropertyDescriptor _numElements = new PropertyDescriptor("_numElements", beanClass, "get_numElements", null);
			_numElements.setValue("width", "60");
			_numElements.setValue("title", "Elements");
			_numElements.setValue("alignment", "2");

			PropertyDescriptor _qi = new PropertyDescriptor("_qi", beanClass, "get_qi", null);
			_qi.setValue("width", "55");
			_qi.setValue("title", "Quant.Inv");
			_qi.setValue("alignment", "2");

                        PropertyDescriptor _loaded = new PropertyDescriptor("_loaded", beanClass, "is_loaded", null);
                        _loaded.setValue("width", "28");
                        _loaded.setValue("title", "Ld");
                        _loaded.setValue("alignment", "1");

                        PropertyDescriptor _monochromatic = new PropertyDescriptor("_monochromatic", beanClass, "isMonochromatic", null);
                        _monochromatic.setValue("width", "50");
                        _monochromatic.setValue("title", "Monochrom.");
                        _monochromatic.setValue("alignment", "1");

			PropertyDescriptor[] pds = new PropertyDescriptor[] { _hg, _numElements, _qi, _loaded, _monochromatic };
			return pds;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
