package com.jayrun.travelmate;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.jayrun.utils.SaveViewUtil;
import com.jayrun.widgets.HuaBanView;

public class PainterActivity extends Activity implements OnClickListener,
		AMapLocationListener {
	public final static String GRAFFITI_ACTION = "com.example.travelmate.uploadGraffiti";
	static final int CLEAR = 1;
	static final int CLOSE = 2;
	static final int SUBMIT = 3;

	private String locationInfo = "";
	private int bubbleColor = 5;
	private File graffitiFile;
	private String scenicId;
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	private boolean isFirstSend = true;

	private int windowWidth;
	private HuaBanView huaBan;
	private RelativeLayout paintBoard;
	private View paintBgView;
	private ImageView paintStyle;
	private ImageView paintColor;
	private ImageView paintBg;
	private ImageView paintClear;

	private ImageView paintUndo;
	private ImageView paintRedo;
	private PopupWindow paintStylePop;
	private PopupWindow paintColorPop;
	private PopupWindow paintBackgroundPop;
	private RadioButton eraserPen;
	private RadioButton normalPen;

	private SeekBar penWidth;
	private SeekBar penAlpha;

	private TextView back;
	private TextView submit;

	private GraffitiLoadedReceiver loadedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_painter);
		Intent intent = getIntent();
		scenicId = intent.getStringExtra("scenicId");
		WindowManager windowManager = this.getWindowManager();
		huaBan = (HuaBanView) findViewById(R.id.paint_view);
		paintBgView = (View) findViewById(R.id.paint_bg_view);
		paintBoard = (RelativeLayout) findViewById(R.id.paint_board);
		windowWidth = windowManager.getDefaultDisplay().getWidth();
		LinearLayout.LayoutParams params = (LayoutParams) paintBoard
				.getLayoutParams();
		params.height = (int) ((windowWidth) / 1.526);
		paintBoard.setLayoutParams(params);
		huaBan.setPaintAlpha(255);
		huaBan.setStyle(HuaBanView.PEN);
		huaBan.setPaintWidth(10);
		huaBan.setPaintColor(Color.RED);
		init();
	}

	private void init() {
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		locationOption.setNeedAddress(true);
		locationOption.setInterval(3000);
		// 设置定位监听
		locationClient.setLocationListener(this);

		initPaintStylePop();
		initPaintColorPop();
		initPaintBackgroundPop();
		paintStyle = (ImageView) findViewById(R.id.paint_style);
		paintStyle.setOnClickListener(this);
		paintColor = (ImageView) findViewById(R.id.paint_color);
		paintColor.setOnClickListener(this);
		paintBg = (ImageView) findViewById(R.id.paint_bg);
		paintBg.setOnClickListener(this);
		paintClear = (ImageView) findViewById(R.id.paint_clear);
		paintClear.setOnClickListener(this);
		penWidth = (SeekBar) findViewById(R.id.pen_width);
		penWidth.setOnSeekBarChangeListener(widthChangeListener);
		penAlpha = (SeekBar) findViewById(R.id.pen_alpha);
		penAlpha.setOnSeekBarChangeListener(alphaChangeListener);

		paintUndo = (ImageView) findViewById(R.id.paint_undo);
		paintUndo.setOnClickListener(this);
		paintRedo = (ImageView) findViewById(R.id.paint_redo);
		paintRedo.setOnClickListener(this);

		back = (TextView) findViewById(R.id.paint_back);
		submit = (TextView) findViewById(R.id.paint_submit);
		back.setOnClickListener(this);
		submit.setOnClickListener(this);
		// 注册上传涂鸦状态接收
		loadedReceiver = new GraffitiLoadedReceiver();
		IntentFilter filter = new IntentFilter(GRAFFITI_ACTION);
		registerReceiver(loadedReceiver, filter);
	}

	// 改变画笔宽度监听
	private OnSeekBarChangeListener widthChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {

		}

		@Override
		public void onProgressChanged(SeekBar arg0, int width, boolean arg2) {
			huaBan.setPaintWidth(width);
		}
	};
	// 改变画笔透明度监听
	private OnSeekBarChangeListener alphaChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {

		}

		@Override
		public void onProgressChanged(SeekBar arg0, int alpha, boolean arg2) {
			huaBan.setPaintAlpha(alpha);
		}
	};

	private void initPaintStylePop() {
		paintStylePop = new PopupWindow(PainterActivity.this);
		View view1 = getLayoutInflater().inflate(
				R.layout.window_pop_paint_pen2, null);
		paintStylePop.setWidth(LayoutParams.WRAP_CONTENT);
		paintStylePop.setHeight(LayoutParams.WRAP_CONTENT);
		paintStylePop.setBackgroundDrawable(new BitmapDrawable());
		paintStylePop.setFocusable(true);
		paintStylePop.setOutsideTouchable(true);
		paintStylePop.setContentView(view1);
		eraserPen = (RadioButton) view1.findViewById(R.id.pen_eraser);
		normalPen = (RadioButton) view1.findViewById(R.id.pen_normal);
		RadioGroup penGroup = (RadioGroup) view1
				.findViewById(R.id.paint_pen_rag);
		penGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				switch (id) {
				case R.id.pen_normal:
					paintStyle
							.setImageResource(R.drawable.bg_button_pen_normal);
					huaBan.setStyle(HuaBanView.PEN);
					huaBan.setPaintColor(HuaBanView.paintColor);
					break;
				case R.id.pen_fill:
					paintStyle.setImageResource(R.drawable.bg_button_pen_fill);
					huaBan.setStyle(HuaBanView.PAIL);
					huaBan.setPaintColor(HuaBanView.paintColor);
					break;

				case R.id.pen_eraser:
					paintStyle
							.setImageResource(R.drawable.bg_button_pen_eraser);
					huaBan.setStyle(HuaBanView.ERASER);
					break;

				}
				paintStylePop.dismiss();
			}
		});
	}

	private void initPaintColorPop() {
		paintColorPop = new PopupWindow(PainterActivity.this);
		View view2 = getLayoutInflater().inflate(
				R.layout.window_pop_paint_color2, null);
		paintColorPop.setWidth(LayoutParams.WRAP_CONTENT);
		paintColorPop.setHeight(LayoutParams.WRAP_CONTENT);
		paintColorPop.setBackgroundDrawable(new BitmapDrawable());
		paintColorPop.setFocusable(true);
		paintColorPop.setOutsideTouchable(true);
		paintColorPop.setContentView(view2);
		RadioGroup colorGroup = (RadioGroup) view2
				.findViewById(R.id.paint_color_rag);
		colorGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				if (eraserPen.isChecked()) {
					normalPen.setChecked(true);
					paintStyle
							.setImageResource(R.drawable.bg_button_pen_normal);
				}
				int color = getResources().getColor(R.color.red);
				int bgId = R.drawable.bg_button_color_red;
				switch (id) {
				case R.id.color_white:
					bgId = R.drawable.bg_button_color_white;
					color = getResources().getColor(R.color.white);
					break;
				case R.id.color_pink:
					bgId = R.drawable.bg_button_color_pink;
					color = getResources().getColor(R.color.pink);
					break;
				case R.id.color_yellow:
					bgId = R.drawable.bg_button_color_yellow;
					color = getResources().getColor(R.color.yellow);
					break;
				case R.id.color_orange:
					bgId = R.drawable.bg_button_color_orange;
					color = getResources().getColor(R.color.orange);
					break;
				case R.id.color_red:
					bgId = R.drawable.bg_button_color_red;
					color = getResources().getColor(R.color.red);
					break;
				case R.id.color_purple:
					bgId = R.drawable.bg_button_color_purple;
					color = getResources().getColor(R.color.purple);
					break;
				case R.id.color_green:
					bgId = R.drawable.bg_button_color_green;
					color = getResources().getColor(R.color.green);
					break;
				case R.id.color_green2:
					bgId = R.drawable.bg_button_color_green2;
					color = getResources().getColor(R.color.green2);
					break;
				case R.id.color_blue:
					bgId = R.drawable.bg_button_color_blue;
					color = getResources().getColor(R.color.blue);
					break;
				case R.id.color_blue2:
					bgId = R.drawable.bg_button_color_blue2;
					color = getResources().getColor(R.color.blue2);
					break;
				case R.id.color_gray:
					bgId = R.drawable.bg_button_color_gray;
					color = getResources().getColor(R.color.gray);
					break;
				case R.id.color_black:
					bgId = R.drawable.bg_button_color_black;
					color = getResources().getColor(R.color.black);
					break;
				}
				paintColor.setImageResource(bgId);
				huaBan.setPaintColor(color);
				paintColorPop.dismiss();
			}
		});
	}

	private void initPaintBackgroundPop() {
		paintBackgroundPop = new PopupWindow(PainterActivity.this);
		View view3 = getLayoutInflater().inflate(
				R.layout.window_pop_paint_bgcolor, null);
		paintBackgroundPop.setWidth(LayoutParams.WRAP_CONTENT);
		paintBackgroundPop.setHeight(LayoutParams.WRAP_CONTENT);
		paintBackgroundPop.setBackgroundDrawable(new BitmapDrawable());
		paintBackgroundPop.setFocusable(true);
		paintBackgroundPop.setOutsideTouchable(true);
		paintBackgroundPop.setContentView(view3);
		RadioGroup bgGroup = (RadioGroup) view3.findViewById(R.id.paint_bg_rag);
		bgGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				int bgColor = Color.WHITE;
				int bgId = R.drawable.bg_button_color_white;
				switch (id) {
				case R.id.bg_color_white:
					bubbleColor = 5;
					bgColor = getResources().getColor(R.color.white);
					bgId = R.drawable.bg_button_color_white;
					break;
				case R.id.bg_color_yellow:
					bubbleColor = 0;
					bgColor = getResources().getColor(R.color.yellow_alpha);
					bgId = R.drawable.bg_button_color_yellow;
					break;
				case R.id.bg_color_purple:
					bubbleColor = 2;
					bgColor = getResources().getColor(R.color.purple_alpha);
					bgId = R.drawable.bg_button_color_purple;
					break;
				case R.id.bg_color_green:
					bubbleColor = 1;
					bgColor = getResources().getColor(R.color.green_alpha);
					bgId = R.drawable.bg_button_color_green_alpha;
					break;
				case R.id.bg_color_blue:
					bubbleColor = 3;
					bgColor = getResources().getColor(R.color.blue_alpha);
					bgId = R.drawable.bg_button_color_blue;
					break;
				case R.id.bg_color_black:
					bubbleColor = 4;
					bgColor = getResources().getColor(R.color.black_alpha);
					bgId = R.drawable.bg_button_color_black;
					break;
				}
				paintBgView.setBackgroundColor(bgColor);
				paintBg.setBackgroundResource(bgId);
				paintBackgroundPop.dismiss();
			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.paint_style:
			showUp(view, paintStylePop);
			break;
		case R.id.paint_color:
			showUp(view, paintColorPop);
			break;
		case R.id.paint_bg:
			showUp(view, paintBackgroundPop);
			break;
		case R.id.paint_clear:
			if (huaBan.getDrewPaths().size() > 0) {
				showRemindDialog("画板将清空！", CLEAR);
			} else {
				huaBan.clearScreen();
			}
			break;
		case R.id.paint_undo:
			huaBan.undo();
			break;
		case R.id.paint_redo:
			huaBan.redo();
			break;
		case R.id.paint_back:
			if (huaBan.getDrewPaths().size() > 0) {
				showRemindDialog("画板将丢弃！", CLOSE);
			} else {
				finish();
			}
			break;
		case R.id.paint_submit:
			showRemindDialog("发表涂鸦留言？", SUBMIT);
			break;
		}

	}

	private void showRemindDialog(String remindText, final int action) {
		LayoutInflater inflater = LayoutInflater.from(PainterActivity.this);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.dialog_reminder, null);
		TextView textView = (TextView) layout.findViewById(R.id.remind);
		textView.setText(remindText);
		Button dialogOK = (Button) layout.findViewById(R.id.dia_ok);
		Button dialogCancle = (Button) layout.findViewById(R.id.dia_cancle);
		final Dialog dialog = new Dialog(PainterActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(
				R.drawable.dialog_bg_alpha0);
		dialog.show();
		dialog.getWindow().setContentView(layout);
		dialogCancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialogOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				switch (action) {
				case CLEAR:
					huaBan.clearScreen();
					break;
				case CLOSE:
					finish();
					break;
				case SUBMIT:
					isFirstSend = true;
					submit.setEnabled(false);
					uoloadPaint();
					break;
				}
				dialog.dismiss();
			}
		});
	}

	// 上传涂鸦
	protected void uoloadPaint() {
		File file1 = new File(Environment.getExternalStorageDirectory()
				.toString() + "/travelmate/graffiti");
		if (!file1.exists()) {
			file1.mkdirs();
		}
		graffitiFile = new File(file1, +System.currentTimeMillis() + ".png");
		if (SaveViewUtil.saveScreen(huaBan, graffitiFile.getPath())) {
			locationClient.setLocationOption(locationOption);
			// 定位成功后在Service中上传
			locationClient.startLocation();
		} else {
			submit.setEnabled(true);
			Toast.makeText(PainterActivity.this, "发表失败", Toast.LENGTH_LONG)
					.show();
		}

	}

	private Handler positionGotHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0x21) {
				AMapLocation location = (AMapLocation) msg.obj;
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				Intent intent = new Intent(
						"com.jayrun.services.UploadGraffitiService");
				intent.putExtra("scenicId", scenicId);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("bubbleColor", bubbleColor);
				intent.putExtra("locationInfo", locationInfo);
				intent.putExtra("graffitiUri", graffitiFile.getPath());
				startService(intent);
			}
		};
	};

	private void showUp(View view, PopupWindow window) {
		int[] location2 = new int[2];
		view.getLocationOnScreen(location2);
		window.showAtLocation(view, Gravity.NO_GRAVITY, 0, location2[1] - 250);
	}

	public class GraffitiLoadedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			boolean isSuccess = intent.getBooleanExtra("isSuccess", false);
			if (isSuccess) {
				PainterActivity.this.finish();
			} else {
				submit.setEnabled(true);
			}

		}

	}

	@Override
	public void onLocationChanged(AMapLocation loc) {
		if (null != loc && loc.getErrorCode() == 0) {
			Message msg = positionGotHandler.obtainMessage();
			msg.obj = loc;
			msg.what = 0x21;
			if (isFirstSend) {
				locationInfo = loc.getCity() + loc.getDistrict();
				positionGotHandler.sendMessage(msg);
				isFirstSend = false;
			}
		} else {
			submit.setEnabled(true);
			Toast.makeText(PainterActivity.this, "位置获取失败请稍后再试",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != locationClient) {
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (huaBan.getDrewPaths().size() > 0) {
				showRemindDialog("画板将丢弃！", CLOSE);
			} else {
				finish();
			}
		}
		return false;
	}
}
