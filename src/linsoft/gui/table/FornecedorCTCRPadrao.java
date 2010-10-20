package linsoft.gui.table;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FornecedorCTCRPadrao
    implements FornecedorCTCR {

    public FornecedorCTCRPadrao() {
    }

    /**
     * Create Configurable Cell Renderer from Property Descriptor
     * values.
     */
    public ConfigurableCellRendererDefault getCTCR(Class objectClass, String rendererKey) {
        /**@todo Implement this linsoft.gui.table.FornecedorCTCR method*/
        ConfigurableCellRendererDefault renderer;

        if (objectClass == Boolean.class) {
            renderer = new ConfigurableCellRendererBoolean();
        }

        else {
            ConfigurableCellRendererText textRenderer = new
                ConfigurableCellRendererText();
            renderer = textRenderer;
        }

        return renderer;
    }

}