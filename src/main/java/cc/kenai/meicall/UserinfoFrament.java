package cc.kenai.meicall;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;

import net.umipay.android.GameParamInfo;
import net.umipay.android.UmiPaySDKManager;
import net.umipay.android.UmiPaymentInfo;
import net.umipay.android.UmipayOrderInfo;
import net.umipay.android.UmipaySDKStatusCode;
import net.umipay.android.interfaces.InitCallbackListener;
import net.umipay.android.interfaces.OrderReceiverListener;
import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;

import java.util.ArrayList;
import java.util.List;

import cc.kenai.common.ad.KenaiTuiguang;
import cc.kenai.common.program.Question;
import cc.kenai.common.sharedpreferences.SharedPreferencesHelper;
import cc.kenai.common.stores.StoreUtil;
import cc.kenai.meicall.call.config.Configs;
import cc.kenai.meicall.call.config.Configs_SharedPreference;
import cc.kenai.meicall.call.utils.DialogHelper;
import cc.kenai.meicall.call.utils.UserInfoUtil;
import cc.kenai.meicall.call.utils.YuntongxunRegistUtil;

public class UserinfoFrament extends Fragment implements OnClickListener {
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.userinfo_ad:
                    AdManager.getInstance(getActivity()).init(Configs.YoumiID,
                            Configs.YoumiSE, false);

                    if (UserInfoUtil.getYuntongxunInfo(getActivity()) != null) {
                        OffersManager.getInstance(getActivity()).setCustomUserId(
                                UserInfoUtil.getUserName(getActivity()));
                    }
                    AdManager.getInstance(getActivity()).init(Configs.YoumiID,
                            Configs.YoumiSE, false);
                    OffersManager.getInstance(getActivity()).onAppLaunch();
                    OffersManager.getInstance(getActivity()).showOffersWall();
                    break;
                case R.id.userinfo_buy:
                    GameParamInfo gameParamInfo = new GameParamInfo();
                    gameParamInfo.setAppId(Configs.PayID);
                    gameParamInfo.setAppSecret(Configs.PaySE);
                    // 设置成True时，接收SDK回调。 设置成False，不接收SDK回调(接口仍要实现，以便代码能编译通过)
                    gameParamInfo.setSDKCallBack(false);
                    // 初始化结果回调接口
                    final InitCallbackListener initCallbackListener = new InitCallbackListener() {
                        @Override
                        public void onInitCallback(int code, String msg) {
                            if (code == UmipaySDKStatusCode.SUCCESS) {
                                if (UserInfoUtil.getYuntongxunInfo(getActivity()) != null) {

                                    // 应用调用支付充值
                                    UmiPaymentInfo paymentInfo = new UmiPaymentInfo();
                                    //业务类型，SERVICE_TYPE_QUOTA(固定额度模式，充值金额在支付页面不可修改)，SERVICE_TYPE_RATE(汇率模式，充值金额在支付页面可修改）
                                    paymentInfo.setServiceType(UmiPaymentInfo.SERVICE_TYPE_RATE);
                                    // 【可选】设置充值金币数量。ps：只是传入值，非最终充值数额，最终充值额度以服务器通知结果为准
                                    paymentInfo.setAmount(10);
                                    //订单描述
                                    paymentInfo.setDesc("10元");
                                    // 【可选】应用自定义数据。该值将在用户充值成功后，在支付工具服务器回调给开发者时携带该数据
                                    paymentInfo
                                            .setCustomInfo(UserInfoUtil.getUserName(getActivity()));
                                    // 【可选】false:支付完成会允许继续充值； true：
                                    // 支付完成后关闭支付界面,不能继续充值
                                    paymentInfo.setSinglePayMode(true);
                                    paymentInfo.setMinFee(1);
                                    UmiPaySDKManager.showPayView(getActivity()
                                            .getApplicationContext(), paymentInfo);
                                } else {
                                    XToast.xToast(getActivity(), "未注册用户无法充值");
                                }
                            } else if (code == UmipaySDKStatusCode.INIT_FAIL) {
                                // 初始化失败，一般在这里提醒用户网络有问题，反馈，等等问题
                                XToast.xToast(getActivity(), "网络故障，无法调用充值接口");
                            }
                        }
                    };
                    // 订单回调接口
                    final OrderReceiverListener orderReceiverListener = new OrderReceiverListener() {
                        /**
                         * 接收到服务器返回的订单信息 ！！！注意，该返回是在非ui线程中回调，如果需要更新界面，需要手动使用主线刷新
                         */
                        @Override
                        public List onReceiveOrders(List list) {
                            List<UmipayOrderInfo> newOrderList = list;
                            List<UmipayOrderInfo> doneOrderList = new ArrayList<UmipayOrderInfo>();
                            // TODO 服务器返回的订单信息newOrderList，并将已经处理好充值的订单返回给sdk
                            // TODO sdk将已经处理完的订单通知给服务器。服务器下次将不再返回游戏客户端已经处理过的订单
                            for (UmipayOrderInfo newOrder : newOrderList) {
                                try {
                                    // TODO 对订单order进行结算
                                    if (newOrder.getStatus() == 1) {
                                        // 在主线程更新界面
                                        // ...
                                        // ...
                                        // ...
                                        doneOrderList.add(newOrder);
                                    }
                                } catch (Exception e) {

                                }
                            }
                            return doneOrderList; // 将已经处理过的订单返回给sdk，下次服务器不再返回这些订单
                        }
                    };
                    // 调用SDK初始化接口
                    UmiPaySDKManager.initSDK(getActivity(), gameParamInfo,
                            initCallbackListener, orderReceiverListener);

                    break;
                case R.id.userinfo_help:
                    DialogHelper.showHelper(getActivity());
                    break;
                case R.id.userinfo_problem: {
                    Question.NotificationAndDialog(getActivity());
                }
                break;
                case R.id.userinfo_update: {
                    StoreUtil.showInMeizuStore(getActivity(),"da351f4f35ff4cd390610626486342e4");
                }
                break;
                case R.id.userinfo_tuiguang:{
                    KenaiTuiguang.show(getActivity());
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            OffersManager.getInstance(getActivity()).onAppExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        XLog.xLog("Fragment", "onCreateView");
        return inflater.inflate(R.layout.meicall_userinfo_layout, container,
                false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        // OffersManager.getInstance(getActivity()).onAppExit();
        super.onDestroyView();
        OffersManager.getInstance(getActivity()).onAppExit();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment newFragment = new InformationFragment();
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.userinfo_framelayout, newFragment);
        transaction.commit();

        getView().findViewById(R.id.userinfo_ad).setOnClickListener(this);
        getView().findViewById(R.id.userinfo_buy).setOnClickListener(this);
        getView().findViewById(R.id.userinfo_help).setOnClickListener(this);
        getView().findViewById(R.id.userinfo_problem).setOnClickListener(this);
        getView().findViewById(R.id.userinfo_update).setOnClickListener(this);
        getView().findViewById(R.id.userinfo_tuiguang).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO 自动生成的方法存根
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        XLog.xLog("Fragment", "onStop");
        super.onStop();
    }

    public static class InformationFragment extends Fragment implements
            OnClickListener, OnLongClickListener {
        public final static String Intent_Broadcast = "cc.kenai.meicall.InformationFragment.Intent_Broadcast";
        public final static String Intent_Action_First_Action = "action";
        public final static String Intent_Action_First_Value = "value";
        public final static String Intent_Action_Second_Register = "register";

        public InformationFragment() {
            super();
        }

        public final BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bun = intent.getExtras();
                if (bun != null) {
                    String action = bun.getString(Intent_Action_First_Action);
                    if (action != null) {
                        if (action.equals(Intent_Action_Second_Register)) {
                            String value = bun
                                    .getString(Intent_Action_First_Value);
                            if (value != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                if (value.equals("succeed")) {
                                    value = "请等待服务器下发验证短信\n近期移动短信延时比较长了，敬请谅解";
                                }
                                builder.setMessage(value);
                                builder.setCancelable(true);
                                AlertDialog dia = builder.create();
                                dia.setCanceledOnTouchOutside(true);
                                dia.show();
                            }
                        }
                    }
                }
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(
                    R.layout.meicall_userinfo_information_layout, container,
                    false);
        }

        TextView tx1;
        TextView tx2;
        TextView tx3;
        SharedPreferencesHelper shareHelper;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            tx1 = (TextView) getView().findViewById(R.id.userinfo_phonenumber);
            tx2 = (TextView) getView().findViewById(R.id.userinfo_money);
            tx3 = (TextView) getView().findViewById(R.id.userinfo_userstate);
            shareHelper = new SharedPreferencesHelper(getActivity(),
                    Configs_SharedPreference.USERINFO) {

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
            getView().setOnClickListener(this);
            getView().setOnLongClickListener(this);
            loadView();

            updateInformation();

        }

        @Override
        public void onDestroyView() {
            getActivity().unregisterReceiver(receiver);
            shareHelper.unregisterListener(getActivity());
            super.onDestroyView();
        }

        private final void updateInformation() {
            getActivity().registerReceiver(receiver,
                    new IntentFilter(Intent_Broadcast));
            UserInfoUtil.BaiduInfo userInfo_baidu = UserInfoUtil.getBaiduInfo(getActivity());

            if (userInfo_baidu == null) {
                getView().setClickable(false);
            } else {
                getView().setClickable(true);

                UserInfoUtil.YuntongxunInfo userInfo = UserInfoUtil.getYuntongxunInfo(getActivity());
                if (userInfo == null) {
                    getView().setClickable(true);
                    tx1.setText("手机号码： 点击绑定");
                    tx3.setText("用户状态： 点击注册");
                } else {
                    if (userInfo.getPhone().length() < 2) {
                        getView().setClickable(true);
                        tx1.setText("手机号码： 点击绑定");

                        tx3.setText("用户状态： 网络注册");
                        UserInfoUtil.getMoney(getActivity(),
                                new UserInfoUtil.MoneyCallBack() {
                                    @Override
                                    public void onCallBack(final float f) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tx2.setText("剩余费用： " + f + "元");
                                            }
                                        });
                                    }
                                }, UserInfoUtil.getUserName(getActivity())
                        );
                    } else {
                        getView().setClickable(false);
                        tx1.setText("手机号码： " + userInfo.getPhone());
                        tx3.setText("用户状态： 手机绑定");
                        UserInfoUtil.getMoney(getActivity(),
                                new UserInfoUtil.MoneyCallBack() {

                                    @Override
                                    public void onCallBack(final float f) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tx2.setText("剩余费用： " + f + "元");
                                            }
                                        });
                                    }
                                }, userInfo.getPhone()
                        );
                    }
                }
            }

        }

        private final void loadView() {
            tx2.setText("剩余费用： *元");
        }

        @Override
        public void onClick(View v) {
            YuntongxunRegistUtil.regist(getActivity());
        }

        @Override
        public boolean onLongClick(View v) {
            UserInfoUtil.YuntongxunInfo phone = UserInfoUtil.getYuntongxunInfo(getActivity());
            if (phone.getPhone().length() > 2) {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setTitle("解除手机绑定");
                builder.setMessage("账户费用与本手机绑定，因此此项操作并不会导致本手机用户的费用，此项操作用于重新绑定手机号码");
                builder.setPositiveButton("确认",
                        new AlertDialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                YuntongxunRegistUtil.clearPhone1(getActivity());
                            }
                        }
                );
                builder.setNegativeButton("取消",
                        new AlertDialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }
                );
                builder.create().show();
            }

            return true;
        }

    }
}
