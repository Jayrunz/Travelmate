package com.jayrun.fragments;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.helper.GsonUtil;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

import com.google.gson.Gson;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.jayrun.beans.ScenicInfo;
import com.jayrun.travelmate.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ScenicFragment extends Fragment implements OnClickListener {
	private String scenicId = null;
	private ScenicInfo scenicInfo;
	private ImageView scenicPic;
	private TextView scenicName, scenicLevel, scenicLocation,
			scenicDescription;
	private View parentView;
	private RelativeLayout speakScenicDescription;
	private ImageView imageSpeaking;
	private ImageView imageStop;
	private SpeechSynthesizer speechSynthesizer = null;
	private boolean isSpeaking = false;
	private AnimationDrawable animationDrawable;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).showImageForEmptyUri(R.drawable.error_pic)
			.showImageOnFail(R.drawable.error_pic)
			.showStubImage(R.drawable.no_picture).cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisc(true)
			.displayer(new RoundedBitmapDisplayer(0)).build();;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_scenic, null);
		scenicPic = (ImageView) parentView.findViewById(R.id.scenic_pic);
		scenicName = (TextView) parentView.findViewById(R.id.scenic_name);
		scenicLevel = (TextView) parentView.findViewById(R.id.scenic_level);
		scenicLocation = (TextView) parentView
				.findViewById(R.id.scenic_location);
		scenicDescription = (TextView) parentView
				.findViewById(R.id.scenic_description);
		speakScenicDescription = (RelativeLayout) parentView
				.findViewById(R.id.btn_read_description);
		speakScenicDescription.setOnClickListener(this);
		imageSpeaking = (ImageView) parentView
				.findViewById(R.id.voice_speaking);
		imageStop = (ImageView) parentView.findViewById(R.id.voice_stop);
		speechSynthesizer = SpeechSynthesizer.createSynthesizer(getActivity(),new InitListener() {
			
			@Override
			public void onInit(int arg0) {
				Log.i("讯飞", "init"+arg0);
				
			}
		});
		setSpeakerParam();
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0123) {
					scenicInfo = (ScenicInfo) msg.obj;
					scenicName.setText(scenicInfo.getName());
					if (null != scenicInfo.getProvince()
							&& "" != scenicInfo.getProvince()) {
						scenicLocation.setText(scenicInfo.getProvince() + "·"
								+ scenicInfo.getCity());
					} else {
						scenicLocation.setText(scenicInfo.getCity());
					}
					scenicDescription.setText(scenicInfo.getDescribtion());
					if (scenicInfo.getLevel() != null) {
						scenicLevel.setText(scenicInfo.getLevel());
					}
					if (scenicInfo.getPic() != null) {
						imageLoader.displayImage(scenicInfo.getPic()
								.getFileUrl(), scenicPic, options);
					} else if (scenicInfo.getUrls().size() > 0) {
						int cursor = 0;
						if (null != scenicInfo.getUrlCursor()) {
							cursor = scenicInfo.getUrlCursor();
						}
						imageLoader.displayImage(
								scenicInfo.getUrls().get(cursor), scenicPic,
								options);
					}
				}
			}
		};
		if (getArguments().containsKey("scenicId")) {
			scenicId = getArguments().getString("scenicId");
		}
		BmobQuery<ScenicInfo> query = new BmobQuery<ScenicInfo>();
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereEqualTo("objectId", scenicId);
		query.findObjects(new FindListener<ScenicInfo>() {
			
			@Override
			public void done(List<ScenicInfo> infos, BmobException e) {
				
				if (e==null) {
					ScenicInfo info=infos.get(0);
					speakScenicDescription.setEnabled(true);
					Message message = new Message();
					message.what = 0123;
					message.obj = info;
					handler.sendMessage(message);
					// 通知增加阅读数量
					Intent intent2 = new Intent(
							"com.jayrun.services.UpdateReadCountService");
					intent2.putExtra("scenicId", info.getObjectId());
					getActivity().startService(intent2);
				}
				else {
					if (e.getErrorCode() != 9015 && e.getErrorCode() != 9009) {
						Toast.makeText(getActivity(), "景区信息获取失败，请稍后重试！",
								Toast.LENGTH_LONG).show();
						Log.e("错误", "查询错误"+e.getMessage());
					}
				}
			}
		});
		return parentView;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_read_description:
			animationDrawable = (AnimationDrawable) imageSpeaking.getDrawable();
			if (isSpeaking) {
				imageSpeaking.setVisibility(View.GONE);
				imageStop.setVisibility(View.VISIBLE);
				speechSynthesizer.stopSpeaking();
				animationDrawable.stop();
			} else {
				speechSynthesizer.startSpeaking(scenicDescription.getText()
						.toString(), synthesizerListener);
				isSpeaking=true;
				//Toast.makeText(getActivity(), "朗读code"+code, Toast.LENGTH_LONG).show();
			}
			break;

		default:
			break;
		}
	}

	private void setSpeakerParam() {
		speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE,
				SpeechConstant.TYPE_CLOUD);
		speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyu");
		speechSynthesizer.setParameter(SpeechConstant.SPEED, "45");
		speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
		speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
		speechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
		speechSynthesizer
				.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		speechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		speechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}

	// 定义讯飞语音合成的监听
	private SynthesizerListener synthesizerListener = new SynthesizerListener() {

		@Override
		public void onSpeakResumed() {
			imageSpeaking.setVisibility(View.VISIBLE);
			imageStop.setVisibility(View.GONE);
			animationDrawable.start();
			
			//Toast.makeText(getActivity(), "onSpeakResumed",
			//Toast.LENGTH_LONG).show();

		}

		@Override
		public void onSpeakProgress(int arg0, int arg1, int arg2) {

		}

		@Override
		public void onSpeakPaused() {
			imageSpeaking.setVisibility(View.GONE);
			imageStop.setVisibility(View.VISIBLE);
			isSpeaking = false;
			animationDrawable.stop();
			//Toast.makeText(getActivity(), "onSpeakPaused",
			//Toast.LENGTH_LONG).show();
			
		}

		@Override
		public void onSpeakBegin() {
			imageSpeaking.setVisibility(View.VISIBLE);
			imageStop.setVisibility(View.GONE);
			speakScenicDescription.setEnabled(true);
			animationDrawable.start();
			isSpeaking = true;
			//Toast.makeText(getActivity(), "onSpeakBegin",
			//Toast.LENGTH_LONG).show();
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			isSpeaking = false;
			//Log.e("讯飞", "朗读错误"+arg3.toString());
			//Toast.makeText(getActivity(), "onEvent",
			//Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCompleted(SpeechError arg0) {
			imageSpeaking.setVisibility(View.GONE);
			imageStop.setVisibility(View.VISIBLE);
			animationDrawable.stop();
			isSpeaking = false;
			speakScenicDescription.setEnabled(true);
			Toast.makeText(getActivity(), "朗读完毕", Toast.LENGTH_LONG).show();
			//Toast.makeText(getActivity(), "onCompleted",
			//Toast.LENGTH_LONG).show();
		}

		@Override
		public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {

		}

	};

	@Override
	public void onDestroy() {
		speechSynthesizer.stopSpeaking();
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (hidden) {
			speechSynthesizer.stopSpeaking();
			imageSpeaking.setVisibility(View.GONE);
			imageStop.setVisibility(View.VISIBLE);
			if (animationDrawable != null) {
				animationDrawable.stop();
			}
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onPause() {
		speechSynthesizer.stopSpeaking();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
