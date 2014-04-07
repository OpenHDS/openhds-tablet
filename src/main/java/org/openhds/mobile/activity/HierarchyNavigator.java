package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.database.queries.QueryResult;

public interface HierarchyNavigator {

	public Map<String, Integer> getStateLabels();

	public List<String> getStateSequence();

	public void jumpUp(String state);

	public void stepDown(QueryResult qr);
}
