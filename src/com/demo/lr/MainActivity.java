package com.demo.lr;

import java.util.ArrayList;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.demo.view.DynamicImage;
import com.demo.view.LotteryView;
import com.demo.view.RotateListener;
/**
 * 
 * <dl>
 * <dt>MainActivity.java</dt>
 * <dd>Description: 抽奖页面</dd>
 * </dl>
 * 
 * @author abner
 */
public class MainActivity  extends Activity implements RotateListener,OnClickListener {
	
	//文字提示
	private TextView title;
	//中奖显示
	private TextView info;
	//抽奖转盘
	private LotteryView lotteryView;
	//指针按钮
	private DynamicImage arrowBtn;
	
	private int[] itemColor;//选项颜色
	private String[] itemText;//选项文字
	public ArrayList<String> arrayList;
	private float surfacViewWidth = 0;
	private float surfacViewHeight = 0;
	
	private SoundPool soundPool = null;
	private int explosionId = 0;	//内存加载ID
	private int playSourceId = 0;	//播放ID

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lottery_rotary);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		initView();
	}
	
    protected void onResume() {
        super.onResume();
        if (soundPool == null) {
        	//指定声音池的最大音频流数目为10，声音品质为5
        	soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        	//载入音频流，返回在池中的id 
            explosionId = soundPool.load(this, R.raw.music, 1);
        }
    }

    protected void onPause() {
        super.onPause();
        if (soundPool != null) {
        	soundPool.stop(explosionId);
            soundPool.release();
            soundPool = null;
        }
        if(lotteryView != null){
        	lotteryView.rotateDisable();
        }
    }
	
	/**
	 * 
	* Description:初始化界面元素
	*
	 */
	public void initView(){
		initItem();
		title = (TextView)this.findViewById(R.id.title);
		info = (TextView)this.findViewById(R.id.info);
		arrowBtn = (DynamicImage) this.findViewById(R.id.arrowBtn);
		lotteryView = (LotteryView) this.findViewById(R.id.lotteryView);
		
		arrowBtn.setOnClickListener(this);
		lotteryView.initAll(itemColor, itemText);
		lotteryView.setRotateListener(this);
		lotteryView.start();

		surfacViewHeight = lotteryView.getHeight();
		surfacViewWidth = lotteryView.getWidth();
		
		Log.d("Log", "width = " + surfacViewWidth + ":height = "+ surfacViewHeight);
	}
	
	/**
	 * 
	* Description:初始化转盘的颜色，文字
	*
	 */
	public void initItem(){
		// 转盘选项的颜色
		itemColor = new int[] { 0xFFB0E0E6,// 粉蓝色　
				0xFF444444,// 深灰色
				0xFF008B8B,// 暗青色
				0xFFFFA500,// 橙色
				0xFF7FFF00,// 黄绿色
				0xFFF08080,// 亮珊瑚色
				0xFFB0C4DE,// 亮钢兰色
				0xFFFFFFFF // 白色
		};
		
		// 转盘选项的名称
		itemText = new String[] { "恭喜发财", "智能手机", "5元话费", "2元话费", "1元话费","恭喜发财" };
	}
	
	/**
	 * 
	* Description:转盘开始旋转
	*
	* @param sp
	* @param isRoating
	 */
	public void begin(float speed, int group, boolean isRoating) {
		lotteryView.setDirection(speed,group,isRoating);
		lotteryView.rotateEnable();
		/*播放音频，第二个参数为左声道音量;第三个参数为右声道音量;
		  第四个参数为优先级；第五个参数为循环次数，0不循环，-1循环;
		  第六个参数为速率，速率    最低0.5最高为2，1代表正常速度  */
		playSourceId = soundPool.play(explosionId,1, 1, 0, -1, 1);
	}

	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			info.setText( (CharSequence) msg.obj);
			if(!lotteryView.isRotateEnabled()){
				soundPool.stop(playSourceId);
				title.setText("恭喜您获得");
				arrowBtn.stopRotation();
			}
		};
	};

	@Override
	public void showEndRotate(String str) {
		Message msg = new Message();
		msg.obj = str;
		handler.sendMessage(msg);
	}

	@Override
	public void onClick(View v) {
		// 没有旋转状态
		if(!lotteryView.isRotateEnabled()){
			title.setText("抽奖按钮变红时按下更容易中奖哦");
			begin(Math.abs(50),8,false);
			arrowBtn.startRoation(new int[]{R.drawable.arrow_green,R.drawable.arrow_red}, 200);
		}
		//旋转状态
		else {
			//一直旋转状态
			if(!lotteryView.isRoating()){
				//设置中奖项(随机的话请注释)
				lotteryView.setAwards(0);
				//设置为缓慢停止
				lotteryView.setRoating(true);
				title.setText("");
			}
		}
	}
}
