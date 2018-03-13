package com.jayrun.travelmate;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;

import com.jayrun.beans.User;
import com.jayrun.widgets.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class UserInfoActivity extends Activity implements OnClickListener {
	public final static int PHOTO_ZOOM = 0;
	public final static int TAKE_PHOTO = 1;
	public final static int PHOTO_RESULT = 2;
	public static final String IMAGE_UNSPECIFIED = "image/*";
	// 头像保存路径
	private File file;

	private Button back;
	private CircleImageView userHead;
	private TextView nickName;
	private TextView myWords;
	private TextView myStrategy;
	private TextView changInfo;
	private TextView changePwd;
	private TextView preference;
	private TextView changeUser;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private UserInfoReceiver infoReceiver;

	private PopupWindow pop = null;
	private LinearLayout ll_popup;
	private View parentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parentView = getLayoutInflater().inflate(R.layout.activity_user_info,
				null);
		setContentView(parentView);
		init();
	}

	private void init() {
		userHead = (CircleImageView) findViewById(R.id.info_head);
		userHead.setOnClickListener(this);
		nickName = (TextView) findViewById(R.id.info_name);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).showStubImage(R.drawable.head_default) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.head_default) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.head_default) // 设置图片加载或解码过程中发生错误显示的图片
				.displayer(new RoundedBitmapDisplayer(0)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
		imageLoader = ImageLoader.getInstance();
		initUserInfo();
		back = (Button) findViewById(R.id.user_info_back);
		back.setOnClickListener(this);
		myWords = (TextView) findViewById(R.id.my_words);
		myWords.setOnClickListener(this);
		myStrategy = (TextView) findViewById(R.id.my_strategy);
		myStrategy.setOnClickListener(this);
		changInfo = (TextView) findViewById(R.id.change_info);
		changInfo.setOnClickListener(this);
		changePwd = (TextView) findViewById(R.id.change_pwd);
		changePwd.setOnClickListener(this);
		preference = (TextView) findViewById(R.id.preference);
		preference.setOnClickListener(this);
		changeUser = (TextView) findViewById(R.id.change_user);
		changeUser.setOnClickListener(this);
		// 为用户信息改变注册广播接收
		infoReceiver = new UserInfoReceiver();
		IntentFilter filter = new IntentFilter(MainActivity.LOGIN_ACTION);
		registerReceiver(infoReceiver, filter);

		// 初始化popWindow
		pop = new PopupWindow(UserInfoActivity.this);
		View view = getLayoutInflater().inflate(
				R.layout.plugin_item_popupwindows, null);
		ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
		pop.setWidth(LayoutParams.MATCH_PARENT);
		pop.setHeight(LayoutParams.WRAP_CONTENT);
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setFocusable(true);
		pop.setOutsideTouchable(true);
		pop.setContentView(view);
		RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
		Button fromCamera = (Button) view
				.findViewById(R.id.item_popupwindows_camera);
		Button fromGallery = (Button) view
				.findViewById(R.id.item_popupwindows_Photo);
		Button cancle = (Button) view
				.findViewById(R.id.item_popupwindows_cancel);

		parent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		fromCamera.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				creatFile();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(intent, TAKE_PHOTO);
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		fromGallery.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(IMAGE_UNSPECIFIED);
				// Intent wrapperIntent = Intent.createChooser(intent, null);
				startActivityForResult(intent, PHOTO_ZOOM);
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		cancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PHOTO_ZOOM) {
				photoZoom(data.getData());
			}
			if (requestCode == TAKE_PHOTO) {
				photoZoom(Uri.fromFile(file));
			}

			if (requestCode == PHOTO_RESULT) {
				// 启动服务上传头像
				Intent intent = new Intent(
						"com.jayrun.services.UploadHeadService");
				intent.putExtra("headUri", file.getPath());
				startService(intent);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 图片缩放
	public void photoZoom(Uri uri) {
		creatFile();
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_RESULT);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.user_info_back:
			finish();
			break;
		case R.id.my_words:
			Intent intent = new Intent(UserInfoActivity.this,
					MyWordsActivity.class);
			startActivity(intent);
			break;
		case R.id.my_strategy:
			Intent intent1 = new Intent(UserInfoActivity.this,
					MyStrategyActivity.class);
			startActivity(intent1);
			break;
		case R.id.change_info:
			Intent intent2 = new Intent(UserInfoActivity.this,
					EditUserInfoActivity.class);
			startActivity(intent2);
			break;
		case R.id.info_head:
			ll_popup.startAnimation(AnimationUtils.loadAnimation(
					UserInfoActivity.this, R.anim.activity_translate_in));
			pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
			break;
		case R.id.change_pwd:
			// 根据用户手机号判断是否为第三方登录账号
			if (BmobUser.getCurrentUser()
					.getMobilePhoneNumber() == null) {
				Toast.makeText(UserInfoActivity.this, "第三登录用户不支持密码修改哦！",
						Toast.LENGTH_SHORT).show();
			} else {
				startActivity(new Intent(UserInfoActivity.this,
						ChangePwdActivity.class));
			}
			break;
		case R.id.preference:
			startActivity(new Intent(UserInfoActivity.this,
					PreferenceActivity.class));
			break;
		case R.id.change_user:
			LayoutInflater inflater = LayoutInflater
					.from(UserInfoActivity.this);
			RelativeLayout layout = (RelativeLayout) inflater.inflate(
					R.layout.dialog_reminder, null);
			TextView textView = (TextView) layout.findViewById(R.id.remind);
			textView.setText("您确定退出当前账号吗？");
			Button dialogOK = (Button) layout.findViewById(R.id.dia_ok);
			Button dialogCancle = (Button) layout.findViewById(R.id.dia_cancle);
			final Dialog dialog = new Dialog(UserInfoActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setBackgroundDrawableResource(
					R.drawable.dialog_bg_alpha0);
			dialog.show();
			dialog.getWindow().setContentView(layout);
			dialogOK.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					BmobUser.logOut();
					sendBroadcast(new Intent(MainActivity.LOGIN_ACTION));
					Intent intent = new Intent(UserInfoActivity.this,
							WayOfLoginActivity.class);
					startActivity(intent);
					dialog.dismiss();
					finish();
				}
			});
			dialogCancle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					dialog.dismiss();
				}
			});
		default:
			break;
		}
	}

	private void initUserInfo() {
		User user = BmobUser.getCurrentUser(User.class);
		if (user != null) {
			imageLoader.displayImage(
					user.getUserHead().getFileUrl(),
					userHead, options);
			nickName.setText(user.getNickName());
		}
	}

	private void creatFile() {
		File file1 = new File(Environment.getExternalStorageDirectory()
				.toString() + "/travelmate/head");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		file = new File(file1, +System.currentTimeMillis() + ".JPEG");
	}

	public class UserInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			initUserInfo();
		}

	}

}
