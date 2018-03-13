package com.jayrun.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.beans.ScenicInfo;

public class ChangeUrlCursorService extends Service {
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String scenicId = intent.getStringExtra("sceincId");
		BmobQuery<ScenicInfo> query = new BmobQuery<ScenicInfo>();
		query.getObject(scenicId, 
				new QueryListener<ScenicInfo>() {
					
					@Override
					public void done(ScenicInfo scenicInfo, BmobException e) {
						if (e==null) {
							ScenicInfo info = new ScenicInfo();
							info.setObjectId(scenicId);
							int cursor = 0;
							if (scenicInfo.getUrls().size() > 0) {
								if (null != scenicInfo.getUrlCursor()) {
									cursor = scenicInfo.getUrlCursor();
								}
							}
							int newCursor = (cursor + 1) % scenicInfo.getUrls().size();
							info.setUrlCursor(newCursor);

							info.update(new UpdateListener() {

								public void done(BmobException e) {
								};
							});
							// }
						
						}
					}
				}
		
		// new GetListener<ScenicInfo>() {
		// @Override
		// public void onFailure(int arg0, String arg1) {
		//
		// }
		//
		// @Override
		// public void onSuccess(ScenicInfo scenicInfo) {
		// // if (scenicInfo.getUrls().size() > scenicInfo.getUrlCursor())
		// // {
		// ScenicInfo info = new ScenicInfo();
		// info.setObjectId(scenicId);
		// int cursor = 0;
		// if (scenicInfo.getUrls().size() > 0) {
		// if (null != scenicInfo.getUrlCursor()) {
		// cursor = scenicInfo.getUrlCursor();
		// }
		// }
		// int newCursor = (cursor + 1) % scenicInfo.getUrls().size();
		// info.setUrlCursor(newCursor);
		//
		// info.update(ChangeUrlCursorService.this, new UpdateListener() {
		//
		// @Override
		// public void onSuccess() {
		// // Toast.makeText(ChangeUrlCursorService.this, "光标更改完成",
		// // Toast.LENGTH_SHORT).show();
		// }
		//
		// @Override
		// public void onFailure(int arg0, String arg1) {
		// // Toast.makeText(ChangeUrlCursorService.this, "光标更改失败",
		// // Toast.LENGTH_SHORT).show();
		//
		// }
		// });
		// // }
		// }
		// }
			);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
}
