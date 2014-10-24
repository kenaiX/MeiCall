package cc.kenai.meicall.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StringUtils {

	public final static String encode(String s)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "utf-8");
	}
	public final static String decode(String s)
			throws UnsupportedEncodingException {
		return URLDecoder.decode(s, "utf-8");
	}
}
