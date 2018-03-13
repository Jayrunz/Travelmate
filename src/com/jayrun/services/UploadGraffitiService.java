package com.jayrun.services;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

import com.jayrun.beans.ScenicInfo;
import com.jayrun.beans.User;
import com.jayrun.beans.Words;
import com.jayrun.fragments.LeaveWordsFragment;
import com.jayrun.travelmate.PainterActivity;

public class UploadGraffitiService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("onStartCommand", "onStartCommand");
		final String scenicId = intent.getStringExtra("scenicId");
		final double latitude = intent.getDoubleExtra("latitude", 1);
		final double longitude = intent.getDoubleExtra("longitude", 1);
		final int bubbleColor = intent.getIntExtra("bubbleColor", 0);
		String graffitiUri = intent.getStringExtra("graffitiUri");
		final File graffitiFile = new File(graffitiUri);
		final String locationInfo = intent.getStringExtra("locationInfo");
		String paths[] = new String[] { graffitiUri };
		BmobFile.uploadBatch(paths, new UploadBatchListener() {

			@Override
			public void onSuccess(List<BmobFile> files, List<String> arg1) {
				User currentUser = BmobUser.getCurrentUser(User.class);
				if (currentUser == null) {
					Toast.makeText(getApplicationContext(), "用户信息空",
							Toast.LENGTH_SHORT).show();
				} else {
					Words newWords = new Words();
					newWords.setUser(currentUser);
					newWords.setGraffiti(files.get(0));
					newWords.setLocation(new BmobGeoPoint(longitude, latitude));
					newWords.setLocation(new BmobGeoPoint(longitude, latitude));
					newWords.setIsText(false);
					if (!"".equals(scenicId)) {
						ScenicInfo scenicInfo = new ScenicInfo();
						scenicInfo.setObjectId(scenicId);
						newWords.setScenic(scenicInfo);
					} else {
						newWords.setLocationInfo(locationInfo);
					}
					newWords.setBubbleColor(bubbleColor);
					newWords.save(new SaveListener<String>() {

						@Override
						public void done(String arg0, BmobException e) {
							if (e == null) {
								Intent intent = new Intent(
										PainterActivity.GRAFFITI_ACTION);
								intent.putExtra("isSuccess", true);
								sendBroadcast(intent);
								Intent intent2 = new Intent(
										LeaveWordsFragment.DELETE_ACTION);
								sendBroadcast(intent2);
								graffitiFile.delete();
							} else {
								Intent intent = new Intent(
										PainterActivity.GRAFFITI_ACTION);
								intent.putExtra("isSuccess", false);
								sendBroadcast(intent);
								Toast.makeText(UploadGraffitiService.this,
										"发表失败，请稍后再试" + e.getMessage(),
										Toast.LENGTH_SHORT).show();
							}
						}
					}
					// new SaveListener() {
					//
					// @Override
					// public void onSuccess() {
					// Intent intent = new Intent(
					// PainterActivity.GRAFFITI_ACTION);
					// intent.putExtra("isSuccess", true);
					// sendBroadcast(intent);
					// Intent intent2 = new Intent(
					// LeaveWordsFragment.DELETE_ACTION);
					// sendBroadcast(intent2);
					// graffitiFile.delete();
					// }
					//
					// @Override
					// public void onFailure(int arg0, String arg1) {
					// Intent intent = new Intent(
					// PainterActivity.GRAFFITI_ACTION);
					// intent.putExtra("isSuccess", false);
					// sendBroadcast(intent);
					// Toast.makeText(UploadGraffitiService.this,
					// "发表失败，请稍后再试" + arg1,
					// Toast.LENGTH_SHORT).show();
					// }
					// }

					);
				}
			}

			@Override
			public void onProgress(int arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void onError(int arg0, String arg1) {
				Toast.makeText(UploadGraffitiService.this, "发表失败，请稍后再试" + arg1,
						Toast.LENGTH_SHORT).show();
			}
		});
		return super.onStartCommand(intent, flags, startId);
	}
}
