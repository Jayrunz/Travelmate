package com.jayrun.travelmate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.adapters.MyWordsDetailsListAdapter;
import com.jayrun.adapters.MyWordsDetailsListAdapter.DeleteButtonCallBack;
import com.jayrun.beans.User;
import com.jayrun.beans.Words;
import com.jayrun.fragments.LeaveWordsFragment;
import com.jayrun.widgets.AutoListView;
import com.jayrun.widgets.AutoListView.OnLoadListener;
import com.jayrun.widgets.AutoListView.OnRefreshListener;

public class MyWordsActivity extends Activity implements DeleteButtonCallBack,
		OnClickListener, OnRefreshListener, OnLoadListener {
	private static final int REFRESH = 0;
	private static final int LOADMORE = 1;
	private static int skip = 0;
	private static int pageSize = 10;
	private TextView back;
	private AutoListView myWordsListView;
	private MyWordsDetailsListAdapter wordsDetailsListAdapter;
	private int wordsCount;
	private int wordsFlag = 0;
	private Handler getWordsHandler;
	private int wordsType = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_words);
		init();
	}

	private void init() {
		wordsType = getIntent().getIntExtra("wordsType", 0);
		back = (TextView) findViewById(R.id.words_back);
		back.setOnClickListener(this);
		myWordsListView = (AutoListView) findViewById(R.id.my_words_details_list);
		myWordsListView.setOnRefreshListener(this);
		myWordsListView.setOnLoadListener(this);
		wordsDetailsListAdapter = new MyWordsDetailsListAdapter(
				MyWordsActivity.this, this);
		myWordsListView.setAdapter(wordsDetailsListAdapter);
		User user = BmobUser.getCurrentUser(User.class);
		if (user != null) {
			loadwords(REFRESH);
		} else {
			Toast.makeText(MyWordsActivity.this, "Œ¥ªÒ»°µΩ”√ªß–≈œ¢", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void loadwords(final int TYPE) {
		BmobQuery<Words> query = new BmobQuery<Words>();
		query.setLimit(pageSize);
		query.setSkip(skip);
		query.order("createdAt");
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		User currentUser = BmobUser.getCurrentUser(User.class);
		switch (wordsType) {
		case 1:
			query.addWhereEqualTo("isText", true);
			break;
		case 2:
			query.addWhereEqualTo("isText", false);
			break;
		}
		query.addWhereEqualTo("user", new BmobPointer(currentUser));
		query.include("user,scenic");
		query.findObjects(new FindListener<Words>() {

			@Override
			public void done(List<Words> results, BmobException e) {
				if (e == null) {
					onSuccess(results);
				} else {
					onError(e.getErrorCode(), e.getMessage());
				}

			}

			public void onSuccess(final List<Words> results) {

				wordsFlag = 0;
				wordsCount = results.size();
				if (results.size() > 0) {
					loadUserLikes(results.get(wordsFlag));
				} else {
					switch (TYPE) {
					case REFRESH:
						myWordsListView.onRefreshComplete();
						myWordsListView.setResultSize(-1);
						wordsDetailsListAdapter.getMyWordsList().clear();
						wordsDetailsListAdapter.notifyDataSetChanged();
						break;
					case LOADMORE:
						myWordsListView.onLoadComplete();
						myWordsListView.setResultSize(0);
						break;
					}
				}
				getWordsHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == 0x34) {
							loadUserLikes(results.get(wordsFlag));
						} else if (msg.what == 0x35) {
							switch (TYPE) {
							case REFRESH:
								myWordsListView.onRefreshComplete();
								wordsDetailsListAdapter.getMyWordsList()
										.clear();
								wordsDetailsListAdapter.getMyWordsList()
										.addAll(results);
								break;
							case LOADMORE:
								myWordsListView.onLoadComplete();
								wordsDetailsListAdapter.getMyWordsList()
										.addAll(results);
								break;
							}
							myWordsListView.setResultSize(results.size());
							wordsDetailsListAdapter.notifyDataSetChanged();
						}
					}
				};
			}

			public void onError(int arg0, String arg1) {
				if (arg0 != 9015 && arg0 != 9009) {
					Toast.makeText(MyWordsActivity.this, "ªÒ»°¡Ù—‘ ß∞‹,«Î…‘∫Û÷ÿ ‘",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	public void loadUserLikes(final Words words) {
		BmobQuery<User> query = new BmobQuery<User>();
		query.setLimit(999);
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereRelatedTo("likes", new BmobPointer(words));
		query.findObjects(new FindListener<User>() {

			@Override
			public void done(List<User> likeUsers, BmobException e) {

				if (e == null) {
					List<String> ids = new ArrayList<String>();
					for (int i = 0; i < likeUsers.size(); i++) {
						ids.add(likeUsers.get(i).getObjectId());
					}
					words.setLikeUsersId(ids);
					wordsFlag++;
					if (wordsFlag < wordsCount) {
						getWordsHandler.sendEmptyMessage(0x34);
					} else {
						getWordsHandler.sendEmptyMessage(0x35);
					}
				}
			}
			// @Override
			// public void onSuccess(List<User> likeUsers) {
			// List<String> ids = new ArrayList<String>();
			// for (int i = 0; i < likeUsers.size(); i++) {
			// ids.add(likeUsers.get(i).getObjectId());
			// }
			// words.setLikeUsersId(ids);
			// wordsFlag++;
			// if (wordsFlag < wordsCount) {
			// getWordsHandler.sendEmptyMessage(0x34);
			// } else {
			// getWordsHandler.sendEmptyMessage(0x35);
			// }
			// }
			//
			// @Override
			// public void onError(int arg0, String arg1) {
			// }
		});
	}

	@Override
	public void onDeleteClick(final View v) {
		LayoutInflater inflater = LayoutInflater.from(MyWordsActivity.this);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.dialog_reminder, null);
		TextView textView = (TextView) layout.findViewById(R.id.remind);
		textView.setText("ƒ˙»∑∂®…æ≥˝¥À¡Ù—‘¬£ø");
		Button dialogOK = (Button) layout.findViewById(R.id.dia_ok);
		Button dialogCancle = (Button) layout.findViewById(R.id.dia_cancle);
		final Dialog dialog = new Dialog(MyWordsActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(
				R.drawable.dialog_bg_alpha0);
		dialog.show();
		dialog.getWindow().setContentView(layout);
		dialogOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final int delPosition = (Integer) v.getTag();
				final Words words = wordsDetailsListAdapter.getMyWordsList()
						.get(delPosition);
				words.delete(new UpdateListener() {

					@Override
					public void done(BmobException e) {

						if (e == null) {
							wordsDetailsListAdapter.getMyWordsList().remove(
									delPosition);
							wordsDetailsListAdapter.notifyDataSetChanged();
							dialog.dismiss();
							sendBroadcast(new Intent(
									LeaveWordsFragment.DELETE_ACTION));
							if (null != words.getGraffiti()) {
								// …æ≥˝∂‘”¶Õø—ªÕº∆¨
								words.getGraffiti().delete();
							}
						} else {
							Toast.makeText(MyWordsActivity.this,
									"¡Ù—‘…æ≥˝ ß∞‹£¨«ÎºÏ≤ÈÕ¯¬Á∫Û÷ÿ ‘", Toast.LENGTH_SHORT)
									.show();
							dialog.dismiss();
						}
					}
				}
				// new DeleteListener() {
				//
				// @Override
				// public void onSuccess() {
				// wordsDetailsListAdapter.getMyWordsList().remove(
				// delPosition);
				// wordsDetailsListAdapter.notifyDataSetChanged();
				// dialog.dismiss();
				// sendBroadcast(new Intent(
				// LeaveWordsFragment.DELETE_ACTION));
				// if (null != words.getGraffiti()) {
				// // …æ≥˝∂‘”¶Õø—ªÕº∆¨
				// words.getGraffiti().delete(MyWordsActivity.this);
				// }
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// Toast.makeText(MyWordsActivity.this, "¡Ù—‘…æ≥˝ ß∞‹£¨«ÎºÏ≤ÈÕ¯¬Á∫Û÷ÿ ‘",
				// Toast.LENGTH_SHORT).show();
				// dialog.dismiss();
				// }
				// }
				);
			}
		});
		dialogCancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.words_back:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onLoad() {
		skip += pageSize;
		loadwords(LOADMORE);
	}

	@Override
	public void onRefresh() {
		skip = 0;
		loadwords(REFRESH);
	}

}
