package cc.kenai.meicall;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.kenai.function.state.XState;

import cc.kenai.common.ad.LoadDialog;
import cc.kenai.function.base.BaceActivity;
import cc.kenai.meicall.call.utils.DialogHelper;
import cc.kenai.meicall.call.utils.SmartBarUtil;
import cc.kenai.meicall.call.utils.YuntongxunRegistUtil;

public class MainActivity extends BaceActivity {

    public MainActivity() {
        super(TYPE_USUAL, false);
        // TODO 自动生成的构造函数存根
    }

    @Override
    public void xCreate(Bundle arg0) {
        if (XState.get_isfirst(this)) {
            LoadDialog.showDialog(MainActivity.this);
            DialogHelper.showHelper(this);
        }
        boolean findMethod = findActionBarTabsShowAtBottom();
//		if (!findMethod) {
//			getWindow().setUiOptions(0);
//		}
        setContentView(R.layout.activity_main);
//        if (findMethod) {
        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        SmartBarUtil.setActionBarViewCollapsable(bar, false);
        bar.setDisplayOptions(0);

        Tab callout = bar.newTab().setIcon(R.drawable.meicall_tab_callout)
                .setTabListener(new ActionBar.TabListener() {
                    Fragment newFragment = new CallOutFrament();

                    @Override
                    public void onTabReselected(Tab arg0,
                                                FragmentTransaction arg1) {
                        // TODO 自动生成的方法存根

                    }

                    @Override
                    public void onTabSelected(Tab arg0,
                                              FragmentTransaction arg1) {
                        arg1.add(R.id.main_frameLayout, newFragment);
                    }

                    @Override
                    public void onTabUnselected(Tab arg0,
                                                FragmentTransaction arg1) {
                        arg1.remove(newFragment);
                    }

                });
        bar.addTab(callout);

        bar.addTab(bar.newTab().setIcon(R.drawable.meicall_tab_userinfo)
                .setTabListener(new ActionBar.TabListener() {
                    Fragment newFragment = new UserinfoFrament();

                    @Override
                    public void onTabReselected(Tab arg0,
                                                FragmentTransaction arg1) {
                    }

                    @Override
                    public void onTabSelected(Tab arg0,
                                              FragmentTransaction arg1) {
                        arg1.add(R.id.main_frameLayout, newFragment);
                    }

                    @Override
                    public void onTabUnselected(Tab arg0,
                                                FragmentTransaction arg1) {
                        arg1.remove(newFragment);
                    }

                }));

//			bar.addTab(bar.newTab().setIcon(R.drawable.meicall_tab_userinfo)
//					.setTabListener(new ActionBar.TabListener() {
//						Fragment newFragment = new SettingsFrament();
//
//						@Override
//						public void onTabReselected(Tab arg0,
//								FragmentTransaction arg1) {
//							// TODO 自动生成的方法存根
//
//						}
//
//						@Override
//						public void onTabSelected(Tab arg0,
//								FragmentTransaction arg1) {
//							arg1.add(R.id.main_frameLayout, newFragment);
//						}
//
//						@Override
//						public void onTabUnselected(Tab tab,
//								FragmentTransaction ft) {
//							ft.remove(newFragment);
//						}
//					}));


        SmartBarUtil.setActionBarTabsShowAtBottom(bar, true);
//        }


        startService(new Intent(this, MainService.class));


        CallOutFrament.initData(this, true);


        YuntongxunRegistUtil.update(this);


    }

    private boolean findActionBarTabsShowAtBottom() {
        try {
            Class.forName("android.app.ActionBar").getMethod(
                    "setTabsShowAtBottom", new Class[]{boolean.class});
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public void xDestroy() {
    }

    @Override
    public void xPause() {
    }

    @Override
    public void xResume() {

    }


    @Override
    public void xCreatePrepare() {
        // TODO 自动生成的方法存根

    }
}
