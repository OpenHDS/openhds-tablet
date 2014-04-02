package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

public interface HierarchyNavigator {
	
	public Map<String, Integer> getStateLabels();
	public List<String> getStateSequence();
	
	public void jump(String state);
	
	//TODO take in a QueryResult
	public void descend();
}
