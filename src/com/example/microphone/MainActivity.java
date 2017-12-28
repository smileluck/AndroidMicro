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
	// ���岥��������MediaPlayer
    private AudioTrack myAudioTrack;
    // ����ϵͳ��Ƶ��
    private Visualizer mVisualizer; 
    // ����ϵͳ�ľ�����
    private Equalizer mEqualizer;
    // ����ϵͳ���ص���������
    private BassBoost mBass;
    // ����ϵͳ��Ԥ������������
    private PresetReverb mPresetReverb;
    private List<Short> reverbNames = new ArrayList<Short>();
    private List<String> reverbVals = new ArrayList<String>();
 
    private LinearLayout layout;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ�������� ,ж��setContentViewǰ��
		setContentView(R.layout.activity_main);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������   
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����ȫ��
		
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
		//	Toast.makeText(getApplicationContext(), "����������ý���豸��", Toast.LENGTH_LONG);
		//}
	}
	/**
	 * Ԥ����Ƶ��
	 */
	private void setupVisualizer(){
		 final MyVisualizerView mVisualizerView =
		            new MyVisualizerView(this);
		        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
		            ViewGroup.LayoutParams.FILL_PARENT,
		            (int) (80f * getResources().getDisplayMetrics().density)));
		        // ��MyVisualizerView�����ӵ�layout������
		        layout.addView(mVisualizerView);
		        // ��MediaPlayer��AudioSessionId����Visualizer
		        // �൱������Visualizer������ʾ��MediaPlayer����Ƶ����
		        mVisualizer = new Visualizer(myAudioTrack.getAudioSessionId());
		        //������Ҫת�����������ݳ��ȣ�רҵ��˵����ǲ������ò���ֵһ��Ϊ2��ָ��������64,128,256,512,1024��
		        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		        // ΪmVisualizer���ü�����
		        /*
		         * Visualizer.setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft
		         *  
		         *      listener������������������ڲ���ʵ�ָýӿڣ��ýӿ���Ҫʵ����������   
		                rate�� ��ʾ���������ڣ�������ò���һ�Σ���ϵǰ�ľ��Ǹ���ò���128������
		                iswave���ǲ����ź�
		                isfft����FFT�źţ���ʾ�ǻ�ȡ�����źŻ���Ƶ���ź�
		             
		         */
		        mVisualizer.setDataCaptureListener(
		            new Visualizer.OnDataCaptureListener()
		            {
		                //����ص�Ӧ�òɼ����ǿ��ٸ���Ҷ�任�йص�����
		                @Override
		                public void onFftDataCapture(Visualizer visualizer,
		                    byte[] fft, int samplingRate)
		                {
		                }
		                 //����ص�Ӧ�òɼ����ǲ�������
		                @Override
		                public void onWaveFormDataCapture(Visualizer visualizer,
		                    byte[] waveform, int samplingRate)
		                {
		                    // ��waveform�������ݸ���mVisualizerView���
		                    mVisualizerView.updateVisualizer(waveform);
		                }
		            }, Visualizer.getMaxCaptureRate() / 2, true, false);
		        mVisualizer.setEnabled(true);
	}

    /**
     * ��ʼ�����������
     */
    private void setupEqualizer()
    {
        // ��MediaPlayer��AudioSessionId����Equalizer
        // �൱������Equalizer������Ƹ�MediaPlayer
        mEqualizer = new Equalizer(0, myAudioTrack.getAudioSessionId());
        // ���þ������Ч��
        mEqualizer.setEnabled(true);
        TextView eqTitle = new TextView(this);
        eqTitle.setText("������:");
        layout.addView(eqTitle);
        // ��ȡ���������֧����Сֵ�����ֵ
        final short minEQLevel = mEqualizer.getBandLevelRange()[0];//��һ���±�Ϊ��͵��޶ȷ�Χ
        short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // �ڶ����±�Ϊ��ߵ��޶ȷ�Χ
        // ��ȡ���������֧�ֵ�����Ƶ��
        short brands = mEqualizer.getNumberOfBands();
        for (short i = 0; i < brands; i++)
        {
            TextView eqTextView = new TextView(this);
            // ����һ��TextView��������ʾƵ��
            eqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            // ���øþ����������Ƶ��
            eqTextView.setText((mEqualizer.getCenterFreq(i) / 1000)
                +  "Hz");
            layout.addView(eqTextView);
            // ����һ��ˮƽ���������LinearLayout
            LinearLayout tmpLayout = new LinearLayout(this);
            tmpLayout.setOrientation(LinearLayout.HORIZONTAL);
            // ������ʾ�����������Сֵ��TextView
            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            // ��ʾ�������������Сֵ
            minDbTextView.setText((minEQLevel / 100) +  "dB");
            // ������ʾ������������ֵ��TextView
            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            // ��ʾ��������������ֵ          
            maxDbTextView.setText((maxEQLevel / 100) +  "dB");
            LinearLayout.LayoutParams layoutParams = new
                LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            // ����SeekBar��Ϊ��������
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(i));
            final short brand = i;
            // ΪSeekBar���϶��¼������¼�������
            bar.setOnSeekBarChangeListener(new SeekBar
                .OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                    int progress, boolean fromUser)
                {
                    // ���ø�Ƶ�ʵľ���ֵ
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
            // ʹ��ˮƽ���������LinearLayout��ʢװ��3�����
            tmpLayout.addView(minDbTextView);
            tmpLayout.addView(bar);
            tmpLayout.addView(maxDbTextView);
            // ��ˮƽ���������LinearLayout��ӵ�myLayout������
            layout.addView(tmpLayout);
        }
    }
 
    /**
     * ��ʼ���ص���������
     */
    private void setupBassBoost()
    {
        // ��MediaPlayer��AudioSessionId����BassBoost
        // �൱������BassBoost������Ƹ�MediaPlayer
        mBass = new BassBoost(0, myAudioTrack.getAudioSessionId());
        // ���������ص���Ч��
        mBass.setEnabled(true);
        TextView bbTitle = new TextView(this);
        bbTitle.setText("�ص�����");
        layout.addView(bbTitle);
        // ʹ��SeekBar��Ϊ�ص����ĵ������� 
        SeekBar bar = new SeekBar(this);
        // �ص����ķ�ΧΪ0��1000
        bar.setMax(1000);
        bar.setProgress(0);
        // ΪSeekBar���϶��¼������¼�������
        bar.setOnSeekBarChangeListener(new SeekBar
            .OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar
                , int progress, boolean fromUser)
            {
                // �����ص�����ǿ��
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
     * ��ʼ��Ԥ������������
     */
    private void setupPresetReverb()
    {
        // ��MediaPlayer��AudioSessionId����PresetReverb
        // �൱������PresetReverb������Ƹ�MediaPlayer
        mPresetReverb = new PresetReverb(0,myAudioTrack.getAudioSessionId());
        // ��������Ԥ����������
        mPresetReverb.setEnabled(true);
        TextView prTitle = new TextView(this);
        prTitle.setText("����:");
        layout.addView(prTitle);
        // ��ȡϵͳ֧�ֵ�����Ԥ������
        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++)
        {
            reverbNames.add(i);
            reverbVals.add(mEqualizer.getPresetName(i));
        }
        // ʹ��Spinner��Ϊ����ѡ�񹤾�
        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(MainActivity.this,
            android.R.layout.simple_spinner_item, reverbVals));
        // ΪSpinner���б���ѡ���¼����ü�����
        sp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
                // �趨����
                mPresetReverb.setPreset(reverbNames.get(position));
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}});
        layout.addView(sp);
    }
 
    
	/**
     * ����Visualizer���������ݶ�̬���Ʋ���Ч�����ֱ�Ϊ��
     * ��״���Ρ���״���Ρ����߲���
     */
    private static class MyVisualizerView extends View
    {
        // bytes���鱣���˲��γ������ֵ
        private byte[] bytes;
        private float[] points;
        private Paint paint = new Paint();
        private Rect rect = new Rect();
        private byte type = 0;
        public MyVisualizerView(Context context)
        {
            super(context);
            bytes = null;
            // ���û��ʵ�����
            paint.setStrokeWidth(1f);
            paint.setAntiAlias(true);//�����
            paint.setColor(Color.YELLOW);//������ɫ
            paint.setStyle(Style.FILL);
        }
 
        public void updateVisualizer(byte[] ftt)
        {
            bytes = ftt;
            // ֪ͨ������ػ��Լ���
            invalidate();
        }
         
        @Override
        public boolean onTouchEvent(MotionEvent me)
        {
            // ���û����������ʱ���л���������
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
            // ���ư�ɫ����
            canvas.drawColor(Color.LTGRAY);          
            // ʹ��rect�����¼������Ŀ�Ⱥ͸߶�
            rect.set(0,0,getWidth(),getHeight());
            switch(type)
            {
                // -------���ƿ�״�Ĳ���ͼ-------
                case 0: 
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        float left = getWidth() * i / (bytes.length - 1);
                        // ���ݲ���ֵ����þ��εĸ߶�        
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                            * rect.height() / 128;
                        float right = left + 1;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------������״�Ĳ���ͼ��ÿ��18�����������һ�����Σ�-------
                case 1:
                    for (int i = 0; i < bytes.length - 1; i += 18)
                    {
                        float left = rect.width()*i/(bytes.length - 1);
                        // ���ݲ���ֵ����þ��εĸ߶�
                        float top = rect.height()-(byte)(bytes[i+1]+128)
                            * rect.height() / 128;
                        float right = left + 6;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------�������߲���ͼ-------
                case 2:
                    // ���point���黹δ��ʼ��
                    if (points == null || points.length < bytes.length * 4)
                    {
                        points = new float[bytes.length * 4];
                    }
                    for (int i = 0; i < bytes.length - 1; i++)
                    {
                        // �����i�����x����
                        points[i * 4] = rect.width()*i/(bytes.length - 1);
                        // ����bytes[i]��ֵ�����ε��ֵ�������i�����y����
                        points[i * 4 + 1] = (rect.height() / 2)
                            + ((byte) (bytes[i] + 128)) * 128
                            / (rect.height() / 2);
                        // �����i+1�����x����
                        points[i * 4 + 2] = rect.width() * (i + 1)
                            / (bytes.length - 1);
                        // ����bytes[i+1]��ֵ�����ε��ֵ�������i+1�����y����
                        points[i * 4 + 3] = (rect.height() / 2)
                            + ((byte) (bytes[i + 1] + 128)) * 128
                            / (rect.height() / 2);
                    }
                    // ���Ʋ�������
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
            // �ͷ����ж���
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
