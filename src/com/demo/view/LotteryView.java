package com.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.lr.R;

/**
 * 
 * <dl>
 * <dt>LotteryView.java</dt>
 * <dd>Description: 转盘view</dd>
 * </dl>
 * 
 * @author abner
 */
public class LotteryView extends SurfaceView implements SurfaceHolder.Callback {

	private float screenHight, screenWidth;// 屏幕的宽和高
	private float radius;// 绘制圆的半径
	private float circleRadius; // 半径
	private float startAngle = 0.0f;// 开始角度
	private float sweepAngle = 0.0f; // 扫过的角度
	private float speed; // 速度
	private float acceleration; // 加速度

	private int group; // 旋转圈数
	private int itemCount;// 选项个数

	private int[] itemColor;// 选项颜色
	private String[] itemText;// 选项文字

	private Paint mPaint;
	private Paint textPaint;
	private Canvas mCanvas;
	private Path path;
	private SurfaceViewThread myThread;
	private SurfaceHolder holder;

	private boolean done = false;
	private boolean surfaceExist = false;
	private boolean rotateEnabled = false;
	private boolean isRoating = false;

	private RotateListener listern;

	public LotteryView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public void initAll(int[] itemColor, String[] itemText) {
		// 创建一个新的SurfaceHolder， 并分配这个类作为它的回调(callback)
		holder = getHolder();
		holder.addCallback(this);

		this.itemColor = itemColor;
		this.itemText = itemText;
		this.itemCount = itemText.length;

		// 图像画笔
		mPaint = new Paint();
		// 文字画笔
		textPaint = new Paint();
		textPaint.setTextSize(22);
		textPaint.setColor(itemColor[itemColor.length - 1]);

		// 半径
		radius = 180;
		circleRadius = 30;
		startAngle = 0;
		// 加速度
		acceleration = 1;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (myThread != null) {
			myThread.start();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceExist = true;
		// 高度
		screenHight = getHeight();
		screenWidth = getWidth();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceExist = false;
		myThread = null;
		done = true;
	}

	class SurfaceViewThread extends Thread {

		public SurfaceViewThread() {
		}

		public void updateView() {

			SurfaceHolder surfaceHolder = holder;
			mCanvas = surfaceHolder.lockCanvas();

			float f1 = screenWidth / 2;
			float f2 = screenHight / 2;
			// 填充背景色
			mCanvas.drawColor(0xff639EC3);
			mCanvas.save();

			// *********************************确定参考区域*************************************
			float f3 = f1 - radius;// X轴 - 左
			float f4 = f2 - radius; // Y轴 - 上
			float f5 = f1 + radius; // X轴 - 右
			float f6 = f2 + radius; // Y轴 - 下
			RectF rectF = new RectF(f3, f4, f5, f6);

			// *********************************画每个区域的颜色块*********************************
			drawItem(rectF);
			// *********************************画边上渐变的圆环出来*******************************
			Paint localPaint = new Paint();
			// 设置取消锯齿效果
			localPaint.setAntiAlias(true);
			// 风格为圆环
			localPaint.setStyle(Paint.Style.STROKE);
			// 圆环宽度
			localPaint.setStrokeWidth(circleRadius);
			// 圆环背景
			Bitmap myBg = BitmapFactory.decodeResource(getResources(),
					R.drawable.bg);
			mCanvas.drawBitmap(myBg, f3 - 45, f4 - 45, localPaint);
			mCanvas.save();

			// *********************************使能转动****************************************
			if (rotateEnabled) {
				startAngle += speed;
				// Log.e("TAG", "速度 s："+speed);
				// Log.e("TAG", "角度 v："+startAngle);
				if (isRoating) {
					speed -= acceleration;
				}
				// Log.e("TAG", "加速度 s1："+acceleration);
				// Log.e("TAG", "~~~~~~~~~~~~~~~~~~~~~~");
				// 速度等于0则停下来
				if (speed <= 0) {
					rotateEnabled = false;
				}
				proRotateStop(startAngle);
			} else {
				// 避免进入了以后不点开始startAngel太大，其实没有什么关系
				startAngle %= 360;
				if (startAngle < 0) {
					startAngle += 360.0;
				}
			}
			// 解锁Canvas，并渲染当前图像
			surfaceHolder.unlockCanvasAndPost(mCanvas);
		}

		// *********************************画上各个Item的名称*********************************
		public void drawText(RectF localRectf, float localStartAngle,
				float localSweepAngle, String str) {
			// 旋转弧度
			float l = (float) ((-(localStartAngle + sweepAngle / 2) * Math.PI) / 180);

			// 中心点坐标
			float centerX = screenWidth / 2;
			float centerY = screenHight / 2;

			// 初始位置
			float pointX = screenWidth / 2 + radius;
			float pointY = screenHight / 2;
			float newX = (float) ((pointX - centerX) * Math.cos(l)
					+ (pointY - centerY) * Math.sin(l) + centerX);
			float newY = (float) (-(pointX - centerX) * Math.sin(l)
					+ (pointY - centerY) * Math.cos(l) + centerY);

			path = new Path();
			path.moveTo(screenWidth / 2, screenHight / 2);
			path.lineTo(newX, newY);

			float hOffset = 90; // 越大离圆心越远
			float vOffset = 10;
			mCanvas.drawTextOnPath(str, path, hOffset, vOffset, textPaint);
			mCanvas.save();
		}

		// *********************************画每个扇形*********************************
		public void drawItem(RectF localRectf) {
			float temp = startAngle;
			for (int i = 0; i < itemCount; i++) {
				mPaint.setColor(itemColor[i]);
				// startAngle为每次移动的角度大小
				sweepAngle = (float) (360 / itemCount);
				/*
				 * oval：圆弧所在的椭圆对象。 startAngle：圆弧的起始角度。sweepAngle：圆弧的角度。
				 * useCenter：是否显示半径连线，true表示显示圆弧与圆心的半径连线，false表示不显示。
				 * paint：绘制时所使用的画笔
				 */
				mCanvas.drawArc(localRectf, temp, sweepAngle, true, mPaint);
				mCanvas.save();
				drawText(localRectf, temp, sweepAngle, itemText[i]);
				temp += sweepAngle;
			}
		}

		@Override
		public void run() {
			super.run();
			// 公共在这里处理
			mPaint.setAntiAlias(true);
			updateView();
			while (!done) {
				if (rotateEnabled) {
					updateView();
				}
			}
		}
	}

	public void setDirection(float speed, int group, boolean isRoating) {
		this.isRoating = isRoating;
		this.group = group;
		this.speed = speed;
	}

	public void rotateEnable() {
		rotateEnabled = true;
	}

	public void rotateDisable() {
		rotateEnabled = false;
	}

	public void start() {
		if (myThread == null) {
			myThread = new SurfaceViewThread();
		}
		if (surfaceExist) {
			myThread.start();
		}
	}

	public void stopRotate() {
		// 杀死渲染线程
		if (myThread != null) {
			myThread = null;
			done = true;
		}
	}

	public void setRotateListener(RotateListener ln) {
		listern = ln;
	}

	public void proRotateStop(float startAngle) {
		float testfloat = startAngle + 90;
		testfloat %= 360.0;
		// Log.e("TAG", "startAngle2=" + startAngle + ",testfloat=" + testfloat);
		for (int i = 0; i < itemText.length; i++) {
			// 中奖角度范围
			float lotteryAngleFrom = 90 + 270 - (i + 1) * (360 / itemCount);
			float lotteryAngleTo = 90 + 270 - i * (360 / itemCount);

			if ((testfloat > lotteryAngleFrom) && (testfloat < lotteryAngleTo)) {
				listern.showEndRotate(itemText[i]);
				Log.d("Wheel", itemText[i]);
				return;
			}
		}
	}

	private void calcBeginSpeed(int lotteryIndex) {
		// 每项角度区域
		float eachAngle = (float) (360 / itemCount);
		// 中奖角度范围
		float lotteryAngleFrom = 360 + 270 - (lotteryIndex + 1) * eachAngle;
		float lotteryAngleTo = 360 + 270 - lotteryIndex * eachAngle;

		/**
		 * 根据等差数列求和公式S = n * (a1 + an) / 2 v是初始速度，a是加速度即等差数列的公差，数列的个数是：(v / a) +
		 * 1 所以s = (v/a + 1) * (0 + v) / 2，即得到一元二次方程： v * v + a * v - 2 * a * s
		 * = 0; 求解一元二次方程: 得v = (Math.sqrt(a * a + 8* a * s) - a) / 2
		 * 
		 */
		float sFrom = group * 360 + lotteryAngleFrom;
		float v1 = (float) (FloatMath.sqrt(acceleration * acceleration + 8
				* acceleration * sFrom) - acceleration) / 2;

		float sTo = group * 360 + lotteryAngleTo;
		float v2 = (float) (FloatMath.sqrt(acceleration * acceleration + 8
				* acceleration * sTo) - acceleration) / 2;

		speed = (float) (v1 + Math.random() * (v2 - v1));

	}

	/**
	 * 
	 * Description:设置奖项-转到哪一项停止
	 * 
	 * @param item
	 */
	public void setAwards(int item) {
		this.startAngle = 0;
		this.calcBeginSpeed(item);
	}

	public boolean isRoating() {
		return isRoating;
	}

	public void setRoating(boolean isRoating) {
		this.isRoating = isRoating;
	}

	public boolean isRotateEnabled() {
		return rotateEnabled;
	}

	public void setRotateEnabled(boolean rotateEnabled) {
		this.rotateEnabled = rotateEnabled;
	}

}