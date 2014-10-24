package cc.kenai.meicall;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.kenai.function.message.XLog;
import com.kenai.function.message.XToast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.kenai.meicall.call.config.VoipApi;
import cc.kenai.meicall.call.utils.PhoneNumberUtil;
import cc.kenai.meicall.searchcontact.BaseUtil;
import cc.kenai.meicall.searchcontact.ContactsAdapter;
import cc.kenai.meicall.searchcontact.Model;
import cc.kenai.meicall.voip.CallInActivity;
import cc.kenai.meicall.voip.CallOutActivity;

@SuppressLint("ValidFragment")
public class CallOutFrament extends Fragment {

    private Context context;
    private static List<Model> allContactList;

    private EditText et;

    private abstract class DialFragment extends Fragment implements
            TextWatcher, OnClickListener, OnLongClickListener {
        public DialFragment(EditText et, ListView lv) {
            super();
            this.et = et;
            this.lv = lv;
        }

        private final EditText et;
        private final ListView lv;
        private ImageButton yuncall;
        private final Map<String, Boolean> phoneMap = new HashMap<String, Boolean>();
        /**
         * 当前拨号盘提取出的手机号码
         */
        private String phoneNumber;

        HandlerThread uIhandlerThread = new HandlerThread("update");

        // Handler UIhandler = new Handler(uIhandlerThread.getLooper());
        Handler uIhandler;

        abstract void onYunCall();

        abstract void onCall();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            XLog.xLog("Fragment", "onCreateView");
            return inflater.inflate(R.layout.meicall_callout_dial_fragment,
                    container, false);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            XLog.xLog("Fragment", getTag() + " onCreate");
            uIhandlerThread.start();
            uIhandler = new Handler(uIhandlerThread.getLooper());
        }

        @Override
        public void onDestroy() {
            // TODO 自动生成的方法存根
            super.onDestroy();
            XLog.xLog("Fragment", getTag() + " onDestroy");
            if (uIhandlerThread != null) {
                uIhandlerThread.quit();
            }
        }

        @Override
        public void onStart() {
            initData(context, false);
            init();

            super.onStart();
            XLog.xLog("Fragment", getTag() + " onStart");

        }

        @Override
        public void onResume() {
            super.onResume();
            XLog.xLog("Fragment", getTag() + " onResume");
        }

        @Override
        public void onStop() {
            // TODO 自动生成的方法存根
            super.onStop();
            XLog.xLog("Fragment", getTag() + " onStop");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            this.yuncall.setEnabled(false);
            this.yuncall.clearAnimation();
            phoneNumber = s.toString().replace("+86", "");
            String phone = PhoneNumberUtil.getPhoneNumber(s.toString());
            if (phone != null) {
                RotateAnimation rotate = new RotateAnimation(0, 360,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                rotate.setFillAfter(true);
                rotate.setFillEnabled(true);
                rotate.setDuration(1000);
                rotate.setRepeatCount(5);
                this.yuncall.startAnimation(rotate);

                checkPhoneOnline(phone);
                phoneNumber = phone;
            }

            if (allContactList != null) {
                String _str = s.toString().replace("*", "");
                final String str = _str.replace("#", "");
                if (TextUtils.isEmpty(str)) {
                    adapter.refresh_clear();
                } else {

                    uIhandler.post(new Runnable() {

                        @Override
                        public void run() {
                            search(str.toString());
                        }
                    });

                }
            }

        }

        public void init() {
            View mainView = getView();

            contactList = new ArrayList<Model>();
            adapter = new ContactsAdapter(getActivity());

            yuncall = (ImageButton) mainView
                    .findViewById(R.id.callout_action_add);
            yuncall.setEnabled(false);

            mainView.findViewById(R.id.callout_number_0).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_1).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_2).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_3).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_4).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_5).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_6).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_7).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_8).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_9).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_number_pound)
                    .setOnClickListener(this);
            mainView.findViewById(R.id.callout_number_star).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_action_add).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_action_call).setOnClickListener(
                    this);
            mainView.findViewById(R.id.callout_action_delete)
                    .setOnClickListener(this);

            mainView.findViewById(R.id.callout_number_0)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_1)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_2)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_3)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_4)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_5)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_6)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_7)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_8)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_9)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_pound)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_number_star)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_action_add)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_action_call)
                    .setOnLongClickListener(this);
            mainView.findViewById(R.id.callout_action_delete)
                    .setOnLongClickListener(this);
            et.addTextChangedListener(this);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int position, long arg3) {
                    try {
                        et.getText().clear();
                        et.getText().insert(et.getSelectionEnd(),
                                contactList.get(position).telnum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {

                case R.id.callout_action_delete: // 删除输入框内容
                    et.getText().clear();

                    break;
                case R.id.callout_action_call:
                    if (!contactList.isEmpty()) {
                        et.getText().clear();
                        et.getText().insert(et.getSelectionEnd(),
                                contactList.get(0).telnum);
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callout_number_0:
                    et.getText().insert(et.getSelectionEnd(), "0");
                    break;
                case R.id.callout_number_1:
                    et.getText().insert(et.getSelectionEnd(), "1");
                    break;
                case R.id.callout_number_2:
                    et.getText().insert(et.getSelectionEnd(), "2");
                    break;
                case R.id.callout_number_3:
                    et.getText().insert(et.getSelectionEnd(), "3");
                    break;
                case R.id.callout_number_4:
                    et.getText().insert(et.getSelectionEnd(), "4");
                    break;
                case R.id.callout_number_5:
                    et.getText().insert(et.getSelectionEnd(), "5");
                    break;
                case R.id.callout_number_6:
                    et.getText().insert(et.getSelectionEnd(), "6");
                    break;
                case R.id.callout_number_7:
                    et.getText().insert(et.getSelectionEnd(), "7");
                    break;
                case R.id.callout_number_8:
                    et.getText().insert(et.getSelectionEnd(), "8");
                    break;
                case R.id.callout_number_9:
                    et.getText().insert(et.getSelectionEnd(), "9");
                    break;
                case R.id.callout_number_star:
                    et.getText().insert(et.getSelectionEnd(), "*");
                    break;
                case R.id.callout_number_pound:
                    et.getText().insert(et.getSelectionEnd(), "#");
                    break;
                case R.id.callout_action_delete: // 删除输入框内容
                    int length = et.getSelectionEnd();
                    if (length > 0) {
                        et.getText().delete(length - 1, length);
                    }
                    break;
                case R.id.callout_action_add:
                    onYunCall();
                    break;
                case R.id.callout_action_call:
                    if (et.getText().toString().isEmpty()) {
                        XToast.xToast(getActivity(), "未输入号码");
                    } else {
                        onCall();
                    }
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO 自动生成的方法存根

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO 自动生成的方法存根

        }

        private void checkPhoneOnline(final String phone) {
            XLog.xLog(phone);
            if (!phoneMap.containsKey(phone)) {
                XLog.xLog("检索网端");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpClient hc = new DefaultHttpClient();
                        try {

                            HttpGet get = new HttpGet(
                                    VoipApi.URIHEAD + "userscour/"
                                            + phone
                            );
                            HttpResponse response = hc.execute(get);

                            // HttpHelper helper = new HttpHelper();
                            // String value = helper
                            // .getHtml(VoipApi.URIHEAD+"userscour/"
                            // + phone);
                            if (response.getStatusLine() != null) {
                                if (response.getStatusLine().getStatusCode() == 200) {
                                    phoneMap.put(phone, true);
                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (phoneNumber != null
                                                    && phoneNumber
                                                    .equals(phone)
                                                    && yuncall != null) {
                                                yuncall.setEnabled(true);
                                                yuncall.clearAnimation();
                                            }
                                        }
                                    });

                                } else if (response.getStatusLine()
                                        .getStatusCode() == 201) {
                                    phoneMap.put(phone, false);
                                    yuncall.setEnabled(false);
                                    yuncall.clearAnimation();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            hc.getConnectionManager().shutdown();
                        }

                    }
                }).start();
            } else {
                XLog.xLog("检索本地缓存");
                if (phoneMap.get(phone)) {
                    yuncall.setEnabled(true);
                    yuncall.clearAnimation();
                } else {
                    yuncall.setEnabled(false);
                    yuncall.clearAnimation();
                }
            }
        }

        private List<Model> contactList;
        private ContactsAdapter adapter;

        // public static void request(Context context) {
        // Cursor phone = context.getContentResolver()
        // .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        // new String[] { CommonDataKinds.Phone.NUMBER }, null,
        // null, null);
        // XLog.xLog("request");
        // phone.close();
        // }

        /**
         * 按号码-拼音搜索联系人
         *
         * @param str
         */
        public synchronized void search(String str) {

            contactList.clear();
            // 如果搜索条件以0 1 +开头则按号码搜索
            if (str.toString().startsWith("0")
                    || str.toString().startsWith("1")
                    || str.toString().startsWith("+")) {
                for (Model model : allContactList) {
                    if (model.telnum.contains(str)) {
                        model.group = str;
                        contactList.add(model);
                    }
                }
            } else {
                StringBuffer sb = new StringBuffer();
                // 获取每一个数字对应的字母列表并以'-'隔开
                for (int i = 0; i < str.length(); i++) {
                    sb.append((str.charAt(i) <= '9' && str.charAt(i) >= '0') ? BaseUtil.STRS[str
                            .charAt(i) - '0'] : str.charAt(i));
                    if (i != str.length() - 1) {
                        sb.append("-");
                    }
                }

                for (Model model : allContactList) {
                    if (contains(sb.toString(), model, str)) {
                        contactList.add(model);
                    } else if (model.telnum.contains(str)) {
                        model.group = str;
                        contactList.add(model);
                    }
                }
            }
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (!phoneNumber.isEmpty()) {
                        adapter.refresh(contactList, false);
                    } else {
                        adapter.refresh_clear();
                    }
                }
            });

        }

        /**
         * 根据拼音搜索
         *
         * @param str 正则表达式
         *            搜索条件是否大于6个字符
         * @return
         */
        public boolean contains(String str, Model model, String search) {
            if (TextUtils.isEmpty(model.pyname)) {
                return false;
            }
            model.group = "";
            // 搜索条件大于6个字符将不按拼音首字母查询
            if (search.length() < 6) {
                // 根据首字母进行模糊查询
                Pattern pattern = Pattern.compile("^"
                        + str.toUpperCase().replace("-", "[*+#a-z]*"));
                Matcher matcher = pattern.matcher(model.pyname);

                if (matcher.find()) {
                    String tempStr = matcher.group();
                    for (int i = 0; i < tempStr.length(); i++) {
                        if (tempStr.charAt(i) >= 'A'
                                && tempStr.charAt(i) <= 'Z') {
                            model.group += tempStr.charAt(i);
                        }
                    }
                    return true;
                }
            }
            // 根据全拼查询
            Pattern pattern = Pattern.compile(str.replace("-", ""),
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(model.pyname);
            boolean flag = matcher.find();
            if (flag) {
                model.group = matcher.group();
            }
            return flag;
        }
    }

    private class YuncallFragment extends Fragment implements OnClickListener,
            OnLongClickListener {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // TODO 自动生成的方法存根
            return inflater.inflate(R.layout.meicall_callout_yuncall_fragment,
                    container, false);
        }

        @Override
        public void onDestroyView() {
            // TODO 自动生成的方法存根
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            // TODO 自动生成的方法存根
            super.onViewCreated(view, savedInstanceState);
            Fragment newFragment = new ExpenseFragment();
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.callout_yuncall_framelayout, newFragment,
                    "userinfo_information");
            transaction.commit();

            getView().findViewById(R.id.callout_yuncall)
                    .setOnClickListener(this);
        }

        @Override
        public void onStop() {
            // TODO 自动生成的方法存根
            super.onStop();
        }

        @Override
        public boolean onLongClick(View v) {
            // TODO 自动生成的方法存根
            return false;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callout_yuncall:
                    Intent it = new Intent(getActivity(), CallInActivity.class);
                    it.putExtra(CallOutActivity.Intent_First_Action,
                            CallOutActivity.Intent_Second_Action_CALLOUT_P2P);
                    String phone = et.getText().toString();
                    if (PhoneNumberUtil.getPhoneNumber(phone) != null) {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber,
                                PhoneNumberUtil.getPhoneNumber(phone));
                    } else {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber, phone);
                    }

                    getActivity().startActivity(it);
                    break;

                default:
                    break;
            }
        }

    }

    private class CallFragment extends Fragment implements OnClickListener,
            OnLongClickListener {

        public CallFragment(EditText et) {
            super();
            this.et = et;
        }

        private final EditText et;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // TODO 自动生成的方法存根
            return inflater.inflate(R.layout.meicall_callout_call_fragment,
                    container, false);
        }

        @Override
        public void onDestroy() {
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            // TODO 自动生成的方法存根
            super.onViewCreated(view, savedInstanceState);
            Fragment newFragment = new ExpenseFragment();
            FragmentTransaction transaction = this.getFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.callout_call_framelayout, newFragment);
            transaction.commit();

            getView().findViewById(R.id.callout_call_1)
                    .setOnClickListener(this);
            getView().findViewById(R.id.callout_call_2)
                    .setOnClickListener(this);
            getView().findViewById(R.id.callout_call_3)
                    .setOnClickListener(this);

        }

        @Override
        public boolean onLongClick(View v) {
            // TODO 自动生成的方法存根
            return false;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callout_call_1: {
                    Intent it = new Intent("android.intent.action.CALL",
                            Uri.parse("tel:" + et.getText().toString()));
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(it);
                }
                break;
                case R.id.callout_call_2: {
                    Intent it = new Intent(getActivity(), CallOutActivity.class);
                    it.putExtra(CallOutActivity.Intent_First_Action,
                            CallOutActivity.Intent_Second_Action_CALLOUT_P2L);
                    String phone = this.et.getText().toString();
                    if (PhoneNumberUtil.getPhoneNumber(phone) != null) {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber,
                                PhoneNumberUtil.getPhoneNumber(phone));
                    } else {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber, phone);
                    }

                    getActivity().startActivity(it);
                }

                break;
                case R.id.callout_call_3: {
                    XToast.xToast(context, "正在拨出...");
                    Intent it = new Intent(getActivity(), CallOutActivity.class);
                    it.putExtra(CallOutActivity.Intent_First_Action,
                            CallOutActivity.Intent_Second_Action_CALLOUT_CALLBACK);
                    String phone = this.et.getText().toString();
                    if (PhoneNumberUtil.getPhoneNumber(phone) != null) {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber,
                                PhoneNumberUtil.getPhoneNumber(phone));
                    } else {
                        it.putExtra(CallOutActivity.Intent_First_PhoneNumber, phone);
                    }
                    getActivity().startActivity(it);
                }
                break;
            }

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.meicall_callout_layout, container,
                false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XLog.xLog("Fragment", "oncreate");
        context = getActivity();
    }

    @Override
    public void onDestroy() {
        XLog.xLog("Fragment", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO 自动生成的方法存根
        super.onActivityCreated(savedInstanceState);
        this.et = (EditText) getView().findViewById(R.id.callout_text_2);
        ListView lv = (ListView) getView().findViewById(R.id.callout_list);
        Fragment newFragment = new DialFragment(et, lv) {

            @Override
            void onYunCall() {
                Fragment newFragment = new YuncallFragment();
                FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.callout_frame2, newFragment);
                transaction.addToBackStack(this.getTag());
                transaction.commit();
            }

            @Override
            void onCall() {
                Fragment newFragment = new CallFragment(et);
                FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.callout_frame2, newFragment);
                transaction.addToBackStack(this.getTag());
                transaction.commit();
            }

        };
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.callout_frame2, newFragment);
        transaction.commit();
    }

    @Override
    public void onResume() {
        XLog.xLog("Fragment", "onResume");
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        XLog.xLog("Fragment", "onStop");
        super.onStop();
    }

    public static void initData(final Context context, boolean preInit) {
        if (preInit) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (allContactList == null) {
                        allContactList = new ArrayList<Model>();
                        Cursor cursor = context.getContentResolver().query(
                                Phone.CONTENT_URI, BaseUtil.PHONES_PROJECTION,
                                null, null, null);
                        Model m = null;
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                                .moveToNext()) {
                            m = new Model(cursor.getString(0), cursor
                                    .getString(1));
                            allContactList.add(m);
                        }
                        cursor.close();
                        if (allContactList.isEmpty()) {
                            allContactList = null;
                        }
                    }

                }
            }).start();
        } else {
            if (allContactList == null) {
                allContactList = new ArrayList<Model>();
                Cursor cursor = context.getContentResolver().query(
                        Phone.CONTENT_URI, BaseUtil.PHONES_PROJECTION, null,
                        null, null);
                Model m = null;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                        .moveToNext()) {
                    m = new Model(cursor.getString(0), cursor.getString(1));
                    allContactList.add(m);
                }
                cursor.close();
                if (allContactList.isEmpty()) {
                    allContactList = null;
                }
            }
        }
        // adapter.refresh(allContactList, true);
    }
}
