package com.jayrun.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.bmob.v3.BmobUser;

import com.jayrun.beans.Strategy;
import com.jayrun.travelmate.R;
import com.jayrun.utils.FriendlyTimeUtil;
import com.jayrun.widgets.CircleImageView;
import com.jayrun.widgets.CommentListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class StrategyListAdapter extends BaseAdapter implements
		OnClickListener, OnItemClickListener {
	private boolean isMyStrategy;
	private int windowWidth;
	private ViewHolder holder;
	private Context context;
	private List<Strategy> strategies = new ArrayList<Strategy>();
	private Strategy strategy;
	private OnButtonInChildClick buttonInChildClick;
	private OnCommentInChildClick commentInChildClick;
	public static CommentListAdapter commentAdapter;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private DisplayImageOptions Headoptions;
	private List<ArrayList<String>> urlLists = new ArrayList<ArrayList<String>>();
	private Bitmap bitmap;

	public StrategyListAdapter(Context context, boolean isMyStrategy,
			OnButtonInChildClick buttonInChildClick,
			OnCommentInChildClick commentInChildClick) {
		super();
		WindowManager windowManager = ((Activity) context).getWindowManager();
		windowWidth = windowManager.getDefaultDisplay().getWidth();
		this.context = context;
		this.isMyStrategy = isMyStrategy;
		this.buttonInChildClick = buttonInChildClick;
		this.commentInChildClick = commentInChildClick;
		imageLoader = ImageLoader.getInstance();
		this.options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(100))
				.build();
		this.Headoptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true)
				.showStubImage(R.drawable.head_default)
				.showImageForEmptyUri(R.drawable.head_default)
				.showImageOnFail(R.drawable.head_default)
				.displayer(new RoundedBitmapDisplayer(0)).build();
		this.bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.no_picture);
	}

	@Override
	public int getCount() {
		return strategies.size();
		// return 10;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// Log.e("getView", strategies.size() + "");
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_strategy, parent, false);

			holder.head = (CircleImageView) convertView.findViewById(R.id.head);
			holder.image1 = (ImageView) convertView.findViewById(R.id.img1);
			holder.image2 = (ImageView) convertView.findViewById(R.id.img2);
			holder.image3 = (ImageView) convertView.findViewById(R.id.img3);

			holder.textTime = (TextView) convertView
					.findViewById(R.id.strategy_time);
			holder.textUserName = (TextView) convertView
					.findViewById(R.id.strategry_user_name);
			holder.textLikeCount = (TextView) convertView
					.findViewById(R.id.like_count);
			holder.textCommentsCount = (TextView) convertView
					.findViewById(R.id.comments_count);

			holder.textStrategyDetails = (TextView) convertView
					.findViewById(R.id.strategy_details);
			holder.imageContainer = (LinearLayout) convertView
					.findViewById(R.id.image_container);
			holder.commentListView = (CommentListView) convertView
					.findViewById(R.id.comment_list);
			holder.likeImage = (ImageView) convertView
					.findViewById(R.id.like_img);
			holder.commentImage = (ImageView) convertView
					.findViewById(R.id.comment_img);
			holder.deleteImage = (ImageView) convertView
					.findViewById(R.id.delete_strategy);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		strategy = strategies.get(position);
		holder.textTime.setText(FriendlyTimeUtil.convertTimeToFormat(strategy
				.getCreatedAt()));
		holder.textUserName.setText(strategy.getUser().getNickName());
		holder.textLikeCount.setText(strategy.getLikeUserIds().size() + "");
		holder.textCommentsCount.setText(strategy.getCommentList().size() + "");
		holder.textStrategyDetails.setText(strategy.getStrategy());
		holder.image1.setImageBitmap(bitmap);
		holder.image2.setImageBitmap(bitmap);
		holder.image3.setImageBitmap(bitmap);
		commentAdapter = new CommentListAdapter(strategy.getCommentList(),
				context);
		holder.commentListView.setAdapter(commentAdapter);
		if (strategy.getImg1() != null) {
			holder.image1.setTag(R.string.tag_url, strategy.getImg1()
					.getFileUrl());
			holder.image1.setTag(R.string.tag_parent_pos, position);
			holder.image1.setTag(R.string.tag_child_pos, 0);
			holder.image1.setOnClickListener(this);
		}
		if (strategy.getImg2() != null) {
			holder.image2.setTag(R.string.tag_url, strategy.getImg2()
					.getFileUrl());
			holder.image2.setTag(R.string.tag_parent_pos, position);
			holder.image2.setTag(R.string.tag_child_pos, 1);
			holder.image2.setOnClickListener(this);
		}
		if (strategy.getImg3() != null) {
			holder.image3.setTag(R.string.tag_url, strategy.getImg3()
					.getFileUrl());
			holder.image3.setTag(R.string.tag_parent_pos, position);
			holder.image3.setTag(R.string.tag_child_pos, 2);
			holder.image3.setOnClickListener(this);
		}
		imageLoader.displayImage(
				strategy.getUser().getUserHead().getFileUrl(),
				holder.head, Headoptions);
		if (strategy.getImg1() == null && strategy.getImg2() == null
				&& strategy.getImg3() == null) {
			holder.imageContainer.setVisibility(View.GONE);
		} else {
			holder.imageContainer.setVisibility(View.VISIBLE);
			int imgCount = 0;
			if (strategy.getImg1() != null) {
				imgCount = 1;
				if (strategy.getImg2() != null) {
					imgCount = 2;
					if (strategy.getImg3() != null) {
						imgCount = 3;
					}
				}
			}
			switch (imgCount) {
			case 1:
				holder.image1.setVisibility(View.VISIBLE);
				holder.image2.setVisibility(View.GONE);
				holder.image3.setVisibility(View.GONE);
				holder.image1.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				holder.image1.setTag(strategy.getImg1().getFileUrl());
				holder.image1.setTag(R.string.app_name, "");
				imageLoader.loadImage(strategy.getImg1().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image1
										.getTag(R.string.tag_url))) {
									holder.image1.setImageBitmap(loadedImage);
								}
							}
						});
				break;

			case 2:
				holder.image1.setVisibility(View.VISIBLE);
				holder.image2.setVisibility(View.VISIBLE);
				holder.image3.setVisibility(View.GONE);
				LayoutParams layoutParams2 = new LayoutParams(windowWidth / 2,
						windowWidth / 2, 1);
				layoutParams2.setMargins(2, 0, 2, 0);
				holder.image1.setLayoutParams(layoutParams2);
				holder.image1.setTag(strategy.getImg1().getFileUrl());
				imageLoader.loadImage(strategy.getImg1().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image1
										.getTag(R.string.tag_url))) {
									holder.image1.setImageBitmap(loadedImage);
								}
							}
						});

				holder.image2.setLayoutParams(layoutParams2);
				holder.image2.setTag(strategy.getImg2().getFileUrl());
				imageLoader.loadImage(strategy.getImg2().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {

								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image2
										.getTag(R.string.tag_url))) {
									holder.image2.setImageBitmap(loadedImage);
								}
							}
						});
				break;
			case 3:
				holder.image1.setVisibility(View.VISIBLE);
				holder.image2.setVisibility(View.VISIBLE);
				holder.image3.setVisibility(View.VISIBLE);
				LayoutParams layoutParams3 = new LayoutParams(windowWidth / 3,
						windowWidth / 3, 1);
				layoutParams3.setMargins(2, 0, 2, 0);
				holder.image1.setLayoutParams(layoutParams3);
				holder.image1.setTag(strategy.getImg1().getFileUrl());
				imageLoader.loadImage(strategy.getImg1().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image1
										.getTag(R.string.tag_url))) {
									holder.image1.setImageBitmap(loadedImage);
								}
							}
						});

				holder.image2.setLayoutParams(layoutParams3);
				holder.image2.setTag(strategy.getImg2().getFileUrl());
				imageLoader.loadImage(strategy.getImg2().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {

								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image2
										.getTag(R.string.tag_url))) {
									holder.image2.setImageBitmap(loadedImage);
								}

							}
						});

				holder.image3.setLayoutParams(layoutParams3);
				holder.image3.setTag(strategy.getImg3().getFileUrl());
				imageLoader.loadImage(strategy.getImg3().getFileUrl(),
						new ImageSize(300, 300), options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view,
										loadedImage);
								if (imageUri.equals(holder.image3
										.getTag(R.string.tag_url))) {
									holder.image3.setImageBitmap(loadedImage);
								}
							}
						});
				break;
			}
		}
		// 判断是否已经点赞
		BmobUser currentUser = BmobUser.getCurrentUser();
		if (currentUser == null) {
			holder.likeImage.setEnabled(true);
			holder.likeImage.setOnClickListener(this);
		} else if (strategy.getLikeUserIds()
				.contains(currentUser.getObjectId())) {
			holder.likeImage.setEnabled(false);
		} else {
			holder.likeImage.setEnabled(true);
			holder.likeImage.setOnClickListener(this);
		}
		// 注册事件
		holder.commentImage.setOnClickListener(this);
		holder.commentListView.setOnItemClickListener(this);
		// 用按钮的tag保存position，以便调用接口时使用
		holder.likeImage.setTag(position);
		holder.commentImage.setTag(position);
		holder.commentListView.setTag(position);
		// 判断是否用在我的攻略界面，是的话显示删除按钮
		if (isMyStrategy) {
			holder.deleteImage.setVisibility(View.VISIBLE);
			holder.deleteImage.setOnClickListener(this);
			holder.deleteImage.setTag(position);
		} else {
			holder.deleteImage.setVisibility(View.GONE);
		}
		return convertView;
	}

	public List<Strategy> getStrategies() {
		return strategies;
	}

	public void setStrategies(List<Strategy> strategies) {
		this.strategies = strategies;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.comment_img:
			buttonInChildClick.onCommentButtonClick(view);
			break;
		case R.id.like_img:
			buttonInChildClick.onLikeButtonClick(view);
			break;
		case R.id.delete_strategy:
			buttonInChildClick.onDeleteButtonClick(view);
			break;
		case R.id.img1:
			buttonInChildClick.onImageClick(view);
			break;
		case R.id.img2:
			buttonInChildClick.onImageClick(view);
			break;
		case R.id.img3:
			buttonInChildClick.onImageClick(view);
			break;
		}

	}

	public interface OnButtonInChildClick {
		public void onCommentButtonClick(View view);

		public void onLikeButtonClick(View view);

		public void onDeleteButtonClick(View view);

		public void onImageClick(View view);

	}

	public interface OnCommentInChildClick {
		public void onCommentClick(AdapterView<?> arg0, View view,
				int childPosition, int parentPosition);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int childPosition,
			long arg3) {
		View parentView = (View) view.getParent();
		int parentPosition = (Integer) parentView.getTag();
		commentInChildClick.onCommentClick(arg0, view, childPosition,
				parentPosition);
	}

	private static class ViewHolder {

		TextView textUserName, textTime, textCommentsCount, textLikeCount,
				textStrategyDetails;
		ImageView image1, image2, image3;
		CircleImageView head;
		LinearLayout imageContainer;
		ImageView likeImage;
		ImageView commentImage;
		ImageView deleteImage;
		CommentListView commentListView;
	}

	/**
	 * 图片加载第一次显示监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 是否第一次显�?
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					// Log.e("===onLoadingComplete====", " 图片淡入效果");
					FadeInBitmapDisplayer.animate(imageView, 500);

					displayedImages.add(imageUri);
				}
			}
		}
	}

	public List<ArrayList<String>> getUrlLists() {
		return urlLists;
	}

	public void setUrlLists(List<ArrayList<String>> urlLists) {
		this.urlLists = urlLists;
	}

}
