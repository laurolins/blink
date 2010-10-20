package blink;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GemEntryBeanInfo extends SimpleBeanInfo {
    Class beanClass = GemEntry.class;

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor _id = new PropertyDescriptor("_id", beanClass, "getId", null);
            _id.setValue("width", "55");
            _id.setValue("title", "ID");
            _id.setValue("alignment", "1");

            PropertyDescriptor _numVertices = new PropertyDescriptor("_numVertices", beanClass, "getNumVertices", null);
            _numVertices.setValue("width", "24");
            _numVertices.setValue("title", "#V");
            _numVertices.setValue("alignment", "1");

            PropertyDescriptor _catalogNumber = new PropertyDescriptor("_catalogNumber", beanClass, "getCatalogNumber", null);
            _catalogNumber.setValue("width", "24");
            _catalogNumber.setValue("title", "Cat.");
            _catalogNumber.setValue("alignment", "1");

            PropertyDescriptor _handleNumber = new PropertyDescriptor("_handleNumber", beanClass, "getHandleNumber", null);
            _handleNumber.setValue("width", "30");
            _handleNumber.setValue("title", "Hnd.");
            _handleNumber.setValue("alignment", "1");

            PropertyDescriptor _tsClassSize = new PropertyDescriptor("_tsClassSize", beanClass, "getTSClassSize", null);
            _tsClassSize.setValue("width", "35");
            _tsClassSize.setValue("title", "Mut.");
            _tsClassSize.setValue("alignment", "1");

            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                                       _id,
                                       _numVertices,
                                       _tsClassSize,
                                       _handleNumber,
                                       _catalogNumber};

            return pds;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
