package org.openhds.mobile.task;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openhds.mobile.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class HttpTask<Params, Progress> extends
		AsyncTask<Params, Progress, HttpTask.EndResult> {
	private static final int UNAUTHORIZED_STATUS_CODE = 401;
	private static final int SUCCESS_STATUS_CODE = 200;
	private static final int NO_CONTENT_CODE = 204;

	protected RequestContext requestContext;
	protected TaskListener listener;
	protected HttpGet httpGet;
	protected Context ctx;

	public HttpTask(Context ctx, RequestContext requestContext, TaskListener listener) {
		this(ctx, requestContext);
		this.listener = listener;
	}

	public HttpTask(Context ctx, RequestContext requestContext) {
		this.ctx = ctx;
		this.requestContext = requestContext;
		httpGet = new HttpGet(requestContext.url.getPath());
	}

	public static enum EndResult {
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
		DefaultHttpClient httpClient = buildHttpClient(requestContext.user,
				requestContext.password);
		try {
			HttpResponse response;
			boolean isReachable = false;
			isReachable = isReachable(requestContext.url);
			if(isReachable){
				response = executeGet(httpClient, requestContext);
			}
			else{
				return EndResult.CONNECTION_ERROR;
			}
			switch (response.getStatusLine().getStatusCode()) {
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
		} catch (AuthenticationException e) {
			return EndResult.BAD_AUTHENTICATION;
		}
		catch(Exception e){
			return EndResult.CONNECTION_ERROR;
		}
	}
	
	private boolean isReachable(URL pUrl){
		boolean result = false;
		try
		{
	        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
	        String serverKey = getResourceString(ctx,R.string.openhds_server_url_key);
	        String openhdsurl = settings.getString(serverKey, "UNDEF");
					
		    HttpGet request = new HttpGet(openhdsurl);
		    HttpParams httpParameters = new BasicHttpParams();
		    int timeout = 3000;
		    HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
		    HttpClient httpClient = new DefaultHttpClient(httpParameters);
		    HttpResponse response = httpClient.execute(request);

		    int status = response.getStatusLine().getStatusCode();
		    if (status == HttpStatus.SC_OK) 
		    {
		        result = true;
		    }
		}
		catch (SocketTimeoutException e)
		{
		    result = false;
		} catch (ClientProtocolException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		}
		return result;
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
		httpClient.getParams().setIntParameter(
				HttpConnectionParams.SO_TIMEOUT, 60 * 1000);		
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

	public HttpResponse executeGet(DefaultHttpClient client,
			RequestContext requestContext) throws ClientProtocolException,
			IOException, AuthenticationException {
		HttpHost host = new HttpHost(requestContext.url.getHost(),
				requestContext.url.getPort());

		// preemptively provide credentials
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				requestContext.user, requestContext.password);
		httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));

		return client.execute(host, httpGet);
	}

	public void addHeader(String name, String value) {
		httpGet.addHeader(name, value);
	}

	@Override
	protected void onPostExecute(EndResult result) {
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

	protected EndResult handleResponseData(HttpResponse response) {
		return EndResult.SUCCESS;
	}
}
