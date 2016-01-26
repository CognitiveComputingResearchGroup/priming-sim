package myagent.modules;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;

public class PrimingEnvironment extends EnvironmentImpl{


	public static final int FIXATION_PERIOD=1;
	public static final int PRIME_DURATION=2;

    public static final int ENVIRONMENT_WIDTH = 100;
    public static final int ENVIRONMENT_HEIGHT = 100;

    private Map<String, Object> blankData= new HashMap<String, Object>();
    private Map<String, Object> targetData= new HashMap<String, Object>();
    private Map<String, Object> consistentPrimingData= new HashMap<String, Object>();
    private Map<String, Object> inconsistentPrimingData= new HashMap<String, Object>();
    
    private int blankDuration=0;

	@Override
	public void init(){
	blankDuration=(int) getParam("blank_duration", 10);

	blankData.put("dot_color","white");
	blankData.put("dot_Xpos", 1);
	blankData.put("dot_Ypos", 1);


	targetData.put( "red",true);
	targetData.put( "green",true);
	targetData.put("red_position", 1);
	targetData.put("green_position", 3);

	consistentPrimingData.put("red", true);
	consistentPrimingData.put("green", true );
	consistentPrimingData.put("red_position", 1);
	consistentPrimingData.put("green_position", 3);

	inconsistentPrimingData.put( "red",true);
	inconsistentPrimingData.put( "green",true);
	inconsistentPrimingData.put("red_position", 3);
	inconsistentPrimingData.put("green_position", 1);
	}

	@Override
	public Object getState(Map<String, ?> arg0) {

		if(TaskManager.getCurrentTick()<=FIXATION_PERIOD){
			return blankData;
		}
		else if(TaskManager.getCurrentTick()>FIXATION_PERIOD && TaskManager.getCurrentTick()<=(PRIME_DURATION+FIXATION_PERIOD)){
			if((boolean) getParam("consistent", true)){
			 return consistentPrimingData;	
			}
			else{
				return inconsistentPrimingData;
			}
		}
		else if(TaskManager.getCurrentTick()>(PRIME_DURATION+FIXATION_PERIOD) && TaskManager.getCurrentTick()<=(blankDuration+PRIME_DURATION+FIXATION_PERIOD)){
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
