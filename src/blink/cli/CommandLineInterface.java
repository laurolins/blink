package blink.cli;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class CommandLineInterface {
    public static final CommandLineInterface _singleton = new CommandLineInterface();
    public static CommandLineInterface getInstance() {
        return _singleton;
    }

    private Console _console;
    private DataMap _dataMap;
    private FunctionMap _functionMap;

    CommandLineInterfaceParser _parser;

    public String _currentCommand;

    private CommandLineInterface() {
        _console = new Console();
        _dataMap = new DataMap();
        _functionMap = new FunctionMap();

        Function functions[] = {
                             new FunctionGBlink(),
                             new FunctionQI(),
                             new FunctionListFunctions(),
                             new FunctionListData(),
                             new FunctionHomologyGroup(),
                             new FunctionMergeGBlinks(),
                             new FunctionDual(),
                             new FunctionDraw(),
                             new FunctionDualLabel(),
                             new FunctionRepresentantGBlink(),
                             new FunctionGem(),
                             new FunctionReidemeisterII(),
                             new FunctionReidemeisterIII(),
                             new FunctionDrawCP(),
                             new FunctionHelp(),
                             new FunctionTwistor(),
                             new FunctionAttractor(),
                             new FunctionCompare(),
                             new FunctionReflection(),
                             new FunctionReflectionLabel(),
                             new FunctionRefDual(),
                             new FunctionRefDualLabel(),
                             new FunctionDBStatus(),
                             new FunctionChangeDB(),
                             new FunctionLogHGNormQIClasses(),
                             new FunctionPrint(),
                             new FunctionSwap(),
                             new FunctionExport(),
                             new FunctionLoadGBlinks(),
                             new FunctionDiffGBlinks(),
                             new FunctionPrime(),
                             new FunctionCancelDipoles(),
                             new FunctionClassHGNormQI(),
                             new FunctionCountSpaces(),
                             new FunctionImportAttractors(),
                             new FunctionTestPaths(),
                             new FunctionTestPrimality(),
                             new FunctionSaveSpaces(),
                             new FunctionDeleteSpaces(),
                             new FunctionLength(),
                             new FunctionConcatenate(),
                             new FunctionSpace(),
                             new FunctionDrawGBlinks(),
                             new FunctionDetail(),
                             new FunctionBlocks(),
                             new FunctionIdentifySwaps(),
                             new FunctionPackGBlinkIds(),
                             new FunctionExportAttractor(),
                             new FunctionUpdateMinGem(),
                             new FunctionProtect(),
                             new FunctionProduceEPSCatalog(),
                             new FunctionFourCluster(),
                             new FunctionGemWithoutFourCluster(),
                             new FunctionRhoPair(),
                             new FunctionAdjacentOppositeCurls(),
                             new FunctionIdentifyAdjacentOppositeCurls(),
                             new FunctionIdentifyBlinksThatAreNotRepresentant(),
                             new FunctionGenerateBlocks(),
                             new FunctionCombineGBlinks(),
                             new FunctionColorGBlinks(),
                             new FunctionExportGBlinks(),
                             new FunctionFilterGBlinksUsingRM3(),
                             new FunctionCombine2(),
                             new FunctionGemRCatalogNumber(),
                             new FunctionAlpha1(),
                             new FunctionSet(),
                             new FunctionGist(),
                             new FunctionTryConnectingGems(),
                             new FunctionStopThread(),
                             new FunctionLinkGraphInGMLFormat(),
                             new FunctionNetSimplex(),
                             new FunctionTamassia(),
                             new FunctionDrawLink(),
                             new FunctionDrawBlink(),
                             new FunctionTestTamassia(),
                             new FunctionInducedGraph(),
                             new FunctionSearchPath(),
                             new FunctionGemsThatShouldBeConnected(),
                             new FunctionClear(),
                             new FunctionWhatSpace(),
                             new FunctionRepresentantNOGBlink(),
                             new FunctionDrawGBlink(),
                             new FunctionID(),
                             new FunctionBreakGemOnAnyDisconnectingQuartet(),
                             new FunctionRSpace(),
                             new FunctionComposeQI(),
                             new FunctionGenerate3Con(),
                             new FunctionCode(),
                             new FunctionSpacesRepNOCardinality(),
                             new FunctionDrawEmbeddedGraph(),
                             new FunctionMapOfEmbeddedGrap(),
                             new FunctionGamma(),
                             new FunctionApply(),
                             new FunctionBigon(),
                             new FunctionSaveGraph(),
                             new FunctionLoadGraph(),
                             new FunctionDrawBigon(),
                             new FunctionMatveevCode(),
                             new FunctionMatveevReport()
        };
        for (Function f : functions) {
            _functionMap.addFunction(f.getName(), f);
        }

        ByteArrayInputStream is = new java.io.ByteArrayInputStream("".getBytes());
        _parser = new CommandLineInterfaceParser(is);
    }

    public Console getConsole() {
        return _console;
    }

    public void outputText(String text) {
        _console.write(text);
    }

    public DataMap getDataMap() {
        return _dataMap;
    }

    public void clear() {
        _console.clear();
    }

    public FunctionMap getFunctionMap() {
        return _functionMap;
    }

    public void outputGraphics(JComponent c) {
        // _console.write(text);
    }

    /**
     * @todo The script "[]" does not throw an error. It should.
     * @param script String
     */
    public void execute(String script) {
        ByteArrayInputStream is = new java.io.ByteArrayInputStream(script.getBytes());
        try {
            _parser.ReInit(is);
            NodeScript nodeScript = _parser.Script();
            _currentCommand = script;
            this.execute(nodeScript);
        } catch (ParseException ex) {
            _console.write(ex.getMessage());
        }
    }

    public void execute(Node script) {
        try {
            ArrayList<Object> list = (ArrayList<Object>)script.evaluate();
            for (Object o: list)
                if (o != null)
                    _console.write("\n" + o);
            _console.prompt();
        } catch (EvaluationException ex) {
            _console.write(ex.getMessage());
            _console.prompt();
        }
    }

    public Object evaluateNodeScript(NodeScript nodeScript) throws EvaluationException {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Node n: nodeScript.getChilds())
            result.add(n.evaluate());
        return result;
    }

    public Object evaluateNodeFunction(NodeFunction functionNode) throws EvaluationException {
        Function f = _functionMap.getFunction(functionNode.getIdentifier());
        if (f == null) {
            throw new EvaluationException("Don't know function: " + functionNode.getIdentifier());
        }
        ArrayList<Object> params = new ArrayList<Object>();
        DataMap localDataMap = null;
        for (Node n: functionNode.getChilds()) {
            if (n instanceof NodeLocalAssignment) {
                if (localDataMap == null)
                    localDataMap = new DataMap();
                NodeLocalAssignment nla = (NodeLocalAssignment) n;
                String id = nla.getIdentifier();
                Object value = nla.valueNode().evaluate();
                localDataMap.addData(id,value);
            }
            else {
                params.add(n.evaluate());
            }
        }
        Object obj = f.evaluate(params,localDataMap);
        if (functionNode.getIndex() != null) {
            NodeIndex index = functionNode.getIndex();
            List list = ((List) index.evaluate());
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            for (Object i: list) {
                if (i instanceof Number) {
                    indexes.add(((Number) i).intValue()-1);
                }
                else if (i instanceof List) {
                    for (Object ii: (List)i) {
                        indexes.add(((Number) ii).intValue()-1);
                    }
                }
                else throw new EvaluationException("Not valid index");
            }
            if (indexes.size() == 1) {
                obj = ((List)obj).get(indexes.get(0));
            }
            else {
                ArrayList result = new ArrayList();
                for (int i: indexes) {
                    result.add(((List)obj).get(i));
                }
                obj =  result;
            }
        }
        return obj;
    }

    public Object evaluateNodeList(NodeList listNode) throws EvaluationException {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Node n: listNode.getChilds()) {
            result.add(n.evaluate());
        }
        return result;
    }

    public Object evaluateNodeIndex(NodeIndex listIndex) throws EvaluationException {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Node n: listIndex.getChilds()) {
            result.add(n.evaluate());
        }
        return result;
    }

    public Object evaluateNodeAssignment(NodeAssignment nodeAssignment) throws EvaluationException {
        Object o = nodeAssignment.valueNode().evaluate();
        _dataMap.addData(nodeAssignment.getIdentifier(),o);
        return null;
    }

    public Object evaluateNodeVariable(NodeVariable nodeVariable) throws EvaluationException {
        Object obj = _dataMap.getData(nodeVariable.getIdentifier());
        if (nodeVariable.getIndex() != null) {
            NodeIndex index = nodeVariable.getIndex();
            List list = ((List) index.evaluate());
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            for (Object i: list) {
                if (i instanceof Number) {
                    indexes.add(((Number) i).intValue()-1);
                }
                else if (i instanceof List) {
                    for (Object ii: (List)i) {
                        indexes.add(((Number) ii).intValue()-1);
                    }
                }
                else throw new EvaluationException("Not valid index");
            }
            if (indexes.size() == 1) {
                obj = ((List)obj).get(indexes.get(0));
            }
            else {
                ArrayList result = new ArrayList();
                for (int i: indexes) {
                    result.add(((List)obj).get(i));
                }
                obj =  result;
            }
        }
        return obj;
    }

    public Object evaluateNodeObject(NodeObject nodeObject) throws EvaluationException {
        return nodeObject.getObject();
    }

    public static void main(String[] args) {
        CommandLineInterface cli = CommandLineInterface.getInstance();
        Console c = cli.getConsole();
        linsoft.gui.util.Library.resizeAndCenterWindow(c,800,600);
        c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.setVisible(true);
    }
}
