package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.database.queries.QueryResult;

public interface HierarchyNavigator {

	public Map<String, Integer> getStateLabels();

	public List<String> getStateSequence();

	public void jump(String state);

	public void descend(QueryResult qr);
}
