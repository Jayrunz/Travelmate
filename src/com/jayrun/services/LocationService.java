package com.jayrun.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.jayrun.fragments.RecommendFragment;

public class LocationService extends Service implements AMapLocationListener {
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(this);
		locationOption.setOnceLocation(true);
		locationOption.setNeedAddress(true);
		locationClient.setLocationOption(locationOption);
		locationClient.startLocation();
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		if (location == null) {
			if (locationClient != null) {
				locationClient.startLocation();
			}
			Toast.makeText(LocationService.this, "定位失败", Toast.LENGTH_SHORT)
					.show();
		} else if (location.getErrorCode() != 0) {
			Toast.makeText(LocationService.this, "定位失败", Toast.LENGTH_SHORT)
					.show();
		} else {
			String city = location.getCity().replace("市", "");
			String province = location.getProvince().replace("省", "");
			province = province.replace("壮族自治区", "");
			province = province.replace("维吾尔族治区", "");
			province = province.replace("回族治区", "");
			province = province.replace("自治区", "");
			Intent intent = new Intent(RecommendFragment.ACTION_LOCATION);
			intent.putExtra("city", city);
			intent.putExtra("province", province);
			sendBroadcast(intent);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}
}
