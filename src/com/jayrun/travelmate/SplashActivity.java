package com.jayrun.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import cn.bmob.v3.listener.BmobDialogButtonListener;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		BmobUpdateAgent.setUpdateOnlyWifi(false);
		BmobUpdateAgent.update(SplashActivity.this);
		BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {

			@Override
			public void onUpdateReturned(int status, UpdateResponse arg1) {
				if (status != UpdateStatus.Yes) {
					goToMianActivity();
				}
			}
		});
		BmobUpdateAgent.setDialogListener(new BmobDialogButtonListener() {

			@Override
			public void onClick(int which) {
				switch (which) {
				case UpdateStatus.Update:
					finish();
					break;
				case UpdateStatus.NotNow:
					goToMianActivity();
					break;
				case UpdateStatus.Close:
					goToMianActivity();
					break;
				}
			}
		});
	}

	private void goToMianActivity() {
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		SplashActivity.this.finish();
	}
}
