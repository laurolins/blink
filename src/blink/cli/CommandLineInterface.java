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
                             new FunctionAdjacentOppositeCurls(),
                             new FunctionAlpha1(),
                             new FunctionApply(),
                             new FunctionAttractor(),
                             new FunctionBigon(),
                             new FunctionBlocks(),
                             new FunctionBreakGemOnAnyDisconnectingQuartet(),
                             new FunctionCancelDipoles(),
                             new FunctionChangeDB(),
                             new FunctionClassHGNormQI(),
                             new FunctionClear(),
                             new FunctionCode(),
                             new FunctionColorGBlinks(),
                             new FunctionCombine2(),
                             new FunctionCombineGBlinks(),
                             new FunctionCompare(),
                             new FunctionComposeQI(),
                             new FunctionConcatenate(),
                             new FunctionCountSpaces(),
                             new FunctionDBStatus(),
                             new FunctionDeleteSpaces(),
                             new FunctionDetail(),
                             new FunctionDiffGBlinks(),
                             new FunctionDraw(),
                             new FunctionDrawBigon(),
                             new FunctionDrawBlink(),
                             new FunctionDrawCP(),
                             new FunctionDrawEmbeddedGraph(),
                             new FunctionDrawGBlink(),
                             new FunctionDrawGBlinks(),
                             new FunctionDrawLink(),
                             new FunctionDual(),
                             new FunctionDualLabel(),
                             new FunctionExport(),
                             new FunctionExportAttractor(),
                             new FunctionExportGBlinks(),
                             new FunctionFilterGBlinksUsingRM3(),
                             new FunctionFourCluster(),
                             new FunctionGBlink(),
                             new FunctionGamma(),
                             new FunctionGem(),
                             new FunctionGemRCatalogNumber(),
                             new FunctionGemWithoutFourCluster(),
                             new FunctionGemsThatShouldBeConnected(),
                             new FunctionGenerate3Con(),
                             new FunctionGenerateBlocks(),
                             new FunctionGist(),
                             new FunctionHelp(),
                             new FunctionHomologyGroup(),
                             new FunctionID(),
                             new FunctionIdentifyAdjacentOppositeCurls(),
                             new FunctionIdentifyBlinksThatAreNotRepresentant(),
                             new FunctionIdentifySwaps(),
                             new FunctionImportAttractors(),
                             new FunctionInducedGraph(),
                             new FunctionLength(),
                             new FunctionLinkGraphInGMLFormat(),
                             new FunctionListData(),
                             new FunctionListFunctions(),
                             new FunctionLoadGBlinks(),
                             new FunctionLoadGraph(),
                             new FunctionLogHGNormQIClasses(),
                             new FunctionMapOfEmbeddedGrap(),
                             new FunctionMatveevCode(),
                             new FunctionMatveevReport(),
                             new FunctionMergeGBlinks(),
                             new FunctionNetSimplex(),
                             new FunctionPackGBlinkIds(),
                             new FunctionPrime(),
                             new FunctionPrint(),
                             new FunctionProduceEPSCatalog(),
                             new FunctionProtect(),
                             new FunctionQI(),
                             new FunctionRSpace(),
                             new FunctionRefDual(),
                             new FunctionRefDualLabel(),
                             new FunctionReflection(),
                             new FunctionReflectionLabel(),
                             new FunctionReidemeisterII(),
                             new FunctionReidemeisterIII(),
                             new FunctionRepresentantGBlink(),
                             new FunctionRepresentantNOGBlink(),
                             new FunctionRhoPair(),
                             new FunctionSaveGraph(),
                             new FunctionSaveSpaces(),
                             new FunctionSearchPath(),
                             new FunctionSet(),
                             new FunctionSpace(),
                             new FunctionSpacesRepNOCardinality(),
                             new FunctionStopThread(),
                             new FunctionSwap(),
                             new FunctionTamassia(),
                             new FunctionTestPaths(),
                             new FunctionTestPrimality(),
                             new FunctionTestTamassia(),
                             new FunctionTryConnectingGems(),
                             new FunctionTwistor(),
                             new FunctionUpdateMinGem(),
                             new FunctionWhatSpace(),
                             new FunctionGenerateRandomGem(),
                             new FunctionGemRes(),
                             new FunctionGenLnk()
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
     * @TODO The script "[]" does not throw an error. It should.
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
