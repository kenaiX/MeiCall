package cc.kenai.meicall.voip;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import cc.kenai.meicall.R;
import cc.kenai.meicall.call.utils.UserInfoUtil;
import cc.kenai.meicall.ui.VerticalSeekBar;

import com.hisun.phone.core.voice.Device.CallType;
import com.hisun.phone.core.voice.util.VoiceUtil;
import com.kenai.function.message.XLog;

/**
 * 
 * Voip呼出界面，呼出方用于显示和操作通话过程。
 * 
 * @version 1.0.0
 */
public class CallOutActivity extends BaceCallActivity implements
		OnClickListener {
	public final static String CancelCall = "cc.kenai.meicall.CallOutActivity.CancelCall";

	String TAG = "CallOutActivity";
	// 免提按钮
	private ImageView mCallHandFree;
	// 挂断按钮
	private ImageView mCallShutdown;
	private VerticalSeekBar mCallSound;
	private TextView text;
	TextView state;

	boolean isHandsFree = false;

	BroadcastReceiver myReceiveCallReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(CancelCall)) {
				toast("对方挂断");
				finish();
			}
		}
	};

	@Override
	public void onConnectedSuccseed() {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent it = getIntent();
				if (it == null) {
					finish();
					return;
				}
				Bundle bd = it.getExtras();
				if (bd == null) {
					finish();
					return;
				}
				final String action = bd.getString(Intent_First_Action);
				if (action == null) {
					finish();
					return;
				}
				if (action.equals(Intent_Second_Action_CALLOUT_P2L)) {
					final String second = bd
							.getString(Intent_First_PhoneNumber);
					if (second != null && !second.equals("")) {
						XLog.xLog(TAG, "phonenumber : " + second);
						text.setText(second);
						calloutID = mDevice.makeCall(CallType.VOICEP2L,
								VoiceUtil.getStandardMDN(second));
						XLog.xLog(TAG, "make call : " + calloutID);
						if (calloutID == null || calloutID.length() < 1) {
							finish();
							return;
						}

					} else {
						finish();
						return;
					}
				} else if (action.equals(Intent_Second_Action_CALLOUT_P2P)) {
					final String second = bd
							.getString(Intent_First_PhoneNumber);
					if (second != null && !second.equals("")) {
						XLog.xLog(TAG, "phonenumber : " + second);
						text.setText("internet");
						calloutID = mDevice.makeCall(CallType.VOICEP2P, second);
						XLog.xLog(TAG, "make call : " + calloutID);
						if (calloutID == null || calloutID.length() < 1) {
							finish();
							return;
						}

					} else {
						finish();
						return;
					}
				} else if (action.equals(Intent_Second_Action_CALLOUT_CALLBACK)) {
					XLog.xLog(TAG, "action : callback");
					final String second = bd
							.getString(Intent_First_PhoneNumber);
					if (second != null && !second.equals("")) {
						XLog.xLog(TAG, "phonenumber : " + second);
						text.setText(second);
							String phone = UserInfoUtil.getYuntongxunInfo(
									CallOutActivity.this).getPhone();
							if (phone != null && phone.length() > 0) {
								mDevice.makeCallback(
										VoiceUtil.getStandardMDN(phone),
										VoiceUtil.getStandardMDN(second));
								toast("请注意接听系统来电");
							} else {
								toast("请先绑定手机号码");
							}

					} else {
						finish();
						return;
					}
				}
			}
		}, 500);

	}

	@Override
	public void onCallAnswered() {
		// TODO 自动生成的方法存根

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_call_interface);
		initButton();
		registerReceiver(myReceiveCallReceiver, new IntentFilter(CancelCall));
	}

	private void initButton() {
		mCallHandFree = (ImageView) findViewById(R.id.callout_loud);
		mCallShutdown = (ImageView) findViewById(R.id.callout_shut);
		mCallSound = (VerticalSeekBar) findViewById(R.id.callout_sound);
		text = (TextView) findViewById(R.id.callout_text);
		state = (TextView) findViewById(R.id.callout_state);
		mCallSound.setAlpha(0);
		// am.set

		mCallHandFree.setOnClickListener(this);
		mCallShutdown.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.callout_shut:

			// 挂断电话
			finish();
			break;
		case R.id.callout_loud:
			// 设置免提
			sethandfreeUI();
			break;
		}

	}

	/**
	 * 设置免提
	 */
	private void sethandfreeUI() {
		try {
			if (isHandsFree) {
				mCallHandFree
						.setImageResource(R.drawable.btn_call_loud_background);
			} else {
				mCallHandFree
						.setImageResource(R.drawable.btn_call_loud_background_select);
			}
			getDevice().enableLoudsSpeaker(isHandsFree);
			isHandsFree = getDevice().getLoudsSpeakerStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(myReceiveCallReceiver);
		stopNotification(this);
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 屏蔽返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		stopNotification(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		startNotification(this);
	}

	public final static void startNotification(Context context) {
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "通话服务转入后台运行";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		CharSequence contentTitle = "通话服务";
		CharSequence contentText = "点击打开通话页面";
		Intent notificationIntent = new Intent(context, CallOutActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(14331234, notification);
	}

	public final static void stopNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(14331234);
	}

}
