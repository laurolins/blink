package blink;

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class EPSCatalog {
    private static double CONVERT_MM_2_POINTS = 72.0 / 25.4;

    private double _pageWidth = 10 * 18 * CONVERT_MM_2_POINTS;
    private double _pageHeight = 5 * 52 * CONVERT_MM_2_POINTS;

    private double _cellWidth = 18 * CONVERT_MM_2_POINTS;
    private double _cellHeight = 36 * CONVERT_MM_2_POINTS;
    //private double _cellHeight = 36 * CONVERT_MM_2_POINTS;

    private GemGraph _gemGraph;

    //
    private ClassEntry _currentClass;
    private BlinkEntry _currentClassRepresentantBlink;
    private ArrayList<BlinkEntry> _currentBlinks;

    class PageManager {
        private int _numColumns;
        private int _numRows;
        private boolean _pageIsOpened;
        private int _page;
        private PrintWriter _pw;
        private int _currentColumn;
        private int _currentRow;

        private Object[][] _cellOwner;

        public PageManager() {
            _numColumns = (int)(_pageWidth/_cellWidth);
            _numRows = (int)(_pageHeight/_cellHeight);
            _pageIsOpened = false;
            _page=0;
        }
        public void setOwnerOfCurrentCell(Object o) {
            if (!_pageIsOpened)
                throw new RuntimeException("No page opened");
            _cellOwner[_currentRow][_currentColumn] = o;
        }
        public void openPage() throws IOException {
            System.out.println("Open Page "+(_page+1));
            if (_pageIsOpened) {
                this.closePage();
            }
            _page++;
            _pw = new PrintWriter(new FileWriter("log/catalog" + (_page < 10 ? "00" + _page : (_page < 100 ? "0" + _page : "" + _page)) +".eps"));
            printHeader(_pw);
            _currentColumn = 0;
            _currentRow = 0;
            _pageIsOpened = true;
            _cellOwner = new Object[_numRows][_numColumns];
        }

        private void printSeparations() {

            // vertical lines
            for (int i=0;i<_numRows;i++) {
                for (int j=0;j<_numColumns-1;j++) {
                    if (_cellOwner[i][j] != _cellOwner[i][j+1]) {
                        printLine(_pw,(j+1)*_cellWidth,i*_cellHeight,0,_cellHeight);
                    }
                }
            }

            //horizontal lines
            for (int i=0;i<_numRows-1;i++) {
                for (int j=0;j<_numColumns;j++) {
                    if (_cellOwner[i][j] != _cellOwner[i+1][j]) {
                        printLine(_pw,j*_cellWidth,(i+1)*_cellHeight,_cellWidth,0);
                    }
                }
            }

            //
            printRecangle(_pw,0,0,_numColumns*_cellWidth,_numRows*_cellHeight);

        }

        public int getCurrentColumn() throws IOException {
            if (!_pageIsOpened)
                throw new RuntimeException("No page opened");
            return _currentColumn;
        }
        public int getCurrentRow() throws IOException {
            if (!_pageIsOpened)
                throw new RuntimeException("No page opened");
            return _currentRow;
        }
        public int availableCells() {
            if (_currentColumn >= _numColumns) return 0;
            if (_currentRow >= _numRows) return 0;
            return (_numColumns*_numRows) - ((_currentRow*_numColumns)+_currentColumn);
        }
        public int availableCellsOnThisRow() {
            if (!_pageIsOpened) return 0;
            return Math.max(0,_numColumns - _currentColumn);
        }
        public void closePage() {
            if (_pageIsOpened) {
                System.out.println("Close Page "+(_page));
                printSeparations();
                printFooter(_pw);
                _pw.flush();
                _pw.close();
                _pageIsOpened = false;
                _pw = null;
            }
        }
        public void opanPageIfNoPageIsOpened() throws IOException {
            if (!_pageIsOpened)
                this.openPage();
        }

        public PrintWriter getPrintWriterOpeningPageIfNotOpened() throws IOException {
            if (!_pageIsOpened) {
                System.out.println("Get Writer...");
                this.openPage();
            }
            return _pw;
        }
        public void consumeCell() {
            _currentColumn++;
            if (_currentColumn >= _numColumns) {
                _currentColumn = 0;
                _currentRow++;
            }
            if (availableCells() == 0) {
                this.closePage();
            }
        }
        public void consumeCellsUntilNextLine() {
            _currentRow++;
            _currentColumn = 0;
            if (availableCells() == 0) {
                this.closePage();
            }
        }
    }

    private ArrayList<ClassEntry> _classes;

    public EPSCatalog(ArrayList<ClassEntry> classes) throws IOException, SQLException, ClassNotFoundException {
        _classes = classes;
        int maxBlinksShownForEachSpace = 5000;

        // open new page
        PageManager PM = new PageManager();

        //
         for (int i=0;i<_classes.size();i++) {
        // for (int i: new int[] {27,38,33,180,193}) {
        //for (int i: new int[] {513}) {
            ClassEntry C = _currentClass = _classes.get(i);
            _currentBlinks = App.getRepositorio().getBlinksByClass(C);
            _currentClassRepresentantBlink = _currentBlinks.get(0);

            if (PM.availableCellsOnThisRow() < 4) {
                System.out.println("Available cells on row < 4. break");
                PM.consumeCellsUntilNextLine();
            }

            if (PM.availableCells() < 6) {
                System.out.println("Available cells on page < 4. break");
                PM.closePage();
            }


            // open new page
            System.out.println("Generating "+C.getNumEdges()+"."+C.getOrder()+"...");

            // write space info
            printManifoldInfo(PM.getPrintWriterOpeningPageIfNotOpened(),PM.getCurrentColumn()*_cellWidth,PM.getCurrentRow()*_cellHeight,4*_cellWidth,1*_cellHeight);

            // tag and consume 4 columns as of class C
            for (int ii=0;ii<4;ii++) {
                PM.setOwnerOfCurrentCell(C);
                PM.consumeCell();
            }

            // draw gem
            /*
            GemEntry ge = M.getGemEntry();
            if (ge != null) {
                drawGem(PM.getPrintWriter(), ge.getGem(), PM.getCurrentColumn() * _cellWidth,
                        PM.getCurrentRow() * _cellHeight, _cellWidth, _cellHeight);
                PM.setOwnerOfCurrentCell(M);
                PM.consumeCell();
            }*/

            // lines per
            int kk = 2;
            // int kk = 3;

            //ArrayList<BlinkEntry> blinks = M.getEntries();
            boolean usedCell = false;
            for (int j=0;j<maxBlinksShownForEachSpace && j<_currentBlinks.size();j++) {
                BlinkEntry be = _currentBlinks.get(j);
                PM.opanPageIfNoPageIsOpened(); // warranty that there is an open page
                double xx = PM.getCurrentColumn()*_cellWidth;
                double yy = PM.getCurrentRow()*_cellHeight;
                yy += (j % kk) * _cellHeight/kk;
                printBlink(PM.getPrintWriterOpeningPageIfNotOpened(),be,xx,yy,_cellWidth*2,_cellHeight/kk);
                PM.setOwnerOfCurrentCell(C);
                usedCell = true;
                if (j % kk == kk-1) {
                    PM.consumeCell();
                    PM.setOwnerOfCurrentCell(C);
                    PM.consumeCell();
                    usedCell = false;
                }
            }
            if (usedCell) {
                PM.consumeCell();
                PM.setOwnerOfCurrentCell(C);
                PM.consumeCell();
            }
        }
        PM.closePage();
    }

    private void printHeader(PrintWriter pw) {
        int W = (int) (_pageWidth);
        int H = (int) (_pageHeight);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: -2 -2 %d %d", W+4, H+4));

        pw.println("/FSD {findfont exch scalefont def} bind def");
        pw.println("/SMS {setfont moveto show} bind def");
        pw.println("/MS {moveto show} bind def");

        int fontSizes[] = {3,4,5,6,7,8,9,10,11,12,14,16};
        for (int fs: fontSizes) {
            pw.println("/Helvetica"+fs+" "+fs+" /Helvetica FSD");
            pw.println("/Times"+fs+" "+fs+" /Times FSD");
            pw.println("/Courier"+fs+" "+fs+" /Courier FSD");
            pw.println("/Symbol"+fs+" "+fs+" /Symbol FSD");
            pw.println("/Helvetica-Oblique"+fs+" "+fs+" /Helvetica-Oblique FSD");
            pw.println("/Times-Oblique"+fs+" "+fs+" /Times-Oblique FSD");
            pw.println("/Courier-Oblique"+fs+" "+fs+" /Courier-Oblique FSD");
            pw.println("/Helvetica-Bold"+fs+" "+fs+" /Helvetica-Bold FSD");
            pw.println("/Times-Bold"+fs+" "+fs+" /Times-Bold FSD");
            pw.println("/Courier-Bold"+fs+" "+fs+" /Courier-Bold FSD");
        }

        // pw.println("0.01 setlinewidth");
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    public void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }

    public void printLine(PrintWriter pw, double x0, double y0, double w, double h) {
        { // show boundary lines
            pw.println("gsave");
            pw.println("0.25 setlinewidth");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x0, y0));
            pw.println(String.format("%.4f %.4f lineto", x0+w , y0+h));
            pw.println("0.5 setgray");
            pw.println("stroke");
            pw.println("grestore");
        } // end: show boundary lines
    }

    public void printRecangle(PrintWriter pw, double x0, double y0, double w, double h) {
        { // show boundary lines
            pw.println("gsave");
            pw.println("0.25 setlinewidth");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x0,y0));
            pw.println(String.format("%.4f %.4f lineto", x0,y0+h));
            pw.println(String.format("%.4f %.4f lineto", x0+w,y0+h));
            pw.println(String.format("%.4f %.4f lineto", x0+w,y0));
            pw.println("closepath");
            pw.println("0.5 setgray");
            pw.println("stroke");
            pw.println("grestore");
        } // end: show boundary lines
    }


    //
    QI _qiCurrentSpace;

    public void printManifoldInfo(PrintWriter pw, double x0, double y0, double w, double h) throws
            SQLException, IOException, ClassNotFoundException {

        ClassEntry C = _currentClass;

        ArrayList<BlinkEntry> blinks = _currentBlinks;

        /*
        { // show boundary lines
            pw.println("gsave");
            pw.println("0.25 setlinewidth");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x0, y0 + h));
            pw.println(String.format("%.4f %.4f lineto", x0 , y0));
            pw.println(String.format("%.4f %.4f lineto", x0 + w, y0));
            // pw.println(String.format("%.4f %.4f lineto", x0, y0 + h));
            // pw.println("closepath");
            pw.println("0.5 setgray");
            pw.println("stroke");
            pw.println("grestore");
        } // end: show boundary lines
        */

        { // space label

            // control variables
            double spaceLabelX = x0+4;
            double spaceLabelY = y0+17;
            String normalFont = "Helvetica16";
            String scriptFont = "Helvetica8";
            double ysub=2;
            double ysup=-8.5;

            boolean prime = true;
            if (C.get_status().indexOf("prime") == -1)
                prime = false;

            // show space name top left
            SubAndSuperScriptedWord label = new SubAndSuperScriptedWord(""+C.getNumEdges(),""+C.getOrder(),prime ? "" : "t");
            // SubAndSuperScriptedWord label = new SubAndSuperScriptedWord(""+C.getNumEdges(),""+C.getOrder(),"t");
            label.printEPS(pw,spaceLabelX,spaceLabelY,normalFont,scriptFont,ysub,ysup);

        } // end: space label

        { // linear index of S
            // control variables
            String normalFont = "Helvetica8";
            String scriptFont = "Helvetica7";
            double ysub=3;
            double ysup=-8.5;

            // show space name top left
            //ScriptedWord spaceLabel = new ScriptedWord();
            //spaceLabel.addWord("=S",ScriptedWord.Position.normal);
            //spaceLabel.addWord(""+M.getNumber(),ScriptedWord.Position.subscript);
            //double xx = x0+4;
            //double yy = y0+35;
            //spaceLabel.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);
        }

        { // prime status, homology group, gem
            // control variables
            double gapy = 6;
            double ysub=3;
            double ysup=-4.5;
            double xx,yy;
            String normalFont = "Helvetica8";
            String scriptFont = "Helvetica7";

            // prime status
            xx = x0 + 40;
            yy = y0 + 7;
            //ScriptedWord primeSatusWord = new ScriptedWord(C.get_status());
            //primeSatusWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // homology group
            ScriptedWord homologyGroupWord = new ScriptedWord();
            HomologyGroup hg = blinks.get(0).getBlink().homologyGroupFromGBlink();
            ArrayList<Integer> listhg = hg.getNumbers();
            homologyGroupWord.addWord("("+listhg.get(0)+") ",ScriptedWord.Position.normal);
            for (int i=1;i<listhg.size();i+=2) {
                int base = listhg.get(i);
                int exp = listhg.get(i+1);
                homologyGroupWord.addWord(""+base,ScriptedWord.Position.normal);
                homologyGroupWord.addWord(""+exp,ScriptedWord.Position.superscript);
            }
            xx = x0 + 40;
            yy = y0 + 18;
            homologyGroupWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // homology group
            ScriptedWord numberOfBlinksWord = new ScriptedWord(blinks.size()+" blinks");
            xx = x0 + 62;
            yy = y0 + 18;
            numberOfBlinksWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // gem number
            GemEntry ge = App.getRepositorio().getGemById(blinks.get(0).getMinGem());

            if (ge != null) {
                int handleNumber = ge.getHandleNumber();
                if (handleNumber > 0 && ge.getNumVertices() <= 30) {
                    ArrayList<GemEntry> gems = App.getRepositorio().
                                               getGemsByHashcodeAndHandleNumber(ge.getGemHashCode(), 0);
                    for (GemEntry geCandidate : gems) {
                        if (geCandidate.getGem().getCurrentLabelling().
                            equalsNotCheckingHandle(ge.getGem().getCurrentLabelling())) {
                            ge = geCandidate;
                            break;
                        }
                    }
                }

                GeneralWord gemWord = new GeneralWord(normalFont, scriptFont, ysub, ysup + 0.2);
                if (ge != null) {

                    if (ge.getCatalogNumber() > 0) {
                        gemWord.addWordElement("r", "" + ge.getCatalogNumber(), "" + ge.getNumVertices());
                        if (handleNumber > 0)
                            gemWord.addWordElement(" h" + handleNumber, "", "");
                        //" #ts:"+ge.getTSClassSize()
                    } else {
                        gemWord.addWordElement("r", "?", "" + ge.getNumVertices());
                        if (handleNumber > 0)
                            gemWord.addWordElement(" h" + handleNumber, "", "");

                        // = new ScriptedWord("R "+ge.getNumVertices()+" ? #ts:"+ge.getTSClassSize());
                    }

                    if (ge.isTSRepresentant())
                        gemWord.addWordElement("      #ts(full) " + ge.getTSClassSize(), null, null);
                    else
                        gemWord.addWordElement("      #ts(partial) " + ge.getTSClassSize(), null, null);

                } else
                    gemWord.addWordElement("null", null, null);
                xx = x0 + 32;
                yy = y0 + 35;
                gemWord.printEPS(pw, xx, yy);
            }
        } // end: space label



        { // prime status, homology group, gem
            //QI qi = blinks.get(0).getBlink().optimizedQuantumInvariant(3,12);
            QI qi = App.getRepositorio().getQI(blinks.get(0).get_qi());
            _qiCurrentSpace = qi;

            double x = x0+4;
            double y = y0+45;

            String normalFont = "Courier8";
            double xx = x;
            double yy = y;

            String symbolFont = "Symbol6";
            ScriptedWord word = new ScriptedWord(String.format("%-2s %13s %13s %6s","r","mod","theta/pi","#sts"));
            word.printEPS(pw,xx,yy,normalFont,normalFont,0,0);
            yy+=7.3;


            for (int r=3;r<=qi.get_rmax();r++) {
                QIEntry qie = qi.getEntryByR(r);
                double polar[] = qi.polarModulusAndAngleInRadians(qie.get_real(),qie.get_imaginary());
                word = new ScriptedWord(String.format("%2s %13.9f %13.9f %7d",(r<10 ? "0"+r : ""+r),polar[0]+1e-11,polar[1]/Math.PI+1e-11,qie.get_states()));
                word.printEPS(pw,xx,yy,normalFont,normalFont,0,0);
                yy+=7.3;
            }

            /*
            for (int r=3;r<=qi.get_rmax();r++) {
                QIEntry qie = qi.getEntryByR(r);
                double polar[] = qi.polarModulusAndAngleInRadians(qie.get_real(),qie.get_imaginary());
                word = new ScriptedWord(String.format("%2s %13.10f %13.10f %7d",(r<10 ? "0"+r : ""+r),polar[0],polar[1]/Math.PI,qie.get_states()));
                word.printEPS(pw,xx,yy,normalFont,normalFont,0,0);
                yy+=7.3;
            }*/

            GemEntry ge = App.getRepositorio().getGemById(C.get_gem());

            if (ge != null) {

                String code[] = {null, null, null};
                String handle = "";
                String numVert = "";
                if (ge != null) {
                    StringTokenizer st = new StringTokenizer(ge.getGem().getCurrentLabelling().getLettersString(","), ",");
                    code[0] = st.nextToken();
                    code[1] = st.nextToken();
                    code[2] = st.nextToken();
                    handle = "" + ge.getHandleNumber();
                    numVert = "" + ge.getNumVertices();
                }

                GeneralWord gemCode = new GeneralWord(normalFont, normalFont, 0, 0);
                //gemCode.printEPS(pw,xx,yy);
                yy += 1.65 * CONVERT_MM_2_POINTS;
                // xx+=5 *CONVERT_MM_2_POINTS;
                for (int ii = 0; ii < 3; ii++) {
                    gemCode.clear();
                    gemCode.addWordElement(code[ii], null, null);
                    gemCode.printEPS(pw, xx + 10 * CONVERT_MM_2_POINTS, yy - CONVERT_MM_2_POINTS);

                    gemCode.clear();
                    if (ii == 0) {
                        gemCode.addWordElement("gem:", null, null, "Helvetica8", "Helvetica8", 0, 0);
                        gemCode.printEPS(pw, xx, yy + 0.5 * CONVERT_MM_2_POINTS);
                    } else if (ii == 1) {
                        //gemCode.addWordElement("h"+handle,null,null,"Helvetica5","Helvetica5",0,0);
                    } else if (ii == 2) {
                        gemCode.addWordElement("h" + handle + " v" + numVert, null, null, "Helvetica7", "Helvetica7", 0, 0);
                        gemCode.printEPS(pw, xx + 1 * CONVERT_MM_2_POINTS, yy - 1.27 * CONVERT_MM_2_POINTS);
                    }
                    //gemCode.printEPS(pw,xx,yy);
                    //gemCode.addWordElement("h"+handle,null,null);
                    //gemCode.printEPS(pw, xx + 5 *CONVERT_MM_2_POINTS, yy);

                    yy += 7.7;
                }
            }
        } // end: space label


    }

    public void printString(PrintWriter pw, double x0, double y0, String fontName, String stringToWrite) {
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f moveto", x0, y0));
        pw.println("1 -1 scale");
        pw.println(fontName + " setfont");
        pw.println("("+stringToWrite+") show");
        pw.println("grestore");
    }

    private void printBlink(PrintWriter pw, BlinkEntry be, double x0, double y0, double ww, double hh) throws
            ClassNotFoundException, IOException, SQLException {


        GBlink b = be.getBlink();

        double topMargin = 10; // space to info
        double leftMargin = 2;
        double bottomMargin = 1; // space to info
        double rightMargin = 2;
        double w = ww - leftMargin - rightMargin;
        double h = hh - topMargin - bottomMargin;

        BlinkDrawing blinkDrawing = new BlinkDrawing(be.getBlink(),2,-1);
        LinkDrawing linkDrawing = new LinkDrawing(be.getBlink(),2,4,-1);

        blinkDrawing.drawEPS(pw,x0,y0+topMargin,w/2.0,h,0.1);
        linkDrawing.drawEPS(pw,x0+w/2.0,y0+topMargin,w/2.0,h,0.1);

        {  // write label
            double xx = x0 + 2;
            double yy = y0 + 8;
            double ysub = 2;
            double ysup = -3.5;
            String normalFont = "Helvetica6";
            String scriptFont = "Helvetica5";

            GeneralWord gw = new GeneralWord(normalFont, scriptFont, ysub, ysup);
            //gw.addWordElement("U", "" + be.getCatalogNumber(), "" + be.get_numEdges());
            //gw.addWordElement("=U", ""+be.get_id(), null);
            gw.addWordElement("  T["+be.get_id()+"]", null, null);

//            gw.addWordElement("  z"+b.getNumberOfGZigZags(), null, null,scriptFont,scriptFont,0,0);
            gw.addWordElement("   edges: "+b.getNumberOfGEdges()+"   blocks: "+b.copy().breakMap().size(), null, null,scriptFont,scriptFont,0,0);


            QI qiThisBlink = App.getRepositorio().getQI(be.get_qi());
            if (qiThisBlink.isEqualUpToMaxR(_qiCurrentSpace)) {
                gw.addWordElement("   orient: ",null,null,scriptFont,scriptFont,0,0);
                gw.addWordElement("+",null,null);
            }
            else {
                gw.addWordElement("   orient: ",null,null,scriptFont,scriptFont,0,0);
                gw.addWordElement("-",null,null);
            }




            gw.printEPS(pw,xx,yy);

            //SubAndSuperScriptedWord word = new SubAndSuperScriptedWord("B", "" + be.getCatalogNumber(), "" + be.get_numEdges());
            //word.printEPS(pw, xx, yy, normalFont, scriptFont, ysub, ysup);
        }

        /*
        {  // write label
            double xx = x0 + 19;
            double yy = y0 + 8;
            double ysub = 2;
            double ysup = -3.5;
            String normalFont = "Helvetica6";
            String scriptFont = "Helvetica5";
            SubAndSuperScriptedWord word = new SubAndSuperScriptedWord("B", "" + be.get_id(), "");
            word.printEPS(pw, xx, yy, normalFont, scriptFont, ysub, ysup);
        }


        {  // write zigzags
            double xx = x0 + 44;
            double yy = y0 + 8;
            double ysub = 2;
            double ysup = -3.5;
            String normalFont = "Helvetica6";
            String scriptFont = "Helvetica5";
            ScriptedWord word = new ScriptedWord("z"+b.getNumberOfZigZags());
            word.printEPS(pw, xx, yy, normalFont, scriptFont, ysub, ysup);
        }*/

    }

    // --------------

    // ------------------------------------------------------
    // ---- Draw Gem part
    private double MARK_SEPARATION = 0.75; // in points
    private double HALF_MARK_LENGTH = 1.4; // in points
    private double VERTEX_RADIUS = 1.4; // in points

    // all of these are in cm
    private boolean _colors = true;
    private GemColor _specialColor = GemColor.yellow;

    HashMap<GemVertex, Point2D.Double> _map;
    private void drawGem(PrintWriter pw, Gem gem, double x0, double y0, double width, double height) {

        // the coordinates of the vertices of the gem
        // comes in points (72 points = 2.54 cm)
        _map = TuttesLayout.tutteLayout(
                gem,
                _specialColor,
                4,
                4,
                (width - 8),
                (height - 8));

        // only process these colors
        // GemColor[] colors = GemColor.getComplementColors(_specialColor);
        GemColor[] colors = GemColor.getComplementColors();

        // prepare connections
        ArrayList<Connection> connections = new ArrayList<Connection>();
        for (GemVertex v : gem.getVertices()) {
            for (GemColor c : colors) {
                GemVertex u = v.getNeighbour(c);
                Connection con = null;
                for (Connection icon : connections) {
                    if (icon.connects(u, v)) {
                        con = icon;
                        break;
                    }
                }
                if (con == null) {
                    con = new Connection(v, u);
                    connections.add(con);
                }
                con.addColor(c);
            }
        }

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate", x0, y0));

        // connection
        for (Connection con : connections) {
            for (ConnectionArc a : con.getConnectionArcs()) {
                a.printArc(pw);
            }
        }
        for (GemVertex v : gem.getVertices()) {
            Point2D.Double p = _map.get(v);
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), VERTEX_RADIUS));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");
    }

    private Point2D.Double scale(Point2D.Double p, double s) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    private Point2D.Double add(Point2D.Double p, Point2D.Double q) {
        return new Point2D.Double(p.getX() + q.getX(), p.getY() + q.getY());
    }

    private Point2D.Double perp(Point2D.Double p) {
        return new Point2D.Double( -p.getY(), p.getX());
    }

    private Point2D.Double sub(Point2D.Double p, Point2D.Double q) {
        return new Point2D.Double(p.getX() - q.getX(), p.getY() - q.getY());
    }

    private double modulus(Point2D.Double p) {
        return p.distance(0, 0);
    }

    class Connection {
        private double SEPARATION_ARCS = 7.5;
        GemVertex _u;
        GemVertex _v;
        ArrayList<GemColor> _colors = new ArrayList<GemColor>(1);
        ArrayList<ConnectionArc> _arcs = null;
        public Connection(GemVertex u, GemVertex v) {
            _u = u;
            _v = v;
        }

        public boolean connects(GemVertex uu, GemVertex vv) {
            return ((_u == uu) && (_v == vv)) || ((_u == vv) && (_v == uu));
        }

        public GemVertex getU() {
            return _u;
        }

        public GemVertex getV() {
            return _v;
        }

        public Point2D.Double getPosU() {
            return _map.get(_u);
        }

        public Point2D.Double getPosV() {
            return _map.get(_v);
        }

        public void addColor(GemColor c) {
            if (!_colors.contains(c))
                _colors.add(c);
        }

        public void prepareConnectionArcs() {
            if (_arcs == null) {
                _arcs = new ArrayList<ConnectionArc>(1);
                Point2D.Double p0 = this.getPosU();
                Point2D.Double p3 = this.getPosV();
                if (_colors.size() == 0) {
                    throw new RuntimeException();
                } else if (_colors.size() == 1) {
                    _arcs.add(new ConnectionArc(this, _colors.get(0)));
                } else {
                    Point2D.Double p0p3 = sub(p3, p0);
                    p0p3 = scale(p0p3, 1.0 / modulus(p0p3));
                    Point2D.Double p0p3perp = perp(p0p3);

                    int arcs = _colors.size();
                    double length = (arcs - 1) * SEPARATION_ARCS;

                    Point2D.Double p10 = add(p0, scale(p0p3perp, -length / 2));
                    Point2D.Double p20 = add(p3, scale(p0p3perp, -length / 2));

                    for (int i = 0; i < arcs; i++) {
                        Point2D.Double p1i = add(p10, scale(p0p3perp, i * SEPARATION_ARCS));
                        Point2D.Double p2i = add(p20, scale(p0p3perp, i * SEPARATION_ARCS));
                        _arcs.add(new ConnectionArc(this, _colors.get(i), p1i, p2i));
                    }
                }
            }
        }

        public int getNumberOfConnectionArcs() {
            this.prepareConnectionArcs();
            return _arcs.size();
        }

        public ConnectionArc getConnectionArc(int index) {
            this.prepareConnectionArcs();
            return _arcs.get(index);
        }

        public ArrayList<ConnectionArc> getConnectionArcs() {
            this.prepareConnectionArcs();
            return (ArrayList<ConnectionArc>) _arcs.clone();
        }
    }


    class ConnectionArc {
        Connection _connection;
        private Point2D.Double _p1;
        private Point2D.Double _p2;
        GemColor _color;
        public ConnectionArc(Connection connection, GemColor color) {
            this(connection, color, null, null);
        }

        public ConnectionArc(Connection connection, GemColor color, Point2D.Double p1, Point2D.Double p2) {
            _connection = connection;
            _color = color;
            _p1 = p1;
            _p2 = p2; // bezier control points
            prepareGeometry();
        }

        public boolean isLinear() {
            return _p1 == null;
        }

        public double length(double t0, double t1, int segments) {
            double segLength = (t1 - t0) / segments;
            double result = 0;
            for (int i = 0; i < segments; i++) {
                Point2D.Double pi = getCurvePoint(t0 + i * segLength);
                Point2D.Double pim1 = getCurvePoint(t0 + (i + 1) * segLength);
                result += pi.distance(pim1);
            }
            return result;
        }

        public double binarySearch(double t0, double distance, int segments) {
            double l, r;
            if (distance < 0) {
                l = 0;
                r = t0;
                while ( -distance > length(t0, l, segments))
                    l = (l - 1) * 10;
            } else if (distance > 0) {
                l = t0;
                r = 1;
                while (distance > length(r, t0, segments))
                    r = 10 * r;
            } else
                return t0;

            double absDistance = Math.abs(distance);
            double tResult = 0;
            while (true) {
                double m = (l + r) / 2.0;
                double currDistance = length(m, t0, segments);
                if (Math.abs(currDistance - absDistance) < 1.0e-4) {
                    tResult = m;
                    break;
                }
                if (distance > 0) {
                    if (currDistance < absDistance)
                        l = m;
                    else
                        r = m;
                } else {
                    if (currDistance < absDistance)
                        r = m;
                    else
                        l = m;
                }
            }
            return tResult;
        }


        private Point2D.Double p[];
        private double x[];
        private double y[];
        private double ax, ay, bx, by, cx, cy;
        private void prepareGeometry() {
            // prepare vectors
            p = new Point2D.Double[] {_connection.getPosU(), _p1, _p2, _connection.getPosV()};
            if (!this.isLinear()) {
                x = new double[] {p[0].getX(), p[1].getX(), p[2].getX(), p[3].getX()};
                y = new double[] {p[0].getY(), p[1].getY(), p[2].getY(), p[3].getY()};
                cx = 3 * x[1] - 3 * x[0];
                cy = 3 * y[1] - 3 * y[0];
                bx = 3 * x[2] - 6 * x[1] + 3 * x[0];
                by = 3 * y[2] - 6 * y[1] + 3 * y[0];
                ax = x[3] - x[0] + 3 * x[1] - 3 * x[2];
                ay = y[3] - y[0] + 3 * y[1] - 3 * y[2];
            } else {
                x = new double[] {p[0].getX(), p[0].getX(), p[0].getX(), p[3].getX()};
                y = new double[] {p[0].getY(), p[0].getX(), p[0].getX(), p[3].getY()};
                ax = ay = bx = by = 0;
                cx = x[3] - x[0];
                cy = y[3] - y[0];
            }
        }

        public Point2D.Double getCurvePoint(double t) {
            return new Point2D.Double(ax * t * t * t + bx * t * t + cx * t + x[0],
                                      ay * t * t * t + by * t * t + cy * t + y[0]);
        }

        public Point2D.Double getCurveTangent(double t) {
            return new Point2D.Double(3 * ax * t * t + 2 * bx * t + cx, 3 * ay * t * t + 2 * by * t + cy);
        }

        public void printArc(PrintWriter pw) {

            // setup color
            if (_colors) {
                if (_color == GemColor.green)
                    pw.println("0 180 0 setrgbcolor");
                else if (_color == GemColor.red)
                    pw.println("180 0 0 setrgbcolor");
                else if (_color == GemColor.blue)
                    pw.println("0 0 180 setrgbcolor");
                else if (_color == GemColor.yellow)
                    pw.println("180 180 0 setrgbcolor");
            } else {
                pw.println("0 0 0 setrgbcolor");
            }

            // draw the arc segment
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x[0], y[0]));
            if (isLinear()) {
                pw.println(String.format("%.4f %.4f lineto", x[3], y[3]));
            } else {
                pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto", x[1], y[1], x[2], y[2], x[3], y[3]));
            }
            pw.println("stroke");

            // if no color, put the marks
            if (!_colors) {
                int marks = _color.getNumber();
                double segLength = (marks - 1) * MARK_SEPARATION;
                for (int i = 0; i < marks; i++) {
                    double distance = -segLength / 2.0 + i * MARK_SEPARATION;
                    double t = binarySearch(0.5, distance, 10);
                    System.out.println("distance " + length(0.5, t, 10) + " should be " + distance);
                    Point2D.Double pi = this.getCurvePoint(t);
                    Point2D.Double ti = this.getCurveTangent(t);
                    ti = scale(ti, 1 / modulus(ti));
                    ti = perp(ti);

                    Point2D.Double pia = add(pi, scale(ti, HALF_MARK_LENGTH));
                    Point2D.Double pib = add(pi, scale(ti, -HALF_MARK_LENGTH));

                    pw.println("newpath");
                    pw.println(String.format("%.4f %.4f moveto", pia.getX(), pia.getY()));
                    pw.println(String.format("%.4f %.4f lineto", pib.getX(), pib.getY()));
                    pw.println("stroke");
                }
            }
        }
    }


    // -------------
}
