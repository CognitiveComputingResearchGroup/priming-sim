package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

public class PrimingEnvironment extends EnvironmentImpl{


    public static final int ENVIRONMENT_WIDTH = 100;
    public static final int ENVIRONMENT_HEIGHT = 100;

    private Map<String, Object> blankData= new HashMap<String, Object>();
    private Map<String, Object> targetData= new HashMap<String, Object>();
    private Map<String, Object> consistentPrimingData= new HashMap<String, Object>();
    private Map<String, Object> unconsistentPrimingData= new HashMap<String, Object>();
    
    private int blankDuration=0;

	@Override
	public void init(){
	blankDuration=getParam("blank_duration", 10);

	blankData.put("dot_color","white");
	blankData.put("dot_Xpos", 1);
	blankData.put("dot_Ypos", 1);

	targetData.put("annulus1_color", "red");	
	targetData.put("annulus2_color", "green");	
	targetData.put("annulus1_Xpos", 2);
	targetData.put("annulus1_Ypos", 2);
	targetData.put("annulus2_Xpos", 0);
	targetData.put("annulus2_Ypos", 0);

	consistentPrimingData.put("disc1_color", "red");	
	consistentPrimingData.put("disc2_color", "green");	
	consistentPrimingData.put("disc1_Xpos", 2);
	consistentPrimingData.put("disc1_Ypos", 2);
	consistentPrimingData.put("disc2_Xpos", 0);
	consistentPrimingData.put("disc2_Ypos", 0);

	unconsistentPrimingData.put("disc1_color", "red");	
	unconsistentPrimingData.put("disc2_color", "green");	
	unconsistentPrimingData.put("disc1_Xpos", 0);
	unconsistentPrimingData.put("disc1_Ypos", 0);
	unconsistentPrimingData.put("disc2_Xpos", 2);
	unconsistentPrimingData.put("disc2_Ypos", 2);

	}

	@Override
	public Object getState(Map<String, ?> arg0) {

		if(TaskManager.getCurrentTick()<700){
			return blankData;
		}
		else if(TaskManager.getCurrentTick()>700 && TaskManager.getCurrentTick()<(10+700)){
			if(getParam("consistent", true)){
			 return consistentPrimingData;	
			}
			else{
				return unconsistentPrimingData;
			}
		}
		else if(TaskManager.getCurrentTick()>(10+700) && TaskManager.getCurrentTick()<(blankDuration+10+700)){
			return blankData;
		}
		else{
			return targetData;
		}
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
