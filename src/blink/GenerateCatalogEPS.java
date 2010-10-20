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
public class GenerateCatalogEPS {
    private static double CONVERT_MM_2_POINTS = 72.0 / 25.4;

    private double _pageWidth = 10 * 18 * CONVERT_MM_2_POINTS;
    private double _pageHeight = 5 * 52 * CONVERT_MM_2_POINTS;

    private double _cellWidth = 18 * CONVERT_MM_2_POINTS;
    private double _cellHeight = 52 * CONVERT_MM_2_POINTS;

    private ManifoldCatalog _catalog;
    private GemGraph _gemGraph;


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


    public GenerateCatalogEPS() throws IOException, SQLException, ClassNotFoundException {

        _catalog = new ManifoldCatalog();

        int maxBlinksShownForEachSpace = 18;

        // open new page
        PageManager PM = new PageManager();

        //
         for (int i=1;i<=_catalog.getNumberOfManifolds();i++) {
        // for (int i: new int[] {27,38,33,180,193}) {
        //for (int i: new int[] {513}) {
            Manifold M = _catalog.getManifold(i);

            if (!(M.getGemId() == 1365 ||M.getGemId() == 1823 ||M.getGemId() == 840 ||M.getGemId() == 68235))
                continue;


            //if (i > 41) break;
            //if (M.blinkComplexity() > 5)
            //    break;
            /*
            if (M.getGemPrimeStatus() != GemPrimeStatus.POTENTIALLY_COMPOSITE &&
                M.getGemPrimeStatus() != GemPrimeStatus.COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP &&
                M.getGemPrimeStatus() != GemPrimeStatus.COMPOSITE_FROM_HANDLE) {
                continue;
            }*/

            if (PM.availableCellsOnThisRow() < 4) {
                System.out.println("Available cells on row < 3. break");
                PM.consumeCellsUntilNextLine();
            }

            if (PM.availableCells() < 5) {
                System.out.println("Available cells on page < 4. break");
                PM.closePage();
            }

            // open new page
            System.out.println("Generating "+M.getNumber()+"...");
            printManifoldInfo(M,PM.getPrintWriterOpeningPageIfNotOpened(),PM.getCurrentColumn()*_cellWidth,PM.getCurrentRow()*_cellHeight,4*_cellWidth,1*_cellHeight);
            PM.setOwnerOfCurrentCell(M);
            PM.consumeCell();
            PM.setOwnerOfCurrentCell(M);
            PM.consumeCell();
            PM.setOwnerOfCurrentCell(M);
            PM.consumeCell();
            PM.setOwnerOfCurrentCell(M);
            PM.consumeCell();

            // draw gem
            /*
            GemEntry ge = M.getGemEntry();
            if (ge != null) {
                drawGem(PM.getPrintWriter(), ge.getGem(), PM.getCurrentColumn() * _cellWidth,
                        PM.getCurrentRow() * _cellHeight, _cellWidth, _cellHeight);
                PM.setOwnerOfCurrentCell(M);
                PM.consumeCell();
            }*/


            int kk = 3;

            ArrayList<BlinkEntry> blinks = M.getEntriesOnTheSameOrientationAsTheMinimum();
            //ArrayList<BlinkEntry> blinks = M.getEntries();
            boolean usedCell = false;
            for (int j=0;j<maxBlinksShownForEachSpace && j<blinks.size();j++) {
                BlinkEntry be = blinks.get(j);
                PM.opanPageIfNoPageIsOpened(); // warranty that there is an open page
                double xx = PM.getCurrentColumn()*_cellWidth;
                double yy = PM.getCurrentRow()*_cellHeight;
                yy += (j % kk) * _cellHeight/kk;
                printBlink(PM.getPrintWriterOpeningPageIfNotOpened(),be,xx,yy,_cellWidth,_cellHeight/kk);
                PM.setOwnerOfCurrentCell(M);
                usedCell = true;
                if (j % kk == kk-1) {
                    PM.consumeCell();
                    usedCell = false;
                }
            }
            if (usedCell)
                PM.consumeCell();
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


    public void printManifoldInfo(Manifold M, PrintWriter pw, double x0, double y0, double w, double h) throws
            SQLException, IOException, ClassNotFoundException {

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
            int spaceNumber = M.getNumber();
            double ysub=2;
            double ysup=-8.5;

            // show space name top left
            ScriptedWord spaceLabel = new ScriptedWord();
            spaceLabel.addWord("S",ScriptedWord.Position.normal);
            spaceLabel.addWord(""+spaceNumber,ScriptedWord.Position.subscript);
            //spaceLabel.printEPS(pw,spaceLabelX,spaceLabelY,normalFont,scriptFont,ysub,ysup);

            SubAndSuperScriptedWord label = new SubAndSuperScriptedWord("S",""+M.getNumberOnComplexity(),""+M.blinkComplexity());
            label.printEPS(pw,spaceLabelX,spaceLabelY,normalFont,scriptFont,ysub,ysup);

        } // end: space label

        { // linear index of S
            // control variables
            String normalFont = "Helvetica8";
            String scriptFont = "Helvetica7";
            double ysub=3;
            double ysup=-8.5;

            // show space name top left
            ScriptedWord spaceLabel = new ScriptedWord();
            spaceLabel.addWord("=S",ScriptedWord.Position.normal);
            spaceLabel.addWord(""+M.getNumber(),ScriptedWord.Position.subscript);
            double xx = x0+4;
            double yy = y0+35;
            spaceLabel.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);
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
            xx = x0 + 32;
            yy = y0 + 7;
            ScriptedWord primeSatusWord = new ScriptedWord(M.getGemPrimeStatus().getSmallDescription());
            primeSatusWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // homology group
            ScriptedWord homologyGroupWord = new ScriptedWord();
            HomologyGroup hg = M.getHomologuGroup();
            ArrayList<Integer> listhg = hg.getNumbers();
            homologyGroupWord.addWord("("+listhg.get(0)+") ",ScriptedWord.Position.normal);
            for (int i=1;i<listhg.size();i+=2) {
                int base = listhg.get(i);
                int exp = listhg.get(i+1);
                homologyGroupWord.addWord(""+base,ScriptedWord.Position.normal);
                homologyGroupWord.addWord(""+exp,ScriptedWord.Position.superscript);
            }
            xx = x0 + 32;
            yy = y0 + 18;
            homologyGroupWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // homology group
            ScriptedWord numberOfBlinksWord = new ScriptedWord(M.numberOfBlinksOnTheSameOrientation()+"("+M.numberOfBlinks()+") blinks");
            xx = x0 + 72;
            yy = y0 + 18;
            numberOfBlinksWord.printEPS(pw,xx,yy,normalFont,scriptFont,ysub,ysup);

            // gem number
            GemEntry ge = M.getGemEntry();
            GeneralWord gemWord = new GeneralWord(normalFont,scriptFont,ysub,ysup+0.2);
            if (ge != null) {

                if (ge.getCatalogNumber() > 0) {
                    gemWord.addWordElement("r", "" + ge.getCatalogNumber(), "" + ge.getNumVertices());
                    //" #ts:"+ge.getTSClassSize()
                } else {
                    gemWord.addWordElement("r", "?", "" + ge.getNumVertices());
                    // = new ScriptedWord("R "+ge.getNumVertices()+" ? #ts:"+ge.getTSClassSize());
                }

                gemWord.addWordElement("      #ts "+ ge.getTSClassSize(),null, null);



                // CASE OF DISCONNECTING QUARTET
                if (ge.getGemPrimeStatus() == GemPrimeStatus.POTENTIALLY_COMPOSITE ||
                    ge.getGemPrimeStatus() == GemPrimeStatus.COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP) {


                    GemPrimeTest GPT = new GemPrimeTest();
                    GPT.test(ge.getGem());
                    Gem[] parts = GPT.getParts();
                    GemSimplificationPathFinder SA0 = new GemSimplificationPathFinder(parts[0],0,5000L,0);
                    GemSimplificationPathFinder SA1 = new GemSimplificationPathFinder(parts[1],0,5000L,0);
                    parts[0] = (SA0.getBestAttractorFound() != null ? SA0.getBestAttractorFound() : parts[0]);
                    parts[1] = (SA1.getBestAttractorFound() != null ? SA1.getBestAttractorFound() : parts[1]);

                    GemEntry ge0 = null;
                    GemEntry ge1 = null;

                    ArrayList<GemEntry> ges0 = App.getRepositorio().getGemsByHashcodeAndHandleNumber(parts[0].getGemHashCode(),parts[0].getHandleNumber());
                    for (GemEntry g : ges0) {
                        if (g.getGem().equals(parts[0])) {
                            ge0 = g;
                            break;
                        }
                    }
                    ArrayList<GemEntry> ges1 = App.getRepositorio().getGemsByHashcodeAndHandleNumber(parts[1].getGemHashCode(),parts[1].getHandleNumber());
                    for (GemEntry g: ges1) {
                        if (g.getGem().equals(parts[1])) {
                            ge1 = g;
                            break;
                        }
                    }

                    if (_gemGraph == null) {
                        System.out.println("Loading gemGraph");
                        _gemGraph = new GemGraph();
                    }

                    gemWord.addWordElement("       ",null, null);
                    if (ge0 != null) {
                        long gemId = _gemGraph.getRepresentantGemId(ge0.getId());
                        Manifold S = _catalog.getManifoldFromMinGem(gemId);
                        if (S != null) {
                            gemWord.addWordElement("S",""+S.getNumberOnComplexity(),""+S.blinkComplexity());
                        }
                        else {
                            gemWord.addWordElement("S","?","?");
                        }
                    }
                    else gemWord.addWordElement("S","?","?");
                    gemWord.addWordElement(" # ",null,null);
                    if (ge1 != null) {
                        long gemId = _gemGraph.getRepresentantGemId(ge1.getId());
                        Manifold S = _catalog.getManifoldFromMinGem(gemId);
                        if (S != null) {
                            gemWord.addWordElement("S",""+S.getNumberOnComplexity(),""+S.blinkComplexity());
                        }
                        else {
                            gemWord.addWordElement("S","?","?");
                        }
                    }
                    else gemWord.addWordElement("S","?","?");
                }

                // CASE OF HANDLE_NUMBER > 0
                else if (ge.getGemPrimeStatus() == GemPrimeStatus.COMPOSITE_FROM_HANDLE) {
                    Gem gg = ge.getGem();
                    GemEntry ge2= null;
                    ArrayList<GemEntry> ges = App.getRepositorio().getGemsByHashcodeAndHandleNumber(
                            gg.getGemHashCode(), gg.getHandleNumber() - 1);
                    for (GemEntry g : ges) {
                        if (g.getGem().equals(gg)) {
                            ge2 = g;
                            break;
                        }
                    }
                    if (_gemGraph == null) {
                        System.out.println("Loading gemGraph");
                        _gemGraph = new GemGraph();
                    }

                    gemWord.addWordElement("       ",null, null);
                    gemWord.addWordElement("S", "1", "0");
                    gemWord.addWordElement(" # ",null, null);
                    if (ge2 != null) {
                        long gemId = _gemGraph.getRepresentantGemId(ge2.getId());
                        Manifold S = _catalog.getManifoldFromMinGem(gemId);
                        if (S != null) {
                            gemWord.addWordElement("S",""+S.getNumberOnComplexity(),""+S.blinkComplexity());
                        }
                        else {
                            gemWord.addWordElement("S","?","?");
                        }
                    }
                    else gemWord.addWordElement("S","?","?");
                }

            }
            else gemWord.addWordElement("null",null,null);
            xx = x0 + 32;
            yy = y0 + 35;
            gemWord.printEPS(pw,xx,yy);

        } // end: space label



        { // prime status, homology group, gem
            QI qi = M.calculateQI(12);

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
                word = new ScriptedWord(String.format("%2s %13.10f %13.10f %7d",(r<10 ? "0"+r : ""+r),polar[0],polar[1]/Math.PI,qie.get_states()));
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

            GemEntry ge = M.getGemEntry();
            String code[] = {null,null,null};
            String handle = "";
            String numVert = "";
            if (ge != null) {
                StringTokenizer st = new StringTokenizer(ge.getGem().getCurrentLabelling().getLettersString(","),",");
                code[0] = st.nextToken();
                code[1] = st.nextToken();
                code[2] = st.nextToken();
                handle = ""+ge.getHandleNumber();
                numVert = ""+ge.getNumVertices();
            }

            GeneralWord gemCode = new GeneralWord(normalFont,normalFont,0,0);
            //gemCode.printEPS(pw,xx,yy);
            yy+=1.65 * CONVERT_MM_2_POINTS;
            // xx+=5 *CONVERT_MM_2_POINTS;
            for (int ii=0;ii<3;ii++) {
                gemCode.clear();
                gemCode.addWordElement(code[ii], null, null);
                gemCode.printEPS(pw, xx + 10 * CONVERT_MM_2_POINTS, yy - CONVERT_MM_2_POINTS);

                gemCode.clear();
                if (ii == 0) {
                    gemCode.addWordElement("gem:",null,null,"Helvetica8","Helvetica8",0,0);
                    gemCode.printEPS(pw, xx , yy+0.5*CONVERT_MM_2_POINTS);
                }
                else if (ii == 1) {
                    //gemCode.addWordElement("h"+handle,null,null,"Helvetica5","Helvetica5",0,0);
                }
                else if (ii == 2) {
                    gemCode.addWordElement("h"+handle+" v"+numVert,null,null,"Helvetica7","Helvetica7",0,0);
                    gemCode.printEPS(pw, xx + 1*CONVERT_MM_2_POINTS , yy-1.27*CONVERT_MM_2_POINTS);
                }
                //gemCode.printEPS(pw,xx,yy);
                //gemCode.addWordElement("h"+handle,null,null);
                //gemCode.printEPS(pw, xx + 5 *CONVERT_MM_2_POINTS, yy);

                yy += 7.7;
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

    private void printBlink(PrintWriter pw, BlinkEntry be, double x0, double y0, double ww, double hh) {


        GBlink b = be.getBlink();

        double topMargin = 10; // space to info
        double leftMargin = 2;
        double bottomMargin = 1; // space to info
        double rightMargin = 2;
        double w = ww - leftMargin - rightMargin;
        double h = hh - topMargin - bottomMargin;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,leftMargin,topMargin,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        /*
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.05*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show",
                                 "b"+be.get_id()+
                                 " g"+be.getMinGem()+
                                 " z"+be.getBlink().getNumberOfZigZags()));
        pw.println("grestore");*/



        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate", x0, y0));

        ArrayList<Variable> varVertices = b.getGVertices();
        for (Variable var: varVertices) {
            Point2D.Double p = new Point2D.Double(0,0);
            for (GBlinkVertex vv: var.getVertices()) {
                Point2D.Double pAux = map.get(vv);
                p.setLocation(p.getX()+pAux.getX(),p.getY()+pAux.getY());
            }
            if (var.size() == 0) {
                p.setLocation(leftMargin+w/2.0,topMargin+h/2.0);
            }
            else {
                p.setLocation(p.getX()/var.size(),p.getY()/var.size());
            }
            mapVPos.put(var,p);
        }

        for (int i=1;i<=b.getNumberOfGEdges();i++) {
            GBlinkVertex va = b.findVertex((i-1)*4+1);
            GBlinkVertex vb = b.findVertex((i-1)*4+2);
            GBlinkVertex vc = b.findVertex((i-1)*4+3);
            GBlinkVertex vd = b.findVertex((i-1)*4+4);

            Point2D.Double p1 = mapVPos.get(b.findVariable(va,Variable.G_VERTICE));
            Point2D.Double p2 = new Point2D.Double(0,0);
            p2.setLocation((map.get(va).getX() + map.get(vb).getX()) / 2.0,
                           (map.get(va).getY() + map.get(vb).getY()) / 2.0);
            Point2D.Double p3 = new Point2D.Double(0,0);
            p3.setLocation((map.get(vc).getX() + map.get(vd).getX()) / 2.0,
                           (map.get(vc).getY() + map.get(vd).getY()) / 2.0);
            Point2D.Double p4 = mapVPos.get(b.findVariable(vc,Variable.G_VERTICE));

            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p1.getX(), p1.getY()));
            pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                     p2.getX(),
                                     p2.getY(),
                                     p3.getX(),
                                     p3.getY(),
                                     p4.getX(),
                                     p4.getY()));

            if (b.getColor(i) == BlinkColor.green)
                pw.println("0 180 0 setrgbcolor");
            else
                pw.println("180 0 0 setrgbcolor");
            pw.println("stroke");
        }

        // draw vertices as black dots
        for (Variable var: varVertices) {
            Point2D.Double p = mapVPos.get(var);
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 1.0));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");




        {  // write label
            double xx = x0 + 2;
            double yy = y0 + 8;
            double ysub = 2;
            double ysup = -3.5;
            String normalFont = "Helvetica6";
            String scriptFont = "Helvetica5";

            GeneralWord gw = new GeneralWord(normalFont, scriptFont, ysub, ysup);
            gw.addWordElement("B", "" + be.getCatalogNumber(), "" + be.get_numEdges());
            gw.addWordElement("=B", ""+be.get_id(), null);
            gw.addWordElement("  z"+b.getNumberOfGZigZags(), null, null,scriptFont,scriptFont,0,0);
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






    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        new GenerateCatalogEPS();
        System.exit(0);
    }
}

abstract class ScriptedWordEPS {
    enum Position { normal, subscript, superscript };
    /**
     * @param pw PrintWriter where to write the eps source
     * @param x0 double x-position of the word baseline starting point
     * @param y0 double y-position of the word baseline starting point
     * @param normalFontName String name of the normal sized font
     * @param scriptFontName String name of the script sized font
     * @param ysub double y-translation of subscript
     * @param ysup double y-translation of superscript
     */
    public abstract void printEPS(
            PrintWriter pw,
            double x0, double y0, // position to start writing the word
            String normalFontName, // font name of normal sized words
            String scriptFontName, // font name of script sized words
            double ysub, // translate vector of a sub script after a normal word
            double ysup // translate vector of a sub script after a normal word
            );
}

class ScriptedWord extends ScriptedWordEPS {
    ArrayList<String> _words;
    ArrayList<Position> _positions;

    public ScriptedWord() {
        _words = new ArrayList<String>();
        _positions = new ArrayList<Position>();
    }

    public ScriptedWord(String word) {
        this();
        this.addWord(word, Position.normal);
    }

    public void addWord(String word, Position pos) {
        _words.add(word);
        _positions.add(pos);
    }

    /**
     *
     *
     * @param pw PrintWriter where to write the eps source
     * @param x0 double x-position of the word baseline starting point
     * @param y0 double y-position of the word baseline starting point
     * @param normalFontName String name of the normal sized font
     * @param scriptFontName String name of the script sized font
     * @param ysub double y-translation of subscript
     * @param ysup double y-translation of superscript
     */
    public void printEPS(
                PrintWriter pw,
                double x0, double y0, // position to start writing the word
                String normalFontName, // font name of normal sized words
                String scriptFontName, // font name of script sized words
                double ysub, // translate vector of a sub script after a normal word
                double ysup // translate vector of a sub script after a normal word
            ) {

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f moveto", x0, y0));
        for (int i=0;i<_words.size();i++) {
            String word = _words.get(i);
            Position wordPos = _positions.get(i);
            if (wordPos == Position.subscript) {
                pw.println(String.format("%.4f %.4f rmoveto", 0.0, ysub));
                pw.println(scriptFontName + " setfont");
            }
            else if (wordPos == Position.superscript) {
                pw.println(String.format("%.4f %.4f rmoveto", 0.0, ysup));
                pw.println(scriptFontName + " setfont");
            }
            else {
                pw.println(normalFontName + " setfont");
            }
            pw.println("1 -1 scale"); // admit that the font are upside down.
            pw.println("("+word+") show");
            pw.println("1 -1 scale"); // admit that the font are upside down.
            if (wordPos == Position.subscript) {
                pw.println(String.format("%.4f %.4f rmoveto", 0.0, -ysub));
            }
            else if (wordPos == Position.superscript) {
                pw.println(String.format("%.4f %.4f rmoveto", 0.0, -ysup));
            }
        }
        pw.println("grestore");
    }
}

class SubAndSuperScriptedWord extends ScriptedWordEPS {
    String _word;
    String _subscript;
    String _superscript;
    public SubAndSuperScriptedWord(String word, String subscript, String superscript) {
        _word = word;
        _subscript = subscript;
        _superscript = superscript;
    }

    /**
     *
     *
     * @param pw PrintWriter where to write the eps source
     * @param x0 double x-position of the word baseline starting point
     * @param y0 double y-position of the word baseline starting point
     * @param normalFontName String name of the normal sized font
     * @param scriptFontName String name of the script sized font
     * @param ysub double y-translation of subscript
     * @param ysup double y-translation of superscript
     */
    public void printEPS(
                PrintWriter pw,
                double x0, double y0, // position to start writing the word
                String normalFontName, // font name of normal sized words
                String scriptFontName, // font name of script sized words
                double ysub, // translate vector of a sub script after a normal word
                double ysup // translate vector of a sub script after a normal word
            ) {

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f moveto", x0, y0));

        pw.println(normalFontName + " setfont");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("("+_word+") show");
        pw.println("1 -1 scale"); // admit that the font are upside down.

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f rmoveto", 0.0, ysub));
        pw.println(scriptFontName + " setfont");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("("+_subscript+") show");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("grestore");

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f rmoveto", 0.0, ysup));
        pw.println(scriptFontName + " setfont");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("("+_superscript+") show");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("grestore");

        pw.println("grestore");
    }

}




class GeneralWordElement {
    private String _word;
    private String _subscript;
    private String _superscript;
    private double _ysub;
    private double _ysup;
    private String _scriptFont;
    private String _normalFont;
    public GeneralWordElement(
            String word,
            String subscript,
            String superscript,
            String normalFont,
            String scriptFont,
            double ysub,
            double ysup) {
        _word = word;
        _subscript = subscript;
        _superscript = superscript;
        _normalFont = normalFont;
        _scriptFont = scriptFont;
        _ysub = ysub;
        _ysup = ysup;
    }

    private void printScript(PrintWriter pw, boolean trueIsSubscriptFalseIsSuperscript) {
        if (trueIsSubscriptFalseIsSuperscript) {
            pw.println(String.format("%.4f %.4f rmoveto", 0.0, _ysub));
            pw.println(_scriptFont + " setfont");
            pw.println("1 -1 scale"); // admit that the font are upside down.
            pw.println("(" + _subscript + ") show");
            pw.println("1 -1 scale"); // admit that the font are upside down.
            pw.println(String.format("%.4f %.4f rmoveto", 0.0, -_ysub));
        }
        else {
            pw.println(String.format("%.4f %.4f rmoveto", 0.0, _ysup));
            pw.println(_scriptFont + " setfont");
            pw.println("1 -1 scale"); // admit that the font are upside down.
            pw.println("("+_superscript+") show");
            pw.println("1 -1 scale"); // admit that the font are upside down.
            pw.println(String.format("%.4f %.4f rmoveto", 0.0, -_ysup));
        }
    }

    public void print(PrintWriter pw) {

        pw.println(_normalFont + " setfont");
        pw.println("1 -1 scale"); // admit that the font are upside down.
        pw.println("("+_word+") show");
        pw.println("1 -1 scale"); // admit that the font are upside down.

        boolean bothScripts = _subscript != null && _superscript != null;
        boolean subScriptFirst = true;
        if ((_subscript == null && _superscript != null) ||
            (bothScripts && _superscript.length() < _subscript.length()))  {
            subScriptFirst = false;
        }

        if (bothScripts) {
            pw.println("gsave");
            printScript(pw, subScriptFirst);
            pw.println("grestore");
            printScript(pw, !subScriptFirst);
        }
        else if (_subscript != null) {
            printScript(pw, true);
        }
        else if (_superscript != null) {
            printScript(pw, false);
        }
    }
}

class GeneralWord {
    private double _ysub;
    private double _ysup;
    private String _scriptFont;
    private String _normalFont;
    ArrayList<GeneralWordElement> _elements;

    public GeneralWord(String normalFont, String scriptFont, double ysub, double ysup) {
        _normalFont = normalFont;
        _scriptFont = scriptFont;
        _ysub = ysub;
        _ysup = ysup;
        _elements = new ArrayList<GeneralWordElement>();
    }

    public void clear() {
        _elements.clear();
    }

    public void addWordElement(String word, String subscript, String superscript, String normalFont, String scriptFont,
                               double ysub, double ysup) {
        _elements.add(new GeneralWordElement(word,subscript,superscript,normalFont,scriptFont,ysub,ysup));
    }

    public void addWordElement(String word, String subscript, String superscript) {
        this.addWordElement(word,subscript,superscript,_normalFont,_scriptFont,_ysub,_ysup);
    }

    /**
     *
     *
     * @param pw PrintWriter where to write the eps source
     * @param x0 double x-position of the word baseline starting point
     * @param y0 double y-position of the word baseline starting point
     * @param normalFontName String name of the normal sized font
     * @param scriptFontName String name of the script sized font
     * @param ysub double y-translation of subscript
     * @param ysup double y-translation of superscript
     */
    public void printEPS(PrintWriter pw, double x0, double y0) {

        pw.println("gsave");
        pw.println(String.format("%.4f %.4f moveto", x0, y0));
        for (GeneralWordElement gwe: _elements) {
            gwe.print(pw);
        }
        pw.println("grestore");
    }
}







