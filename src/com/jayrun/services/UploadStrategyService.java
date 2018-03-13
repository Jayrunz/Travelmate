package com.jayrun.services;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

import com.jayrun.beans.ScenicInfo;
import com.jayrun.beans.Strategy;
import com.jayrun.beans.User;
import com.jayrun.fragments.StrategyFragment;
import com.jayrun.photo.util.Bimp;

public class UploadStrategyService extends Service {
	private Strategy strategyObj;
	private int imgSize;
	private int uploadProgress;
	private ProgressBinder binder = new ProgressBinder();

	public class ProgressBinder extends Binder {
		public int getUploadProgress() {
			return uploadProgress;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {

		// uploadProgress = 0;
		// Log.e("====onUnbind====", "uploadProgress" + uploadProgress);
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		// Log.e("====onRebind====", "uploadProgress" + uploadProgress);
	}

	@Override
	public IBinder onBind(Intent intent) {

		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uploadProgress = 0;
		strategyObj = new Strategy();
		imgSize = Bimp.tempSelectBitmap.size();
		final String strategy = intent.getStringExtra("strategy");
		final String scenicId = intent.getStringExtra("scenicId");
		String[] filePaths = new String[imgSize];
		for (int i = 0; i < filePaths.length; i++) {
			filePaths[i] = Bimp.tempSelectBitmap.get(i).getImagePath();
		}
		final Handler saveHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x56) {
					int imgCount = (Integer) msg.obj;
					User user = BmobUser.getCurrentUser(User.class);
					if (user == null) {
						Toast.makeText(UploadStrategyService.this,
								"用户信息丢失，请重新登录后再试", Toast.LENGTH_LONG).show();
					} else if (imgCount == imgSize) {
						strategyObj.setStrategy(strategy);
						ScenicInfo scenicInfo = new ScenicInfo();
						scenicInfo.setObjectId(scenicId);
						strategyObj.setScenic(scenicInfo);
						strategyObj.setUser(user);
						strategyObj.save(new SaveListener<String>() {

							@Override
							public void done(String arg0, BmobException e) {
								if (e == null) {
									uploadProgress = 100;
									Toast.makeText(getApplicationContext(),
											"发表成功！", Toast.LENGTH_LONG).show();
									Intent intent = new Intent(
											StrategyFragment.UPDATE_STRATEGY);
									intent.putExtra("uploadState", true);
									sendBroadcast(intent);
								} else {
									Toast.makeText(getApplicationContext(),
											"发表失败!" + arg0, Toast.LENGTH_LONG)
											.show();
									Intent intent = new Intent(
											StrategyFragment.UPDATE_STRATEGY);
									intent.putExtra("uploadState", false);
									sendBroadcast(intent);
								}

							}
						}
						// new SaveListener() {
						// @Override
						// public void onSuccess() {
						// uploadProgress = 100;
						// Toast.makeText(getApplicationContext(),
						// "发表成功！", Toast.LENGTH_LONG)
						// .show();
						// Intent intent = new Intent(
						// StrategyFragment.UPDATE_STRATEGY);
						// intent.putExtra("uploadState", true);
						// sendBroadcast(intent);
						// }
						//
						// @Override
						// public void onFailure(int arg0, String arg1) {
						// Toast.makeText(getApplicationContext(),
						// "发表失败!" + arg1,
						// Toast.LENGTH_LONG).show();
						// Intent intent = new Intent(
						// StrategyFragment.UPDATE_STRATEGY);
						// intent.putExtra("uploadState", false);
						// sendBroadcast(intent);
						// }
						// }
								);
					}
				}

			}
		};
		if (imgSize > 0) {
			BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
				@Override
				public void onSuccess(List<BmobFile> bmobFiles,
						List<String> urls) {
					if (bmobFiles.size() == imgSize) {
						switch (bmobFiles.size()) {
						case 1:
							strategyObj.setImg1(bmobFiles.get(0));
							break;
						case 2:
							strategyObj.setImg1(bmobFiles.get(0));
							strategyObj.setImg2(bmobFiles.get(1));
							break;
						case 3:
							strategyObj.setImg1(bmobFiles.get(0));
							strategyObj.setImg2(bmobFiles.get(1));
							strategyObj.setImg3(bmobFiles.get(2));
							break;
						}
						Message message = new Message();
						message.what = 0x56;
						message.obj = bmobFiles.size();
						saveHandler.sendMessage(message);
					}

				}

				@Override
				public void onProgress(int curIndex, int curPercent, int total,
						int totalPercent) {
					// Log.e("===onProgress===", "arg0:" + curIndex
					// + "arg1:" + curPercent + "arg2:" + total
					// + "arg3:" + totalPercent);
					uploadProgress = totalPercent;
				}

				@Override
				public void onError(int arg0, String arg1) {
					Toast.makeText(getApplicationContext(), "发表失败！" + arg1,
							Toast.LENGTH_LONG).show();
					Intent intent = new Intent(StrategyFragment.UPDATE_STRATEGY);
					intent.putExtra("uploadState", false);
					sendBroadcast(intent);
				}
			});
		} else {
			Message message = new Message();
			message.what = 0x56;
			message.obj = 0;
			saveHandler.sendMessage(message);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
