package org.openhds.mobile.listener;

import org.openhds.mobile.task.HttpTask;

public interface SyncDatabaseListener {
	void collectionComplete(HttpTask.EndResult result);
}
