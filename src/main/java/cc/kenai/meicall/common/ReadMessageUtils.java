package cc.kenai.meicall.common;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * 此工具类用来解析来自百度服务器的消息
 * @author kenai
 *
 */
public class ReadMessageUtils {
	public final static class IDMessage {
		protected JSONObject Id = new JSONObject();

		protected IDMessage(JSONObject Id) {
			this.Id = Id;
		}

	}

	public final static class TaskMessage {
		protected JSONObject task = new JSONObject();

		protected TaskMessage(JSONObject task) {
			this.task = task;
		}
		public JSONObject getJson() {
			return this.task;
		}
	}

	public final static class PushMessage {
		private JSONObject message;
		private IDMessage id;
		private TaskMessage task;

		protected PushMessage(String message) throws JSONException {
			this.message = new JSONObject(message);
			id = new IDMessage(this.message.getJSONObject("id"));
			task = new TaskMessage(this.message.getJSONObject("task"));
		}

		public TaskMessage getTask() {
			return task;
		}

		public IDMessage getId() {
			return id;
		}

		@Override
		public final String toString() {
			return message.toString().trim();
		}

	}

	public final static PushMessage analysis(String message) throws JSONException, UnsupportedEncodingException {
		return new PushMessage(StringUtils.decode(message));
	}
}
