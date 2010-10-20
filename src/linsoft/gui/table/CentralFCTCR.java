package linsoft.gui.table;

/**
 * FCFCTR = Fornecedor de Configurable Table Cell Renderer
 */
public class CentralFCTCR {
    private static FornecedorCTCR _fornecedorCTCR = new FornecedorCTCRPadrao();

    public static FornecedorCTCR getFornecedorCTCR() {
        return _fornecedorCTCR;
    }

    public static void setFornecedorCTCR(FornecedorCTCR fornecedorCTCR) {
        _fornecedorCTCR = fornecedorCTCR;
    }
}