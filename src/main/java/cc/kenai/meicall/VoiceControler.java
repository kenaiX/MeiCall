package cc.kenai.meicall;

import com.kenai.function.message.XLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class VoiceControler {
	public final static String Broadcast_VoiceControler = "cc.kenai.meicall.MainVoipCallService.VoiceControler";
	public final static String Broadcast_VoiceControl_Shutdown = "cc.kenai.meicall.MainVoipCallService.VoiceControler.shudown";

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			XLog.xLog("VoiceControler");
			String action = intent.getAction();
			if (action.equals(Broadcast_VoiceControler)) {
				onVoiceControlerShown();
			} else if (action.equals(Broadcast_VoiceControl_Shutdown)) {
				onShutdown();
			}
		}
	};

	public abstract void onVoiceControlerShown();

	public abstract void onShutdown();

	private final Context context;

	public VoiceControler(Context context) {
		this.context = context;
	}

	public final void onCreate() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Broadcast_VoiceControler);
		intentFilter.addAction(Broadcast_VoiceControl_Shutdown);
		context.registerReceiver(myReceiver, intentFilter);
	}

	public final void onDestroy() {
		context.unregisterReceiver(myReceiver);
	}

	public final static void shutdown(Context context) {
		context.sendBroadcast(new Intent(Broadcast_VoiceControl_Shutdown));
	}
	
	public final static void show(Context context) {
		context.sendBroadcast(new Intent(Broadcast_VoiceControler));
	}
}
