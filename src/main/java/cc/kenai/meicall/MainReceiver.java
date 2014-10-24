package cc.kenai.meicall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kenai.function.message.XLog;

public class MainReceiver extends BroadcastReceiver {
	public static final String TAG = MainReceiver.class.getSimpleName();
	@Override
	public void onReceive(final Context context, Intent intent) {
        XLog.xLog("new message");
		intent.setClass(context.getApplicationContext(), MainService.class);
		context.getApplicationContext().startService(intent);
	}

}
