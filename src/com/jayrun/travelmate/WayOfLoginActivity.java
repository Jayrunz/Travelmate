package com.jayrun.travelmate;

import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.BmobUser.BmobThirdUserAuth;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.UpdateListener;
import com.jayrun.beans.QQToken;
import com.jayrun.beans.QQUserInfo;
import com.jayrun.beans.User;
import com.jayrun.utils.Constants;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class WayOfLoginActivity extends Activity implements OnClickListener {
	// private Button back;

	private Button qqLogin;
	private Tencent tencent;
	private Button phoneNumberLogin;
	private Button phoneRegister;
	private Button sinaLogin;
	private QQToken qqToken;
	private LinearLayout otherLoginProgressbar;
	public static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
			+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
			+ "follow_app_official_microblog," + "invitation_write";
	// 新浪授权
	private SsoHandler ssoHandler;
	// 授权信息
	private AuthInfo authInfo;
	// 返回的用户信息
	private Oauth2AccessToken weiboAccessToken;
	private boolean tencentIsCancel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_way_of_login);
		init();
	}

	private void init() {
		phoneNumberLogin = (Button) findViewById(R.id.login_with_phone_number);
		phoneNumberLogin.setOnClickListener(this);
		phoneRegister = (Button) findViewById(R.id.register);
		phoneRegister.setOnClickListener(this);
		sinaLogin = (Button) findViewById(R.id.sina_login);
		sinaLogin.setOnClickListener(this);
		qqLogin = (Button) findViewById(R.id.qq_login);
		qqLogin.setOnClickListener(this);
		otherLoginProgressbar = (LinearLayout) findViewById(R.id.other_login_pro_lin);
		authInfo = new AuthInfo(WayOfLoginActivity.this, "2530041509",
				"https://api.weibo.com/oauth2/default.html", SCOPE);
		ssoHandler = new SsoHandler(WayOfLoginActivity.this, authInfo);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 微博授权回调
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		// 腾讯授权回掉
		if (tencent != null) {
			Tencent.onActivityResultData(requestCode, resultCode, data,
					qqLoginListener);
		}
		// 登录成功返回的结果
		if (requestCode == 2000 && resultCode == 2000) {
			Log.e("requestCode == 2000 && requestCode == 2000", "");
			setResult(3000);
			finish();
		}
		// 注册成功但未登录返回的结果
		else if (requestCode == 2000 && resultCode == 2001) {
			Log.e("requestCode == 2000 && resultCode == 2001", "");
			Toast.makeText(WayOfLoginActivity.this, "请登录", Toast.LENGTH_SHORT)
					.show();
			// 启动登录界面
			Intent intent = new Intent(WayOfLoginActivity.this,
					PhoneLoginActivity.class);
			startActivityForResult(intent, 2000);
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
		//手机号码登录
		case R.id.login_with_phone_number:
			Intent intent = new Intent(WayOfLoginActivity.this,
					PhoneLoginActivity.class);
			startActivityForResult(intent, 2000);
			break;
		//注册
		case R.id.register:
			Intent intent2 = new Intent(WayOfLoginActivity.this,
					RegisterActivity.class);
			startActivityForResult(intent2, 2000);
			break;
		//微博登录
		case R.id.sina_login:
			otherLoginProgressbar.setVisibility(View.VISIBLE);
			ssoHandler.authorize(weiboAuthListener);
			break;
		//QQ登录
		case R.id.qq_login:
			tencentIsCancel = false;
			otherLoginProgressbar.setVisibility(View.VISIBLE);
			tencent = Tencent.createInstance("1105311195",
					getApplicationContext());
			if (!tencent.isSessionValid()) {
				tencent.login(WayOfLoginActivity.this, null, qqLoginListener);
			} else {
				tencent.logout(WayOfLoginActivity.this);
				tencent.login(WayOfLoginActivity.this, null, qqLoginListener);
			}
			break;
		}
	}

	// 微博登录授权监听
	private WeiboAuthListener weiboAuthListener = new WeiboAuthListener() {

		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast.makeText(getApplicationContext(), "登录失败！请稍后再试",
					Toast.LENGTH_SHORT).show();
			otherLoginProgressbar.setVisibility(View.GONE);
		}

		@Override
		public void onComplete(Bundle value) {
			weiboAccessToken = Oauth2AccessToken.parseAccessToken(value);
			if (weiboAccessToken.isSessionValid()) {
				BmobThirdUserAuth userAuth = new BmobThirdUserAuth("weibo",
						weiboAccessToken.getToken(),
						weiboAccessToken.getExpiresTime() + "",
						weiboAccessToken.getUid());
				BmobUser.loginWithAuthData(userAuth,
						weiboLoginListener);
			} else {
				Toast.makeText(getApplicationContext(), "登录失败！请稍后再试",
						Toast.LENGTH_SHORT).show();
				otherLoginProgressbar.setVisibility(View.GONE);
			}
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "授权取消", Toast.LENGTH_SHORT)
					.show();
			otherLoginProgressbar.setVisibility(View.GONE);
		}
	};
	
	// 比目新浪微博第三方登录监听
	private LogInListener<JSONObject> weiboLoginListener=new LogInListener<JSONObject>() {
		
		@Override
		public void done(JSONObject jsonObject, BmobException e) {
			if (e==null) {
				User user = new User();
				User olduser = BmobUser.getCurrentUser(User.class);
				if (olduser != null) {
					if ("".equals(olduser.getNickName())
							|| null == olduser.getNickName()) {
						user.setNickName(weiboAccessToken.getUid());
					}
					if (null == olduser.getUserHead()) {
						BmobFile headFile = new BmobFile("defaultHead", "default",
								Constants.DEFAULT_HEAD_URL);
						user.setUserHead(headFile);
					}
					user.update(olduser.getObjectId(),updateUserListener);
				}
			}else{
				Toast.makeText(getApplicationContext(), "登录失败！请稍后再试",
						Toast.LENGTH_SHORT).show();
				otherLoginProgressbar.setVisibility(View.GONE);
			}
			
		}
	};
	// 比目新浪微博第三方登录监听
	// private OtherLoginListener weiboLoginListener = new OtherLoginListener()
	// {
	//
	// @Override
	// public void onSuccess(JSONObject arg0) {
	// User user = new User();
	// User olduser = BmobUser.getCurrentUser(User.class);
	// if (olduser != null) {
	// if ("".equals(olduser.getNickName())
	// || null == olduser.getNickName()) {
	// user.setNickName(weiboAccessToken.getUid());
	// }
	// if (null == olduser.getUserHead()) {
	// BmobFile headFile = new BmobFile("defaultHead", "default",
	// Constants.DEFAULT_HEAD_URL);
	// user.setUserHead(headFile);
	// }
	// user.update(olduser.getObjectId(),updateUserListener);
	// }
	// }
	//
	// @Override
	// public void onFailure(int arg0, String arg1) {
	// Toast.makeText(getApplicationContext(), "登录失败！请稍后再试",
	// Toast.LENGTH_SHORT).show();
	// otherLoginProgressbar.setVisibility(View.GONE);
	// }
	// };
	// 腾讯登陆授权监听
	private IUiListener qqLoginListener = new IUiListener() {

		@Override
		public void onError(UiError arg0) {
			Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
					Toast.LENGTH_LONG).show();
			otherLoginProgressbar.setVisibility(View.GONE);
		}

		@Override
		public void onComplete(Object jsonObj) {
			Log.e("===onComplete===", jsonObj.toString());
			qqToken = Constants
					.getThirdToken(jsonObj.toString(), QQToken.class);
			Log.e("==qqtoken==",
					"Access_token:" + qqToken.getAccess_token() + "   opid"
							+ qqToken.getOpenid() + "  exprisIn"
							+ qqToken.getExpires_in());
			if (!tencentIsCancel) {
				BmobThirdUserAuth authInfo = new BmobThirdUserAuth("qq",
						qqToken.getAccess_token(), qqToken.getExpires_in(),
						qqToken.getOpenid());
				BmobUser.loginWithAuthData(authInfo,qQLoginListener);
				tencentIsCancel = false;
			}

		}

		@Override
		public void onCancel() {
			tencentIsCancel = true;
			otherLoginProgressbar.setVisibility(View.GONE);
		}
	};
	
	// 比目qq第三方登录监听
	private LogInListener<JSONObject> qQLoginListener=new LogInListener<JSONObject>() {
		
		@Override
		public void done(JSONObject jsonObject, BmobException e) {
			
			if (e==null) {
				tencent.setAccessToken(qqToken.getAccess_token(),
						qqToken.getExpires_in());
				tencent.setOpenId(qqToken.getOpenid());
				UserInfo userInfo = new UserInfo(WayOfLoginActivity.this,
						tencent.getQQToken());
				userInfo.getUserInfo(getUserInfoListener);
			}else{
				Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
						Toast.LENGTH_LONG).show();
				otherLoginProgressbar.setVisibility(View.GONE);
			}
		}
	};
	
	// // 比目qq第三方登录监听
	// private OtherLoginListener qQLoginListener = new OtherLoginListener() {
	//
	// @Override
	// public void onSuccess(JSONObject jsonObject) {
	// // Log.e("===ThrirdloginSuccess===", jsonObject.toString());
	// tencent.setAccessToken(qqToken.getAccess_token(),
	// qqToken.getExpires_in());
	// tencent.setOpenId(qqToken.getOpenid());
	// UserInfo userInfo = new UserInfo(WayOfLoginActivity.this,
	// tencent.getQQToken());
	// userInfo.getUserInfo(getUserInfoListener);
	// }
	//
	// @Override
	// public void onFailure(int arg0, String msg) {
	// Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
	// Toast.LENGTH_LONG).show();
	// Log.i("==onFailure==", "第三方登陆失败：" + msg);
	// otherLoginProgressbar.setVisibility(View.GONE);
	// }
	// };
	
	
	// 腾讯获取用户信息监听
	private IUiListener getUserInfoListener = new IUiListener() {

		@Override
		public void onError(UiError arg0) {
			Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
					Toast.LENGTH_LONG).show();
			otherLoginProgressbar.setVisibility(View.GONE);
		}

		@Override
		public void onComplete(Object jsonObj) {
			Log.e("getInfo", jsonObj.toString());
			User user = new User();
			User olduser = BmobUser.getCurrentUser(User.class);
			if (olduser != null) {
				QQUserInfo qqUserInfo = Constants.getThirdToken(
						jsonObj.toString(), QQUserInfo.class);
				if ("".equals(olduser.getNickName())
						|| null == olduser.getNickName()) {
					user.setNickName(qqUserInfo.getNickname());
				}
				if ("".equals(olduser.getSex()) || null == olduser.getSex()) {
					user.setSex(qqUserInfo.getGender());
				}
				if (null == olduser.getUserHead()) {
					BmobFile headFile = new BmobFile("defaultHead", "default",
							Constants.DEFAULT_HEAD_URL);
					user.setUserHead(headFile);
				}
				user.update(olduser.getObjectId(),
						updateUserListener);
			}
		}

		@Override
		public void onCancel() {

		}
	};
	
	// 比目完善第三方登录用户的信息监听
	private UpdateListener updateUserListener=new UpdateListener() {
		
		@Override
		public void done(BmobException e) {
			
			if (e==null) {
				Intent intent = new Intent(MainActivity.LOGIN_ACTION);
				sendBroadcast(intent);
				WayOfLoginActivity.this.finish();
				otherLoginProgressbar.setVisibility(View.GONE);
			}else{
				Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
						Toast.LENGTH_LONG).show();
				otherLoginProgressbar.setVisibility(View.GONE);
			}
		}
	};
	
	// 比目完善第三方登录用户的信息监听
	// private UpdateListener updateUserListener = new UpdateListener() {
	//
	// @Override
	// public void onSuccess() {
	// Intent intent = new Intent(MainActivity.LOGIN_ACTION);
	// sendBroadcast(intent);
	// WayOfLoginActivity.this.finish();
	// otherLoginProgressbar.setVisibility(View.GONE);
	// }
	//
	// @Override
	// public void onFailure(int arg0, String arg1) {
	// Toast.makeText(WayOfLoginActivity.this, "登录失败，请稍后重试",
	// Toast.LENGTH_LONG).show();
	// otherLoginProgressbar.setVisibility(View.GONE);
	// }
	// };

}
