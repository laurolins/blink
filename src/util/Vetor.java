package util;

public class Vetor {
	private double _x;
	private double _y;
	
	
	
	public Vetor(double x, double y) {
		super();
		_x = x;
		_y = y;
	}
	
	public double getX() {
		return _x;
	}
	public void setX(double x) {
		_x = x;
	}
	public double getY() {
		return _y;
	}
	public void setY(double y) {
		_y = y;
	}
	
	public Vetor soma (Vetor v){
		return new Vetor(this._x + v.getX(), + this._y + v.getY());
	}
	
	public Vetor subtracao (Vetor v){
		return new Vetor(this._x - v.getX(), - this._y + v.getY());
	}
	
	public Vetor multiplicacao(double d){
		return new Vetor(this._x*d, this._y*d);
	}
	
	public double produtoInterno (Vetor v){
		return (this._x * v.getX()) + (this._y * v.getY());
	}
	
	public double modulo (){
		return Math.sqrt(this.produtoInterno(this));
	}
	
	public void normalize() {
		double modulo = this.modulo();
		_x = _x / modulo;
		_y = _y / modulo;
	}
	
	public Vetor projecaoSobre (Vetor v){
		double k = v.produtoInterno(this);
		k = k / v.produtoInterno(v);
		
		return v.multiplicacao(k);
	} 
	
}