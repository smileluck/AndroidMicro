package com.example.microphone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.jsandjava.R;
import com.example.jsandjava.R.layout;

import Audio.AudioRecording;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private Boolean clickBool = false; 
	private AudioRecording ar;
	// 定义播放声音的MediaPlayer
    private AudioTrack myAudioTrack;
    // 定义系统的频谱
    private Visualizer mVisualizer; 
    // 定义系统的均衡器
    private Equalizer mEqualizer;
    // 定义系统的重低音控制器
    private BassBoost mBass;
    // 定义系统的预设音场控制器
    private PresetReverb mPresetReverb;
    private List<Short> reverbNames = new ArrayList<Short>();
    private List<String> reverbVals = new ArrayList<String>();
 
    private LinearLayout layout;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏 ,卸载setContentView前面
		setContentView(R.layout.activity_main);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏   
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		
		/*if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
		
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		boolean isA2dp = am.isBluetoothA2dpOn();	
		ar= new AudioRecording();
		myAudioTrack =ar.getAudioTrack();
		layout = (LinearLayout)findViewById(R.id.Linear);
		
		setupVisualizer();
		setupEqualizer();
		setupBassBoost();
		setupPresetReverb();
		setupPlay();
		//if(isA2dp){
		
		//}else{
		//	Toast.makeText(getApplicationContext(), "请连接蓝牙媒体设备！", Toast.LENGTH_LONG);
		//}
	}
	/**
	 * 预设置频谱
	 */
	private void setupVisualizer(){
		 final MyVisualizerView mVisualizerView =
		            new MyVisualizerView(this);
		        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
		            ViewGroup.LayoutParams.FILL_PARENT,
		            (int) (80f * getResources().getDisplayMetrics().density)));
		        // 将MyVisualizerView组件添加到layout容器中
		        layout.addView(mVisualizerView);
		        // 以MediaPlayer的AudioSessionId创建Visualizer
		        // 相当于设置Visualizer负责显示该MediaPlayer的音频数据
		        mVisualizer = new Visualizer(myAudioTrack.getAudioSessionId());
		        //设置需要转换的音乐内容长度，专业的说这就是采样，该采样值一般为2的指数倍，如64,128,256,512,1024。
		        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		        // 为mVisualizer设置监听器
		        /*
		         * Visualizer.setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft
		         *  
		         *      listener，表监听函数，匿名内部类实现该接口，该接口需要实现两个函数   
		                rate， 表示采样的周期，即隔多久采样一次，联系前文就是隔多久采样128个数据
		                iswave，是波形信号
		                isfft，是FFT信号，表示是获取波形信号还是频域信号
		             
		         */
		        mVisualizer.setDataCaptureListener(
		            new Visualizer.OnDataCaptureListener()
		            {
		                //这个回调应该采集的是快速傅里叶变换有关的数据
		                @Override
		                public void onFftDataCapture(Visualizer visualizer,
		                    byte[] fft, int samplingRate)
		                {
		                }
		                 //这个回调应该采集的是波形数据
		                @Override
		                public void onWaveFormDataCapture(Visualizer visualizer,
		                    byte[] waveform, int samplingRate)
		                {
		                    // 用waveform波形数据更新mVisualizerView组件
		                    mVisualizerView.updateVisualizer(waveform);
		                }
		            }, Visualizer.getMaxCaptureRate() / 2, true, false);
		        mVisualizer.setEnabled(true);
	}

    /**
     * 初始化均衡控制器
     */
    private void setupEqualizer()
    {
        // 以MediaPlayer的AudioSessionId创建Equalizer
        // 相当于设置Equalizer负责控制该MediaPlayer
        mEqualizer = new Equalizer(0, myAudioTrack.getAudioSessionId());
        // 启用均衡控制效果
        mEqualizer.setEnabled(true);
        TextView eqTitle = new TextView(this);
        eqTitle.setText("均衡器:");
        layout.addView(eqTitle);
        // 获取均衡控制器支持最小值和最大值
        final short minEQLevel = mEqualizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
        short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
        // 获取均衡控制器支持的所有频率
        short brands = mEqualizer.getNumberOfBands();
        for (short i = 0; i < brands; i++)
        {
            TextView eqTextView = new TextView(this);
            // 创建一个TextView，用于显示频率
            eqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            // 设置该均衡控制器的频率
            eqTextView.setText((mEqualizer.getCenterFreq(i) / 1000)
                +  "Hz");
            layout.addView(eqTextView);
            // 创建一个水平排列组件的LinearLayout
            LinearLayout tmpLayout = new LinearLayout(this);
            tmpLayout.setOrientation(LinearLayout.HORIZONTAL);
            // 创建显示均衡控制器最小值的TextView
            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最小值
            minDbTextView.setText((minEQLevel / 100) +  "dB");
            // 创建显示均衡控制器最大值的TextView
            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最大值          
            maxDbTextView.setText((maxEQLevel / 100) +  "dB");
            LinearLayout.LayoutParams layoutParams = new
                LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            // 定义SeekBar做为调整工具
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(i));
            final short brand = i;
            // 为SeekBar的拖动事件设置事件监听器
            bar.setOnSeekBarChangeListener(new SeekBar
                .OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                    int progress, boolean fromUser)
                {
                    // 设置该频率的均衡值
                    mEqualizer.setBandLevel(brand,
                        (short) (progress + minEQLevel));
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });
            // 使用水平排列组件的LinearLayout“盛装”3个组件
            tmpLayout.addView(minDbTextView);
            tmpLayout.addView(bar);
            tmpLayout.addView(maxDbTextView);
            // 将水平排列组件的LinearLayout添加到myLayout容器中
            layout.addView(tmpLayout);
        }
    }
 
    /**
     * 初始化重低音控制器
     */
    private void setupBassBoost()
    {
        // 以MediaPlayer的AudioSessionId创建BassBoost
        // 相当于设置BassBoost负责控制该MediaPlayer
        mBass = new BassBoost(0, myAudioTrack.getAudioSessionId());
        // 设置启用重低音效果
        mBass.setEnabled(true);
        TextView bbTitle = new TextView(this);
        bbTitle.setText("重低音：");
        layout.addView(bbTitle);
        // 使用SeekBar做为重低音的调整工具 
        SeekBar bar = new SeekBar(this);
        // 重低音的范围为0～1000
        bar.setMax(1000);
        bar.setProgress(0);
        // 为SeekBar的拖动事件设置事件监听器
        bar.setOnSeekBarChangeListener(new SeekBar
            .OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar
                , int progress, boolean fromUser)
            {
                // 设置重低音的强度
                mBass.setStrength((short) progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });
        layout.addView(bar);
    }
    
    /**
     * 初始化预设音场控制器
     */
    private void setupPresetReverb()
    {
        // 以MediaPlayer的AudioSessionId创建PresetReverb
        // 相当于设置PresetReverb负责控制该MediaPlayer
        mPresetReverb = new PresetReverb(0,myAudioTrack.getAudioSessionId());
        // 设置启用预设音场控制
        mPresetReverb.setEnabled(true);
        TextView prTitle = new TextView(this);
        prTitle.setText("音场:");
        layout.addView(prTitle);
        // 获取系统支持的所有预设音场
        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++)
        {
            reverbNames.add(i);
            reverbVals.add(mEqualizer.getPresetName(i));
        }
        // 使用Spinner做为音场选择工具
        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(MainActivity.this,
            android.R.layout.simple_spinner_item, reverbVals));
        // 为Spinner的列表项选中事件设置监听器
        sp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
                // 设定音场
                mPresetReverb.setPreset(reverbNames.get(position));
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}});
        layout.addView(sp);
    }
 
    
	/**
     * 根据Visualizer传来的数据动态绘制波形效果，分别为：
     * 块状波形、柱状波形、曲线波形
     */
    private static class MyVisualizerView extends View
    {
        // bytes数组保存了波形抽样点的值
        private byte[] bytes;
        private float[] points;
        private Paint paint = new Paint();
        private Rect rect = new Rect();
        private byte type = 0;
        public MyVisualizerView(Context context)
        {
            super(context);
            bytes = null;
            // 设置画笔的属性
            paint.setStrokeWidth(1f);
            paint.setAntiAlias(true);//抗锯齿
            paint.setColor(Color.YELLOW);//画笔颜色
            paint.setStyle(Style.FILL);
        }
 
        public void updateVisualizer(byte[] ftt)
        {
            bytes = ftt;
            // 通知该组件重绘自己。
            invalidate();
        }
         
        @Override
        public boolean onTouchEvent(MotionEvent me)
        {
            // 当用户触碰该组件时，切换波形类型
            if(me.getAction() != MotionEvent.ACTION_DOWN)
            {
                return false;
            }
            type ++;
            if(type >= 3)
            {
                type = 0;
            }
            return true;
        }
 
        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            if (bytes == null)
            {
                return;
            }
            // 绘制白色背景
            canvas.drawColor(Color.LTGRAY);          
            // 使用rect对象记录该组件的宽度和高度
            rect.set(0,0,getWidth(),getHeight());
            switch(type)
            {
                // -------绘制块状的波形图-------
                case 0: 
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        float left = getWidth() * i / (bytes.length - 1);
                        // 根据波形值计算该矩形的高度        
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                            * rect.height() / 128;
                        float right = left + 1;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制柱状的波形图（每隔18个抽样点绘制一个矩形）-------
                case 1:
                    for (int i = 0; i < bytes.length - 1; i += 18)
                    {
                        float left = rect.width()*i/(bytes.length - 1);
                        // 根据波形值计算该矩形的高度
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                            * rect.height() / 128;
                        float right = left + 6;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制曲线波形图-------
                case 2:
                    // 如果point数组还未初始化
                    if (points == null || points.length < bytes.length * 4)
                    {
                        points = new float[bytes.length * 4];
                    }
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        // 计算第i个点的x坐标
                        points[i * 4] = rect.width()*i/(bytes.length - 1);
                        // 根据bytes[i]的值（波形点的值）计算第i个点的y坐标
                        points[i * 4 + 1] = (rect.height() / 2)
                            + ((byte) (bytes[i] + 128)) * 128
                            / (rect.height() / 2);
                        // 计算第i+1个点的x坐标
                        points[i * 4 + 2] = rect.width() * (i + 1)
                            / (bytes.length - 1);
                        // 根据bytes[i+1]的值（波形点的值）计算第i+1个点的y坐标
                        points[i * 4 + 3] = (rect.height() / 2)
                            + ((byte) (bytes[i + 1] + 128)) * 128
                            / (rect.height() / 2);
                    }
                    // 绘制波形曲线
                    canvas.drawLines(points, paint);
                    break;
            }
        }
    }   

    private void setupPlay(){
		ImageButton ib = new ImageButton(getApplicationContext());
		layout.addView(ib);
		//ib.setBackgroundResource(R.drawable.mc2);
		//ib.setBackgroundColor(Color.TRANSPARENT);
		ib.setImageResource(this.getResources().getIdentifier("mc2", "drawable", getPackageName()));
		ib.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(clickBool){
	                ((ImageButton)v).setImageDrawable(getResources().getDrawable(R.drawable.mc2));
	                clickBool = false;
					ar.interrupt(); 
					ar.pause(); 
				}else{
		            ((ImageButton)v).setImageDrawable(getResources().getDrawable(R.drawable.mc)); 
	                clickBool = true;  
					ar.start();
				}
			}});
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (isFinishing() && myAudioTrack != null)
        {
            // 释放所有对象
            mVisualizer.release();
            mEqualizer.release();
            mPresetReverb.release();
            mBass.release();
            myAudioTrack.release();
            myAudioTrack = null;
        }
    }
	
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
    }  
  
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
