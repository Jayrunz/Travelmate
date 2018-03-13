package com.jayrun.travelmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.BmobDialogButtonListener;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

import com.jayrun.utils.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PreferenceActivity extends Activity implements OnClickListener {
	private Button back;
	private TextView clearCache;
	private LinearLayout updateLin;
	private TextView updateInfo;
	private TextView function;
	private TextView suggest;
	private TextView appVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);
		init();
	}

	private void init() {
		back = (Button) findViewById(R.id.preference_back);
		back.setOnClickListener(this);
		appVersion = (TextView) findViewById(R.id.app_version);
		appVersion.setText(Constants.getVersion(getApplicationContext()));
		clearCache = (TextView) findViewById(R.id.clear_cache);
		clearCache.setOnClickListener(this);
		updateLin = (LinearLayout) findViewById(R.id.update_lin);
		updateLin.setOnClickListener(this);
		updateInfo = (TextView) findViewById(R.id.update_info);
		function = (TextView) findViewById(R.id.function);
		function.setOnClickListener(this);
		suggest = (TextView) findViewById(R.id.suggest);
		suggest.setOnClickListener(this);
	}

	private void queryUpdateInfo() {
		BmobUpdateAgent.forceUpdate(this);
		BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {

			@Override
			public void onUpdateReturned(int status, UpdateResponse arg1) {
				String updateText;
				switch (status) {
				case UpdateStatus.Yes:
				case UpdateStatus.IGNORED:
					updateText = "存在新版本";
					break;
				case UpdateStatus.No:
					updateText = "已是最新版本";
					break;
				case UpdateStatus.TimeOut:
					updateText = "";
					break;
				default:
					updateText = "";
					break;
				}
				updateInfo.setText(updateText);
			}
		});
		BmobUpdateAgent.setDialogListener(new BmobDialogButtonListener() {

			@Override
			public void onClick(int arg0) {

			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.preference_back:
			finish();
			break;
		case R.id.clear_cache:
			// 清除内存卡上的图片缓存
			ImageLoader imageLoader = ImageLoader.getInstance();
			imageLoader.clearDiscCache();
			BmobQuery.clearAllCachedResults();
			Toast.makeText(PreferenceActivity.this, "清理完毕", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.update_lin:
			queryUpdateInfo();
			break;
		case R.id.function:
			startActivity(new Intent(PreferenceActivity.this,
					IntroActivity.class));
			break;
		case R.id.suggest:
			startActivity(new Intent(PreferenceActivity.this,
					SuggestionActivity.class));
			break;
		default:
			break;
		}

	}

}
