package cc.kenai.meicall;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;
import com.kenai.function.message.XLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cc.kenai.common.sharedpreferences.SharedPreferencesHelper;
import cc.kenai.function.base.BaceService;
import cc.kenai.function.base.FloatActivity;
import cc.kenai.meicall.call.config.Configs;
import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.config.VoipApi;
import cc.kenai.meicall.call.utils.HttpHelper;
import cc.kenai.meicall.call.utils.MessageUtil;
import cc.kenai.meicall.call.utils.PhoneNumberUtil;
import cc.kenai.meicall.call.utils.UserInfoUtil;
import cc.kenai.meicall.call.utils.YuntongxunRegistUtil;
import cc.kenai.meicall.voip.CallOutActivity;

public class MainService extends BaceService {
    String TAG = "MainService";

    public final static int Handler_What_StartNotification = 1,
            Handler_What_StopNotification = 2;
    private final static String Broadcast_Onclick = "cc.kenai.meicall.MainService.Broadcast_Onclick";

    private final static String Broadcast_Onclick_Newcal = "cc.kenai.meicall.MainService.Broadcast_Onclick_Newcal";

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String action = bundle.getString("action");
                if (action == null) {
                    return;
                }
                if (action.equals(Broadcast_Onclick_Newcal)) {
                    String value = bundle.getString("value");
                    FloatActivity
                            .showView(new NewCallFloatView(context, value));
                }
            }
        }
    };

    SharedPreferencesHelper sharedHelper;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Handler_What_StartNotification: {
                    int icon = R.drawable.ic_launcher;
                    CharSequence tickerText = "有来电~";
                    long when = System.currentTimeMillis();
                    Notification notification = new Notification(icon, tickerText,
                            when);
                    CharSequence contentTitle = "新来电";
                    CharSequence contentText = "点击打开通话页面";
                    Intent notificationIntent = new Intent(Broadcast_Onclick);
                    notificationIntent.putExtra("action", Broadcast_Onclick_Newcal);
                    String value = (String) msg.obj;
                    notificationIntent.putExtra("value", value);
                    PendingIntent contentIntent = PendingIntent.getBroadcast(
                            MainService.this, 0, notificationIntent, 0);

                    notification.setLatestEventInfo(MainService.this, contentTitle,
                            contentText, contentIntent);

                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notification.sound = android.provider.Settings.System
                            .getUriFor(Settings.System.RINGTONE);
                    long[] vib = new long[20];
                    for (int i = 0; i < 20; i++) {
                        if (i % 2 == 0) {
                            vib[i] = 1000;
                        } else {
                            vib[i] = 100;
                        }
                    }
                    notification.vibrate = vib;

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(14331235, notification);
                }
                break;
                case Handler_What_StopNotification: {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(14331235);
                }
                default:
                    break;
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO 自动生成的方法存根
        return null;
    }

    @Override
    public void reStart() {
        // TODO 自动生成的方法存根

    }

    @Override
    public void xConfigurationChanged(Configuration arg0) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void xCreate() {
        sharedHelper = new SharedPreferencesHelper(this,
                Configs_SharedPreference.USERINFO) {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                if (key.equals(UserInfoUtil.Settings_BaiduInfo)) {
                    XLog.xLog_bug("BaiduInfo changed");
                    UserInfoUtil.clearYuntongxunInfo(MainService.this);
                    YuntongxunRegistUtil.internetRegist1(MainService.this);
                } else if (key.equals(UserInfoUtil.Settings_UserName)) {
                    XLog.xLog_bug("UserName changed");
                    UserInfoUtil.clearYuntongxunInfo(MainService.this);
                    YuntongxunRegistUtil.internetRegist1(MainService.this);
                }
            }
        };
        sharedHelper.registerListener(this);
        PushSettings.enableDebugMode(getApplicationContext(), false);
        PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY,
                Configs.BaiduApi);

        List<String> tag = new ArrayList<String>();
        tag.add(Configs.BaiduTag1);
        tag.add(Configs.BaiduTag2);
        PushManager.setTags(this, tag);
        registerReceiver(receiver, new IntentFilter(Broadcast_Onclick));

        UserInfoUtil.udateUserName(this);


    }

    @Override
    public void xDestroy() {
        unregisterReceiver(receiver);
        sharedHelper.unregisterListener(this);
    }

    @Override
    public void xstart(Intent intent, int arg1, int arg2) {
        try {
            XLog.xLog("new message in service");
            if (intent.getAction() != null) {
                if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
                    // 获取消息内容
                    String message = intent.getExtras().getString(
                            PushConstants.EXTRA_PUSH_MESSAGE_STRING);
                    // 消息的用户自定义内容读取方式
                    XLog.xLog("onMessage: " + message);
                    try {
                        JSONObject json = new JSONObject(message);
                        MessageUtil.analysis(this, handler, json);
                    } catch (JSONException e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }

                } else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
                    // 处理绑定等方法的返回数据
                    // 获取方法
                    final String method = intent
                            .getStringExtra(PushConstants.EXTRA_METHOD);
                    // 方法返回错误码�?若绑定返回错误（�?），则应用将不能正常接收消息�?
                    // 绑定失败的原因有多种，如网络原因，或access token过期�?
                    // 请不要在出错时进行简单的startWork调用，这有可能导致死循环�?
                    // 可以通过限制重试次数，或者在其他时机重新调用来解决�?
                    final int errorCode = intent.getIntExtra(
                            PushConstants.EXTRA_ERROR_CODE,
                            PushConstants.ERROR_SUCCESS);
                    // 返回内容
                    final String content = new String(
                            intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));

                    // 用户在此自定义处理消�?以下代码为demo界面展示�?
                    Log.d(TAG, "onMessage: method : " + method);
                    Log.d(TAG, "onMessage: result : " + errorCode);
                    Log.d(TAG, "onMessage: content : " + content);
                    if (method.equals(PushConstants.METHOD_BIND)) {
                        JSONObject receive = null;
                        try {
                            receive = new JSONObject(content);
                            receive = receive.getJSONObject("response_params");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (receive != null) {
                            UserInfoUtil.BaiduInfo local = UserInfoUtil.getBaiduInfo(this);
                            if (local != null
                                    && !receive.toString().equals(local.toString())) {
                                // 更新百度帐号
                                UserInfoUtil.savaBaiduInfo(this, receive);
                            } else if (local == null) {
                                // 储存百度帐号
                                UserInfoUtil.savaBaiduInfo(this, receive);
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class NewCallFloatView extends FloatActivity {
    String value;

    public NewCallFloatView(Context context, String value) {
        super(context, R.layout.meicall_newcall_layout, true, 600, 400);
        this.value = value;
    }

    @Override
    public void onCreate() {
        try {
            final JSONObject json = new JSONObject(value);

            final Intent it = new Intent(context, CallOutActivity.class);
            it.putExtra(CallOutActivity.Intent_First_Action,
                    CallOutActivity.Intent_Second_Action_CALLOUT_P2P);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            final String phone = json.getString("voip");
            final String phoneNumber = json.getString("phone");
            if (PhoneNumberUtil.getPhoneNumber(phoneNumber) != null) {
                it.putExtra(CallOutActivity.Intent_First_PhoneNumber,
                        PhoneNumberUtil.getPhoneNumber(phoneNumber));
            } else {
                it.putExtra(CallOutActivity.Intent_First_PhoneNumber, phoneNumber);
            }

            getMianView().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO 自动生成的方法存根

                }
            });
            getMianView().findViewById(R.id.newcall_accept).setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            context.startActivity(it);
                            FloatActivity.closeView(NewCallFloatView.this);
                        }
                    }
            );
            getMianView().findViewById(R.id.newcall_shut).setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    HttpHelper hp = new HttpHelper();
                                    final String response = hp
                                            .getHtml(VoipApi.URIHEAD + "createcall/refuse/"
                                                    + phoneNumber);
                                }
                            }).start();
                            FloatActivity.closeView(NewCallFloatView.this);
                        }

                    }
            );
            TextView tx = (TextView) getMianView().findViewById(
                    R.id.newcall_num);
            if (phoneNumber != null && phoneNumber.length() > 0) {
                tx.setText(phoneNumber);
            } else {
                tx.setText("internet");
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroy() {
    }

}