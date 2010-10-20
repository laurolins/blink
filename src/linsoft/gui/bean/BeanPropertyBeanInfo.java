package linsoft.gui.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BeanPropertyBeanInfo extends SimpleBeanInfo {
	Class beanClass = BeanProperty.class;
	String iconColor16x16Filename;
	String iconColor32x32Filename;
	String iconMono16x16Filename;
	String iconMono32x32Filename;

	public BeanPropertyBeanInfo() {
	}
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor _descricao = new PropertyDescriptor("descricao", beanClass, "getDescricao", null);
			PropertyDescriptor _title = new PropertyDescriptor("title", beanClass, "getTitle", null);
            PropertyDescriptor _name = new PropertyDescriptor("name", beanClass, "getName", null);

            // _name
            _name.setValue("name","Nome");
            _name.setValue("width","80");
            _name.setValue("alignment","0"); // right = 2
            _name.setValue("font","Tahoma,bold,11"); // right = 2

			// _name
			_title.setValue("title","Título");
			_title.setValue("width","150");
			_title.setValue("alignment","0"); // right = 2
			_title.setValue("font","Tahoma,plain,11"); // right = 2

			// _descriçao
			_descricao.setValue("title","Descrição");
			_descricao.setValue("width","290");
			_descricao.setValue("alignment","0"); // right = 2
            _descricao.setValue("font","Tahoma,plain,11"); // right = 2

			PropertyDescriptor[] pds = new PropertyDescriptor[] {
                _name,
				_descricao,
				_title};
			return pds;


		}
		catch(IntrospectionException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public java.awt.Image getIcon(int iconKind) {
		switch (iconKind) {
		case BeanInfo.ICON_COLOR_16x16:
			  return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
		case BeanInfo.ICON_COLOR_32x32:
			  return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
		case BeanInfo.ICON_MONO_16x16:
			  return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
		case BeanInfo.ICON_MONO_32x32:
			  return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
								}
		return null;
	}
}
