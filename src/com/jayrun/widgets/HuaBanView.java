package com.jayrun.widgets;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/** 实现画板功能的View */
public class HuaBanView extends View {

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	private PaintPath pp;

	/** 缓冲位图 */
	private Bitmap cacheBitmap;
	/** 缓冲位图的画板 */
	private Canvas cacheCanvas;
	/** 缓冲画笔 */
	private Paint paint;
	/** 实际画笔 */
	private Paint BitmapPaint;
	/** 保存绘制曲线路径 */
	private Path path;
	/** 画布高 */
	private int height;
	/** 画布宽 */
	private int width;

	/** 保存上一次绘制的终点横坐标 */
	private float pX;
	/** 保存上一次绘制的终点纵坐标 */
	private float pY;

	/** 画笔初始颜色 */
	public static int paintColor = Color.RED;
	/** 线状状态 */
	public static Paint.Style paintStyle = Paint.Style.STROKE;
	/** 画笔粗细 */
	private static int paintWidth = 10;
	/** 画笔透明度 */
	private static int paintAlpha = 255;

	private Canvas canvas;

	// 保存第一次触点的横坐标
	private float fX;
	// 保存第一次触点的纵坐标
	private float fY;
	private ArrayList<PaintPath> deletedPaths;
	private ArrayList<PaintPath> drewPaths;

	private boolean isEraser = false;

	private class PaintPath {
		Paint paint;
		Path path;
	}

	public ArrayList<PaintPath> getDeletedPaths() {
		return deletedPaths;
	}

	public void setDeletedPaths(ArrayList<PaintPath> deletedPaths) {
		this.deletedPaths = deletedPaths;
	}

	public ArrayList<PaintPath> getDrewPaths() {
		return drewPaths;
	}

	public void setDrewPaths(ArrayList<PaintPath> drewPaths) {
		this.drewPaths = drewPaths;
	}

	private void init() {
		cacheBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		cacheCanvas = new Canvas(cacheBitmap);
		cacheCanvas.drawColor(Color.argb(0, 255, 255, 255));
		paint = new Paint();
		path = new Path();
		BitmapPaint = new Paint();
		updatePaint();
	}

	private void updatePaint() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(paintWidth);
		if (isEraser) {
			paint.setAlpha(0);
			// 设置两图相交时的模式
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			paint.setAntiAlias(true);
			paint.setDither(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);// 平滑
			paint.setStrokeCap(Paint.Cap.ROUND);// 圆头
		} else {
			paint.setStyle(paintStyle);
			paint.setColor(paintColor);
			paint.setAlpha(paintAlpha);
		}

	}

	public HuaBanView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 得到屏幕的分辨率
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);

		width = dm.widthPixels;
		height = dm.heightPixels - 2 * 45;
		init();
		// undo();
		deletedPaths = new ArrayList<PaintPath>();
		drewPaths = new ArrayList<PaintPath>();
	}

	public HuaBanView(Context context) {
		super(context);
		// 得到屏幕的分辨率
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);

		width = dm.widthPixels;
		height = dm.heightPixels - 2 * 45;
		init();
		// undo();
		deletedPaths = new ArrayList<PaintPath>();
		drewPaths = new ArrayList<PaintPath>();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.canvas = canvas;
		BitmapPaint = new Paint();
		canvas.drawBitmap(cacheBitmap, 0, 0, BitmapPaint);
		if (null != path) {
			// 实时的显示
			canvas.drawPath(path, paint);
		}
	}

	/** 更新画笔颜色 */
	public void setPaintColor(int color) {
		paintColor = color;
		updatePaint();
	}

	/** 更新画笔透明度 */
	public void setPaintAlpha(int alpha) {
		paintAlpha = alpha;
		updatePaint();
	}

	/** 设置画笔粗细 */
	public void setPaintWidth(int width) {
		paintWidth = width;
		updatePaint();
	}

	public static final int PEN = 1;
	public static final int PAIL = 2;
	public static final int ERASER = 3;

	/** 设置画笔样式 */
	public void setStyle(int style) {
		switch (style) {
		case PEN:
			paintStyle = Paint.Style.STROKE;
			isEraser = false;
			updatePaint();
			break;
		case PAIL:
			paintStyle = Paint.Style.FILL;
			isEraser = false;
			updatePaint();
			break;
		case ERASER:
			// paint.setColor(eraserColor);
			isEraser = true;
			paint.setStyle(Paint.Style.STROKE);
			updatePaint();
			break;
		}

	}

	/** 清空画布 */
	public void clearScreen() {
		init();
		deletedPaths.clear();
		drewPaths.clear();
		invalidate();
	}

	/**
	 * 撤销的核心思想就是将画布清空， 将保存下来的Path路径最后一个移除掉， 重新将路径画在画布上面。
	 */
	public void undo() {
		if (drewPaths != null && drewPaths.size() > 0) {
			// 调用初始化画布函数以清空画布
			init();
			// 将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
			PaintPath paintPath = drewPaths.get(drewPaths.size() - 1);
			deletedPaths.add(paintPath);
			drewPaths.remove(drewPaths.size() - 1);

			// 将路径保存列表中的路径重绘在画布上
			for (int i = 0; i < drewPaths.size(); i++) {
				PaintPath pp = drewPaths.get(i);
				cacheCanvas.drawPath(pp.path, pp.paint);
			}
			invalidate();// 刷新
		}
	}

	/**
	 * 恢复的核心思想就是将撤销的路径保存到另外一个列表里面(栈)， 然后从redo的列表里面取出最顶端对象， 画在画布上面即可
	 */
	public void redo() {
		if (deletedPaths.size() > 0) {
			// 将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
			PaintPath pp = deletedPaths.get(deletedPaths.size() - 1);
			drewPaths.add(pp);
			// 将取出的路径重绘在画布上
			cacheCanvas.drawPath(pp.path, pp.paint);
			// 将该路径从删除的路径列表中去除
			deletedPaths.remove(deletedPaths.size() - 1);
			invalidate();
		}
	}

	private void touch_start(float x, float y) {
		path.reset();// 清空path
		path.moveTo(x, y);
		mX = x;
		mY = y;
		fX = x;
		fY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);// 源代码是这样写的，可是我没有弄明白，为什么要这样？
			mX = x;
			mY = y;
		}
	}

	private void touch_up(float x, float y) {
		if (fX != x && fY != y) {
			path.lineTo(mX, mY);
			cacheCanvas.drawPath(path, paint);
			drewPaths.add(pp);
			deletedPaths.clear();
			path = null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			path = new Path();
			pp = new PaintPath();
			pp.path = path;
			pp.paint = paint;

			touch_start(x, y);
			invalidate(); // 清屏
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up(x, y);
			invalidate();
			break;
		}
		return true;
	}
}
