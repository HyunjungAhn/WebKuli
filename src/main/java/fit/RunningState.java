package fit;

public class RunningState {
	private FlowState runState;
	
	public RunningState () {
		runState = FlowState.RUN;
	}
	
	public RunningState (FlowState state) {
		runState = state;
	}
	
	public FlowState getRunState() {
		return runState;
	}
	
	public void setRunState(FlowState runState) {
		this.runState = runState;
	}
	

}
