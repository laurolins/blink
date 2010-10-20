package linsoft.gui.bean;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Classe do table model para o painel de configuração das colunas
 * de uma tabela.
 */
public class ColunaTableModel extends AbstractTableModel {

	/**
	 * Classe para representar as colunas da tabela sendo configurada.
	 */
	class Coluna {
		private String _nome;
		private boolean _visivel;
        private int _id;
		public Coluna(String nome, boolean visivel, int id) {
			_nome = nome;
			_visivel = visivel;
            _id = id;
		}
		public void setVisivel(boolean v) {
			_visivel = v;
		}
		public String getNome() {
			return _nome;
		}
		public boolean getVisivel() {
			return _visivel;
		}
		public int getId() {
			return _id;
		}
	}


	/**
	 * Conjunto das colunas a serem editadas.
	 */
	private Vector _colunas;

	/**
	 * Table model que será editado.
	 */
	private TableModel _tableModel;


	public ColunaTableModel() {
		_colunas = new Vector();
	}


	public void setTableModel(TableModel tableModel) {
		_colunas.clear();
		_tableModel = tableModel;
        if(_tableModel instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel beanModel = (MultipleBeanTableModel) _tableModel;
            int[] visiveis = beanModel.getVisiblePropertySequence();
            for(int i = 0; i < beanModel.getNumberOfProperties(); i++) {
                boolean visivel = false;

                for(int j = 0; !visivel && j < visiveis.length; j++) {
                    if(visiveis[j] == i)
                        visivel = true;
                }

                Coluna coluna = new Coluna(beanModel.getPropertyName(i), visivel, i);
                _colunas.add(coluna);
            }
        }
        else {
            for(int i = 0; i < _tableModel.getColumnCount(); i++) {
                Coluna coluna = new Coluna(_tableModel.getColumnName(i), true, i);
                _colunas.add(coluna);
            }
        }
	}


	public String getColumnName(int col) {
		String nome = null;
		if(col == 0)
			nome = "Nome";
		else if(col == 1)
			nome = "Visível";
		return nome;
	}


	public int getColumnCount() {
		return 2;
	}


	public Class getColumnClass(int col) {
		Class cl = null;
		if(col == 0)
			cl = String.class;
		else if(col == 1)
			cl = Boolean.class;
		return cl;
	}


	public Object getValueAt(int lin, int col) {
		Coluna coluna = (Coluna) _colunas.get(lin);
		Object valor = null;
		if(col == 0)
			valor = coluna.getNome();
		else if(col == 1)
			valor = new Boolean(coluna.getVisivel());
		return valor;
	}


	public void setValueAt(Object valor, int lin, int col) {
		Coluna coluna = (Coluna) _colunas.get(lin);
		if(col == 1) {
			Boolean bool = (Boolean) valor;
			coluna.setVisivel(bool.booleanValue());
		}
	}


	public boolean isCellEditable(int lin, int col) {
		boolean editavel = false;
		if(col == 1)
			editavel = true;
		return editavel;
	}


	public int getRowCount() {
		return _colunas.size();
	}


	public void diminuirPosicao(int i) {
		if(i > 0) {
			Object o1 = _colunas.get(i - 1);
			Object o2 = _colunas.get(i);
			_colunas.setElementAt(o1, i);
			_colunas.setElementAt(o2, i - 1);
		}
	}


    public void aumentarPosicao(int i) {
		if(i < _colunas.size() - 1) {
			Object o1 = _colunas.get(i);
			Object o2 = _colunas.get(i + 1);
			_colunas.setElementAt(o1, i + 1);
			_colunas.setElementAt(o2, i);
		}
	}


    public void atualizarModelo() {
        if(_tableModel instanceof MultipleBeanTableModel) {
            MultipleBeanTableModel beanModel = (MultipleBeanTableModel) _tableModel;

            int numColunasVisiveis = 0;
            for(int i = 0; i < _colunas.size(); i++) {
                Coluna coluna = (Coluna) _colunas.get(i);
                if(coluna.getVisivel())
                    numColunasVisiveis++;
            }

            int[] ids = new int[numColunasVisiveis];

            for(int i = 0, j = 0; i < _colunas.size(); i++) {
                Coluna coluna = (Coluna) _colunas.get(i);
                if(coluna.getVisivel()) {
                    ids[j] = coluna.getId();
                    j++;
                }
            }

            beanModel.setVisiblePropertySequence(ids);
        }
    }
}