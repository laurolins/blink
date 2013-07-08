package blink.cli;

import java.util.ArrayList;

import blink.Gem;
import blink.GemVertex;

public class FunctionGemRes  extends Function {
	public FunctionGemRes() {
		super("gemRes","description");
	}

	public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
		try {
			Object result = hardwork(params, localData);
			return result;
		} catch (EvaluationException ex) {
			ex.printStackTrace();
			throw ex;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new EvaluationException(e.getMessage());
		}
	}

	public Object hardwork(ArrayList params, DataMap localMap) throws EvaluationException, Exception {
		if (params.get(0) instanceof Gem) {
			Resoluvel gemRes = new Resoluvel();
			return gemRes.find_AGPMDMX(((Gem)params.get(0)).getCurrentLabelling().getLettersString(","));
		}
		return "Error!";
	}
}
