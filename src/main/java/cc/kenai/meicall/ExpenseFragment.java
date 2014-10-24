package cc.kenai.meicall;

import org.json.JSONException;
import org.json.JSONObject;

import com.kenai.function.message.XLog;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cc.kenai.common.sharedpreferences.SharedPreferencesHelper;
import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.config.VoipApi;
import cc.kenai.meicall.call.utils.HttpHelper;
import cc.kenai.meicall.call.utils.UserInfoUtil;
import cc.kenai.meicall.call.utils.YuntongxunRegistUtil;

public class ExpenseFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.meicall_expense_layout, container,
				false);
	}

	TextView tx1;
	SharedPreferencesHelper shareHelper;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		tx1 = (TextView) getView().findViewById(R.id.expense_main);

		shareHelper = new SharedPreferencesHelper(getActivity(),
				Configs_SharedPreference.CACHE) {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals(UserInfoUtil.Settings_BaiduInfo)) {
					YuntongxunRegistUtil.regist(getActivity());
				}
				updateInformation();
			}
		};
		shareHelper.registerListener(getActivity());

		final Long cache=shareHelper.myShared.getLong("expense_time", 0);
		final Long now =System.currentTimeMillis();
		if((now-cache>24*60*60*1000)){
			XLog.xLog("test","update");
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					HttpHelper hp=new HttpHelper();
					String response=hp.getHtml(VoipApi.URIHEAD+"expense");
					if(response!=null&&response.length()>5){
						try {
							JSONObject json=new JSONObject(response);
							String p2p=json.getString("p2p");
							String p2l=json.getString("p2l");
							String callback=json.getString("callback");
							response="云呼叫：需要双方安装本软件，需要网络支持。 全国"+ p2p+"分/分钟\n\n" +
				                    "网络呼叫：需要网络支持。 全国"+ p2l+"分/分钟\n\n" +
				                    "回拨呼叫：仅初始化时需要网络支持。 全国"+ callback+"分/分钟";
						} catch (JSONException e) {
							return;
						}
						
						shareHelper.putLong("expense_time", now);
					    shareHelper.putString("expense_string", response);
					}
				}
			}).start();
		}else{
			XLog.xLog("test","not update");
		}
		final String s=shareHelper.myShared.getString("expense_string", "");
		tx1.setText(s);
		updateInformation();

	}

	@Override
	public void onDestroyView() {
		shareHelper.unregisterListener(getActivity());
		super.onDestroyView();
	}

	private final void updateInformation() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final String s=shareHelper.myShared.getString("expense_string", "");
				tx1.setText(s);
			}
		});
	}

}
