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
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import cc.kenai.meicall.R;
import cc.kenai.meicall.call.config.VoipApi;
import cc.kenai.meicall.call.utils.HttpHelper;
import cc.kenai.meicall.call.utils.UserInfoUtil;
import cc.kenai.meicall.ui.VerticalSeekBar;

import com.hisun.phone.core.voice.Device;
import com.kenai.function.message.XLog;

/**
 * Voip呼出界面，呼出方用于显示和操作通话过程。
 *
 * @version 1.0.0
 */
public class CallInActivity extends BaceCallActivity implements OnClickListener {
    final static String ReceiveCall = "cc.kenai.meicall.CallInActivity.ReceiveCall";

    //	public final static String NewCall = "cc.kenai.meicall.CallInActivity.NewCall";
    public final static String RefuseCall = "cc.kenai.meicall.CallInActivity.RefuseCall";

    BroadcastReceiver myReceiveCallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            XLog.xLog(TAG, "phonenumber : " + "asdfasdg");
            String action = intent.getAction();
            if (action.equals(ReceiveCall)) {
                Bundle extras = intent.getExtras();
                String mVoipAccount = extras.getString(Device.CALLER);
                XLog.xLog("mVoipAccount : " + mVoipAccount);
                // 通话 ID
                String callinID = extras.getString(Device.CALLID);
                XLog.xLog("callID : " + callinID);
                // 设置呼叫者的姓名和手机号码
                String[] infos = extras.getStringArray(Device.REMOTE);
                XLog.xLog(infos.toString());
                text.setText("internet");
                mDevice.acceptCall(callinID);

                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(100);
            } else if (action.equals(RefuseCall)) {

                toast("对方拒接");
                finish();
            }
        }
    };

    @Override
    public void onCallAnswered() {
        // TODO 自动生成的方法存根

    }

    String TAG = "CallOutActivity";
    // 免提按钮
    private ImageView mCallHandFree;
    // 挂断按钮
    private ImageView mCallShutdown;
    private VerticalSeekBar mCallSound;
    private TextView text;
    TextView state;

    Statebar mStatebar;

    boolean isHandsFree = false;


    String toPhone;

    @Override
    public void onConnectedSuccseed() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(ReceiveCall);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        CallInActivity.this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                mDevice.setIncomingIntent(pendingIntent);

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
                if (action.equals(Intent_Second_Action_CALLOUT_P2P)) {
                    toPhone = bd
                            .getString(Intent_First_PhoneNumber);
                    if (toPhone != null && !toPhone.equals("")) {
                        XLog.xLog(TAG, "phonenumber : " + toPhone);
                        try {
                            final String s = UserInfoUtil.getYuntongxunInfo(
                                    getBaseContext()).getPhone();
                            if (s.length() > 2) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HttpHelper hp = new HttpHelper();
                                        final String response = hp
                                                .getHtml(VoipApi.URIHEAD + "createcall/makecall/"
                                                        + s + "/" + toPhone);
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                toast(response);
                                            }
                                        });

                                    }
                                }).start();
                                text.setText(toPhone);
                            } else {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        toast("请先绑定手机号码");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    toast("未知错误");
                                }
                            });
                        }

                    } else {
                        finish();
                        return;
                    }
                } else {
                    finish();
                    return;
                }
            }
        }, 500);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_call_interface);
        mStatebar = new Statebar(this);
        initButton();
        initCall();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiveCall);
        filter.addAction(RefuseCall);
        registerReceiver(myReceiveCallReceiver, filter);

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

        state.setText("正在拨出..");
    }

    /**
     * Initialize mode
     */
    private void initCall() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.callout_shut:
                // 挂断电话
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpHelper hp = new HttpHelper();
                        final String response = hp
                                .getHtml(VoipApi.URIHEAD + "createcall/cancel/"
                                        + toPhone);
                        XLog.xLog("挂断电话 ： " + response);
                    }
                }).start();

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
        Intent notificationIntent = new Intent(context, CallInActivity.class);
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
