package cc.kenai.meicall.call.utils;

import android.app.Activity;
import android.app.AlertDialog;

/**
 * Created by yujunqing on 14-6-5.
 */
public class DialogHelper {
    public static void showHelper(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(
                activity);
        builder.setTitle("使用说明");
        builder.setMessage("kenai在此感谢MY长期以来的支持！本软件只为回馈MY不以盈利为目的，经过长期测试，在现有费率基础上能够基本达到服务器成本的收支平衡\n---------\n所有费用和本手机绑定，并不与手机号绑定。root后可能造成费用数据出错。\n---------\n长按拨号键可以快速选择联想出的第一位联系人");
        builder.create().show();
    }
}
