package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DescricaoComponente {
    private JComponent _component;

    private boolean _estaVisivel = false;

    JLabel _label = new JLabel();

    //
    private String _descricao;
    private int _width;
    private int _height;

    private javax.swing.Timer _timer;
    private int _segundosEspera = 4;

    /////////////////////////////////
    // modo debug
    private boolean _debug = false;
    private javax.swing.Timer _timerDebug;
    private int _contadorSegundos;
    JLabel _labelDebug = new JLabel();
    // modo debug
    /////////////////////////////////

    // create an empty and non opaque glass pane
    JPanel _glass = new JPanel();

    public DescricaoComponente(JComponent component, String descricao, int width, int height) {
        _component = component;
        _descricao = descricao;
        _width = width;
        _height = height;

        _glass.setOpaque(false);
        _glass.setLayout(null);
        _glass.add(_label);
        _glass.add(_labelDebug);

        this.installTimer();

        this.install();
    }

    private void installTimer() {
        //////////////////////////////////////
        // install timers
        // criar os timers para esperar...
        // wait a second and then execute these...
        _timer = new javax.swing.Timer(_segundosEspera * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDescricao();
            }
        });
        // install timers
        ////////////////////////////////////
    }

    private void installTimerDebug() {
        //////////////////////////////////////
        // install timers
        // criar os timers para esperar...
        // wait a second and then execute these...
        _debug = true;
        _timerDebug = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDebug();
            }
        });
        // install timers
        ////////////////////////////////////

    }

    public void install() {

        _component.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                in(e);

            }

            public void mouseExited(MouseEvent e) {
                out();
            }
        });
    }

    public void showDebug() {
        if (_contadorSegundos < _segundosEspera) {
            // renderizar o debug

            String debugText = _contadorSegundos + "";

            // texto da descricao deste componente
            _labelDebug.setText(debugText);

            // constantes para o tamanho
            int width = debugText.length() * 30;
            int height = 40; // fixo

            _labelDebug.setFont(new Font("Arial", 1, 15));
            _labelDebug.setHorizontalAlignment(JLabel.CENTER);
            _labelDebug.setSize(width, height);

            // verificar algum alinhamento que não estoure os limites da tela
            // se não exisitr nenhum então escolha algum aleatório
            Rectangle rectDescricao = null;
            boolean achouAlinhamento = false;

            rectDescricao = _component.getBounds();

            //
            Point compPos = _component.getLocationOnScreen();
            Point rootPanePos = _component.getRootPane().getLocationOnScreen();

            int x = (int) (compPos.getX() - rootPanePos.getX());
            int y = (int) (compPos.getY() - rootPanePos.getY());

            _labelDebug.setLocation(x, y);

            // teste
            _labelDebug.setBackground(Color.RED);

            _labelDebug.setOpaque(true);

//
            _component.getRootPane().setGlassPane(_glass);

            _labelDebug.setVisible(true);
            _glass.setVisible(true);

            // incrementando o timer debug...
            _contadorSegundos++;
        } else {
            _labelDebug.setVisible(false);
            this.stopTimerDebug();
        }
    }

    public void showDescricao() {
        if (_estaVisivel == false) {

            // texto da descricao deste componente
            _label.setText(_descricao);

            // constantes para o tamanho
            // int width = _descricao.length() * 7;
            //int height = 20; // fixo
            _label.setSize(_width, _height);

            // verificar algum alinhamento que não estoure os limites da tela
            // se não exisitr nenhum então escolha algum aleatório
            Rectangle rectDescricao = null;
            boolean achouAlinhamento = false;

            for (int i = 0; i < pontosDeAlinhamento.length &&
                 !achouAlinhamento; i++) {
                if (this.testarSePontoDeAlinhamentoEstouraATela(pontosDeAlinhamento[i])) {
                    rectDescricao = this.calcularBoundsDescricao(pontosDeAlinhamento[i]);
                    achouAlinhamento = true;
                }
            }

            // se não achou alinh. entao pegue o primeiro
            if (!achouAlinhamento) {
                rectDescricao = this.calcularBoundsDescricao(pontosDeAlinhamento[0]);
            }

            /*
                         // constantes para a posicao
                         // posicao do topLeft do componente sist. coordenada da tela
                         Point compPos = _component.getLocationOnScreen();
                         Point rootPanePos = _component.getRootPane().getLocationOnScreen();
                         compPos.translate(0, - _height);
                         compPos.translate((int)- rootPanePos.getX(), (int)- rootPanePos.getY());
                         // posicao do evento do mouse no sist. coordenada do componente
                         Point mousePosComp = e.getPoint();
                         // posicao do evento do mouse no sist. coordenada da tela
                         int x = (int) (mousePosComp.getX() + compPos.getX());
                         int y = (int) (mousePosComp.getY() + compPos.getY());
                         System.out.println("(x,y) do mouse = ("+x+","+y+")");
             */

            //
            Point compPos = _component.getLocationOnScreen();
            Point rootPanePos = _component.getRootPane().getLocationOnScreen();
            //int x = (int) (rectDescricao.getX() - compPos.getX());
            //int y = (int) (rectDescricao.getY() - compPos.getY());

            int x = (int) (rectDescricao.getX() - rootPanePos.getX());
            int y = (int) (rectDescricao.getY() - rootPanePos.getY());
            //System.out.println("(x,y) do DescricaoComponente = ("+x+","+y+")");

            _label.setLocation(x, y);

            // teste
            _label.setBackground(Color.YELLOW);

            _label.setOpaque(true);

            //
            _component.getRootPane().setGlassPane(_glass);

            _label.setVisible(true);
            _glass.setVisible(true);
            _estaVisivel = true;
        }
    }

    public void in(MouseEvent e) {
        this.startTimer();

        if (_debug) {
            this.startTimerDebug();
        }
    }

    ///////////////////////////////////////////////////
    // Ponto de Alinhamento
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int TOP = 0;
    public static final int MIDDLE = 1;
    public static final int BOTTOM = 2;
    public static PontoDeAlinhamento[] pontosDeAlinhamento = {
        new PontoDeAlinhamento(0, 0, 0, 2), new PontoDeAlinhamento(0, 2, 0, 0)
    };

    private void startTimer() {
        _timer.setRepeats(false);
        _timer.start();
    }

    private void startTimerDebug() {
        _timerDebug.setRepeats(true);
        _timerDebug.start();
        showDebug();
    }

    private void stopTimer() {
        _timer.stop();
    }

    private void stopTimerDebug() {
        _timerDebug.stop();
        _contadorSegundos = 0;
    }

    public Rectangle calcularBoundsDescricao(PontoDeAlinhamento p) {
        // component bounds on screen
        double compScreenLeft = _component.getLocationOnScreen().getX();
        double compScreenTop = _component.getLocationOnScreen().getY();
        double compScreenRight = compScreenLeft + _component.getWidth();
        double compScreenBottom = compScreenTop + _component.getHeight();

        // bounds deste componente no sist. coordenado da tela
        Rectangle componentBounds = new Rectangle( (int) compScreenLeft,
                                                  (int) compScreenTop,
                                                  (int) (compScreenRight - compScreenLeft),
                                                  (int) (compScreenBottom - compScreenTop));

        // component bounds on screen
        double left = componentBounds.getLocation().getX();
        double top = componentBounds.getLocation().getY();
        double right = left + componentBounds.getWidth();
        double bottom = top + componentBounds.getHeight();

        //////////////////////////////////////////////////
        // descricao bounds on screen

        // horizontal
        double dleft = left;
        if (p.get_componentHorizontal() == LEFT && p.get_descricaoHorizontal() == LEFT) {
            dleft = left;
        }
        else if (p.get_componentHorizontal() == LEFT && p.get_descricaoHorizontal() == CENTER) {
            dleft = left - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == LEFT && p.get_descricaoHorizontal() == RIGHT) {
            dleft = left - _width;
        }
        else if (p.get_componentHorizontal() == CENTER && p.get_descricaoHorizontal() == LEFT) {
            dleft = (left + right) / 2.0;
        }
        else if (p.get_componentHorizontal() == CENTER && p.get_descricaoHorizontal() == CENTER) {
            dleft = (left + right) / 2.0 - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == CENTER && p.get_descricaoHorizontal() == RIGHT) {
            dleft = (left + right) / 2.0 - _width;
        }
        else if (p.get_componentHorizontal() == RIGHT && p.get_descricaoHorizontal() == LEFT) {
            dleft = right;
        }
        else if (p.get_componentHorizontal() == RIGHT && p.get_descricaoHorizontal() == CENTER) {
            dleft = right - (_width / 2.0);
        }
        else if (p.get_componentHorizontal() == RIGHT && p.get_descricaoHorizontal() == RIGHT) {
            dleft = right - _width;
        }

        // vertical
        double dtop = top;
        if (p.get_componentVertical() == TOP && p.get_descricaoVertical() == TOP) {
            dtop = top;
        }
        else if (p.get_componentVertical() == TOP && p.get_descricaoVertical() == MIDDLE) {
            dtop = top - (_width / 2.0);
        }
        else if (p.get_componentVertical() == TOP && p.get_descricaoVertical() == BOTTOM) {
            dtop = top - _width;
        }
        else if (p.get_componentVertical() == MIDDLE && p.get_descricaoVertical() == TOP) {
            dtop = (top + bottom) / 2.0;
        }
        else if (p.get_componentVertical() == MIDDLE && p.get_descricaoVertical() == MIDDLE) {
            dtop = (top + bottom) / 2.0 - (_width / 2.0);
        }
        else if (p.get_componentVertical() == MIDDLE && p.get_descricaoVertical() == BOTTOM) {
            dtop = (top + bottom) / 2.0 - _width;
        }
        else if (p.get_componentVertical() == BOTTOM && p.get_descricaoVertical() == TOP) {
            dtop = bottom;
        }
        else if (p.get_componentVertical() == BOTTOM && p.get_descricaoVertical() == MIDDLE) {
            dtop = bottom - (_width / 2.0);
        }
        else if (p.get_componentVertical() == BOTTOM && p.get_descricaoVertical() == BOTTOM) {
            dtop = bottom - _width;
        }

        double dright = left + _width;
        double dbottom = top + _height;
        // descricao bounds on screen
        //////////////////////////////////////////////////

        return new Rectangle( (int) dleft, (int) dtop, (int) (dright - dleft), (int) (dbottom - dtop));
    }

    public boolean testarSePontoDeAlinhamentoEstouraATela(PontoDeAlinhamento p) {
        //
        Rectangle drect = calcularBoundsDescricao(p);

        //
        Dimension windowDim = _component.getRootPane().getSize();

        //
        if (drect.getX() < 0 ||
            drect.getY() < 0 ||
            drect.getX() + drect.getWidth() >= windowDim.getWidth() ||
            drect.getY() + drect.getHeight() >= windowDim.getHeight()) {
            return false;
        }
        return true;
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

    public void setSegundosEspera(int segundosEspera) {
        this._segundosEspera = segundosEspera;
    }

    public void setDebug(boolean modoDebug) {
        if (modoDebug) {
            _debug = true;
            this.installTimerDebug();
        }
    }

}

////////////////////////////////////
// Ponto de Alinhamento
class PontoDeAlinhamento {
    int _componentHorizontal;
    int _componentVertical;
    int _descricaoHorizontal;
    int _descricaoVertical;
    public PontoDeAlinhamento(
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
// Ponto de Alinhamento
