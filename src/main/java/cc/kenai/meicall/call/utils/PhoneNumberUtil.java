package cc.kenai.meicall.call.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtil {
	final static String MobileMatchStr = "^(86|(\\+86))?1+\\d{10}$";

	/**
	 * 给定的参数如果是手机号码则返回后11位，否则返回null
	 * 
	 * @return
	 */
	public final static String getPhoneNumber(String num) {
		if(num==null){
            return null;
        }
        Pattern pattern = Pattern.compile(MobileMatchStr);
		Matcher matcher = pattern.matcher(num);
		if (matcher.find()) {
			pattern = Pattern.compile("1+\\d{10}");
			matcher = pattern.matcher(num);
			if (matcher.find()) {
				return matcher.group();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
