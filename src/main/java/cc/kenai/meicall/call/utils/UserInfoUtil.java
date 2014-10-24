package cc.kenai.meicall.call.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.kenai.function.message.XLog;

import org.json.JSONException;
import org.json.JSONObject;

import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.config.VoipApi;

public class UserInfoUtil {
    public static final String Settings_UserName = "username";

//    public static final String Settings_UserNumber = "userphone";


    public static final String Settings_BaiduInfo = "userBaiduInfo";

    public static final String Settings_YuntongxunInfo = "userYuntongxunInfo";

    public static final String Settings_Money = "userMoney";

    public static final String Settings_Money_Time = "userMoney_time";

    //    public static final void removeUserNumber(Context context, String phone) {
//        final SharedPreferences shared = context.getSharedPreferences(
//                Configs_SharedPreference.USERINFO, 0);
//        shared.edit().remove(Settings_UserNumber).commit();
//    }
//
//    public static final void setUserNumber(Context context, String phone) {
//        final SharedPreferences shared = context.getSharedPreferences(
//                Configs_SharedPreference.USERINFO, 0);
//        shared.edit().putString(Settings_UserNumber, phone).commit();
//    }
//
//    public static final String getUserNumber(Context context) {
//        final SharedPreferences shared = context.getSharedPreferences(
//                Configs_SharedPreference.USERINFO, 0);
//        return shared.getString(Settings_UserNumber, null);
//    }
    public static final void udateUserName(Context context) {
        final SharedPreferences shared = context.getSharedPreferences(
                Configs_SharedPreference.USERINFO, 0);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        shared.edit().putString(Settings_UserName, tm.getDeviceId()).commit();
    }

    public static final String getUserName(Context context) {
        final SharedPreferences shared = context.getSharedPreferences(
                Configs_SharedPreference.USERINFO, 0);
        return shared.getString(Settings_UserName, null);
    }

    public static void getMoney(Context context, final MoneyCallBack cb,
                                final String number) {
        final SharedPreferences shared = context.getSharedPreferences(
                Configs_SharedPreference.USERINFO, 0);
        final long now = System.currentTimeMillis();
        long cache = shared.getLong(Settings_Money_Time, 0l);
        if (Math.abs(now - cache) > 60 * 1000) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        HttpHelper hp = new HttpHelper();
                        String back = hp
                                .getHtml(VoipApi.URIHEAD + "money/"
                                        + number);
                        float money = Float.valueOf(back);
                        shared.edit().putLong(Settings_Money_Time, now).commit();
                        shared.edit().putFloat(Settings_Money, money).commit();
                        cb.onCallBack(money);
                    } catch (Exception e) {

                    }
                }

            }).start();
        } else {
            cb.onCallBack(shared.getFloat(Settings_Money, 0f));
        }
    }

    public static abstract class MoneyCallBack {
        public abstract void onCallBack(float f);
    }

    public static BaiduInfo getBaiduInfo(Context context) {
        String s = context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0)
                .getString(Settings_BaiduInfo, " ");
        try {
            return new BaiduInfo(new JSONObject(s));
        } catch (JSONException e) {
            return null;
        }
    }

    public static YuntongxunInfo getYuntongxunInfo(Context context) {
        String s = context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0)
                .getString(Settings_YuntongxunInfo, " ");
        XLog.xLog_bug("get-" + s);
        try {
            return new YuntongxunInfo(new JSONObject(s));
        } catch (JSONException e) {
            return null;
        }
    }

    public static void savaBaiduInfo(Context context, JSONObject s) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0);
        if (!sharedPreferences.getString(Settings_BaiduInfo, "").equals(s.toString())) {
            XLog.xLog_bug("old baiduinfo : " + sharedPreferences.getString(Settings_BaiduInfo, ""));
            XLog.xLog_bug("new baiduinfo : " + s.toString());
            sharedPreferences.edit()
                    .putString(Settings_BaiduInfo, s.toString()).commit();
        }
    }

    public static void clearYuntongxunInfo(Context context) {
        context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0)
                .edit().remove(Settings_YuntongxunInfo).commit();
    }

    public static void savaPhoneNumber(Context context, String s) {
        String json = context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0)
                .getString(Settings_YuntongxunInfo, " ");
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.put("phone", s);
            context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0).edit()
                    .putString(Settings_YuntongxunInfo, jsonObject.toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void savaYuntongxunInfo(Context context, JSONObject s) {
        XLog.xLog_bug("save-" + s.toString());
        context.getSharedPreferences(Configs_SharedPreference.USERINFO, 0).edit()
                .putString(Settings_YuntongxunInfo, s.toString()).commit();
    }

    public static class BaiduInfo {
        public BaiduInfo(JSONObject json) throws JSONException {
            cid = json.getString("channel_id");
            uid = json.getString("user_id");
        }

        String cid;
        String uid;

        public String getCid() {
            return cid;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

    }

    public static class YuntongxunInfo {
        String subAccountSid;
        String subToken;
        String voipAccount;
        String voipPwd;
        String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSubAccountSid() {
            return subAccountSid;
        }

        public void setSubAccountSid(String subAccountSid) {
            this.subAccountSid = subAccountSid;
        }

        public String getSubToken() {
            return subToken;
        }

        public void setSubToken(String subToken) {
            this.subToken = subToken;
        }

        public String getVoipAccount() {
            return voipAccount;
        }

        public void setVoipAccount(String voipAccount) {
            this.voipAccount = voipAccount;
        }

        public String getVoipPwd() {
            return voipPwd;
        }

        public void setVoipPwd(String voipPwd) {
            this.voipPwd = voipPwd;
        }

        public YuntongxunInfo(JSONObject json) throws JSONException {
            phone = json.getString("phone");
            subAccountSid = json.getString("subAccountSid");
            subToken = json.getString("subToken");
            voipAccount = json.getString("voipAccount");
            voipPwd = json.getString("voipPwd");
        }
    }
}
