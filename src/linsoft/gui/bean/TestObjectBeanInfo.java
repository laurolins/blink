package linsoft.gui.bean;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
public class TestObjectBeanInfo extends SimpleBeanInfo {
	Class beanClass = TestObject.class;

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor _boolean = new PropertyDescriptor("_boolean", beanClass, "is_boolean", "set_boolean");

			PropertyDescriptor _date = new PropertyDescriptor("_date", beanClass, "get_date", "set_date");

			PropertyDescriptor _double = new PropertyDescriptor("_double", beanClass, "get_double", "set_double");

			PropertyDescriptor _float = new PropertyDescriptor("_float", beanClass, "get_float", "set_float");

			PropertyDescriptor _int = new PropertyDescriptor("_int", beanClass, "get_int", "set_int");

			PropertyDescriptor _long = new PropertyDescriptor("_long", beanClass, "get_long", "set_long");

			PropertyDescriptor _short = new PropertyDescriptor("_short", beanClass, "get_short", "set_short");

			PropertyDescriptor _string = new PropertyDescriptor("_string", beanClass, "get_string", "set_string");

			PropertyDescriptor[] pds = new PropertyDescriptor[] { _boolean, _date, _double, _float, _int, _long, _short, _string };
			return pds;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}