package com.jayrun.travelmate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import cn.bmob.v3.BmobUser;

import com.jayrun.beans.User;
import com.jayrun.fragments.LeaveWordsFragment;
import com.jayrun.fragments.RouteFragment;
import com.jayrun.fragments.ScenicFragment;
import com.jayrun.fragments.StrategyFragment;
import com.jayrun.widgets.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ScenicActivity extends Activity implements
		OnCheckedChangeListener, OnClickListener {
	private RadioGroup radioGroup;
	private TextView back;
	private CircleImageView userHead;
	private TextView scenicNameText;
	private ScenicFragment scenicFragment;
	private LeaveWordsFragment leaveWordsFragment;
	private RouteFragment routeFragment;
	private StrategyFragment strategyFragment;
	private FragmentTransaction transaction;
	// private String objectId;
	private String scenicId;
	private String scenicName;
	private String cityName;
	private double scenicLatitude;
	private double scenicLongitude;
	private Bundle arguement;
	private int currentFragement;
	private Fragment[] fragments;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private ScenicReveiver scenicReveiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scenic);
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		loadFragment(checkedId);
	}

	private void loadFragment(int checkedId) {
		switch (checkedId) {
		case R.id.intro:
			switchFragment(fragments[currentFragement], fragments[0]);
			currentFragement = 0;
			break;

		case R.id.leave_words:
			switchFragment(fragments[currentFragement], fragments[1]);
			currentFragement = 1;
			break;
		case R.id.route:
			switchFragment(fragments[currentFragement], fragments[2]);
			currentFragement = 2;
			break;
		case R.id.strategy:
			switchFragment(fragments[currentFragement], fragments[3]);
			currentFragement = 3;
			break;
		}
	}

	private void switchFragment(Fragment from, Fragment to) {
		transaction = getFragmentManager().beginTransaction();
		if (!from.isAdded()) {
			transaction.add(R.id.container, from).commit();
		} else if (!to.isAdded()) {
			transaction.hide(from).add(R.id.container, to).commit();
		} else {
			transaction.hide(from).show(to).commit();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_to_main:
			finish();
			break;
		case R.id.scenic_head:
			Intent intent = new Intent(ScenicActivity.this,
					UserInfoActivity.class);
			startActivityForResult(intent, 4000);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void init() {
		// Log.e("===init()===", "options");
		if (options == null) {
			options = new DisplayImageOptions.Builder().cacheInMemory(true)
					.cacheOnDisc(true).showStubImage(R.drawable.head_default) // 设置图片下载期间显示的图片
					.showImageForEmptyUri(R.drawable.head_default) //
					.showImageOnFail(R.drawable.head_default) // 设置图片加载或解码过程中发生错误显示的图片
					.displayer(new RoundedBitmapDisplayer(0)) // 设置成圆角图片
					.build(); // 创建配置过得DisplayImageOption对象
		}
		imageLoader = ImageLoader.getInstance();

		userHead = (CircleImageView) findViewById(R.id.scenic_head);
		userHead.setOnClickListener(this);
		initUserHead();
		// 注册头像改变的广播接收者
		scenicReveiver = new ScenicReveiver();
		IntentFilter filter = new IntentFilter(MainActivity.LOGIN_ACTION);
		registerReceiver(scenicReveiver, filter);

		radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		back = (TextView) findViewById(R.id.back_to_main);
		back.setOnClickListener(this);
		radioGroup.setOnCheckedChangeListener(this);
		Intent intent = getIntent();
		scenicName = intent.getStringExtra("scenicName");
		scenicNameText = (TextView) findViewById(R.id.scenic_name);
		scenicNameText.setText(scenicName);
		scenicId = intent.getStringExtra("scenicId");
		cityName = intent.getStringExtra("city");
		scenicLatitude = intent.getDoubleExtra("scenicLatitude", -1);
		scenicLongitude = intent.getDoubleExtra("scenicLongitude", -1);
		arguement = new Bundle();
		arguement.putString("scenicName", scenicName);
		arguement.putString("scenicId", scenicId);
		arguement.putString("city", cityName);
		arguement.putDouble("scenicLatitude", scenicLatitude);
		arguement.putDouble("scenicLongitude", scenicLongitude);
		scenicFragment = new ScenicFragment();
		leaveWordsFragment = new LeaveWordsFragment();
		routeFragment = new RouteFragment();
		strategyFragment = new StrategyFragment(false);
		fragments = new Fragment[] { scenicFragment, leaveWordsFragment,
				routeFragment, strategyFragment };
		for (int i = 0; i < fragments.length; i++) {
			fragments[i].setArguments(arguement);
		}
		currentFragement = 0;
		loadFragment(R.id.intro);
	}

	public class ScenicReveiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			initUserHead();
		}

	}

	public void initUserHead() {
		User user = BmobUser.getCurrentUser(User.class);
		if (user != null) {
			userHead.setVisibility(View.VISIBLE);
			imageLoader.displayImage(user.getUserHead().getFileUrl(), userHead,
					options);
		} else {
			userHead.setVisibility(View.GONE);
		}
	}
}
