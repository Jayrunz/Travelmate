package com.jayrun.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.jayrun.adapters.StrategyListAdapter;
import com.jayrun.adapters.StrategyListAdapter.OnButtonInChildClick;
import com.jayrun.adapters.StrategyListAdapter.OnCommentInChildClick;
import com.jayrun.beans.Comment;
import com.jayrun.beans.ScenicInfo;
import com.jayrun.beans.Strategy;
import com.jayrun.beans.User;
import com.jayrun.photo.activity.AddStrategyActivity;
import com.jayrun.travelmate.ImagePagerActivity;
import com.jayrun.travelmate.R;
import com.jayrun.widgets.AutoListView;
import com.jayrun.widgets.AutoListView.OnLoadListener;
import com.jayrun.widgets.AutoListView.OnRefreshListener;

public class StrategyFragment extends Fragment implements OnRefreshListener,
		OnLoadListener, OnClickListener, OnButtonInChildClick,
		OnCommentInChildClick {
	public static final String UPDATE_STRATEGY = "com.example.travelmate.update";
	public static final int REFRESH = 0;
	public static final int LOADMORE = 1;
	public static final int FIRSTLOAD = 2;
	public static final int TYPE_COMMENT = 3;
	public static final int TYPE_REPLY = 4;

	private static int skip = 0;
	private static int pageSize = 10;

	private boolean isMyStrategy;
	private boolean isFirstCreate = true;

	private int parentPosition;
	private int childPosition;
	private String commentText;
	private ImageView addStrategy;
	private AutoListView strategyListView;
	public StrategyListAdapter strategyAdapter;
	private String scenicId;
	private String scenicName;
	private List<Strategy> strategies = new ArrayList<Strategy>();
	private View parentView;
	private PopupWindow editPop;
	private EditText commentEdit;
	private Button commentSend;
	private View popView;
	private int strategyCount;
	private int strategyCommentFlag = 0;
	private int strategyLikeFlag = 0;
	private Handler getStrategyHandler;
	private StrategyReceiver strategyReceiver;

	public StrategyFragment(boolean isMyStrategy) {
		super();
		this.isMyStrategy = isMyStrategy;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.e("====onCreate====", "����");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.e("====onDestroy====", "����");
	}

	@Override
	public void onPause() {
		super.onPause();
		// Log.e("====onPause====", "��ͣ");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		// Log.e("====onStart====", "��ʼ");
	}

	@Override
	public void onStop() {
		super.onStop();
		// Log.e("====onStop====", "ֹͣ");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		strategyReceiver = new StrategyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_STRATEGY);
		getActivity().registerReceiver(strategyReceiver, filter);
		if (isFirstCreate) {
		}
		skip = 0;
		pageSize = 10;

		if (getArguments().containsKey("scenicId")) {
			scenicId = getArguments().getString("scenicId");
		}
		if (getArguments().containsKey("scenicName")) {
			scenicName = getArguments().getString("scenicName");
		}
		parentView = inflater.inflate(R.layout.fragment_strategy, null);
		strategyListView = (AutoListView) parentView
				.findViewById(R.id.strategy_listview);
		strategyListView.setPageSize(pageSize);
		strategyListView.setOnLoadListener(this);
		strategyListView.setOnRefreshListener(this);

		strategyAdapter = new StrategyListAdapter(getActivity(), isMyStrategy,
				this, this);
		strategyListView.setAdapter(strategyAdapter);
		addStrategy = (ImageView) parentView.findViewById(R.id.add_strategy);
		if (isMyStrategy) {
			addStrategy.setVisibility(View.GONE);
		} else {
			addStrategy.setVisibility(View.VISIBLE);
			addStrategy.setOnClickListener(this);
		}

		loadData(REFRESH);

		return parentView;
	}

	private void loadData(final int TYPE) {
		if (TYPE == REFRESH) {
			skip = 0;
		}
		BmobQuery<Strategy> query = new BmobQuery<Strategy>();
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.setSkip(skip);
		query.setLimit(pageSize);
		query.order("-createdAt");
		// 我的攻略
		if (isMyStrategy) {
			BmobUser currentUser = BmobUser.getCurrentUser(User.class);
			query.addWhereEqualTo("user", new BmobPointer(currentUser));
		}

		else {
			ScenicInfo scenicInfo = new ScenicInfo();
			scenicInfo.setObjectId(scenicId);
			query.addWhereEqualTo("scenic", new BmobPointer(scenicInfo));
		}
		query.include("user");
		query.findObjects(new FindListener<Strategy>() {

			@Override
			public void done(List<Strategy> results, BmobException e) {
				if (e == null) {
					onSuccess(results);
				} else {
					onError(e.getErrorCode(), e.getMessage());
				}

			}

			public void onSuccess(final List<Strategy> results) {
				Log.e("==onSuccessresults===", results.size() + "");
				strategyCommentFlag = 0;
				strategyLikeFlag = 0;
				strategyCount = results.size();
				if (results.size() > 0) {
					loadComment(results.get(strategyCommentFlag));
					loadUserLikes(results.get(strategyLikeFlag));
				} else {
					switch (TYPE) {
					case REFRESH:
						strategyListView.onRefreshComplete();
						strategyListView.setResultSize(-1);
						strategyAdapter.getStrategies().clear();
						strategyAdapter.notifyDataSetChanged();
						break;
					case LOADMORE:
						strategyListView.onLoadComplete();
						strategyListView.setResultSize(0);
						break;
					}

				}
				getStrategyHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == 0x34) {
							loadComment(results.get(strategyCommentFlag));
						} else if (msg.what == 0x24) {
							loadUserLikes(results.get(strategyLikeFlag));
						} else if (msg.what == 0x35 && strategyLikeFlag == -1
								&& strategyCommentFlag == -1) {
							switch (TYPE) {
							case FIRSTLOAD:
								strategyAdapter.getStrategies().addAll(results);
								break;
							case REFRESH:
								strategyListView.onRefreshComplete();
								// Log.e("==clearǰ===", strategyAdapter
								// .getStrategies().size() + "");
								strategyAdapter.getStrategies().clear();
								// Log.e("==clear��===", strategyAdapter
								// .getStrategies().size() + "");
								strategyAdapter.getStrategies().addAll(results);
								// Log.e("==add��===", strategyAdapter
								// .getStrategies().size() + "");
								break;
							case LOADMORE:
								strategyListView.onLoadComplete();
								strategyAdapter.getStrategies().addAll(results);
								break;
							}
							strategyListView.setResultSize(results.size());
							strategyAdapter.notifyDataSetChanged();
							strategies = strategyAdapter.getStrategies();
						}
					}

				};
			}

			public void onError(int arg0, String arg1) {
				Log.e("onError", "skip" + skip + "pagesize" + pageSize);
				if (arg0 != 9015 && arg0 != 9009) {
					Toast.makeText(getActivity(), "加载失败，请稍后重试",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void loadComment(final Strategy result) {
		BmobQuery<Comment> query = new BmobQuery<Comment>();
		query.addWhereEqualTo("strategy", new BmobPointer(result));
		query.include("userFro,userTo");
		query.order("createdAt");
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.findObjects(new FindListener<Comment>() {

			@Override
			public void done(List<Comment> commentResult, BmobException e) {
				if (e == null) {
					onSuccess(commentResult);
				}
			}

			public void onSuccess(List<Comment> commentResult) {
				result.setCommentList(commentResult);
				strategyCommentFlag++;
				if (strategyCommentFlag < strategyCount) {
					getStrategyHandler.sendEmptyMessage(0x34);
				} else {
					strategyCommentFlag = -1;
					getStrategyHandler.sendEmptyMessage(0x35);
				}

			}

		});
	}

	private void loadUserLikes(final Strategy strategy) {

		BmobQuery<User> query = new BmobQuery<User>();
		query.setLimit(999);
		query.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1));
		query.addWhereRelatedTo("likes", new BmobPointer(strategy));
		query.findObjects(new FindListener<User>() {

			@Override
			public void done(List<User> likeUsers, BmobException e) {
				if (e == null) {
					List<String> ids = new ArrayList<String>();
					for (int i = 0; i < likeUsers.size(); i++) {
						ids.add(likeUsers.get(i).getObjectId());
					}
					strategy.setLikeUserIds(ids);
					strategyLikeFlag++;
					if (strategyLikeFlag < strategyCount) {
						getStrategyHandler.sendEmptyMessage(0x24);
					} else {
						strategyLikeFlag = -1;
						getStrategyHandler.sendEmptyMessage(0x35);
					}
				}
			}
			// @Override
			// public void onSuccess(List<User> likeUsers) {
			// List<String> ids = new ArrayList<String>();
			// for (int i = 0; i < likeUsers.size(); i++) {
			// ids.add(likeUsers.get(i).getObjectId());
			// }
			// strategy.setLikeUserIds(ids);
			// strategyLikeFlag++;
			// if (strategyLikeFlag < strategyCount) {
			// getStrategyHandler.sendEmptyMessage(0x24);
			// } else {
			// strategyLikeFlag = -1;
			// getStrategyHandler.sendEmptyMessage(0x35);
			// }
			// }
			//
			// @Override
			// public void onError(int arg0, String arg1) {
			// }
		});

	}

	@Override
	public void onLoad() {
		skip += pageSize;
		loadData(LOADMORE);
	}

	@Override
	public void onRefresh() {
		skip = 0;
		loadData(REFRESH);

	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.add_strategy:
			BmobUser user1 = BmobUser.getCurrentUser();
			if (user1 != null) {
				Intent intent = new Intent(getActivity(),
						AddStrategyActivity.class);
				intent.putExtra("scenicId", scenicId);
				intent.putExtra("scenicName", scenicName);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "登录后才能写攻略哦", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.pop_send_comment:
			commentText = commentEdit.getText().toString()
					.replaceFirst("\\s*", "");
			if (commentText.isEmpty()) {
				Toast.makeText(getActivity(), "评论不能为空", Toast.LENGTH_SHORT)
						.show();
				commentEdit.setText("");
			} else {
				commentSend.setEnabled(false);
				commentEdit.setText("");
				editPop.dismiss();
				String strategyId = strategies.get(parentPosition)
						.getObjectId();
				int type = (Integer) view.getTag();
				Comment comment = null;
				User user = BmobUser.getCurrentUser(User.class);
				switch (type) {
				case TYPE_COMMENT:
					comment = new Comment();
					comment.setComment(commentText);
					comment.setUserFro(user);
					Strategy strategy = new Strategy();
					strategy.setObjectId(strategyId);
					comment.setStrategy(strategy);
					break;

				case TYPE_REPLY:
					comment = new Comment();
					comment.setUserFro(user);
					comment.setUserTo(strategies.get(parentPosition)
							.getCommentList().get(childPosition).getUserFro());
					Strategy strategy1 = new Strategy();
					strategy1.setObjectId(strategyId);
					comment.setStrategy(strategy1);
					comment.setComment(commentText);
					break;
				}
				addComment(comment, parentPosition);
			}
		}

	}

	@Override
	public void onLikeButtonClick(View view) {
		final User currentUser = BmobUser.getCurrentUser(User.class);
		if (currentUser == null) {
			Toast.makeText(getActivity(), "登录后才能点赞哦", Toast.LENGTH_SHORT)
					.show();
		} else {
			final int position = (Integer) view.getTag();
			Strategy strategy = strategyAdapter.getStrategies().get(position);
			final BmobRelation relation = new BmobRelation();
			relation.add(currentUser);
			strategy.setLikes(relation);

			strategy.update(new UpdateListener() {
				@Override
				public void done(BmobException e) {
					if (e == null) {
						strategyAdapter.getStrategies().get(position)
								.getLikeUserIds()
								.add(currentUser.getObjectId());
						strategyAdapter.notifyDataSetChanged();
					} else {
						Toast.makeText(getActivity(), "点赞失败，请稍后重试",
								Toast.LENGTH_SHORT).show();
					}

				}

				// @Override
				// public void onSuccess() {
				// strategyAdapter.getStrategies().get(position)
				// .getLikeUserIds().add(currentUser.getObjectId());
				// strategyAdapter.notifyDataSetChanged();
				// // Toast.makeText(getActivity(), "���޳ɹ�",
				// // Toast.LENGTH_SHORT)
				// // .show();
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// Toast.makeText(getActivity(), "点赞失败，请稍后重试",
				// Toast.LENGTH_SHORT).show();
				// }
			});
		}
	}

	@Override
	public void onCommentButtonClick(View view) {
		User currentUser = BmobUser.getCurrentUser(User.class);
		if (currentUser == null) {
			Toast.makeText(getActivity(), "登录后才能评论哦", Toast.LENGTH_SHORT)
					.show();
		} else {
			parentPosition = (Integer) view.getTag();
			showPopwindow(TYPE_COMMENT);
		}

	}

	@Override
	public void onCommentClick(AdapterView<?> arg0, View view,
			int childPosition, int parentPosition) {
		User currentUser = BmobUser.getCurrentUser(User.class);
		if (currentUser == null) {
			Toast.makeText(getActivity(), "登录后才能评论哦", Toast.LENGTH_SHORT)
					.show();
		} else {
			this.parentPosition = parentPosition;
			this.childPosition = childPosition;
			showPopwindow(TYPE_REPLY);
		}
	}

	@Override
	public void onImageClick(View view) {
		int parentPos = (Integer) view.getTag(R.string.tag_parent_pos);
		int childPos = (Integer) view.getTag(R.string.tag_child_pos);
		Strategy strategy = strategies.get(parentPos);
		String[] urls = null;
		if (strategy.getImg3() != null) {
			urls = new String[] { strategy.getImg1().getFileUrl(),
					strategy.getImg2().getFileUrl(),
					strategy.getImg3().getFileUrl() };
		} else if (strategy.getImg2() != null) {
			urls = new String[] { strategy.getImg1().getFileUrl(),
					strategy.getImg2().getFileUrl() };
		} else if (strategy.getImg1() != null) {
			urls = new String[] { strategy.getImg1().getFileUrl() };
		}
		Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
		intent.putExtra("childPos", childPos);
		intent.putExtra("urls", urls);
		startActivity(intent);
	}

	@Override
	public void onDeleteButtonClick(final View view) {

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.dialog_reminder, null);
		TextView textView = (TextView) layout.findViewById(R.id.remind);
		textView.setText("确定删除此攻略吗");
		Button dialogOK = (Button) layout.findViewById(R.id.dia_ok);
		Button dialogCancle = (Button) layout.findViewById(R.id.dia_cancle);
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(
				R.drawable.dialog_bg_alpha0);
		dialog.show();
		dialog.getWindow().setContentView(layout);
		dialogOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final int delPosition = (Integer) view.getTag();
				final Strategy strategy = strategyAdapter.getStrategies().get(
						delPosition);
				strategy.delete(new UpdateListener() {

					@Override
					public void done(BmobException e) {
						if (e == null) {
							Intent intent = new Intent(UPDATE_STRATEGY);
							intent.putExtra("RefreshAfterDelete", true);
							getActivity().sendBroadcast(intent);
							List<BmobObject> deletedComments = new ArrayList<BmobObject>();
							for (int i = 0; i < strategy.getCommentList()
									.size(); i++) {
								deletedComments.add(strategy.getCommentList()
										.get(i));
							}
							new BmobBatch()
									.deleteBatch(deletedComments)
									.doBatch(
											new QueryListListener<BatchResult>() {

												@Override
												public void done(
														List<BatchResult> arg0,
														BmobException arg1) {
													// TODO Auto-generated
													// method stub

												}
											});
							if (strategy.getImg1() != null) {
								strategy.getImg1().delete();
							}
							if (strategy.getImg2() != null) {
								strategy.getImg2().delete();
							}
							if (strategy.getImg3() != null) {
								strategy.getImg3().delete();
							}
							strategyAdapter.getStrategies().remove(delPosition);
							strategyAdapter.notifyDataSetChanged();
							dialog.dismiss();

						}

					}
				}
				// new DeleteListener() {
				//
				// @Override
				// public void onSuccess() {
				// Intent intent = new Intent(UPDATE_STRATEGY);
				// intent.putExtra("RefreshAfterDelete", true);
				// getActivity().sendBroadcast(intent);
				// List<BmobObject> deletedComments = new
				// ArrayList<BmobObject>();
				// for (int i = 0; i < strategy.getCommentList().size(); i++) {
				// deletedComments.add(strategy.getCommentList()
				// .get(i));
				// }
				// new BmobObject().deleteBatch(getActivity(),
				// deletedComments, new DeleteListener() {
				//
				// @Override
				// public void onSuccess() {
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// }
				// });
				// if (strategy.getImg1() != null) {
				// strategy.getImg1().delete(getActivity());
				// }
				// if (strategy.getImg2() != null) {
				// strategy.getImg2().delete(getActivity());
				// }
				// if (strategy.getImg3() != null) {
				// strategy.getImg3().delete(getActivity());
				// }
				// strategyAdapter.getStrategies().remove(delPosition);
				// strategyAdapter.notifyDataSetChanged();
				// dialog.dismiss();
				// }
				//
				// @Override
				// public void onFailure(int arg0, String arg1) {
				// Toast.makeText(getActivity(), "删除失败，请稍后重试" + arg1,
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

	private void addComment(final Comment comment, final int position) {
		comment.save(new SaveListener<String>() {

			@Override
			public void done(String arg0, BmobException e) {
				if (e == null) {
					strategyAdapter.getStrategies().get(position)
							.getCommentList().add(comment);
					strategyAdapter.notifyDataSetChanged();
					commentSend.setEnabled(true);
				} else {
					Toast.makeText(getActivity(), "评论失败，请稍后重试",
							Toast.LENGTH_SHORT).show();
					commentSend.setEnabled(true);
				}

			}
		}
		// new SaveListener() {
		// @Override
		// public void onSuccess() {
		// strategyAdapter.getStrategies().get(position).getCommentList()
		// .add(comment);
		// strategyAdapter.notifyDataSetChanged();
		// commentSend.setEnabled(true);
		// }
		//
		// @Override
		// public void onFailure(int arg0, String arg1) {
		// Toast.makeText(getActivity(), "评论失败，请稍后重试", Toast.LENGTH_SHORT)
		// .show();
		// commentSend.setEnabled(true);
		// }
		// }
		);

	}

	public void showPopwindow(final int TYPE) {
		popView = getActivity().getLayoutInflater().inflate(
				R.layout.popwindow_edit_comment, null);
		commentEdit = (EditText) popView.findViewById(R.id.pop_edit_comment);
		commentSend = (Button) popView.findViewById(R.id.pop_send_comment);
		// �����Ͱ�ť�������
		commentSend.setTag(TYPE);
		commentSend.setOnClickListener(this);
		commentEdit.setFocusable(true);
		commentEdit.setFocusableInTouchMode(true);
		commentEdit.requestFocus();
		editPop = new PopupWindow(getActivity());
		editPop.setHeight(LayoutParams.WRAP_CONTENT);
		editPop.setWidth(LayoutParams.MATCH_PARENT);
		editPop.setFocusable(true);
		editPop.setOutsideTouchable(true);
		editPop.setContentView(popView);
		editPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		editPop.setInputMethodMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
		editPop.setBackgroundDrawable(new BitmapDrawable());
		editPop.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputMethodManager = (InputMethodManager) commentEdit
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(commentEdit, 0);
			}

		}, 100);

	}

	class StrategyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.hasExtra("uploadState")) {
				boolean isSuccess = intent
						.getBooleanExtra("uploadState", false);
				if (isSuccess) {
					loadData(REFRESH);
				}
			}
			if (intent.hasExtra("RefreshAfterDelete") && !isMyStrategy) {
				loadData(REFRESH);
			}
		}

	}

}
