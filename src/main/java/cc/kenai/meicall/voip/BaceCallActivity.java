package cc.kenai.meicall.voip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.hisun.phone.core.voice.CCPCall;
import com.hisun.phone.core.voice.Device;
import com.hisun.phone.core.voice.DeviceListener;
import com.hisun.phone.core.voice.CCPCall.InitListener;
import com.hisun.phone.core.voice.listener.OnInterphoneListener;
import com.hisun.phone.core.voice.listener.OnVoIPListener;
import com.hisun.phone.core.voice.model.CloopenReason;
import com.hisun.phone.core.voice.model.chatroom.Chatroom;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMember;
import com.hisun.phone.core.voice.model.chatroom.ChatroomMsg;
import com.hisun.phone.core.voice.model.im.InstanceMsg;
import com.hisun.phone.core.voice.model.interphone.InterphoneMember;
import com.hisun.phone.core.voice.model.interphone.InterphoneMsg;
import com.hisun.phone.core.voice.model.setup.UserAgentConfig;
import com.kenai.function.lock.XLock;
import com.kenai.function.message.XLog;
import com.kenai.function.sensor.XSensorListener;

import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.utils.UserInfoUtil;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

public abstract class BaceCallActivity extends Activity {
    public final static String Intent_Start = "cc.kenai.meicall.MainVoipCallService.start";
    public final static String Intent_First_Action = "action",
            Intent_First_PhoneNumber = "phonenumber",
            Intent_First_VoipNumber = "voipnumber";
    public final static String Intent_Second_Action_CALLOUT_P2L = "callp2l",
            Intent_Second_Action_CALLOUT_P2P = "callp2p",
            Intent_Second_Action_CALLOUT_CALLBACK = "callback";

    protected Handler mHandler = new Handler();
    Device mDevice;
    String calloutID;

    // 所有状态变化放在此中进行
    MyDeviceListener mMyDeviceListener;

    public abstract void onConnectedSuccseed();

    public abstract void onCallAnswered();

    protected Device getDevice() {
        return mDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // 初始化voip组件
        final InitListener mInitListener = new InitListener() {

            @Override
            public void onInitialized() {
                Log.v("VOIP", "初始化成功");
                try {
                    mDevice = VoiceHelper.createDevice(BaceCallActivity.this,
                            mMyDeviceListener);
                    toast("用户登录成功");
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast("用户数据错误");
                    finish();
                }
            }

            @Override
            public void onError(Exception arg0) {
                Log.v("VOIP", "初始化失败");
                toast("初始化失败");
                finish();
            }
        };
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
        CCPCall.init(getApplicationContext(), mInitListener);
//			}
//		}).start();

        mMyDeviceListener = new MyDeviceListener() {

            @Override
            public void onConnected() {
                super.onConnected();
                onConnectedSuccseed();
            }

            @Override
            public void onDisconnect(Reason arg0) {
                super.onDisconnect(arg0);
                toast("验证失败");
                finish();
            }

            @Override
            public void onCallReleased(String arg0) {
                super.onCallReleased(arg0);
                toast("已挂断通话");
                finish();
            }

            /**
             * 对方挂断
             */
            @Override
            public void onMakeCallFailed(String arg0, Reason arg1) {
                super.onMakeCallFailed(arg0, arg1);
                if (arg1.equals(Reason.CALLMISSED)) {
                    toast("对方拒接");
                } else {
                    toast("未响应");
                }
                finish();
            }

            @Override
            public void onCallAnswered(String arg0) {


//                am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                super.onCallAnswered(arg0);
                BaceCallActivity.this.onCallAnswered();
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                SharedPreferences sharedPreferences = getSharedPreferences(Configs_SharedPreference.OTHER, 0);
                isInAdjustVoice=true;
                int n = sharedPreferences.getInt("voice_adjust", 0);
                for (int i = 0; i < n; i++) {
                    am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                }
                for (int i = 0; i > n; i--) {
                    am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }
                Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(100);
            }

        };
        PowerManager xPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = xPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "PowerServiceDemo");


        listener_PRO = new XSensorListener(
                Sensor.TYPE_PROXIMITY, this) {
            @Override
            public void doInformation(SensorEvent event) {
                if ((int) event.values[0] < 4) {
                    XLock.lockNow(getBaseContext());
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                } else {
                    wakeLock.acquire();
                }
            }

        };
        listener_PRO.mbindSensor();
    }

    @Override
    protected void onDestroy() {
        listener_PRO.munbindSensor();
        listener_PRO = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        toast("电话服务已关闭");
        XLog.xLog("onDestroy");
        VoiceHelper.releaseDevice(mDevice);
        CCPCall.shutdown();
        super.onDestroy();
    }

    Toast toast;

    protected final void toast(final String s) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(BaceCallActivity.this, s,
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    PowerManager.WakeLock wakeLock;

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();

//        KeyguardManager xKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
//        xKeyguardLockhandle = xKeyguardManager.newKeyguardLock("");
//        xKeyguardLockhandle.disableKeyguard();
    }

    //    KeyguardManager.KeyguardLock xKeyguardLockhandle;
    XSensorListener listener_PRO;

    AudioManager am;
boolean isInAdjustVoice=false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (isInAdjustVoice&&keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (am == null) {
                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            }

            am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            SharedPreferences sharedPreferences = getSharedPreferences(Configs_SharedPreference.OTHER, 0);
            sharedPreferences.edit().putInt("voice_adjust", sharedPreferences.getInt("voice_adjust", 0) + 1).commit();
            return true;
        } else if (isInAdjustVoice&&keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (am == null) {
                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            }
            am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            SharedPreferences sharedPreferences = getSharedPreferences(Configs_SharedPreference.OTHER, 0);
            sharedPreferences.edit().putInt("voice_adjust", sharedPreferences.getInt("voice_adjust", 0) - 1).commit();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (isInAdjustVoice&&keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else if (isInAdjustVoice&&keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}


class VoiceHelper {
    public final static void shutdown(Device mDevice, String callId) {
        mDevice.releaseCall(callId);
    }

    /**
     * 创建一个用户实例
     *
     * @param mMyDeviceListener
     * @return
     * @throws JSONException
     */
    public final static Device createDevice(Context context,
                                            MyDeviceListener mMyDeviceListener) throws JSONException {
        Device mDevice = null;
        try {
            UserInfoUtil.YuntongxunInfo userinfo = UserInfoUtil
                    .getYuntongxunInfo(context);

            Map<String, String> params = new HashMap<String, String>();
            // * REST服务器地址
            params.put(UserAgentConfig.KEY_IP, "app.cloopen.com");
            // * REST服务器端口
            params.put(UserAgentConfig.KEY_PORT, "8883");
            // * VOIP账号 , 可以填入CCP网站Demo管理中的测试VOIP账号信息
            params.put(UserAgentConfig.KEY_SID, userinfo.getVoipAccount());
            // * VOIP账号密码, 可以填入CCP网站Demo管理中的测试VOIP账号密码
            params.put(UserAgentConfig.KEY_PWD, userinfo.getVoipPwd());
            // * 子账号, 可以填入CCP网站Demo管理中的测试子账号信息
            params.put(UserAgentConfig.KEY_SUBID, userinfo.getSubAccountSid());
            // * 子账号密码, 可以填入CCP网站Demo管理中的测试子账号密码
            params.put(UserAgentConfig.KEY_SUBPWD, userinfo.getSubToken());
            // User-Agent
            params.put(UserAgentConfig.KEY_UA, "Android;"
                    + Build.VERSION.RELEASE + ";" + "当前软件版本如：0.0.0" + ";"
                    + Build.BRAND + "-" + Build.MODEL);
            mDevice = CCPCall.createDevice(mMyDeviceListener, params);
            mDevice.setOnVoIPListener(mMyDeviceListener);
            mDevice.setOnInterphoneListener(mMyDeviceListener);
            // mDevice.setSrtpEnabled(arg0)
            XLog.xLog("Device : " + mDevice);
            // mDevice.isOnline();
            // mDevice.enableLoudsSpeaker(true);
            if (!userinfo.getPhone().isEmpty()) {
                mDevice.setSelfName(userinfo.getPhone());
                mDevice.setSelfPhoneNumber(userinfo.getPhone());
            }
            // VoiceUtil.getStandardMDN("18504753759"));
            // String s=mDevice.makeCall(CallType.VOICEP2L,
            // VoiceUtil.getStandardMDN("15201438217"));
            // XLog.xLog("make call : "+s);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JSONException("");
        }
        return mDevice;
    }

    /**
     * 销毁一个用户实例
     *
     * @param mDevice
     */
    public final static void releaseDevice(Device mDevice) {
        if (mDevice != null)
            mDevice.release();
    }


}

class MyDeviceListener implements OnInterphoneListener, OnVoIPListener, DeviceListener {
    @Override
    public void onConnected() {
        XLog.xLog("开启device成功");
    }


    @Override
    public void onCallback(int i, String s, String s2) {
        XLog.xLog("onCallback : " + s + " s2");
    }


    @Override
    public void onFirewallPolicyEnabled() {
        XLog.xLog("onFirewallPolicyEnabled");
    }

    @Override
    public void onReceiveEvents(CCPEvents arg0) {
        XLog.xLog("onReceiveEvents : " + arg0);
    }

    @Override
    public void onCallAlerting(String arg0) {
        XLog.xLog("onCallAlerting : " + arg0);
    }

    @Override
    public void onCallAnswered(String arg0) {
        XLog.xLog("onCallAnswered : " + arg0);
    }

    @Override
    public void onCallMediaInitFailed(String arg0, int arg1) {
        XLog.xLog("onCallMediaInitFailed : " + arg0);
    }

    @Override
    public void onCallMediaUpdateRequest(String arg0, int arg1) {
        XLog.xLog("onCallMediaUpdateRequest : " + arg0);
    }

    @Override
    public void onCallMediaUpdateResponse(String arg0, int arg1) {
        XLog.xLog("onCallMediaUpdateResponse : " + arg0);
    }

    @Override
    public void onCallPaused(String arg0) {
        XLog.xLog("onCallPaused : " + arg0);
    }

    @Override
    public void onCallPausedByRemote(String arg0) {
        XLog.xLog("onCallPausedByRemote : " + arg0);
    }

    @Override
    public void onCallProceeding(String arg0) {
        XLog.xLog("onCallProceeding : " + arg0);
    }

    @Override
    public void onCallReleased(String arg0) {
        XLog.xLog("onCallReleased : " + arg0);
    }

    @Override
    public void onCallTransfered(String arg0, String arg1) {
        XLog.xLog("onCallTransfered : " + arg0);
    }


    @Override
    public void onCallVideoRatioChanged(String arg0, String arg1) {
        XLog.xLog("onCallVideoRatioChanged : " + arg0);
    }




    @Override
    public void onDisconnect(Reason arg0) {
        XLog.xLog("开启device失败");
    }




    @Override
    public void onMakeCallFailed(String arg0, Reason arg1) {
        XLog.xLog("onMakeCallFailed : " + arg1);
    }


    @Override
    public void onInterphoneState(CloopenReason cloopenReason, String s) {

    }

    @Override
    public void onControlMicState(CloopenReason cloopenReason, String s) {

    }

    @Override
    public void onReleaseMicState(CloopenReason cloopenReason) {

    }

    @Override
    public void onInterphoneMembers(CloopenReason cloopenReason, List<InterphoneMember> interphoneMembers) {

    }

    @Override
    public void onReceiveInterphoneMsg(InterphoneMsg arg0) {
        XLog.xLog("onReceiveInterphoneMsg : " + arg0);
    }




}

class Statebar {
    public Statebar(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    final Context context;
    final NotificationManager notificationManager;

    abstract class State {
        Notification notification;

        void start() {
            notification = onStart();
            notificationManager.notify(14331234, notification);
        }

        void stop() {
            onStop();
            notificationManager.cancel(14331234);
        }

        abstract Notification onStart();

        abstract void onStop();
    }

    public State currentState;


//	public final State onCallout_start = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "拨出电话";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "正在拨出...";
//			CharSequence contentText = "点击挂断";
//			Intent notificationIntent = new Intent(
//					VoiceControler.Broadcast_VoiceControl_Shutdown);
//			PendingIntent contentIntent = PendingIntent.getBroadcast(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//
//			return notification;
//
//		}
//	};
//	public final State onCallout_fail_callmiss = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "对方拒接";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "My notification";
//			CharSequence contentText = "Hello World!";
//			Intent notificationIntent = new Intent();
//			PendingIntent contentIntent = PendingIntent.getActivity(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//
//			return notification;
//
//		}
//	};
//	public final State onCallout_fail_other = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "未知原因";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "My notification";
//			CharSequence contentText = "Hello World!";
//			Intent notificationIntent = new Intent();
//			PendingIntent contentIntent = PendingIntent.getActivity(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//			return notification;
//
//		}
//	};
//	public final State inCallout = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "接通";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "正在通话...";
//			CharSequence contentText = "点击打开通话界面";
//			Intent notificationIntent = new Intent(
//					VoiceControler.Broadcast_VoiceControler);
//			PendingIntent contentIntent = PendingIntent.getBroadcast(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_ONGOING_EVENT;
//
//			return notification;
//
//		}
//	};
//	public final State afterCallout = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "挂断";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "My notification";
//			CharSequence contentText = "Hello World!";
//			Intent notificationIntent = new Intent();
//			PendingIntent contentIntent = PendingIntent.getActivity(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//
//			return notification;
//
//		}
//	};
//
//	public final State inCallIn = new State() {
//
//		@Override
//		void onStop() {
//			// TODO 自动生成的方法存根
//
//		}
//
//		@Override
//		Notification onStart() {
//			int icon = R.drawable.ic_launcher;
//			CharSequence tickerText = "接通电话";
//			long when = System.currentTimeMillis();
//
//			Notification notification = new Notification(icon, tickerText,
//					when);
//
//			CharSequence contentTitle = "接通电话...";
//			CharSequence contentText = "点击挂断";
//			Intent notificationIntent = new Intent(
//					VoiceControler.Broadcast_VoiceControl_Shutdown);
//			PendingIntent contentIntent = PendingIntent.getBroadcast(
//					context, 0, notificationIntent, 0);
//
//			notification.setLatestEventInfo(context, contentTitle,
//					contentText, contentIntent);
//
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//
//			return notification;
//
//		}
//	};

    void start(State newState) {
        currentState = newState;
        currentState.start();
    }

    void stop() {
        currentState.stop();
        currentState = null;
    }

    void changeState(State newState) {
        if (currentState != null)
            currentState.stop();
        currentState = newState;
        currentState.start();
    }
}
