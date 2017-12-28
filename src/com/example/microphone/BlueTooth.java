package com.example.microphone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.sax.StartElementListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BlueTooth{
	public BluetoothAdapter myBlueTooth;
	private Context context;
	private ArrayAdapter<String> mArrayAdapter;
	private MainActivity activity;
	
	
	public BlueTooth(MainActivity abActivity){
		myBlueTooth = BluetoothAdapter.getDefaultAdapter();
		if(myBlueTooth ==null){
			Toast.makeText(context, "设备不支持蓝牙", Toast.LENGTH_LONG);
			System.gc();
			System.exit(0);
		}
		context= abActivity.getApplicationContext();
		activity = abActivity;
		Toast.makeText(context, "支持蓝牙", Toast.LENGTH_LONG).show();
	}
	public void checkState(){
		if(!myBlueTooth.isEnabled()){
			Intent enabler=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivity(enabler);
		}
	}
	
	public ArrayAdapter<String> getmArrayAdapter(){
		return this.mArrayAdapter;
	}
	
	public void searchDevice(){
		BroadcastReceiver mReceiver =new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				  String action = intent.getAction();
			        //找到设备  
			        if (BluetoothDevice.ACTION_FOUND.equals(action)) {  
			            BluetoothDevice device = intent  
			                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
			  
			            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			                Log.i("find device:" + device.getName()  
			                        , device.getAddress());
			                //String[] strs = new String[]{device.getName()+"\n"+device.getAddress()};
			               // mArrayAdapter.add(device.getName()+"\n"+device.getAddress());
			            } 
			            else{
			            	Log.i("find over","find over");
			            	}
			        }  
			        //搜索完成  
			        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { 
			                Log.i("123", "find over"); 
			        }  

	                //activity.init(mArrayAdapter);
			}
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter); 
		myBlueTooth.startDiscovery();
	}
	
	@SuppressLint("NewApi") 
	public void listenProfile(){
		myBlueTooth.getProfileProxy(context, proxyListen, BluetoothProfile.A2DP);
	}
	
	@SuppressLint("NewApi") 
	private BluetoothProfile.ServiceListener proxyListen = new BluetoothProfile.ServiceListener() {
		
		
		@Override
		public void onServiceDisconnected(int profile) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			// TODO Auto-generated method stub
			
		}
	};
}
