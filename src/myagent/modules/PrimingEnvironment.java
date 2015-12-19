package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;

public class PrimingEnvironment extends EnvironmentImpl{

    private Map<String, Object> sensedData = new HashMap<String, Object>();

    private Map<String, Object> data= new HashMap<String, Object>();

	@Override
	public void init(){
		
	}

	@Override
	public Object getState(Map<String, ?> arg0) {

		return sensedData;
	}

	@Override
	public void processAction(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
	}

}
