package maps.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import maps.Graph;
import maps.Graph.Edge;
import maps.Graph.Vertex;
import drawing.IDrawing;
import drawing.Rectangle;
import drawing.SimpleGroup;
import drawing.View;

public class Editor  {
    
    // world rectangle...
    private Rectangle _worldRectangle;
    
    // will be mapped into this screen rectangle
    private Rectangle _screenRectangle;
    
    // view
    private View _view;
    
    //
    private SimpleGroup _worldObjects;
    
    // DrawPanel
    private DrawPanel _drawPanel;
    
    //
    private Grid _grid;
    
    // acaos
    // private Graph _cambio;
    
    //
    private EditorGraph _editorGraph;
    
    //
    private boolean _snapToGrid;
    private boolean _showGrid;
    
    // list of editor acaos (sync with cambio acaos)
    private ArrayList<EditorVertex> _editorVertexs = new ArrayList<EditorVertex>();
    
    // list of editor arcos (sync with cambio arcos)
    private ArrayList<EditorEdge> _editorEdges = new ArrayList<EditorEdge>();
    
    /**
     * Editor constructor
     */
    public Editor() {
        super();
        
        _drawPanel = new DrawPanel();
        
        // JPanel bottomPanel = new JPanel();
        // bottomPanel.setBackground(Color.white);
        // bottomPanel.setOpaque(true);
        // bottomPanel.setLayout(new BorderLayout());
        // bottomPanel.add(_lblMessage,BorderLayout.CENTER);
        
        
        this.resetView();
        
        this.plugActions();
    }
    
    public JComponent getDrawPanel() {
        return _drawPanel;
    }
    
    public void setGridIsVisible(boolean b) {
        this._showGrid = b;
        _grid.setVisible(b);
        _drawPanel.repaint();
    }
    
    public void setSnapToGrid(boolean b) {
        this._snapToGrid = b;
    }
    
    public void configureGrid(double offsetX, double offsetY, double sizeX, double sizeY) {
        _grid.update(offsetX,offsetY,sizeX,sizeY);
        _drawPanel.repaint();
    }
    
    private void plugActions() {
        // map the keys of the keyboard
        ActionMap amap = _drawPanel.getActionMap();
        amap.put("zoom", ACTION_ZOOM);
        amap.put("unzoom", ACTION_UNZOOM);
        amap.put("left", ACTION_LEFT);
        amap.put("right", ACTION_RIGHT);
        amap.put("up", ACTION_UP);
        amap.put("down", ACTION_DOWN);
        amap.put("addVertex", ACTION_ADD_NODE);
        amap.put("clone", ACTION_CLONE);
        amap.put("delete", ACTION_DELETE);
        amap.put("alignh", ACTION_ALIGN_HORIZONTAL);
        amap.put("alignv", ACTION_ALIGN_VERTICAL);
        amap.put("mirrorv", ACTION_MIRROR_VERTICAL);
        amap.put("mirrorh", ACTION_MIRROR_HORIZONTAL);
        amap.put("import", ACTION_IMPORT_NETWORK);
        
        InputMap imap = _drawPanel.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT,java.awt.event.KeyEvent.CTRL_DOWN_MASK, false), "left");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT,java.awt.event.KeyEvent.CTRL_DOWN_MASK, false), "right");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.KeyEvent.SHIFT_DOWN_MASK, false),"zoom");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.KeyEvent.SHIFT_DOWN_MASK,  false),"unzoom");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP,java.awt.event.KeyEvent.CTRL_DOWN_MASK, false), "up");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN,java.awt.event.KeyEvent.CTRL_DOWN_MASK, false), "down");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0, false),"addVertex");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, 0, false),"clone");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0,false), "delete");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, 0, false),"alignh");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, 0, false),"alignv");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, 0, false),"mirrorv");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, 0,false), "mirrorh");
        imap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_INSERT, 0,false), "import");
        
        _drawPanel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                update(e);
            }
            
            public void mouseEntered(MouseEvent e) {
            }
            
            public void mouseExited(MouseEvent e) {
            }
            
            public void mousePressed(MouseEvent e) {
                press(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                release(e);
            }
        });
        
        // add mouse motion listener
        _drawPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                drag(e);
            }
            
            public void mouseMoved(MouseEvent e) {
                move(e);
            }
        });
        
        // add mouse motion listener
        _drawPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                // TODO Auto-generated method stub
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    int x = e.getWheelRotation();
                    move(x, 0);
                    
                } else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
                    int x = e.getWheelRotation();
                    zoom(x);
                } else {
                    int y = e.getWheelRotation();
                    move(0, y);
                }
            }
        });
    }
    
    private void resetView() {
        //Model model = new Model();
        //Graph c = model.newWorkspace("workspace", "").newGraph("cambio", "");
        
        _editorGraph = new EditorGraph(new Graph());
        
        
        // _cambio = c;
        
        _worldRectangle = new Rectangle(null, 0, 0, 0, 40, 40);
        _worldRectangle.setVisible(false);
        
        _worldObjects = new SimpleGroup(null);
        
        _grid = new Grid(0, 0, 1, 1);
        _grid.setVisible(_showGrid);
        
        SimpleGroup layerZero = new SimpleGroup(null);
        layerZero.addChild(_grid);
        layerZero.addChild(_worldRectangle);
        layerZero.addChild(_worldObjects);
        
        _screenRectangle = new Rectangle(null, 5, 5, 0, 640, 640);
        _screenRectangle.setVisible(false);
        
        _view = new View(_worldRectangle, _screenRectangle);
    }
    
    public EditorVertex addVertex(Vertex acao) {
        EditorVertex ea = _editorGraph.addVertex(acao);
        return this.addVertex(ea);
    }
    
    private EditorVertex addVertex(EditorVertex ea) {
        ea.setPropertyValue("editor", this);
        _editorVertexs.add(ea);
        _worldObjects.addChild(ea);
        return ea;
    }
    
    public EditorVertex addVertex(double x, double y) {
        EditorVertex ea = _editorGraph.addVertex(x, y);
        return this.addVertex(ea);
    }
    
    public EditorVertex getEditorVertexFromVertex(Vertex acao) {
        for (EditorVertex en: this.getEditorVertexs())
            if (en.getVertex() == acao)
                return en;
        throw new RuntimeException();
    }
    
    public EditorEdge addEdge(EditorEdge ea) {
        ea.setPropertyValue("editor", this);
        _editorEdges.add(ea);
        _worldObjects.insertChild(ea);
        return ea;
    }
    
    public EditorEdge addEdge(EditorVertex a, EditorVertex b) {
        EditorEdge ea =_editorGraph.addEdge(a, b);
        return addEdge(ea);
    }
    
    public EditorEdge addEdge(Edge arco) {
        EditorVertex a = getEditorVertexFromVertex(arco.getV1());
        EditorVertex b = getEditorVertexFromVertex(arco.getV2());
        EditorEdge ea =_editorGraph.addEdge(arco,a, b);
        return addEdge(ea);
    }
    
    public void deleteEdge(EditorEdge ec) {
        // System.out.println("delete arco "+ec.getEditorVertexA().getName()+" "+ec.getEditorVertexB().getName());
        _worldObjects.deleteChild(ec);
        _editorEdges.remove(ec);
        _editorGraph.deleteEdge(ec);
    }
    
    public void deleteVertex(EditorVertex en) {
        for (int i=this.getEditorEdges().size()-1;i>=0;i--) {
            EditorEdge ec = this.getEditorEdges().get(i);
            if (ec.getEditorVertexA().equals(en)
            || ec.getEditorVertexB().equals(en)) {
                this.deleteEdge(ec);
            }
        }
        // System.out.println("delete acao "+en.getVertex().getName());
        _worldObjects.deleteChild(en);
        _editorVertexs.remove(en);
        _editorGraph.deleteVertex(en);
    }
    
    public ArrayList<EditorVertex> getEditorVertexs() {
        return _editorVertexs;
    }
    
    public ArrayList<EditorEdge> getEditorEdges() {
        return _editorEdges;
    }
    
    private ArrayList<IDrawing> _selectedObjects = new ArrayList<IDrawing>();
    
    public boolean isSelected(IDrawing d) {
        return _selectedObjects.contains(d);
    }
    
    public ArrayList<IDrawing> getSelectedObjects() {
        return (ArrayList<IDrawing>) _selectedObjects.clone();
    }
    
    
    enum Mode {
        idle, startMove, moving, startConnect, connecting, startSelectingBox, selectingBox
    };
    
    private Mode _mode = Mode.idle;
    
    private double _moveX, _moveY;
    
    private double _dragX0, _dragY0;
    
    private double _dragX1, _dragY1;
    
    public void press(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        
        // select objects
        if (_mode == Mode.idle && e.getButton() == MouseEvent.BUTTON1) {
            IDrawing i = _view.getTopMostDrawingThatContainsPoint(x, y);
            if (i != _worldRectangle && i != null) {
                if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0)
                    _selectedObjects.clear();
                if (!_selectedObjects.contains(i))
                    _selectedObjects.add(i);
            } else {
                _selectedObjects.clear();
            }
            System.out.println("Hit object " + i);
            
            // selection may have changed!
            this.fireSelectionChanged();
            
        }
        
        // select objects
        if (_mode == Mode.idle && _selectedObjects.size() > 0
                && e.getButton() == MouseEvent.BUTTON1) {
            _mode = Mode.startMove;
        } else if (_mode == Mode.idle && e.getButton() != MouseEvent.BUTTON1) {
            _mode = Mode.startConnect;
        } else if (_mode == Mode.idle && e.getButton() == MouseEvent.BUTTON1) {
            _mode = Mode.startSelectingBox;
        }
        
        //
        _dragX0 = x;
        _dragY0 = y;
        _drawPanel.repaint();
    }
    
    private void drag(MouseEvent e) {
        if (_mode == Mode.startMove) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _mode = Mode.moving;
            _drawPanel.repaint();
            
        } else if (_mode == Mode.startConnect) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _mode = Mode.connecting;
            _drawPanel.repaint();
        } else if (_mode == Mode.startSelectingBox) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _mode = Mode.selectingBox;
            _drawPanel.repaint();
        } else if (_mode == Mode.moving) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _drawPanel.repaint();
        } else if (_mode == Mode.connecting) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _drawPanel.repaint();
        } else if (_mode == Mode.selectingBox) {
            _dragX1 = e.getX();
            _dragY1 = e.getY();
            _drawPanel.repaint();
        }
    }
    
    public void move(MouseEvent e) {
        _moveX = e.getX();
        _moveY = e.getY();
        _drawPanel.repaint();
    }
    
    private void release(MouseEvent e) {
        if (_mode == Mode.moving) {
            double p0[] = _view.getWorldCoordinateForScreenCoordinate(_dragX0,
                    _dragY0);
            double p1[] = _view.getWorldCoordinateForScreenCoordinate(_dragX1,
                    _dragY1);
            for (IDrawing d : _selectedObjects) {
                if (d instanceof EditorVertex) {
                    EditorVertex n = (EditorVertex) d;
                    
                    double p[] = { n.getX() + p1[0] - p0[0],
                    n.getY() + p1[1] - p0[1] };
                    if (_snapToGrid) {
                        p = _grid.snap(p[0], p[1]);
                    }
                    n.setPosition(p[0], p[1]);
                }
            }
        } else if (_mode == Mode.connecting) {
            IDrawing a = _view.getTopMostDrawingThatContainsPoint(_dragX0,
                    _dragY0);
            IDrawing b = _view.getTopMostDrawingThatContainsPoint(_dragX1,
                    _dragY1);
            if (a != null && a instanceof EditorVertex && b != null
                    && b instanceof EditorVertex) {
                EditorVertex na = (EditorVertex) a;
                EditorVertex nb = (EditorVertex) b;
                try {
                    this.addEdge(na, nb);
                } catch (RuntimeException e1) {
                    JOptionPane
                            .showMessageDialog(_drawPanel, "Não pode haver loops!");
                    e1.printStackTrace();
                }
            }
        } else if (_mode == Mode.selectingBox) {
            this.selectBox(_dragX0, _dragY0, _dragX1, _dragY1);
        }
        _mode = Mode.idle;
        _drawPanel.repaint();
    }
    
    private void add() {
//        PanelVertex panelVertex = new PanelVertex(_editorGraph.getGraph());
//        
//        //
//        JDialog f = new JDialog((JFrame) null, "Vertex", true);
//        f.setContentPane(panelVertex);
//        f.pack();
//        cambio.util.GUIUtils.resizeAndCenterWindow(f, f.getWidth(), f.getHeight());
//        f.setVisible(true);
//        
//        //
//        if (panelVertex.isToUpdateOrInsert()) {
//            String nome = panelVertex.getNome();
//            String descricao = panelVertex.getDescricao();
//            double[] posicao = panelVertex.getPosition();
//            double[] movimentoDaPonte = panelVertex.getMovimentoDaPonte();
//            double tempo = panelVertex.getTempo();
//            Equipe equipe = panelVertex.getEquipe();
//            boolean isCaminhoFechado = panelVertex.isCaminhoFechado();
//            
//            double p0[] = _view.getWorldCoordinateForScreenCoordinate(_moveX,_moveY);
//            
//            this.addVertex(nome, descricao, p0[0], p0[1], posicao[0], posicao[1], tempo, equipe, isCaminhoFechado, movimentoDaPonte);
//            _drawPanel.repaint();
//        }

        double p0[] = _view.getWorldCoordinateForScreenCoordinate(_moveX,_moveY);
        this.addVertex(p0[0], p0[1]);
        _drawPanel.repaint();

    }
    
    private Action ACTION_ZOOM = new Action("Zoom", null, "Zoom") {
        public void actionPerformed(ActionEvent actionEvent) {
            System.out.println("Zoom");
            double x0 = _worldRectangle.get_x0();
            double y0 = _worldRectangle.get_y0();
            double width = _worldRectangle.get_width();
            double height = _worldRectangle.get_height();
            double theta = _worldRectangle.get_theta();
            setViewBounds(x0, y0, width * 0.952380952, height * 0.952380952);
            _drawPanel.repaint();
        }
    };
    
    private void setViewBounds(double x, double y, double w, double h) {
        _worldRectangle.setTransformToParent(x, y, 0, w, h);
        // @TODO _cambio.setWindow(x, y, w, h);
    }
    
    
    private Action ACTION_UNZOOM = new Action("Unzoom", null, "Unzoom") {
        public void actionPerformed(ActionEvent actionEvent) {
            double x0 = _worldRectangle.get_x0();
            double y0 = _worldRectangle.get_y0();
            double width = _worldRectangle.get_width();
            double height = _worldRectangle.get_height();
            double theta = _worldRectangle.get_theta();
            setViewBounds(x0, y0, width * 1.05, height * 1.05);
            _drawPanel.repaint();
        }
    };
    
    public void move(int dx, int dy) {
        double x0 = _worldRectangle.get_x0();
        double y0 = _worldRectangle.get_y0();
        double width = _worldRectangle.get_width();
        double height = _worldRectangle.get_height();
        double theta = _worldRectangle.get_theta();
        double newX0 = x0 + (dx * width * 0.05);
        double newY0 = y0 + (dy * height * 0.05);
        setViewBounds(newX0, newY0, width, height);
        _drawPanel.repaint();
    }
    
    private void zoom(int f) {
        double x0 = _worldRectangle.get_x0();
        double y0 = _worldRectangle.get_y0();
        double width = _worldRectangle.get_width();
        double height = _worldRectangle.get_height();
        double theta = _worldRectangle.get_theta();
        
        double p[] = _view
                .getWorldCoordinateForScreenCoordinate(_moveX, _moveY);
        
        double alpha = (p[0] - x0) / width;
        double beta = (p[1] - y0) / height;
        
        double neww = width * Math.pow(1.05, f);
        double newh = height * Math.pow(1.05, f);
        
        double newx0 = p[0] - alpha * neww;
        double newy0 = p[1] - beta * newh;
        
        setViewBounds(newx0, newy0, neww, newh);
        _drawPanel.repaint();
    }
    
    private Action ACTION_LEFT = new Action("Left", null, "left") {
        public void actionPerformed(ActionEvent actionEvent) {
            move(-1, 0);
        }
    };
    
    private Action ACTION_RIGHT = new Action("Right", null, "right") {
        public void actionPerformed(ActionEvent actionEvent) {
            move(1, 0);
        }
    };
    
    private Action ACTION_UP = new Action("Up", null, "up") {
        public void actionPerformed(ActionEvent actionEvent) {
            move(0, -1);
        }
    };
    
    private Action ACTION_DOWN = new Action("Down", null, "down") {
        public void actionPerformed(ActionEvent actionEvent) {
            move(0, 1);
        }
    };
    
    private Action ACTION_ADD_NODE = new Action("Add Vertex", null, "add") {
        public void actionPerformed(ActionEvent actionEvent) {
            add();
        }
    };
    
    private Action ACTION_CLONE = new Action("Clone", null, "clone") {
        public void actionPerformed(ActionEvent actionEvent) {
            cloneSelection();
        }
    };
    
    private Action ACTION_DELETE = new Action("Delete", null, "delete") {
        public void actionPerformed(ActionEvent actionEvent) {
            delete();
        }
    };
    
    private Action ACTION_ALIGN_HORIZONTAL = new Action("AlignH", null, "alignh") {
        public void actionPerformed(ActionEvent actionEvent) {
            alignHorizontally();
        }
    };
    
    private Action ACTION_ALIGN_VERTICAL = new Action("AlignV", null, "alignv") {
        public void actionPerformed(ActionEvent actionEvent) {
            alignVertically();
        }
    };
    
    private Action ACTION_MIRROR_VERTICAL = new Action("MirrorV", null, "mirrorv") {
        public void actionPerformed(ActionEvent actionEvent) {
            mirrorSelection(false, true);
        }
    };
    
    private Action ACTION_MIRROR_HORIZONTAL = new Action("MirrorH", null, "mirrorh") {
        public void actionPerformed(ActionEvent actionEvent) {
            mirrorSelection(true, false);
        }
    };
    
    private Action ACTION_IMPORT_NETWORK = new Action("ImportGraph", null, "importGraph") {
        public void actionPerformed(ActionEvent actionEvent) {
            importGraph();
        }
    };
    
    public void importGraph() {
//		double p[] = _view.getWorldCoordinateForScreenCoordinate(_moveX, _moveY);
//
//		JFileChooser f = new JFileChooser();
//		f.setSelectedFile(new File(System.getProperty("user.dir")+"/res/cambios/."));
//		int r = f.showOpenDialog(this);
//		if (r == JFileChooser.CANCEL_OPTION)
//			return;
//		File file = f.getSelectedFile();
//
//		Graph n;
//		try {
//			Graph net = Graph.loadGraph(file.getAbsolutePath());
//			HashMap<Vertex,EditorVertex> map = new HashMap<Vertex,EditorVertex>();
//			for (Vertex nn: net.getAcoes() ) {
//				EditorVertex nnn;
//				try {
//					nnn = this.addVertex(
//							nn.getName(),
//							nn.getType(),
//							nn.getNote(),
//							p[0] + nn.getX(),
//							p[1] + nn.getY(),
//							nn.getVariables());
//				} catch (RuntimeException e) {
//					nnn = this.addVertex(
//							"_"+nn.getName(),
//							nn.getType(),
//							nn.getNote(),
//							p[0] + nn.getX(),
//							p[1] + nn.getY(),
//							nn.getVariables());
//				}
//				map.put(nn, nnn);
//			}
//
//			for (Edge c: net.getEdges() ) {
//				EditorVertex a = map.get(c.getA());
//				EditorVertex b = map.get(c.getB());
//				this.addEdge(a, b);
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		_drawPanel.repaint();
    }
    
    public void alignHorizontally() {
        ArrayList<EditorVertex> acaos = new ArrayList<EditorVertex>();
        for (Object o : _selectedObjects)
            if (o instanceof EditorVertex)
                acaos.add((EditorVertex) o);
        
        if (acaos.size() <= 1)
            return;
        
        double y = acaos.get(0).getY();
        for (int i = 1; i < acaos.size(); i++) {
            acaos.get(i).setPosition(acaos.get(i).getX(), y);
        }
        _drawPanel.repaint();
    }
    
    public void alignVertically() {
        ArrayList<EditorVertex> acaos = new ArrayList<EditorVertex>();
        for (Object o : _selectedObjects)
            if (o instanceof EditorVertex)
                acaos.add((EditorVertex) o);
        
        if (acaos.size() <= 1)
            return;
        
        double x = acaos.get(0).getX();
        for (int i = 1; i < acaos.size(); i++) {
            acaos.get(i).setPosition(x, acaos.get(i).getY());
        }
        _drawPanel.repaint();
    }
    
    public void delete() {
        for (Object o : _selectedObjects) {
            if (o instanceof EditorVertex)
                this.deleteVertex((EditorVertex) o);
            else if (o instanceof EditorEdge)
                this.deleteEdge((EditorEdge) o);
        }
        _drawPanel.repaint();
    }
    
    
    public void selectBox(double x0, double y0, double x1, double y1) {
        double p0[] = _view.getWorldCoordinateForScreenCoordinate(x0, y0);
        double p1[] = _view.getWorldCoordinateForScreenCoordinate(x1, y1);
        for (EditorVertex n : _editorVertexs) {
            if (segmentHasIntersection(p0[0], p1[0], n.getX(), n.getX() + 1)
            && segmentHasIntersection(p0[1], p1[1], n.getY(),
                    n.getY() + 1)) {
                if (!_selectedObjects.contains(n))
                    _selectedObjects.add(n);
            }
        }
        this.fireSelectionChanged();
    }
    
    public boolean segmentHasIntersection(double a, double b, double c, double d) {
        Double aa = Math.min(a, b);
        Double bb = Math.max(a, b);
        Double cc = Math.min(c, d);
        Double dd = Math.max(c, d);
        return !(bb < cc || aa > dd);
    }
    
    public void update(MouseEvent e) {
//        if (e.getClickCount() <= 1)
//            return;
//        
//        
//        for (Object i: _selectedObjects) {
//            
//            // IDrawing i = _view.getTopMostDrawingThatContainsPoint(e.getX(), e.getY());
//            
//            if (i == null || !(i instanceof EditorVertex))
//                continue;
//            
//            Vertex acao = ((EditorVertex) i).getVertex();
//            
//            PanelVertex panelVertex = new PanelVertex(acao);
//            JDialog f = new JDialog((JFrame) null, "Vertex", true);
//            f.setContentPane(panelVertex);
//            // f.setBounds(0, 0, 800, 600);
//            f.pack();
//            cambio.util.GUIUtils.resizeAndCenterWindow(f, f.getWidth(), f.getHeight());
//            f.setVisible(true);
//            
//            if (panelVertex.isToUpdateOrInsert()) {
//                String nome = panelVertex.getNome();
//                String descricao = panelVertex.getDescricao();
//                double[] posicao = panelVertex.getPosition();
//                double[] movimentoDaPonte = panelVertex.getMovimentoDaPonte();
//                double tempo = panelVertex.getTempo();
//                boolean isCaminhoFechado = panelVertex.isCaminhoFechado();
//                Equipe equipe = panelVertex.getEquipe();
//                
//                _editorGraph.getGraph().updateVertex(
//                        acao, nome, descricao,
//                        acao.getEditorX(), acao.getEditorY(),
//                        posicao[0], posicao[1],
//                        tempo, equipe, isCaminhoFechado, movimentoDaPonte);
//                //_cambio.updateVertex(acao, id, type, note, variables);
//                // acao.sortEdges(panelVertex.getEdgesInTheDefinedOrder());
//                _drawPanel.repaint();
//            }
//        }
    }
    
    public void mirrorSelection(boolean horizontal, boolean vertical) {
        ArrayList<EditorVertex> acaos = new ArrayList<EditorVertex>();
        
        //
        for (Object o : _selectedObjects) {
            if (o instanceof EditorVertex)
                acaos.add((EditorVertex) o);
        }
        
        //
        if (acaos.size() <= 1)
            return;
        
        double minX = acaos.get(0).getX();
        double maxX = acaos.get(0).getX();
        double minY = acaos.get(0).getY();
        double maxY = acaos.get(0).getY();
        for (EditorVertex n : acaos) {
            if (n.getX() < minX)
                minX = n.getX();
            if (n.getY() < minY)
                minY = n.getY();
            if (n.getX() > maxX)
                maxX = n.getX();
            if (n.getY() > maxY)
                maxY = n.getY();
        }
        
        for (EditorVertex n : acaos) {
            double newX = (vertical ? maxX - (n.getX() - minX) : n.getX());
            double newY = (horizontal ? maxY - (n.getY() - minY) : n.getY());
            n.setPosition(newX, newY);
        }
        
        _drawPanel.repaint();
    }
    
    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        
        Editor e = new Editor();
        JFrame f = new JFrame("World");
        f.setBounds(0, 0, 1024, 768);
        f.setContentPane(e.getDrawPanel());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // e.setGraph(model.getWorkspaces().get(0).getGraphs().get(0));
        
        f.setVisible(true);
    }
    
    private static abstract class Action extends AbstractAction {
        public Action(String name, Icon icon, String description) {
            super(name, icon);
            this.putValue(Action.SHORT_DESCRIPTION, description);
            this.putValue(Action.LONG_DESCRIPTION, description);
        }
    }
    
    public int countVertexIdsWithPrefix(String st) {
//        int count = 0;
//        for (Vertex n: _editorGraph.getGraph().getAcoes()) {
//            if (n.getNome().indexOf(st) == 0) {
//                count++;
//            }
//        }
//        return count;
    	return 0;
    }
    
    public void cloneSelection() {
        double rx = _moveX, ry = _moveY;
        double p[] = _view.getWorldCoordinateForScreenCoordinate(rx, ry);
        rx = p[0];
        ry = p[1];
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        ArrayList<EditorVertex> acaos = new ArrayList<EditorVertex>();
        for (IDrawing d : _selectedObjects) {
            if (!(d instanceof EditorVertex))
                continue;
            EditorVertex n = (EditorVertex) d;
            if (n.getX() < minX)
                minX = n.getX();
            if (n.getY() < minY)
                minY = n.getY();
            acaos.add(n);
        }
        
        HashMap<EditorVertex, EditorVertex> mapN2N = new HashMap<EditorVertex, EditorVertex>();
        for (EditorVertex n : acaos) {
            double tx = n.getX() - minX;
            double ty = n.getY() - minY;
            EditorVertex nn = this.addVertex(rx + tx, ry + ty);
            mapN2N.put(n, nn);
        }
        
        ArrayList<EditorEdge> list = new ArrayList<EditorEdge>(_editorEdges);
        for (EditorEdge ec : list) {
            if (_selectedObjects.contains(ec.getEditorVertexA())
            && _selectedObjects.contains(ec.getEditorVertexB())) {
                EditorVertex a = mapN2N.get(ec.getEditorVertexA());
                EditorVertex b = mapN2N.get(ec.getEditorVertexB());
                EditorEdge cc = this.addEdge(a, b);
            }
        }
        
        //
        _drawPanel.repaint();
    }
    
    public void setGraph(Graph cambio) {
        this.resetView();
        
        _editorGraph = new EditorGraph(cambio);
        for (EditorVertex ea: _editorGraph.getEditorVertexs())
            this.addVertex(ea);
        for (EditorEdge ea: _editorGraph.getEditorEdges())
            this.addEdge(ea);
        
//		for (_editorGraph.getGraph().getAcoes();
//		for (Vertex n: _editorGraph.getGraph().getAcoes())
//			this.addVertex(n);
//
//		for (Edge c: _editorGraph.getGraph().getEdges())
//			this.addEdge(c);
        
//		// world rectangle
//		this.setViewBounds(
//				_cambio.getViewRectangleX(),
//				_cambio.getViewRectangleY(),
//				_cambio.getViewRectangleWidth(),
//				_cambio.getViewRectangleHeight());
        
        _drawPanel.repaint();
    }
    
    // -----------------------------------------------------
    // Selection listener support
    public static interface SelectionListener {
        public void selectionChanged(Editor e);
    }
    private ArrayList<SelectionListener> _listeners = new ArrayList<SelectionListener>();
    public void addSelectionListener(SelectionListener sl) {
        _listeners.add(sl);
    }
    public void fireSelectionChanged() {
        for (SelectionListener sl: _listeners)
            sl.selectionChanged(this);
    }
    // -----------------------------------------------------
    
    class DrawPanel extends JComponent
            implements MouseListener, FocusListener, Accessible {
        
        public DrawPanel() {
            setFocusable(true);
            addMouseListener(this);
            addFocusListener(this);
        }
        
        public void mouseClicked(MouseEvent e) {
            //Since the user clicked on us, let's get focus!
            requestFocusInWindow();
        }
        
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        
        public void focusGained(FocusEvent e) {
            //Draw the component with a red border
            //indicating that it has focus.
            System.out.println("Editor gained the focus!!!");
            _drawPanel.repaint();
        }
        
        public void focusLost(FocusEvent e) {
            //Draw the component with a black border
            //indicating that it doesn't have focus.
            System.out.println("Editor lost the focus!!!");
            _drawPanel.repaint();
        }
        
        
        public void paintComponent(Graphics G) {
            super.paintComponent(G);
            
            G.setColor(Color.white);
            G.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            
            double d = Math.max(this.getWidth(), this.getHeight());
            _screenRectangle = new Rectangle(null, 0, 0, 0, d, d);
            _screenRectangle.setVisible(false);
            _view = new View(_worldRectangle, _screenRectangle);
            
            ((Graphics2D) G).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D) G).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            _view.paint(G);
            
            if (_mode == Mode.moving) {
                Line2D.Double line = new Line2D.Double(_dragX0, _dragY0,
                        _dragX1, _dragY1);
                            /*
                             * Arrow Head double ux = _dragX1 - _dragX0; double uy = _dragY1 -
                             * _dragY0; double umod = Math.sqrt(ux * ux + uy * uy); double
                             * vx = -uy; double vy = ux; double vmod = Math.sqrt(vx * vx +
                             * vy * vy); ux = ux /umod; uy = uy /umod; vx = vx /vmod; vy =
                             * vy /vmod; double wx = vx /vmod; double wx = vx /vmod;
                             */
                ((Graphics2D) G).setColor(Color.green);
                ((Graphics2D) G).draw(line);
            }
            if (_mode == Mode.connecting) {
                Line2D.Double line = new Line2D.Double(_dragX0, _dragY0,
                        _dragX1, _dragY1);
                            /*
                             * Arrow Head double ux = _dragX1 - _dragX0; double uy = _dragY1 -
                             * _dragY0; double umod = Math.sqrt(ux * ux + uy * uy); double
                             * vx = -uy; double vy = ux; double vmod = Math.sqrt(vx * vx +
                             * vy * vy); ux = ux /umod; uy = uy /umod; vx = vx /vmod; vy =
                             * vy /vmod; double wx = vx /vmod; double wx = vx /vmod;
                             */
                ((Graphics2D) G).setColor(Color.blue);
                ((Graphics2D) G).draw(line);
            }
            if (_mode == Mode.selectingBox) {
                Rectangle2D.Double line = new Rectangle2D.Double(Math.min(
                        _dragX0, _dragX1), Math.min(_dragY0, _dragY1), Math
                        .abs(_dragX1 - _dragX0), Math.abs(_dragY1 - _dragY0));
                            /*
                             * Arrow Head double ux = _dragX1 - _dragX0; double uy = _dragY1 -
                             * _dragY0; double umod = Math.sqrt(ux * ux + uy * uy); double
                             * vx = -uy; double vy = ux; double vmod = Math.sqrt(vx * vx +
                             * vy * vy); ux = ux /umod; uy = uy /umod; vx = vx /vmod; vy =
                             * vy /vmod; double wx = vx /vmod; double wx = vx /vmod;
                             */
                ((Graphics2D) G).setColor(new Color(200, 200, 225, 80));
                ((Graphics2D) G).fill(line);
                ((Graphics2D) G).setColor(Color.red);
                ((Graphics2D) G).draw(line);
            }
            
            {
                double p[] = _view.getWorldCoordinateForScreenCoordinate(
                        _moveX, _moveY);
                G.setColor(Color.black);
                G.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
                G.drawString(String.format("%8.4f X %8.4f", p[0], p[1]), 3,
                        this.getHeight() - 3);
            }
            
            //Add a border, red if picture currently has focus
            if (isFocusOwner()) {
                G.setColor(Color.RED);
                G.drawRect(2, 2, this.getWidth()-4,this.getHeight()-4);
            } else {
                G.setColor(Color.BLACK);
            }
            G.dispose();
        }
    }
    
}
