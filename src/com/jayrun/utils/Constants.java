package com.jayrun.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.google.gson.Gson;

public class Constants {
	public static final String DEFAULT_HEAD_URL = "http://file.bmob.cn/M03/5F/16/oYYBAFcuxdmAfzsSAACRrNoDFys854.jpg";
	public static final String QQ_ID = "";

	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean isInTheSceinc(List<PoiItem> poiItems, LatLng myLatLng) {
		boolean is = false;
		LatLng scenicLatLng = null;
		for (PoiItem poiItem : poiItems) {
			scenicLatLng = AMapUtil.convertToLatLng(poiItem.getLatLonPoint());
			if (AMapUtils.calculateLineDistance(scenicLatLng, myLatLng) < 2000) {
				is = true;
			}
		}
		return is;
	}

	public static boolean isWayAvailable(List<PoiItem> poiItems, LatLng myLatLng) {
		boolean is = false;
		LatLng scenicLatLng = null;
		for (PoiItem poiItem : poiItems) {
			scenicLatLng = AMapUtil.convertToLatLng(poiItem.getLatLonPoint());
			if (AMapUtils.calculateLineDistance(scenicLatLng, myLatLng) < 2000) {
				is = true;
			}
		}
		return is;
	}

	public static boolean isNetAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return (info != null && info.isAvailable());
	}

	public static String getNoBlankString(String str) {
		if (str != null && !"".equals(str)) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			String strNoBlank = m.replaceAll("");
			return strNoBlank;
		} else {
			return str;
		}
	}

	public static String removeBlankAtBegin(String str) {
		if (str != null && !"".equals(str)) {
			return str.replaceFirst("\\s*", "");
		} else {
			return str;
		}

	}

	public static <T> T getThirdToken(String jsonStr, Class<T> cls) {
		T t = null;
		try {
			Gson gson = new Gson();
			t = gson.fromJson(jsonStr, cls);
		} catch (Exception e) {
		}
		return t;

	}
}
