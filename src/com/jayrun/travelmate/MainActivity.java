package com.jayrun.travelmate;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.jayrun.beans.User;
import com.jayrun.fragments.MainFragment;
import com.jayrun.fragments.RecommendFragment;
import com.jayrun.utils.Constants;
import com.jayrun.widgets.CircleImageView;
import com.jayrun.widgets.SlideMenuView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.slidingmenu.lib.SlidingMenu;

public class MainActivity extends FragmentActivity implements OnClickListener {
	public static final String LOGIN_ACTION = "com.example.travelmate.login";
	public static final int STATE_LOGIN = 11;
	public static final int STATE_LOGOUT = 12;

	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mLinearLayout;
	private ViewPager pager;
	private ImageView mImageView;
	private int mScreenWidth;
	private int item_width;
	private List<TextView> textViews = new ArrayList<TextView>();
	private int endPosition;
	private int beginPosition;
	private int currentFragmentIndex;
	private int lastIndext = 0;
	private boolean isEnd;

	private ArrayList<Fragment> fragments;
	private String[] scenicsCategory = { "首页推荐", "游山玩水", "休闲度假", "大学校园",
			"历史人文", "民族风情" };
	private String[] scenicTag = { "推荐", "山水", "休闲", "大学", "人文", "风情" };
	// private Button filedToReload;
	// private ImageView loadingImg;
	private AnimationDrawable aDrawable;
	private ImageView showMenu;
	private TextView scenicType;
	private ImageView beginSearch;
	private ImageView clearSearchText;
	private EditText searchText;
	private TextView searchBottomLine;
	private LinearLayout searchLinear;
	private String keywords = "";
	private MainReceiver mainReceiver;
	private Button loginNow;
	private TextView leadLogin;
	private CircleImageView userHead;
	private TextView nickName;
	private TextView signature;
	private ImageView menuTitleBg;
	private Button exitApp;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private InputMethodManager inputManager;
	private Button myWords;
	private Button myPaint;
	private Button myStrategy;
	private Button myInfo;
	private Button nearWords;
	private Button preference;

	private SlidingMenu slidingMenu;
	private SlideMenuView slidingMenuView;
	private Intent searchIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		init();
		changeLoginState(STATE_LOGIN);
		// aDrawable.start();
	}

	@Override
	protected void onStart() {
		// Log.e("====onStart====", "MainActivity开始");
		super.onStart();
	}

	//
	@Override
	protected void onRestart() {
		// Log.e("====onRestart====", "MainActivity重新开始");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.e("====onResume====", "MainActivity恢复");
	}

	@Override
	protected void onPause() {
		super.onPause();
		hideSearchView();
		// Log.e("====onPause====", "MainActivity暂停");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Log.e("====onStop====", "MainActivity停止");
	}

	private void hideSearchView() {
		searchLinear.setVisibility(View.GONE);
		searchBottomLine.setVisibility(View.GONE);
		searchText.setText("");
		scenicType.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Log.e("====onDestroy====", "MainActivity销毁");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.show_menu:
			slidingMenu.showMenu();
			break;
		case R.id.login_now:
			Intent intent = new Intent(MainActivity.this,
					WayOfLoginActivity.class);
			startActivityForResult(intent, 3000);
			break;
		case R.id.user_head:
			Intent intent2 = new Intent(MainActivity.this,
					UserInfoActivity.class);
			startActivityForResult(intent2, 3000);
			break;
		case R.id.menu_near_words:
			startActivity(new Intent(MainActivity.this, NearWordsActivity.class));
			break;
		case R.id.menu_preference:
			startActivity(new Intent(MainActivity.this,
					PreferenceActivity.class));
			break;
		case R.id.search_begin:
			searchScenic();
			break;
		case R.id.clear_search_text:
			searchText.setText("");
			break;
		case R.id.filed_to_reload:
			aDrawable.start();
			break;
		case R.id.exit_app:
			finish();
			break;
		default:
			break;
		}
	}

	private OnClickListener onMenuClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (BmobUser.getCurrentUser() != null) {
				switch (view.getId()) {
				case R.id.menu_my_words:
					Intent intent3 = new Intent(MainActivity.this,
							MyWordsActivity.class);
					intent3.putExtra("wordsType", 1);
					startActivity(intent3);
					break;
				case R.id.menu_my_paint:
					Intent intent4 = new Intent(MainActivity.this,
							MyWordsActivity.class);
					intent4.putExtra("wordsType", 2);
					startActivity(intent4);
					break;
				case R.id.menu_my_strategy:
					startActivity(new Intent(MainActivity.this,
							MyStrategyActivity.class));
					break;
				case R.id.menu_my_info:
					startActivity(new Intent(MainActivity.this,
							EditUserInfoActivity.class));
					break;
				}

			} else {
				Toast.makeText(MainActivity.this, "登录后才能查看哦",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void searchScenic() {
		if (searchLinear.getVisibility() == View.VISIBLE) {
			if (!Constants.removeBlankAtBegin(searchText.getText().toString())
					.isEmpty()) {
				keywords = searchText.getText().toString();
				// 通知fragment进行搜索操作
				searchIntent = new Intent(MainFragment.ACTION_SEARCH);
				searchIntent.putExtra("keyword", searchText.getText()
						.toString());
				sendBroadcast(searchIntent);
				if (inputManager.isActive()) {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(MainActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			} else {
				keywords = "";
				searchText.setText("");
			}
		} else if (searchLinear.getVisibility() == View.GONE) {
			// 回到主页
			pager.setCurrentItem(0, false);
			// 打开搜索框
			searchBottomLine.setVisibility(View.VISIBLE);
			searchLinear.setVisibility(View.VISIBLE);
			scenicType.setVisibility(View.GONE);
			inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			searchText.requestFocus();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 初始化加载
	private void init() {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.hsv_view);
		mLinearLayout = (LinearLayout) findViewById(R.id.hsv_content);
		mImageView = (ImageView) findViewById(R.id.tab_bootom_line);
		item_width = (int) ((mScreenWidth / 4.0 + 0.5f));
		mImageView.getLayoutParams().width = item_width;
		// 初始化导航
		initNav();
		initViewPager();

		slidingMenuView = new SlideMenuView(this);
		slidingMenu = slidingMenuView.newInstance();

		// loadingImg = (ImageView) findViewById(R.id.loading_img);
		// aDrawable = (AnimationDrawable) loadingImg.getDrawable();
		// filedToReload = (Button) findViewById(R.id.filed_to_reload);
		// filedToReload.setOnClickListener(this);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).showStubImage(R.drawable.head_default)
				.showImageForEmptyUri(R.drawable.head_default)
				.showImageOnFail(R.drawable.head_default)
				.displayer(new RoundedBitmapDisplayer(0)).build();
		imageLoader = ImageLoader.getInstance();
		menuTitleBg = (ImageView) findViewById(R.id.title_bg);
		// 未登录状态MainMenu的title
		leadLogin = (TextView) findViewById(R.id.lead_login_text);
		loginNow = (Button) findViewById(R.id.login_now);
		loginNow.setOnClickListener(this);
		// 已经登录状态MainMenu的title
		userHead = (CircleImageView) findViewById(R.id.user_head);
		userHead.setOnClickListener(this);
		nickName = (TextView) findViewById(R.id.nick_name);
		signature = (TextView) findViewById(R.id.signature);
		myWords = (Button) findViewById(R.id.menu_my_words);
		myWords.setOnClickListener(onMenuClick);
		myPaint = (Button) findViewById(R.id.menu_my_paint);
		myPaint.setOnClickListener(onMenuClick);
		myStrategy = (Button) findViewById(R.id.menu_my_strategy);
		myStrategy.setOnClickListener(onMenuClick);
		myInfo = (Button) findViewById(R.id.menu_my_info);
		myInfo.setOnClickListener(onMenuClick);
		nearWords = (Button) findViewById(R.id.menu_near_words);
		nearWords.setOnClickListener(this);
		preference = (Button) findViewById(R.id.menu_preference);
		preference.setOnClickListener(this);

		// 退出程序按钮
		exitApp = (Button) findViewById(R.id.exit_app);
		exitApp.setOnClickListener(this);
		// 打开menu按钮
		showMenu = (ImageView) findViewById(R.id.show_menu);
		showMenu.setOnClickListener(this);
		scenicType = (TextView) findViewById(R.id.scenic_type);
		// 搜索框
		beginSearch = (ImageView) findViewById(R.id.search_begin);
		beginSearch.setOnClickListener(this);
		clearSearchText = (ImageView) findViewById(R.id.clear_search_text);
		clearSearchText.setOnClickListener(this);
		searchText = (EditText) findViewById(R.id.search_text);
		searchText.addTextChangedListener(watcher);
		searchText.setOnKeyListener(keyListener);
		searchBottomLine = (TextView) findViewById(R.id.search_bottom_line);
		searchLinear = (LinearLayout) findViewById(R.id.searchview_edit);
		// 注册一个广播监听登录状态
		mainReceiver = new MainReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LOGIN_ACTION);
		registerReceiver(mainReceiver, filter);
	}

	private void initNav() {
		for (int i = 0; i < 6; i++) {
			RelativeLayout layout = new RelativeLayout(this);
			TextView view = new TextView(this);
			if (i == 0) {
				view.setTextColor(getResources().getColor(
						R.color.app_main_color));
			}
			view.setText(scenicsCategory[i]);
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 100);
			view.setGravity(Gravity.CENTER);
			view.setLayoutParams(params1);
			textViews.add(view);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 100);
			layout.setGravity(Gravity.CENTER);
			layout.addView(view, params);

			mLinearLayout.addView(layout, (int) (mScreenWidth / 4 + 0.5f), 100);
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					pager.setCurrentItem((Integer) v.getTag());
				}
			});
			layout.setTag(i);
		}
	}

	private void initViewPager() {
		pager = (ViewPager) findViewById(R.id.scenic_pager);
		fragments = new ArrayList<Fragment>();
		for (int i = 0; i < 6; i++) {
			Bundle data = new Bundle();
			// 为每个Fragment传入不同的标签
			data.putString("tag", scenicTag[i]);
			if (i == 0) {
				RecommendFragment fragment = new RecommendFragment();
				fragment.setArguments(data);
				fragments.add(fragment);
			} else {
				MainFragment fragment = new MainFragment();
				fragment.setArguments(data);
				fragments.add(fragment);
			}
		}
		MyFragmentPagerAdapter fragmentPagerAdapter = new MyFragmentPagerAdapter(
				getSupportFragmentManager(), fragments);
		pager.setAdapter(fragmentPagerAdapter);
		fragmentPagerAdapter.setFragments(fragments);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());
		pager.setCurrentItem(0);
		// 启动定位Service
		startService(new Intent("com.jayrun.services.LocationService"));
	}

	private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> fragments;
		private FragmentManager fm;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fm = fm;
		}

		public MyFragmentPagerAdapter(FragmentManager fm,
				ArrayList<Fragment> fragments) {
			super(fm);
			this.fm = fm;
			this.fragments = fragments;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		public void setFragments(ArrayList<Fragment> fragments) {
			if (this.fragments != null) {
				FragmentTransaction ft = fm.beginTransaction();
				for (Fragment f : this.fragments) {
					ft.remove(f);
				}
				ft.commit();
				ft = null;
				fm.executePendingTransactions();
			}
			this.fragments = fragments;
			notifyDataSetChanged();
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			Object obj = super.instantiateItem(container, position);
			return obj;
		}

	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(final int position) {
			hideSearchView();
			MainFragment.isFromSearch = false;
			Animation animation = new TranslateAnimation(endPosition, position
					* item_width, 0, 0);
			beginPosition = position * item_width;
			currentFragmentIndex = position;
			if (animation != null) {
				animation.setFillAfter(true);
				animation.setDuration(0);
				mImageView.startAnimation(animation);
				mHorizontalScrollView.smoothScrollTo((currentFragmentIndex - 1)
						* item_width, 0);
			}
			textViews.get(position).setTextColor(
					getResources().getColor(R.color.app_main_color));
			textViews.get(lastIndext).setTextColor(Color.BLACK);
			lastIndext = position;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (!isEnd) {
				if (currentFragmentIndex == position) {
					endPosition = item_width * currentFragmentIndex
							+ (int) (item_width * positionOffset);
				}
				if (currentFragmentIndex == position + 1) {
					endPosition = item_width * currentFragmentIndex
							- (int) (item_width * (1 - positionOffset));
				}

				Animation mAnimation = new TranslateAnimation(beginPosition,
						endPosition, 0, 0);
				mAnimation.setFillAfter(true);
				mAnimation.setDuration(0);
				mImageView.startAnimation(mAnimation);
				mHorizontalScrollView.invalidate();
				beginPosition = endPosition;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_DRAGGING) {
				isEnd = false;
			} else if (state == ViewPager.SCROLL_STATE_SETTLING) {
				isEnd = true;
				beginPosition = currentFragmentIndex * item_width;
				if (pager.getCurrentItem() == currentFragmentIndex) {
					// 未跳入下一个页面
					mImageView.clearAnimation();
					Animation animation = null;
					// 恢复位置
					animation = new TranslateAnimation(endPosition,
							currentFragmentIndex * item_width, 0, 0);
					animation.setFillAfter(true);
					animation.setDuration(1);
					mImageView.startAnimation(animation);
					mHorizontalScrollView.invalidate();
					endPosition = currentFragmentIndex * item_width;
				}
			}
		}

	}

	public class MainReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			changeLoginState(STATE_LOGIN);
		}
	}

	private void changeLoginState(int STATE) {
		switch (STATE) {
		case STATE_LOGIN:
			User user = BmobUser.getCurrentUser(User.class);
			if (user != null) {
				leadLogin.setVisibility(View.GONE);
				loginNow.setVisibility(View.GONE);
				userHead.setVisibility(View.VISIBLE);
				nickName.setVisibility(View.VISIBLE);
				signature.setVisibility(View.VISIBLE);
				menuTitleBg.setImageResource(R.drawable.menu_bg_normal);
				imageLoader.displayImage(user.getUserHead().getFileUrl(),
						userHead, options);
				nickName.setText(user.getNickName());
				signature.setText(user.getSignature());
			} else {
				leadLogin.setVisibility(View.VISIBLE);
				loginNow.setVisibility(View.VISIBLE);
				userHead.setVisibility(View.GONE);
				nickName.setVisibility(View.GONE);
				signature.setVisibility(View.GONE);
				menuTitleBg.setImageResource(R.drawable.menu_bg_gray);
			}

			break;
		case STATE_LOGOUT:
			leadLogin.setVisibility(View.VISIBLE);
			loginNow.setVisibility(View.VISIBLE);
			userHead.setVisibility(View.GONE);
			nickName.setVisibility(View.GONE);
			signature.setVisibility(View.GONE);
			menuTitleBg.setImageResource(R.drawable.menu_bg_gray);
			break;
		default:
			break;
		}
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence text, int arg1, int arg2,
				int arg3) {
			if (text.toString().length() > 0) {
				clearSearchText.setVisibility(View.VISIBLE);
				keywords = text.toString();
			} else {
				keywords = "";
				clearSearchText.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {

		}
	};
	private OnKeyListener keyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				searchScenic();
			}
			return false;
		}
	};
}
