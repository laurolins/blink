package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DescricaoBeanTable {
    private BeanTable _beanTable;

    // n\uFFFDo est\uFFFD usando ainda
    private JTableHeader _tableHeader;

    // este componente \uFFFD o JTableHeader do _beanTable
    //private JComponent _component;

    JLabel _label = new JLabel();

    private boolean _estaVisivel = false;

    //
    private String _descricao = "";

    // Cor de fundo da descricao
    private Color _corDescricaoBackground = new Color(255, 255, 225);

    private int _width;
    private int _height;

    // timer para contar o tempo de espera antes de abrir descricao
    private javax.swing.Timer _timer;

    // tempo de espera antes de abrir (em milisegundos)
    private int _tempoEspera = 2000;

    /////////////////////////////////
    // modo debug
    private boolean _debug = false;
    private javax.swing.Timer _timerDebug;
    private int _contadorDebugTimer;
    private int _tempoAtualizacaoDebugUI = 400;
    JLabel _labelDebug = new JLabel();
    // modo debug
    /////////////////////////////////

    // create an empty and non opaque glass pane
    JPanel _glass = new JPanel();

    // guarda a coluna corrente do mouse sobre TableHeader
    private int _colunaIndex;


    /////////////////////////////////
    // Construtores

    public DescricaoBeanTable(BeanTable beanTable, String descricao, int width,
                              int height) {
        this(beanTable);

        _descricao = descricao;
        _width = width;
        _height = height;
        _estaVisivel = false;
    }

    public DescricaoBeanTable(BeanTable beanTable) {
        _beanTable = beanTable;
        _tableHeader = _beanTable.getTableHeader();

        _colunaIndex = -1;

        _glass.setOpaque(false);
        _glass.setLayout(null);
        _glass.add(_label);
        _glass.add(_labelDebug);

        _labelDebug.setFont(new Font("Arial", 1, 15));
        _labelDebug.setSize(38, 28);
        _labelDebug.setHorizontalAlignment(JLabel.CENTER);
        _labelDebug.setBorder(new LineBorder(Color.BLACK));
        _labelDebug.setBackground(Color.RED);
        _labelDebug.setOpaque(true);

        _label.setBackground(_corDescricaoBackground);
        _label.setBorder(new LineBorder(Color.BLACK));
        _label.setHorizontalAlignment(JLabel.CENTER);
        _label.setOpaque(true);

        this.installTimer();

        // debug
        this.setDebugging(true);

        this.install();
    }

    /**
     * install timer
     */
    private void installTimer() {
        _timer = new javax.swing.Timer(_tempoEspera, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDescricao();
            }
        });
    }

    /**
     * install debug timer
     */
    private void installTimerDebug() {
        _timerDebug = new javax.swing.Timer(_tempoAtualizacaoDebugUI, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDebug();
            }
        });
    }

    /**
     * install listeners
     */
    public void install() {
        JTableHeader header = _beanTable.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                in(e);

            }

            public void mouseExited(MouseEvent e) {
                out();
            }
        });

        header.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                atualizarColunaEmFoco(e);
            }

            public void mouseDragged(MouseEvent e) {
                atualizarColunaEmFoco(e);
            }
        });
    }

    public void in(MouseEvent e) {
        this.atualizarDadosDePosicao(e);
        this.startTimer();
        if (isDebugging()) {
            this.startTimerDebug();
        }
    }

    public void out() {
        _label.setVisible(false);
        _glass.setVisible(false);
        _estaVisivel = false;
        this.stopTimer();
        if (_debug) {
            _labelDebug.setVisible(false);
            this.stopTimerDebug();
        }
    }

    //////////////////////////////////////////////////
    // Timer Management Routines

    private void startTimer() {
        _timer.setRepeats(false);
        _timer.start();
    }

    private void startTimerDebug() {
        _contadorDebugTimer = 0;
        _timerDebug.setRepeats(true);
        _timerDebug.start();
        showDebug();
    }

    private void stopTimer() {
        _timer.stop();
    }

    private void stopTimerDebug() {
        _timerDebug.stop();
    }

    ///////////////////////////////////////////////
    // Queries

    private boolean isDescricaoVisivel() {
        return _estaVisivel;
    }

    private boolean isDebugging() {
        return _debug;
    }

    ///////////////////////////////////////////////
    // Updates

    private void setDebugging(boolean modoDebug) {
        if (isDebugging() != modoDebug) {
            _debug = modoDebug;
            if (isDebugging()) {
                installTimerDebug();
            }
        }
    }

    public void setTempoEspera(int milisegundos) {
        this._tempoEspera = milisegundos;
    }

    public void setDebug(boolean modoDebug) {
        if (modoDebug) {
            this.installTimerDebug();
        }
    }

    private void installGlassPane() {
        //_tableHeader.getRootPane().getLayeredPane().highestLayer();
        //_tableHeader.getRootPane().getLayeredPane().add
        if (_tableHeader.getRootPane().getGlassPane() != _glass)
            _tableHeader.getRootPane().setGlassPane(_glass);
    }

    private void uninstallGlassPane() {
        if (_tableHeader.getRootPane().getGlassPane() == _glass)
            _tableHeader.getRootPane().setGlassPane(null);
    }

    public void showDescricao() {
        _estaVisivel = true;
        updateDescricao();
        _label.setVisible(true);
        _glass.setVisible(true);
        installGlassPane();
    }

    private void updateDescricao() {
        _descricao = "";
        _label.setSize(0,0);
        if (_currentColumnIndex >= 0) {
            BeanProperty bp = this.getModel().getVisibleProperty(_currentColumnIndex);
            _descricao = bp.getDescricao();
            _width = _descricao.length() * 7;
            _height = ( (_descricao.length() > 0) ? 16 : 0); // fixo
            _label.setText(_descricao);
            _label.setSize(_width, _height);
            int maxWidth = _beanTable.getRootPane().getWidth();
            int maxHeight = _beanTable.getRootPane().getHeight();


            // Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(32,32);
            // System.out.println("Cursor "+d.getWidth()+"x"+d.getHeight());

            // Cursor.getDefaultCursor();
            int x0 = (_currentX + _width <= maxWidth ? _currentX : maxWidth - _width);
            int y0 = (_currentY + _height <= maxHeight ? _currentY : maxHeight - _height);
            _label.setLocation(x0, y0);
            System.out.println("Mandei desenhar");
        }
    }

    public void showDebug() {
        if (!isDescricaoVisivel()) {
            // renderizar o debug

            String debugText = _contadorDebugTimer + "";

            // texto da descricao deste componente
            _labelDebug.setText(debugText);
            _labelDebug.setLocation(0, 0);
            _labelDebug.setVisible(true);
            _glass.setVisible(true);

            //
            installGlassPane();

            // incrementando o timer debug...
            _contadorDebugTimer++;
        }
        else {
            _labelDebug.setVisible(false);
            this.stopTimerDebug();
        }
    }




















    /**
     * Este metodo calcula os bounds dos retangulos
     * que compoem as celulas que fazem parte do header
     * da beanTable.
     */
    private Rectangle getHeaderColumnBounds(int columnIndex) {

        if (columnIndex < 0 || columnIndex >= _beanTable.getColumnCount()) {
            throw new IllegalArgumentException(
                "O parametro columnIndex est\uFFFD out of bounds!");
        }

        TableColumnModel tcm = _beanTable.getColumnModel();

        int xAtual = 0;
        TableColumn tc = null;
        for (int i = 0; i < _beanTable.getColumnCount() &&
             i < columnIndex; i++) {
            tc = tcm.getColumn(i);

            xAtual += tc.getWidth();
        }

        Rectangle rect = _tableHeader.getBounds();

        // atualizando a pos x e a lrg desta celula
        rect.x = rect.x + xAtual;

        tc = tcm.getColumn(columnIndex);
        rect.width = tc.getWidth();

        return rect;
    }

    /////////////////////////////////////////////////////////////
    // Metodos de Posicao
    int _currentColumnIndex = -1;
    int _currentX = 0;
    int _currentY = 0;
    private boolean atualizarDadosDePosicao(MouseEvent e) {
        boolean result = false;
        Point mousePoint = e.getPoint();
        int novaColunaIndex = _beanTable.columnAtPoint(mousePoint);
        //System.out.print("Coluna: "+novaColunaIndex);
        //if (_currentColumnIndex != novaColunaIndex) {
            _currentColumnIndex = novaColunaIndex;
            Point p = getRootPaneCoordinates(mousePoint);
            _currentX = (int) p.getX();
            _currentY = (int) p.getY();
            _currentX += 16;
            _currentY += 16;

            // Cursor.getDefaultCursor().getS

            result = true;
            System.out.println("(X,Y) ("+_currentX+","+_currentY+")");
        //}

        return result;
    }

    private Point getRootPaneCoordinates(Point p) {
        double x = p.getX();
        double y = p.getY();
        Component comp = _tableHeader;
        while (!(comp instanceof JRootPane)) {
            x = x + comp.getX();
            y = y + comp.getY();
            comp = comp.getParent();
        }
        return new Point((int)x, (int)y);
    }

    private void atualizarColunaEmFoco(MouseEvent e) {
        if (atualizarDadosDePosicao(e) && isDescricaoVisivel()) {
            this.updateDescricao();
        }


        /*
// constantes para a posicao
        // posicao do topLeft do componente sist. coordenada da tela
        Point compPos = _tableHeader.getLocationOnScreen();
        Point rootPanePos = _tableHeader.getRootPane().getLocationOnScreen();

        // posicao do evento do mouse no sist. coordenada do componente
        Point mousePosComp = e.getPoint();
        // posicao do evento do mouse no sist. coordenada da tela
        Point mousePointOnScreen = new Point( (int) (mousePosComp.getX() +
            compPos.getX() - rootPanePos.getX()),
                                             (int) (mousePosComp.getY() +
            compPos.getY() - rootPanePos.getY()));

        System.out.println("(x,y) do mouse = (" + mousePointOnScreen.getX() +
                           "," +
                           mousePointOnScreen.getY() + ")");

        int novaColunaIndex = _beanTable.columnAtPoint(mousePointOnScreen);
        System.out.println("Coluna indice " + novaColunaIndex + " da tabela");

        // verificar se precisa redesenhar a descricao
        if (novaColunaIndex != _colunaIndex) {
            _colunaIndex = novaColunaIndex;

            // se mudou a coluna e a descricao
            // esta visivel entao redesenhe
            if (_estaVisivel == true) {
                this.showDescricao();
            }
        }
        */
    }

    private MultipleBeanTableModel getModel() {
        MultipleBeanTableModel mbtm;

        if (_beanTable.getModel()instanceof MultipleBeanTableModel) {
            mbtm = (MultipleBeanTableModel) _beanTable.getModel();
        }
        else {
            throw new RuntimeException(
                "Esta classe n\uFFFDo prove servi\uFFFDo para o model deste BeanTable");
        }

        return mbtm;
    }





















    ///////////////////////////////////////////////////
    // Ponto de Alinhamento
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int TOP = 0;
    public static final int MIDDLE = 1;
    public static final int BOTTOM = 2;
    public static PontoDeAlinhamento2[] pontosDeAlinhamento = {
        new PontoDeAlinhamento2(0, 0, 0, 2), new PontoDeAlinhamento2(0, 2, 0, 0)
    };

    public Rectangle calcularBoundsDescricao(PontoDeAlinhamento2 p) {
        Point compPos = _tableHeader.getLocationOnScreen();
        Point rootPanePos = _tableHeader.getRootPane().getLocationOnScreen();

        // component bounds on screen
        double compScreenLeft = compPos.getX() - rootPanePos.getX() +
            this.getHeaderColumnBounds(_colunaIndex).getX();
        double compScreenTop = compPos.getY() - rootPanePos.getY();
        double compScreenRight = compScreenLeft +
            this.getHeaderColumnBounds(_colunaIndex).getWidth();
        double compScreenBottom = compScreenTop +
            this.getHeaderColumnBounds(_colunaIndex).getHeight();

        // bounds deste componente no sist. coordenado da tela
        Rectangle componentBounds = new Rectangle( (int) compScreenLeft,
                                                  (int) compScreenTop,
                                                  (int) (compScreenRight -
            compScreenLeft),
                                                  (int) (compScreenBottom -
            compScreenTop));

        // component bounds on screen
        double left = componentBounds.getLocation().getX();
        double top = componentBounds.getLocation().getY();
        double right = left + componentBounds.getWidth();
        double bottom = top + componentBounds.getHeight();

        //////////////////////////////////////////////////
        // descricao bounds on screen

        // horizontal
        double dleft = left;
        if (p.get_componentHorizontal() == LEFT &&
            p.get_descricaoHorizontal() == LEFT) {
            dleft = left;
        }
        else if (p.get_componentHorizontal() == LEFT &&
                 p.get_descricaoHorizontal() == CENTER) {
            dleft = left - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == LEFT &&
                 p.get_descricaoHorizontal() == RIGHT) {
            dleft = left - _width;
        }
        else if (p.get_componentHorizontal() == CENTER &&
                 p.get_descricaoHorizontal() == LEFT) {
            dleft = (left + right) / 2.0;
        }
        else if (p.get_componentHorizontal() == CENTER &&
                 p.get_descricaoHorizontal() == CENTER) {
            dleft = (left + right) / 2.0 - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == CENTER &&
                 p.get_descricaoHorizontal() == RIGHT) {
            dleft = (left + right) / 2.0 - _width;
        }
        else if (p.get_componentHorizontal() == RIGHT &&
                 p.get_descricaoHorizontal() == LEFT) {
            dleft = right;
        }
        else if (p.get_componentHorizontal() == RIGHT &&
                 p.get_descricaoHorizontal() == CENTER) {
            dleft = right - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == RIGHT &&
                 p.get_descricaoHorizontal() == RIGHT) {
            dleft = right - _width;
        }

        // vertical
        double dtop = top;
        if (p.get_componentVertical() == TOP &&
            p.get_descricaoVertical() == TOP) {
            dtop = top;
        }
        else if (p.get_componentVertical() == TOP &&
                 p.get_descricaoVertical() == MIDDLE) {
            dtop = top - (_height / 2.0);
        }
        else if (p.get_componentVertical() == TOP &&
                 p.get_descricaoVertical() == BOTTOM) {
            dtop = top - _height;
        }
        else if (p.get_componentVertical() == MIDDLE &&
                 p.get_descricaoVertical() == TOP) {
            dtop = (top + bottom) / 2.0;
        }
        else if (p.get_componentVertical() == MIDDLE &&
                 p.get_descricaoVertical() == MIDDLE) {
            dtop = (top + bottom) / 2.0 - (_height / 2.0);
        }
        else if (p.get_componentVertical() == MIDDLE &&
                 p.get_descricaoVertical() == BOTTOM) {
            dtop = (top + bottom) / 2.0 - _height;
        }
        else if (p.get_componentVertical() == BOTTOM &&
                 p.get_descricaoVertical() == TOP) {
            dtop = bottom;
        }
        else if (p.get_componentVertical() == BOTTOM &&
                 p.get_descricaoVertical() == MIDDLE) {
            dtop = bottom - (_height / 2.0);
        }
        else if (p.get_componentVertical() == BOTTOM &&
                 p.get_descricaoVertical() == BOTTOM) {
            dtop = bottom - _height;
        }

        double dright = dleft + _width;
        double dbottom = dtop + _height;
        // descricao bounds on screen
        //////////////////////////////////////////////////

        return new Rectangle( (int) dleft, (int) dtop, (int) (dright - dleft),
                             (int) (dbottom - dtop));
    }

    public boolean testarSePontoDeAlinhamentoEstouraATela(PontoDeAlinhamento2 p) {
        //
        Rectangle drect = calcularBoundsDescricao(p);

        //
        Dimension windowDim = _tableHeader.getRootPane().getSize();

        //
        if (drect.getX() < 0 ||
            drect.getY() < 0 ||
            drect.getX() + drect.getWidth() >= windowDim.getWidth() ||
            drect.getY() + drect.getHeight() >= windowDim.getHeight()) {
            return false;
        }
        return true;
    }


}

////////////////////////////////////
// Ponto de Alinhamento
class PontoDeAlinhamento2 {
    int _componentHorizontal;
    int _componentVertical;
    int _descricaoHorizontal;
    int _descricaoVertical;

    public PontoDeAlinhamento2(
        int componentHorizontal,
        int componentVertical,
        int descricaoHorizontal,
        int descricaoVertical) {
        _componentHorizontal = componentHorizontal;
        _componentVertical = componentVertical;
        _descricaoHorizontal = descricaoHorizontal;
        _descricaoVertical = descricaoVertical;
    }

    public int get_componentHorizontal() {
        return _componentHorizontal;
    }

    public int get_componentVertical() {
        return _componentVertical;
    }

    public int get_descricaoHorizontal() {
        return _descricaoHorizontal;
    }

    public int get_descricaoVertical() {
        return _descricaoVertical;
    }
}