package Audio;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.media.*;
import android.os.Environment;
import android.util.Log;

public class AudioRecording extends Thread{
	 	static final int frequency = 8000;  
	    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;  
	    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	    private int recBufSize = AudioRecord.getMinBufferSize(frequency,  
	            channelConfiguration, audioEncoding);  
	    private int plyBufSize = AudioTrack.getMinBufferSize(frequency,  
	            channelConfiguration, audioEncoding);  
	    private AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,  
	            channelConfiguration, audioEncoding, recBufSize);  
	  
	    private AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,  
	            channelConfiguration, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);  
	    
	    private boolean isRun = false;
	    
	    public void setisRun(boolean isRun){
	    	this.isRun = isRun;
	    }
	    
	    public void AudioRecording(){
	    	this.isRun = true;
	    }
    
	    public AudioTrack getAudioTrack(){
			return audioTrack;
	    }
	    
	    @Override
	    public void run(){
	    	// TODO Auto-generated method stub  
	    	    byte[] recBuf = new byte[recBufSize];  
	    	    audioRecord.startRecording();  
	    	    audioTrack.play();  
	    	    isRun = true;
	    	    while(isRun){  
	    	        int readLen = audioRecord.read(recBuf, 0, recBufSize); 
		    	    //byte[] tmpBuf  = new byte[recBufSize]; 
	    	       // System.arraycopy(recBuf, 0, tmpBuf, 0, readLen); 
	    	        calc1(recBuf,0,readLen);
	    	        audioTrack.write(recBuf, 0, readLen);  
	    	    }  
	    	    audioTrack.stop();  
	    	    audioRecord.stop();  
	    }
	    
	    public void pause(){
	    	isRun = false;
	    }
	    
	    public void start(){
	    	if(!isRun){
	    		super.start();
	    	}
	    }

	    void calc1(byte[] recBuf,int off,int len) {
	    	int i,j;
	    	for (i = 0; i < len; i++) {
	    		j = recBuf[i+off];
	    		recBuf[i+off] = (byte)(j>>2);
	    	}
	    }
	    
}
