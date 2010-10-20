package blink;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GemPathEntryBeanInfo extends SimpleBeanInfo {
    Class beanClass = GemPathEntry.class;

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor id = new PropertyDescriptor("id", beanClass, "getId", null);
            id.setValue("width", "55");
            id.setValue("title", "ID");
            id.setValue("alignment", "1");

            PropertyDescriptor source = new PropertyDescriptor("source", beanClass, "getSource", null);
            source.setValue("width", "24");
            source.setValue("title", "From");
            source.setValue("alignment", "1");

            PropertyDescriptor target = new PropertyDescriptor("target", beanClass, "getTarget", null);
            target.setValue("width", "24");
            target.setValue("title", "To");
            target.setValue("alignment", "1");

            PropertyDescriptor pathLength = new PropertyDescriptor("pathLength", beanClass, "getPathLength", null);
            pathLength.setValue("width", "24");
            pathLength.setValue("title", "Len");
            pathLength.setValue("alignment", "1");

            PropertyDescriptor[] pds = new PropertyDescriptor[] {id,source,target,pathLength};

            return pds;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
