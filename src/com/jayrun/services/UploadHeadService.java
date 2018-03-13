package com.jayrun.services;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.jayrun.beans.User;
import com.jayrun.travelmate.MainActivity;
import com.jayrun.utils.Constants;

public class UploadHeadService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String headUri = intent.getStringExtra("headUri");
		final File head = new File(headUri);
		final BmobFile headFile = new BmobFile(head);
		headFile.uploadblock(new UploadFileListener() {

			@Override
			public void done(BmobException e) {
				if (e == null) {
					onSuccess();
				} else {
					onFailure(e.getErrorCode(), e.getMessage());
				}

			}

			public void onSuccess() {
				User newUser = new User();
				User oldUser = BmobUser.getCurrentUser(User.class);
				if (oldUser == null) {
					Toast.makeText(getApplicationContext(), "用户信息空",
							Toast.LENGTH_SHORT).show();
				} else {
					if (!oldUser.getUserHead().getFileUrl()
							.equals(Constants.DEFAULT_HEAD_URL)) {
						oldUser.getUserHead().delete();
					}
					newUser.setUserHead(headFile);
					newUser.update(oldUser.getObjectId(), new UpdateListener() {

						public void done(BmobException e) {
							if (e == null) {
								Toast.makeText(getApplicationContext(), "上传成功",
										Toast.LENGTH_SHORT).show();
								head.delete();
								sendBroadcast(new Intent(
										MainActivity.LOGIN_ACTION));
							} else {
								Toast.makeText(getApplicationContext(),
										"上传失败" + e.getMessage(),
										Toast.LENGTH_SHORT).show();
							}
						};
						// @Override
						// public void onSuccess() {
						// Toast.makeText(getApplicationContext(),
						// "上传成功", Toast.LENGTH_SHORT).show();
						// head.delete();
						// sendBroadcast(new Intent(
						// MainActivity.LOGIN_ACTION));
						// }
						//
						// @Override
						// public void onFailure(int arg0, String arg1) {
						// Toast.makeText(getApplicationContext(),
						// "上传失败" + arg1, Toast.LENGTH_SHORT)
						// .show();
						// }
					});
				}
			}

			public void onFailure(int arg0, String arg1) {
				Toast.makeText(getApplicationContext(), "上传失败" + arg1,
						Toast.LENGTH_SHORT).show();
			}
		});
		return super.onStartCommand(intent, flags, startId);
	}
}
