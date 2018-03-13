package com.jayrun.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jayrun.beans.Comment;
import com.jayrun.travelmate.R;

public class CommentListAdapter extends BaseAdapter {
	private List<Comment> comments = new ArrayList<Comment>();
	private Context context;
	private ViewHolder holder;
	private Comment comment;

	public CommentListAdapter(Context context) {
		super();
		this.context = context;
	}

	public CommentListAdapter(List<Comment> comments, Context context) {
		super();
		this.comments = comments;
		this.context = context;
	}

	@Override
	public int getCount() {
		return comments.size();
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
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_comment, null);
			holder.commentText = (TextView) convertView
					.findViewById(R.id.comment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		comment = comments.get(position);
		String friendlyComment = null;
		SpannableStringBuilder builder;
		ForegroundColorSpan blueSpan1 = new ForegroundColorSpan(Color.rgb(93,
				176, 216));
		ForegroundColorSpan blueSpan2 = new ForegroundColorSpan(Color.rgb(93,
				176, 216));
		if (comment.getUserTo() != null) {
			friendlyComment = comment.getUserFro().getNickName() + "»Ø¸´"
					+ comment.getUserTo().getNickName() + ":"
					+ comment.getComment();
			builder = new SpannableStringBuilder(friendlyComment);
			builder.setSpan(blueSpan1, 0, comment.getUserFro().getNickName()
					.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			builder.setSpan(blueSpan2, comment.getUserFro().getNickName()
					.length() + 2, comment.getUserFro().getNickName().length()
					+ 2 + comment.getUserTo().getNickName().length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.commentText.setText(builder);
		} else {
			friendlyComment = comment.getUserFro().getNickName() + ":"
					+ comment.getComment();
			builder = new SpannableStringBuilder(friendlyComment);
			builder.setSpan(blueSpan1, 0, comment.getUserFro().getNickName()
					.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.commentText.setText(builder);
		}
		return convertView;
	}

	public class ViewHolder {
		TextView commentText;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

}
