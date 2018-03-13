package com.jayrun.services;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.beans.ScenicInfo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdateReadCountService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String scenicId = intent.getStringExtra("scenicId");
		ScenicInfo scenicInfo = new ScenicInfo();
		scenicInfo.setObjectId(scenicId);
		scenicInfo.increment("readedCount");
		scenicInfo.update(new UpdateListener() {
			@Override
			public void done(BmobException e) {
				if (e == null) {
					Log.i("travelmate", "景区" + scenicId + "增加一个阅读量");
				}
			}
		});
		return super.onStartCommand(intent, flags, startId);
	}
}
