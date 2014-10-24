package cc.kenai.meicall;

import com.baidu.frontia.FrontiaApplication;

import cc.kenai.common.bace.MainApplication;

/**
 * Created by yujunqing on 14-6-5.
 */
public class MyApplication extends MainApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        FrontiaApplication.initFrontiaApplication(this);
    }
}
