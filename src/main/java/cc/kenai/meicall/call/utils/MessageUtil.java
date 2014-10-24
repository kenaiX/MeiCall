package cc.kenai.meicall.call.utils;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import cc.kenai.meicall.MainService;
import cc.kenai.meicall.UserinfoFrament;
import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.voip.CallInActivity;
import cc.kenai.meicall.voip.CallOutActivity;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;

public class MessageUtil {

	public final static void analysis(final Service context,
			final Handler handler, JSONObject json) throws JSONException {
		String action = json.getString("action");
		String value = json.getString("value");
		if (action.equals("userinfo")) {
            XLog.xLog_bug("userinfo - " + json.toString());
            try {
                JSONObject jsonObject=new JSONObject(value);
                UserInfoUtil.savaYuntongxunInfo(context, jsonObject);
            } catch (JSONException e) {
                UserInfoUtil.savaPhoneNumber(context,value);
            }
        } else if (action.equals("internetregist")) {
			YuntongxunRegistUtil.internetRegist2(context, value);
		} else if (action.equals("clearphone")) {
			YuntongxunRegistUtil.clearPhone2(context, value);
		}
//        else if (action.equals("bindphone")) {
//			YuntongxunRegistUtil.bindPhone2(context, value,
//					new YuntongxunRegistUtil.YunCallBack() {
//						@Override
//						void onCallback(final String response) {
//							handler.post(new Runnable() {
//
//								@Override
//								public void run() {
//									if(!response.equals("succeed")){
//                                        final SharedPreferences shared = context.getSharedPreferences(
//                                                Configs_SharedPreference.CACHE, 0);
//                                        shared.edit().putLong("register_time_sms",0).apply();
//                                    }
//                                    Intent intent =new Intent(UserinfoFrament.InformationFragment.Intent_Broadcast);
//									intent.putExtra(UserinfoFrament.InformationFragment.Intent_Action_First_Action, UserinfoFrament.InformationFragment.Intent_Action_Second_Register);
//									intent.putExtra(UserinfoFrament.InformationFragment.Intent_Action_First_Value, response);
//
//									context.sendBroadcast(intent);
//								}
//							});
//						}
//					});
//		}
        else if (action.equals("newcall")) {
			Message msg = handler.obtainMessage();
			msg.what = MainService.Handler_What_StartNotification;
			msg.obj = value;
			handler.sendMessage(msg);
//			if (handler.hasMessages(MainService.Handler_What_StopNotification)) {
//				handler.removeMessages(MainService.Handler_What_StopNotification);
//			}
//			handler.sendEmptyMessageDelayed(
//					MainService.Handler_What_StartNotification, 20 * 1000);

		} else if (action.equals("refusecall")) {
			context.sendBroadcast(new Intent(CallInActivity.RefuseCall));
		} else if (action.equals("cancelcall")) {
			context.sendBroadcast(new Intent(CallOutActivity.CancelCall));
            Message msg = handler.obtainMessage();
            msg.what = MainService.Handler_What_StopNotification;
            handler.sendMessage(msg);
		}
	}

}
