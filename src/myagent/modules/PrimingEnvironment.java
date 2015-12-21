package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;

public class PrimingEnvironment extends EnvironmentImpl{

    private Map<String, Object> sensedData = new HashMap<String, Object>();

    private Map<String, Object> targetData= new HashMap<String, Object>();
    private Map<String, Object> consistentPrimingData= new HashMap<String, Object>();
    private Map<String, Object> unconsistentPrimingData= new HashMap<String, Object>();

	@Override
	public void init(){
	targetData.put("annulus1_color", "red");	
	targetData.put("annulus2_color", "green");	
	targetData.put("annulus1_Xpos", 1);
	targetData.put("annulus1_Ypos", 1);
	targetData.put("annulus2_Xpos", 0);
	targetData.put("annulus2_Ypos", 0);

	consistentPrimingData.put("circle1_color", "red");	
	consistentPrimingData.put("circle2_color", "green");	
	consistentPrimingData.put("circle1_Xpos", 1);
	consistentPrimingData.put("circle1_Ypos", 1);
	consistentPrimingData.put("circle2_Xpos", 0);
	consistentPrimingData.put("circle2_Ypos", 0);

	unconsistentPrimingData.put("circle1_color", "red");	
	unconsistentPrimingData.put("circle2_color", "green");	
	unconsistentPrimingData.put("circle1_Xpos", 0);
	unconsistentPrimingData.put("circle1_Ypos", 0);
	unconsistentPrimingData.put("circle2_Xpos", 1);
	unconsistentPrimingData.put("circle2_Ypos", 1);

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
