package cc.kenai.meicall.call.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import cc.kenai.meicall.call.config.RestApi;
import cc.kenai.meicall.common.StringUtils;
/**
 * 此工具类用来客户端给主服务器发送消息
 * @author kenai
 *
 */
public class LocalPushUtils {

	public final class TaskMessage {
		private JSONObject task = new JSONObject();

		public TaskMessage() {

		}

		public final void addTask(String action, String value)
				throws JSONException {
			this.task.put(action, value);
		}

		public final JSONObject getJson() {
			return task;
		}
	}

	public final class IDMessage {
		private JSONObject Id = new JSONObject();

		public IDMessage() {

		}

		public final JSONObject getJson() {
			return Id;
		}
	}

	public final class PushMessage {
		private JSONObject message = new JSONObject();

		public PushMessage(IDMessage id, TaskMessage task) throws JSONException {
			message.put("id", id.getJson());
			message.put("task", task.getJson());
		}

		@Override
		public final String toString() {
			return message.toString().trim();
		}

	}

	public abstract class CallBack {
		HttpResponse response;

		public void receiveCallBack(HttpResponse response) {
			this.response = response;
		}

		public int getStatusCode() {
			return response.getStatusLine().getStatusCode();
		}

		public abstract void onError();

		public abstract void onCallBack();
	}

	public final static void pushToNet(PushMessage pm, CallBack cb) {
		/*
		 * 初始化client
		 */
		final HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 10000);
		HttpConnectionParams.setSoTimeout(params, 30000);
		final HttpClient httpclient = new DefaultHttpClient(params);
		final HttpPost httpPost = new HttpPost(RestApi.ServiceUri);
		/*
		 * 创建请求
		 */
		HttpEntity my;
		try {
			my = new StringEntity(StringUtils.encode(pm.toString()));
			httpPost.setEntity(my);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		/*
		 * 发送请求
		 */
		HttpResponse response;

		try {
			response = httpclient.execute(httpPost);
			cb.receiveCallBack(response);
			cb.onCallBack();
		} catch (ClientProtocolException e) {
			cb.onError();
			e.printStackTrace();
		} catch (IOException e) {
			cb.onError();
			e.printStackTrace();
		}

	}
}
