package com.jayrun.adapters;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.mapcore2d.el;
import com.jayrun.services.ChangeUrlCursorService;
import com.jayrun.travelmate.R;
import com.jayrun.beans.ScenicInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ScenicListAdapter extends BaseAdapter {
	private Context context;
	private ViewHolder holder;
	private List<ScenicInfo> scenicinfos = new ArrayList<ScenicInfo>();
	private ScenicInfo scenicInfo;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public ScenicListAdapter(Context context, List<ScenicInfo> scenicinfos) {
		this.scenicinfos = scenicinfos;
		this.context = context;
	}

	public ScenicListAdapter(Context context) {
		this.context = context;
		imageLoader = ImageLoader.getInstance();
		this.options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.showImageForEmptyUri(R.drawable.error_pic)
				.showImageOnFail(R.drawable.error_pic)
				.showStubImage(R.drawable.no_picture).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisc(true)
				.displayer(new RoundedBitmapDisplayer(0)).build();

	}

	@Override
	public int getCount() {
		return scenicinfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_scenic, null);
			holder.layout = (LinearLayout) convertView.findViewById(R.id.lin);
			int version = android.os.Build.VERSION.SDK_INT;
			if (version > 20) {
				holder.layout
						.setBackgroundResource(R.drawable.bg_button_wave_green);
			} else {
				holder.layout
						.setBackgroundResource(R.drawable.bg_button_normal_gray);
			}
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.desc = (TextView) convertView.findViewById(R.id.desc);
			holder.readedCount = (TextView) convertView
					.findViewById(R.id.readedCount);
			holder.pic = (ImageView) convertView.findViewById(R.id.pic);
			holder.location = (TextView) convertView
					.findViewById(R.id.location);
			holder.level = (TextView) convertView.findViewById(R.id.level);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		scenicInfo = scenicinfos.get(position);
		holder.name.setText(scenicInfo.getName());
		holder.desc.setText(scenicInfo.getDescribtion());
		if (scenicInfo.getReadedCount() == null) {
			holder.readedCount.setText(0 + "人看过");
		} else {
			holder.readedCount.setText(scenicInfo.getReadedCount() + "人看过");
		}
		if (null != scenicInfo.getProvince() && "" != scenicInfo.getProvince()) {
			holder.location.setText(scenicInfo.getProvince() + "·"
					+ scenicInfo.getCity());
		} else {
			holder.location.setText(scenicInfo.getCity());
		}

		holder.level.setText(scenicInfo.getLevel());
		if (scenicInfo.getPic() != null) {
			imageLoader.displayImage(scenicInfo.getPic().getFileUrl(),
					holder.pic, options);
		} else if (null != scenicInfo.getUrls()) {
			if (scenicInfo.getUrls().size() > 0) {
				int cursor = 0;
				if (scenicInfo.getUrlCursor() != null) {
					cursor = scenicInfo.getUrlCursor();
				}
				holder.pic.setTag(scenicInfo.getObjectId());
				imageLoader.displayImage(scenicInfo.getUrls().get(cursor),
						holder.pic, options, new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String imageUri,
									View view) {
								// Toast.makeText(context, "onLoadingStarted",
								// Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onLoadingFailed(String imageUri,
									View view, FailReason failReason) {
								// Toast.makeText(context, "onLoadingFailed",
								// Toast.LENGTH_SHORT).show();
								String objId = (String) view.getTag();
								// 图片错误，通知更换url
								Intent intent = new Intent(context,
										ChangeUrlCursorService.class);
								intent.putExtra("sceincId", objId);
								context.startService(intent);
							}

							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								// Toast.makeText(context, "onLoadingComplete",
								// Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onLoadingCancelled(String imageUri,
									View view) {
								// Toast.makeText(context, "onLoadingCancelled",
								// Toast.LENGTH_SHORT).show();

							}
						});
			} else {
				Toast.makeText(context, scenicInfo.getName() + "0",
						Toast.LENGTH_SHORT).show();
				// holder.pic.setImageResource(R.drawable.ic_launcher);
			}
		} else {
			Toast.makeText(context, scenicInfo.getName() + "null",
					Toast.LENGTH_SHORT).show();
			holder.pic.setImageResource(R.drawable.ic_launcher);
		}
		return convertView;
	}

	private class ViewHolder {
		LinearLayout layout;
		ImageView pic;
		TextView name;
		TextView desc;
		TextView readedCount;
		TextView location;
		TextView level;
	}

	public List<ScenicInfo> getScenicinfos() {
		return scenicinfos;
	}

	public void setScenicinfos(List<ScenicInfo> scenicinfos) {
		this.scenicinfos = scenicinfos;
	}
}
