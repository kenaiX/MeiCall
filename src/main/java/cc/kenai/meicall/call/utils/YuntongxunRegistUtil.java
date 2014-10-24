package cc.kenai.meicall.call.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;

import cc.kenai.meicall.R;
import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.config.VoipApi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.List;

public class YuntongxunRegistUtil {
    public final static void update(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfoUtil.BaiduInfo baiduInfo = UserInfoUtil.getBaiduInfo(context);
                if (baiduInfo != null) {
                    HttpHelper hp = new HttpHelper();
                    XLog.xLog(
                            "internetUpdate",
                            hp.getHtml(VoipApi.URIHEAD + "internetregist/update?sn=" + UserInfoUtil.getUserName(context)));
                }

            }
        }).start();
    }


    public final static void regist(final Activity activity) {
        final UserInfoUtil.BaiduInfo baiduInfo = UserInfoUtil.getBaiduInfo(activity);
        if (baiduInfo != null) {
            UserInfoUtil.YuntongxunInfo userInfo = UserInfoUtil.getYuntongxunInfo(activity);
            if (userInfo == null) {
                internetRegist1(activity);
            } else if (userInfo.getPhone().length() < 2) {
                AlertDialog.Builder builder2 = new
                        AlertDialog.Builder(
                        activity);
                builder2.setTitle("注册说明");
                builder2.setMessage("即将发送一条注册短信，仅由运营商收取规定的短信基本费用（一般为0.1元/条）。");
                builder2.setPositiveButton("发送",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int whichButton) {
                                // 移动运营商允许每次发送的字节数据有限，我们可以使用Android给我们提供
                                // 的短信工具。
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HttpHelper helper = new HttpHelper();
                                        final String smscenter = helper.getHtml(VoipApi.URIHEAD + "internetregist/smscenter");
                                        if (smscenter != null&&smscenter.length()>5) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    JSONObject sendjson = new JSONObject();
                                                    try {
                                                        sendjson.put("action", "newuser");
                                                        sendjson.put("sn", UserInfoUtil.getUserName(activity));
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        return;
                                                    }
                                                    final String message = sendjson.toString();
                                                    if (message != null) {
                                                        SmsManager sms = SmsManager
                                                                .getDefault();
                                                        // 如果短信没有超过限制长度，则返回一个长度的List。
                                                        List<String> texts = sms
                                                                .divideMessage(message);
                                                        for (String text : texts) {
                                                            sms.sendTextMessage(
                                                                    smscenter,
                                                                    "", message,
                                                                    null, null);
                                                        }
                                                    }
                                                    XToast.xToast(activity, "注册短信已发送");
                                                }
                                            });



                                        } else {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    XToast.xToast(activity, "获取短信中心号码失败，无法发送注册短信，请检查网络是否通畅");
                                                }
                                            });
                                        }

                                    }
                                }).start();


                            }
                        });
                builder2.create().show();
            } else {
                internetRegist1(activity);
            }
        }
    }

    public final static void internetRegist1(final Context context) {
        XLog.xLog("internetRegist1");
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfoUtil.BaiduInfo baiduInfo = null;

                baiduInfo = UserInfoUtil.getBaiduInfo(context);
                if (baiduInfo != null) {
                    HttpHelper hp = new HttpHelper();
                    XLog.xLog(
                            "internetRegist1",
                            hp.getHtml(VoipApi.URIHEAD + "internetregist/register/1?" +
                                    "cid=" + baiduInfo.getCid() + "&uid=" + baiduInfo.getUid()));
                }

            }
        }).start();

    }

    public final static void internetRegist2(final Context context,
                                             final String psw) {
        XLog.xLog("internetRegist2");
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfoUtil.BaiduInfo json = null;

                json = UserInfoUtil.getBaiduInfo(context);
                if (json != null) {
                    HttpHelper hp = new HttpHelper();
                    XLog.xLog(
                            "internetRegist2",
                            hp.getHtml(VoipApi.URIHEAD + "internetregist/register/2?" +
                                    "cid=" + json.getCid()
                                    + "&uid=" + json.getUid()
                                    + "&psw=" + psw
                                    + "&sn=" + UserInfoUtil.getUserName(context)));
                }

            }
        }).start();

    }

    // public final static void interneiRegister_
    public final static void clearPhone1(final Context context) {
        XLog.xLog("clearPhone1");
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfoUtil.BaiduInfo json = null;

                json = UserInfoUtil.getBaiduInfo(context);
                if (json != null) {
                    HttpHelper hp = new HttpHelper();
                    XLog.xLog(
                            "clearPhone1",
                            hp.getHtml(VoipApi.URIHEAD + "internetregist/clearphone/1?" +
                                    "cid=" + json.getCid() + "&uid=" + json.getUid()));
                }

            }
        }).start();
    }

    public final static void clearPhone2(final Context context, final String psw) {
        XLog.xLog("clearPhone2");
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfoUtil.BaiduInfo json = null;

                json = UserInfoUtil.getBaiduInfo(context);
                if (json != null) {
                    HttpHelper hp = new HttpHelper();
                    XLog.xLog(
                            "clearPhone2",
                            hp.getHtml(VoipApi.URIHEAD + "internetregist/clearphone/2?" +
                                    "cid=" + json.getCid()
                                    + "&uid=" + json.getUid()
                                    + "&psw=" + psw
                                    + "&sn=" + UserInfoUtil.getUserName(context)));
                }

            }
        }).start();
    }

//    public final static void bindPhone1(final Context context) {
//        XLog.xLog("clearPhone1");
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                UserInfoUtil.BaiduInfo json = null;
//                try {
//                    json = UserInfoUtil.getBaiduInfo(context);
//                    HttpHelper hp = new HttpHelper();
//                    XLog.xLog(
//                            "bindPhone1",
//                            hp.getHtml(VoipApi.URIHEAD + "internetregist/bindphone/"
//                                    + json.getCid() + "/" + json.getUid()));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//            }
//        }).start();
//    }
//
//    public final static void bindPhone2(final Context context,
//                                        final String psw, final YunCallBack callback) {
//        XLog.xLog("clearPhone2");
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                UserInfoUtil.BaiduInfo json = null;
//                try {
//                    json = UserInfoUtil.getBaiduInfo(context);
//                    HttpHelper hp = new HttpHelper();
//                    String response = hp
//                            .getHtml(VoipApi.URIHEAD +
//                                    "internetregist/bindphone/"
//                                    + json.getCid()
//                                    + "/"
//                                    + json.getUid()
//                                    + "/"
//                                    + UserInfoUtil.getLocalPhoneNumber(context)
//                                    + "/" + psw);
//                    XLog.xLog("bindPhone2", response);
//                    if (callback != null)
//                        callback.onCallback(response);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//            }
//        }).start();
//    }
//
//    public final static void bindPhone3(final Context context, final String psw) {
//        XLog.xLog("bindPhone3");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                UserInfoUtil.BaiduInfo json = null;
//                try {
//                    json = UserInfoUtil.getBaiduInfo(context);
//                    HttpHelper hp = new HttpHelper();
//                    String response = hp.getHtml(VoipApi.URIHEAD + "internetregist/bindphone3/"
//                            + json.getCid()
//                            + "/"
//                            + json.getUid()
//                            + "/"
//                            + UserInfoUtil.getLocalPhoneNumber(context)
//                            + "/" + psw);
//                    XLog.xLog(
//                            "bindPhone3 response : ", response);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    return;
//                }
//
//            }
//        }).start();
//    }

    public static abstract class YunCallBack {
        abstract void onCallback(String response);
    }
}
