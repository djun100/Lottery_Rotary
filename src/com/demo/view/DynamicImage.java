package com.demo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
/**
 * 
 * <dl>
 * <dt>DynamicImage.java</dt>
 * <dd>Description:实现类gif播放效果，可以伴以提示音 </dd>
 * </dl>
 * 
 * @author abner
 */
public class DynamicImage extends ImageView {
	private int index = 0;
	private MediaPlayer mediaPlayer = null;
	//继续播放标志
	private boolean continuePlay = false;
	private Handler handler;
	
	public DynamicImage(Context context) {
		super(context);
	}

	public DynamicImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 图片切换
	 * @param ms
	 */
	public void startRoation(final int resIds[], final long ms) {
		if (continuePlay) {
			//如果正在播放，不做操作
			return;
		}
		
		if (handler == null) {
			handler = new Handler();
		}
		
		//设置继续播放标志
		continuePlay = true;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (continuePlay) {
					if (index == resIds.length) {
						index = 0;
					}
					DynamicImage.this.setImageResource(resIds[index]);
					index++;
					handler.postDelayed(this, ms);
				}
			}
		};

		handler.post(runnable);
	}
	
	
	/**
	 * 图片切换并伴以声音提示
	 * @param ms
	 */
	public void startRoationWithTone(final int resIds[], final long ms) {
		//播放图片
		startRoation(resIds, ms);
		//播放声音提示
		playTone();
	}
	
	/**
	 * 停止切换
	 */
	public void stopRotation() {
		//设置图片切换停止标志
		continuePlay = false;
		
		//如果声音正在播放，停之
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.release();
		}
	}
	
	/**
     * 播放声音提示
     */
	private void playTone() {
		try {
			if (mediaPlayer == null) {
				mediaPlayer = new MediaPlayer();
			} else {
				mediaPlayer.reset();
			}
			Uri tone = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mediaPlayer.setDataSource(getContext(), tone);
			mediaPlayer.setLooping(false);
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			Log.e("DynamicImage", "playTone error:\r\n" + e.getMessage());
		}
	}
}
