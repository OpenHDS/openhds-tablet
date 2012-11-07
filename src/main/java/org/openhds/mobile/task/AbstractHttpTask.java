package org.openhds.mobile.task;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.os.AsyncTask;

public abstract class AbstractHttpTask<Params, Progress> extends
		AsyncTask<Params, Progress, AbstractHttpTask.EndResult> {
	private static final int UNAUTHORIZED_STATUS_CODE = 401;
	private static final int SUCCESS_STATUS_CODE = 200;
	private static final int NO_CONTENT_CODE = 204;

	protected RequestContext requestCtx;
	private TaskListener listener;

	public AbstractHttpTask(RequestContext requestCtx, TaskListener listener) {
		this.requestCtx = requestCtx;
		this.listener = listener;
	}

	static enum EndResult {
		BAD_AUTHENTICATION, CONNECTION_ERROR, CONNECTION_TIMEOUT, SUCCESS, FAILURE, NO_CONTENT
	}

	public interface TaskListener {
		void onFailedAuthentication();

		void onConnectionError();

		void onConnectionTimeout();

		void onSuccess();

		void onFailure();

		void onNoContent();
	}

	public static class RequestContext {
		URL url;
		String user;
		String password;

		public RequestContext url(URL url) {
			this.url = url;
			return this;
		}

		public RequestContext user(String user) {
			this.user = user;
			return this;
		}

		public RequestContext password(String password) {
			this.password = password;
			return this;
		}
	}

	@Override
	protected EndResult doInBackground(Params... params) {
		DefaultHttpClient httpClient = buildHttpClient(requestCtx.user,
				requestCtx.password);
		try {
			HttpResponse response = executeGet(httpClient, requestCtx.url);
			switch(response.getStatusLine().getStatusCode()) {
			case SUCCESS_STATUS_CODE:
				return handleResponseData(response);
			case NO_CONTENT_CODE:
				return EndResult.NO_CONTENT;
			case UNAUTHORIZED_STATUS_CODE:
				return EndResult.BAD_AUTHENTICATION;
			default:
				return EndResult.CONNECTION_ERROR;
			}
		} catch (ClientProtocolException e) {
			return EndResult.CONNECTION_ERROR;
		} catch (ConnectTimeoutException e) {
			return EndResult.CONNECTION_TIMEOUT;
		} catch (IOException e) {
			return EndResult.CONNECTION_ERROR;
		}
	}

	public DefaultHttpClient buildHttpClient(String user, String password) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		setHttpClientParams(httpClient);
		setHttpClientCredentials(httpClient, user, password);
		return httpClient;
	}

	private void setHttpClientParams(DefaultHttpClient httpClient) {
		httpClient.getParams().setIntParameter(
				HttpConnectionParams.CONNECTION_TIMEOUT, 60 * 1000);
	}

	private void setHttpClientCredentials(DefaultHttpClient httpClient,
			String user, String password) {
		AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
				AuthScope.ANY_REALM);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				user, password);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(scope, creds);

		httpClient.setCredentialsProvider(credsProvider);
	}

	public HttpResponse executeGet(DefaultHttpClient client, URL url)
			throws ClientProtocolException, IOException {
		HttpHost host = new HttpHost(url.getHost(), url.getPort());
		HttpGet httpget = new HttpGet(url.getPath());
		return client.execute(host, httpget);
	}

	@Override
	protected void onPostExecute(DownloadFormsTask.EndResult result) {
		switch (result) {
		case BAD_AUTHENTICATION:
			listener.onFailedAuthentication();
			break;
		case FAILURE:
			listener.onFailure();
			break;
		case CONNECTION_ERROR:
			listener.onConnectionError();
			break;
		case CONNECTION_TIMEOUT:
			listener.onConnectionTimeout();
			break;
		case SUCCESS:
			listener.onSuccess();
			break;
		case NO_CONTENT:
			listener.onNoContent();
			break;
		}
	}

	protected abstract EndResult handleResponseData(HttpResponse response);
}
