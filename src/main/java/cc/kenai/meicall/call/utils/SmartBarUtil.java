package cc.kenai.meicall.call.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.ActionBar;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

public class SmartBarUtil {

	/**
	 * 璋冪敤 ActionBar.setTabsShowAtBottom(boolean) 鏂规硶銆� *
	 * <p>
	 * 濡傛灉 android:uiOptions="splitActionBarWhenNarrow"锛屽垯鍙缃瓵ctionBar
	 * Tabs鏄剧ず鍦ㄥ簳鏍忋�
	 * 
	 * <p>
	 * 绀轰緥锛�/p>
	 * 
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity implements ActionBar.TabListener {
	 * 
	 * 	protected void onCreate(Bundle savedInstanceState) {
	 *         super.onCreate(savedInstanceState);
	 *         ...
	 *         
	 *         final ActionBar bar = getActionBar();
	 *         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	 *         SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
	 *         
	 *         bar.addTab(bar.newTab().setText(&quot;tab1&quot;).setTabListener(this));
	 *         ...
	 *     }
	 * }
	 * </pre>
	 */
	public static void setActionBarTabsShowAtBottom(ActionBar actionbar,
			boolean showAtBottom) {
		try {
			Method method = Class.forName("android.app.ActionBar").getMethod(
					"setTabsShowAtBottom", new Class[] { boolean.class });
			try {
				method.invoke(actionbar, showAtBottom);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 璋冪敤 ActionBar.setActionBarViewCollapsable(boolean) 鏂规硶銆� *
	 * <p>
	 * 璁剧疆ActionBar椤舵爮鏃犳樉绀哄唴瀹规椂鏄惁闅愯棌銆� *
	 * <p>
	 * 绀轰緥锛�/p>
	 * 
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity {
	 * 
	 * 	protected void onCreate(Bundle savedInstanceState) {
	 *         super.onCreate(savedInstanceState);
	 *         ...
	 *         
	 *         final ActionBar bar = getActionBar();
	 *         
	 *         // 璋冪敤setActionBarViewCollapsable锛屽苟璁剧疆ActionBar娌℃湁鏄剧ず鍐呭锛屽垯ActionBar椤舵爮涓嶆樉绀�     *         SmartBarUtils.setActionBarViewCollapsable(bar, true);
	 *         bar.setDisplayOptions(0);
	 *     }
	 * }
	 * </pre>
	 */
	public static void setActionBarViewCollapsable(ActionBar actionbar,
			boolean collapsable) {
		try {
			Method method = Class.forName("android.app.ActionBar").getMethod(
					"setActionBarViewCollapsable",
					new Class[] { boolean.class });
			try {
				method.invoke(actionbar, collapsable);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 璋冪敤 ActionBar.setActionModeHeaderHidden(boolean) 鏂规硶銆� *
	 * <p>
	 * 璁剧疆ActionMode椤舵爮鏄惁闅愯棌銆� *
	 * <p>
	 * 绀轰緥锛�/p>
	 * 
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity {
	 * 
	 * 	protected void onCreate(Bundle savedInstanceState) {
	 *         super.onCreate(savedInstanceState);
	 *         ...
	 *         
	 *         final ActionBar bar = getActionBar();
	 *         
	 *         // ActionBar杞负ActionMode鏃讹紝涓嶆樉绀篈ctionMode椤舵爮
	 *         SmartBarUtils.setActionModeHeaderHidden(bar, true);
	 *     }
	 * }
	 * </pre>
	 */
	public static void setActionModeHeaderHidden(ActionBar actionbar,
			boolean hidden) {
		try {
			Method method = Class.forName("android.app.ActionBar").getMethod(
					"setActionModeHeaderHidden", new Class[] { boolean.class });
			try {
				method.invoke(actionbar, hidden);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 璋冪敤ActionBar.setBackButtonDrawable(Drawable)鏂规硶
	 * 
	 * <p>
	 * 璁剧疆杩斿洖閿浘鏍� *
	 * <p>
	 * 绀轰緥锛�/p>
	 * 
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity {
	 * 
	 * 	protected void onCreate(Bundle savedInstanceState) {
	 *         super.onCreate(savedInstanceState);
	 *         ...
	 *         
	 *         final ActionBar bar = getActionBar();
	 *         // 鑷畾涔堿ctionBar鐨勮繑鍥為敭鍥炬爣
	 *         SmartBarUtils.setBackIcon(bar, getResources().getDrawable(R.drawable.ic_back));
	 *         ...
	 *     }
	 * }
	 * </pre>
	 */
	public static void setBackIcon(ActionBar actionbar, Drawable backIcon) {
		try {
			Method method = Class.forName("android.app.ActionBar").getMethod(
					"setBackButtonDrawable", new Class[] { Drawable.class });
			try {
				method.invoke(actionbar, backIcon);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
