package org.openhds.mobile.task;

import org.apache.http.HttpResponse;
import org.openhds.mobile.database.DatabaseAdapter;
import org.openhds.mobile.model.Supervisor;

/**
 * Task to initially authenticate a user when they first use the application
 */
public class AuthenticateTask extends AbstractHttpTask<Void, Void> {

	private DatabaseAdapter store;

	public AuthenticateTask(AbstractHttpTask.RequestContext requestCtx,
			TaskListener listener, DatabaseAdapter store) {
		super(requestCtx, listener);
		this.store = store;
	}

	@Override
	protected EndResult handleResponseData(HttpResponse response) {
		Supervisor user = new Supervisor();
		user.setName(requestCtx.user);
		user.setPassword(requestCtx.password);

		store.addSupervisor(user);

		return EndResult.SUCCESS;
	}

}
