package com.jayrun.photo.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.mapcore2d.el;
import com.jayrun.travelmate.R;
import com.jayrun.utils.Constants;
import com.jayrun.fragments.StrategyFragment;
import com.jayrun.photo.util.Bimp;
import com.jayrun.photo.util.FileUtils;
import com.jayrun.photo.util.ImageItem;
import com.jayrun.photo.util.PublicWay;
import com.jayrun.photo.util.Res;
import com.jayrun.photo.zoom.IPhotoView;
import com.jayrun.services.UploadStrategyService;

/**
 * 添加攻略页面
 * 
 */
public class AddStrategyActivity extends Activity implements OnClickListener {

	private GridView noScrollgridview;
	private GridAdapter adapter;
	private View parentView;
	private PopupWindow pop = null;
	private LinearLayout ll_popup;
	public static Bitmap bimap;
	private Button back;
	private Button publish;
	private EditText strategyText;
	private Thread myThread;
	private boolean firstLoad = true;
	private String imagePath = "";
	private String scenicId;
	private String scenicName;
	private Thread getProgressThread;
	private ProgressBar uploadProgressBar;
	private int uploadProgress;
	private Handler progressHandler;
	UploadStrategyService.ProgressBinder progressBinder;
	private ServiceConnection uploadConnection;
	private UploadStateReceiver stateReceiver;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Res.init(AddStrategyActivity.this);
		bimap = BitmapFactory.decodeResource(getResources(),
				R.drawable.icon_addpic_unfocused);
		PublicWay.activityList.add(this);
		parentView = getLayoutInflater().inflate(
				R.layout.activity_add_strategy, null);
		setContentView(parentView);
		if (PublicWay.firstCreat) {
			scenicName = getIntent().getStringExtra("scenicName");
			scenicId = getIntent().getStringExtra("scenicId");
			PublicWay.SCENICID = scenicId;
			PublicWay.firstCreat = false;
		}
		Init();
		strategyText.setText(PublicWay.strategyText);
		strategyText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				PublicWay.strategyText = arg0.toString();
				// Log.e("afterTextChanged", arg0.toString());
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PublicWay.strategyText = "";
		// Log.e("===onDestroy===", "AddQuestionActivity销毁");
		// unbindService(uploadConnection);
		// getProgressThread.stop();
		// getProgressThread = null;
	}

	public void Init() {
		// 注册上传攻略状态广播接收
		stateReceiver = new UploadStateReceiver();
		IntentFilter filter = new IntentFilter(StrategyFragment.UPDATE_STRATEGY);
		registerReceiver(stateReceiver, filter);

		uploadProgressBar = (ProgressBar) findViewById(R.id.upload_progress);
		back = (Button) findViewById(R.id.back);
		publish = (Button) findViewById(R.id.publish_strategy);
		strategyText = (EditText) findViewById(R.id.add_strategy);
		back.setOnClickListener(this);
		publish.setOnClickListener(this);
		pop = new PopupWindow(AddStrategyActivity.this);
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
		Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
		Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
		Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
		parent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		bt1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				photo();
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		bt2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AddStrategyActivity.this,
						AlbumActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.activity_translate_in,
						R.anim.activity_translate_out);
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});
		bt3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pop.dismiss();
				ll_popup.clearAnimation();
			}
		});

		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new GridAdapter(this);
		adapter.update();
		noScrollgridview.setAdapter(adapter);
		noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 == Bimp.tempSelectBitmap.size()) {
					// Log.i("ddddddd", "----------");
					ll_popup.startAnimation(AnimationUtils.loadAnimation(
							AddStrategyActivity.this,
							R.anim.activity_translate_in));
					pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
				} else {
					Intent intent = new Intent(AddStrategyActivity.this,
							GalleryActivity.class);
					intent.putExtra("position", "1");
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});

	}

	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private int selectedPosition = -1;
		private boolean shape;

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public GridAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void update() {
			loading();
		}

		public int getCount() {
			if (Bimp.tempSelectBitmap.size() == PublicWay.num) {
				return PublicWay.num;
			}
			return (Bimp.tempSelectBitmap.size() + 1);
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.plugin_item_published_grida, parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == Bimp.tempSelectBitmap.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon_addpic_unfocused));
				if (position == PublicWay.num) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position)
						.getBitmap());
			}

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				case 2:
					myThread = null;
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading() {
			myThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Bimp.tempSelectBitmap.size() > 0 || firstLoad) {
							firstLoad = false;
							if (Bimp.max == Bimp.tempSelectBitmap.size()) {
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
								break;
							} else {
								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							}
						} else {
							handler.sendEmptyMessage(2);
							Bimp.max = 0;
							break;
						}

					}
				}
			});
			myThread.start();
		}
	}

	public String getString(String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}

	protected void onRestart() {
		adapter.update();
		super.onRestart();
	}

	private static final int TAKE_PICTURE = 0x000001;

	public void photo() {
		File file1 = new File(Environment.getExternalStorageDirectory()
				.toString() + "/travelmate/img");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		File file = new File(file1, +System.currentTimeMillis() + ".JPEG");
		imagePath = file.toString();
		Uri uri = Uri.fromFile(file);
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		openCameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			ImageItem takePhoto = new ImageItem();
			if (Bimp.tempSelectBitmap.size() < PublicWay.num
					&& resultCode == RESULT_OK) {
				if (data != null) {
					if (data.hasExtra("data")) {
						Bitmap bm = (Bitmap) data.getParcelableExtra("data");
						takePhoto.setBitmap(bm);
					}
				} else {
					takePhoto.setImagePath(imagePath);
				}
				Bimp.tempSelectBitmap.add(takePhoto);
			}
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 点击手机返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getProgressThread != null) {
				getProgressThread.interrupt();
			}
			finishThis();
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 返回按钮
		case R.id.back:
			if (getProgressThread != null) {
				getProgressThread.interrupt();
			}
			finishThis();
			break;
		//发表攻略
		case R.id.publish_strategy:
			String strategy = strategyText.getText().toString()
					.replaceFirst("\\s*", "");
			if (strategy.isEmpty()) {
				Toast.makeText(AddStrategyActivity.this, "攻略内容不能为空!",
						Toast.LENGTH_LONG).show();
			} else if (!Constants.isNetAvailable(getApplicationContext())) {
				Toast.makeText(AddStrategyActivity.this, "无网络连接，请检查网络后重试",
						Toast.LENGTH_SHORT).show();
			} else {
				uploadProgressBar.setVisibility(View.VISIBLE);
				progressHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == 0x54) {
							uploadProgressBar.setProgress(uploadProgress);
						}
					}
				};
				Intent intent = new Intent(
						"com.jayrun.services.UploadStrategyService");
				intent.putExtra("strategy", strategy);
				intent.putExtra("scenicId", PublicWay.SCENICID);
				startService(intent);
				uploadConnection = new ServiceConnection() {

					@Override
					public void onServiceDisconnected(ComponentName arg0) {

					}

					@Override
					public void onServiceConnected(ComponentName arg0,
							IBinder binder) {
						uploadProgress = 0;
						publish.setClickable(false);
						noScrollgridview.setClickable(false);
						strategyText.setEnabled(false);
						progressBinder = (UploadStrategyService.ProgressBinder) binder;
						getProgressThread = new Thread() {

							@Override
							public void run() {
								int targetProgress = 33;
								if (Bimp.tempSelectBitmap.size() == 3) {
									targetProgress = 33;
								} else if (Bimp.tempSelectBitmap.size() == 2) {
									targetProgress = 50;
								} else {
									targetProgress = 100;
								}
								int virtualProgress = 0;
								int currentProgress = 0;
								while (uploadProgress <= 90) {

									currentProgress = progressBinder
											.getUploadProgress();
									if (currentProgress >= targetProgress
											&& currentProgress != 100) {
										virtualProgress = currentProgress;
										targetProgress = currentProgress
												+ currentProgress
												- targetProgress;
									}
									if (virtualProgress < targetProgress) {
										virtualProgress++;
									}
									uploadProgress = virtualProgress;
									progressHandler.sendEmptyMessage(0x54);
									if (currentProgress == 100) {
										uploadProgress = 100;
										progressHandler.sendEmptyMessage(0x54);
									}
									try {
										Thread.sleep(100);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}

						};
						getProgressThread.start();
					}
				};
				bindService(intent, uploadConnection, Service.BIND_AUTO_CREATE);
			}

			break;
		}
	}

	private void finishThis() {
		Bimp.tempSelectBitmap.clear();
		Bimp.max = 0;
		adapter.notifyDataSetChanged();
		for (int i = 0; i < PublicWay.activityList.size(); i++) {
			if (null != PublicWay.activityList.get(i)) {
				PublicWay.activityList.get(i).finish();
			}
		}
		PublicWay.firstCreat = true;
		finish();
	}

	public class UploadStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.hasExtra("uploadState")) {
				uploadProgressBar.setVisibility(View.GONE);
				uploadProgressBar.setProgress(0);
				uploadProgress = 0;
				try {
					unbindService(uploadConnection);
				} catch (Exception e) {
					e.printStackTrace();
				}

				boolean isSuccess = intent
						.getBooleanExtra("uploadState", false);
				// 发表成功
				if (isSuccess) {
					finishThis();
				}
				// 发表失败
				else {
					publish.setClickable(true);
					noScrollgridview.setClickable(true);
					strategyText.setEnabled(true);
					// Toast.makeText(AddQuestionActivity.this, "发表失败，请稍后再试",
					// Toast.LENGTH_LONG).show();
				}
			}

		}

	}
}
