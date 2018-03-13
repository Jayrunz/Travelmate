package com.jayrun.adapters;

import java.util.ArrayList;
import java.util.List;
import com.jayrun.travelmate.R;
import com.jayrun.beans.Words;
import com.jayrun.utils.FriendlyTimeUtil;
import com.jayrun.widgets.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyWordsDetailsListAdapter extends BaseAdapter implements
		OnClickListener {
	private Context context;
	private ViewHolder holder;
	private DeleteButtonCallBack deleteButtonCallBack;
	private List<Words> myWordsList = new ArrayList<Words>();
	private Words words;
	private DisplayImageOptions headOptions;
	private DisplayImageOptions graffitiOptions;
	private ImageLoader imageLoader;

	public MyWordsDetailsListAdapter(Context context, List<Words> wordsList,
			DeleteButtonCallBack deleteButtonCallBack) {
		this.context = context;
		this.myWordsList = wordsList;
		this.deleteButtonCallBack = deleteButtonCallBack;
	}

	public MyWordsDetailsListAdapter(Context context,
			DeleteButtonCallBack deleteButtonCallBack) {
		headOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).showStubImage(R.drawable.head_default) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.head_default) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.head_default) // 设置图片加载或解码过程中发生错误显示的图片
				.displayer(new RoundedBitmapDisplayer(100)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
		graffitiOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).showStubImage(R.drawable.default_graffiti) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.default_graffiti)
				// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.default_graffiti)
				// 设置图片加载或解码过程中发生错误显示的图片
				.displayer(new RoundedBitmapDisplayer(0)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
		imageLoader = ImageLoader.getInstance();
		this.context = context;
		this.deleteButtonCallBack = deleteButtonCallBack;
	}

	@Override
	public int getCount() {
		return myWordsList.size();
		// return 10;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_my_words_details, null);
			holder.deleteImage = (ImageView) convertView
					.findViewById(R.id.delete_word);
			holder.name = (TextView) convertView
					.findViewById(R.id.my_words_details_user_name);
			holder.content = (TextView) convertView
					.findViewById(R.id.my_words_details);
			holder.likeCount = (TextView) convertView
					.findViewById(R.id.my_words_details_like_count);
			holder.head = (CircleImageView) convertView
					.findViewById(R.id.my_words_details_head);
			holder.wordsTime = (TextView) convertView
					.findViewById(R.id.my_words_details_time);
			holder.scenicName = (TextView) convertView
					.findViewById(R.id.word_scenic);
			holder.graffitiView = (ImageView) convertView
					.findViewById(R.id.my_words_graffi);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		words = myWordsList.get(position);
		holder.name.setText(words.getUser().getNickName());
		if (words.getIsText()) {
			holder.graffitiView.setVisibility(View.GONE);
			holder.content.setVisibility(View.VISIBLE);
			holder.content.setText(words.getContent());
		} else {
			holder.graffitiView.setVisibility(View.VISIBLE);
			holder.content.setVisibility(View.GONE);
			imageLoader.displayImage(words.getGraffiti().getFileUrl(),
					holder.graffitiView, graffitiOptions);
			int bgColor = context.getResources().getColor(R.color.white);
			switch (words.getBubbleColor()) {
			case 0:
				bgColor = context.getResources().getColor(R.color.yellow_alpha);
				break;
			case 1:
				bgColor = context.getResources().getColor(R.color.green_alpha);
				break;
			case 2:
				bgColor = context.getResources().getColor(R.color.purple_alpha);
				break;
			case 3:
				bgColor = context.getResources().getColor(R.color.blue_alpha);
				break;
			case 4:
				bgColor = context.getResources().getColor(R.color.black_alpha);
				break;
			case 5:
				bgColor = context.getResources().getColor(R.color.white);
				break;
			}
			holder.graffitiView.setBackgroundColor(bgColor);
		}
		holder.likeCount.setText((words.getLikeUsersId().size() + ""));
		holder.wordsTime.setText(FriendlyTimeUtil.convertTimeToFormat(words
				.getCreatedAt()));
		if (words.getScenic() == null) {
			holder.scenicName.setText(words.getLocationInfo());
		} else {
			holder.scenicName.setText(words.getScenic().getName());
		}
		imageLoader.displayImage(
				words.getUser().getUserHead().getFileUrl(), holder.head,
				headOptions);
		holder.deleteImage.setOnClickListener(this);
		holder.deleteImage.setTag(position);
		return convertView;
	}

	private class ViewHolder {
		ImageView deleteImage;
		CircleImageView head;
		TextView name;
		TextView scenicName;
		TextView content;
		TextView likeCount;
		TextView wordsTime;
		ImageView graffitiView;
	}

	public List<Words> getMyWordsList() {
		return myWordsList;
	}

	public void setMyWordsList(List<Words> myWordsList) {
		this.myWordsList = myWordsList;
	}

	public interface DeleteButtonCallBack {
		public void onDeleteClick(View v);
	}

	@Override
	public void onClick(View view) {
		deleteButtonCallBack.onDeleteClick(view);
	}

}
