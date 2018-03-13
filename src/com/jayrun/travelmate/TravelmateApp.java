package com.jayrun.travelmate;

import android.app.Application;
import cn.bmob.v3.Bmob;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/*
 * 20160514
 */
public class TravelmateApp extends Application {
	public static final DisplayImageOptions Headoptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true).showStubImage(R.drawable.head_default) // 设置图片下载期间显示的图片
			.showImageForEmptyUri(R.drawable.head_default) // 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.drawable.head_default) // 设置图片加载或解码过程中发生错误显示的图片
			.displayer(new RoundedBitmapDisplayer(200)) // 设置成圆角图片
			.build(); // 创建配置过得DisplayImageOption对象

	@Override
	public void onCreate() {
		super.onCreate();
		// 比目入口
		Bmob.initialize(this, "82c796af51018d18faa856486ee46a37");
		// 讯飞入口
//		StringBuffer param = new StringBuffer();
//		param.append("appid=57355924");
//		param.append(",");
//		// 设置使用v5+
//		param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
		// imageLoader初始化
		SpeechUtility.createUtility(TravelmateApp.this, SpeechConstant.APPID + "=57355924");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs().build();
		ImageLoader.getInstance().init(config);

	}
}
