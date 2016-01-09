/*
 *  Copyright (C) 2009 Levente Bagi, Enhanced by Won Joon (Eric) Sohn, 2015. 
 *
 *  Sensor + Pedometer - Android App
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package name.bagi.levente.pedometer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.math.round;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

//import name.bagi.levente.pedometer.AdvancedMultipleSeriesGraph;
//import com.jjoe64.graphviewdemos.MainActivity;
import name.bagi.levente.pedometer.R;
//import name.bagi.levente.pedometer.RealtimeGraph;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import android.hardware.SensorManager;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class SensorPedometer extends Activity implements SensorEventListener  {
//public class SensorPedometer extends Activity {
	private static final String TAG = "Pedometer";
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private Utils mUtils;
    
    private TextView mStepValueView;
    private TextView mPaceValueView;
    private TextView mDistanceValueView;
//    private TextView mSpeedValueView;
//    private TextView mCaloriesValueView;
    
    private float x=0,y=0,z=0;  // acc
    public int PORT = 15000; 
    //private Button connectPhones;
    private String serverIpAddress = "192.168.0.104";
    private boolean connected = false;   // wifi connectivity
    private boolean writing = false;   // writing to file
    boolean acc_disp = false;
    EditText port;
    EditText ipAdr;
    TextView mStatus;
    ToggleButton mlogWifiToggleButton1;
    ToggleButton mDatalog;
    PrintWriter out;
   
    // acceleration and step detection related. 
    //private StepDetector mStepDector;
    public  double acc_net = 0;
    private int step=0;
    
    TextView mDesiredPaceView;
    private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
//    private float mSpeedValue;
//    private int mCaloriesValue;
    private float mDesiredPaceOrSpeed;
    private int mMaintain;
    private boolean mIsMetric;
    private float mMaintainInc;
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    private float   mLastValues[] = new float[3*2]; 
    private float   mLastValues_hr[] = new float[3*2]; 
    private float   mLastValues_fwa[] = new float[3*2]; 
    
//    /*write to file*/
    private String acc;
    private String read_str = "";
   // String fileName = "acc.txt"; //new SimpleDateFormat("yyyyMMddhhmm.txt'").format(new Date()); // time stamp in the file name
    String fileName = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    private final String filepath = "/mnt/sdcard/" + fileName + "_R.txt"; //TODO: add time tag to file name
    private BufferedWriter mBufferedWriter;
    private BufferedReader mBufferedReader;
 
 
    
    // for real time graph
	private final Handler mHandler2 = new Handler();  // check if commenting out this produce trouble
	private Runnable mTimer00;
	private Runnable mTimer0;
	private Runnable mTimer1;
	private Runnable mTimer2;
	private Runnable mTimer3;	
	private Runnable r;
	private GraphView graphView00;
	private GraphView graphView0;
	private GraphView graphView1;
	private GraphView graphView2;
	private GraphView graphView3;
	private GraphViewSeries exampleStepSeries0;
	private GraphViewSeries exampleSeries0;
	private GraphViewSeries exampleSeries1;
	private GraphViewSeries exampleSeries2;
	private GraphViewSeries exampleSeries3;
	private double sensorStep = 235;  // mOffset  - 5
	private double sensorXYZ = 0;  // calculated weight xyz acc mean 
	private double sensorXYZ_HR = 0;  // Hanning recursive filter 
	private double sensorXYZ_fwa = 0;  //five point weighted average filter 
	private double sensorX = 0;
	private double sensorY = 0;
	private double sensorZ = 0;
	private List<GraphViewData> seriesStep;
	private List<GraphViewData> seriesXYZ;
	private List<GraphViewData> seriesXYZ_HR;  //Hanning recursive filter
	private List<GraphViewData> seriesX;
	private List<GraphViewData> seriesY;
	private List<GraphViewData> seriesZ;
	int dataCount = 1;

	//the Sensor Manager
	private SensorManager sManager;
	private Sensor sensor;
	
	private int h;
	private float   mScale[] = new float[2];
    private float   mYOffset;
    
    DecimalFormat df = new DecimalFormat("##.####");
    
    // time tag
    static int ACCE_FILTER_DATA_MIN_TIME = 1; // 1ms delay. ~35hz is a limiting sampling rate now. why?
    long lastSaved = System.currentTimeMillis();
    
    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "[ACTIVITY] onCreate");
        super.onCreate(savedInstanceState);
        
        mStepValue = 0;
        mPaceValue = 0;
        
        setContentView(R.layout.main);
        
        mUtils = Utils.getInstance();
        acc_disp =false;
        
        // for real time graph
        seriesStep = new ArrayList<GraphViewData>();
        seriesXYZ_HR = new ArrayList<GraphViewData>();
        seriesX = new ArrayList<GraphViewData>();
		seriesY = new ArrayList<GraphViewData>();
		seriesZ = new ArrayList<GraphViewData>();
		
		
        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "[onCreate] sensor registered ");
		
		
       
		// init example series data
        
        // step series 
        exampleStepSeries0 = new GraphViewSeries(new GraphViewData[] {});	
        graphView00 = new LineGraphView(
				this // context
				, "step" // heading
		);
        graphView00.addSeries(exampleStepSeries0); // data
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.graph00);
		layout.addView(graphView00);
        
		Log.i(TAG, "[onCreate] graph00 registered ");
        
        
		// group ACC
        exampleSeries0 = new GraphViewSeries(new GraphViewData[] {});	

		graphView0 = new LineGraphView(
				this // context
				, "Group ACC" // heading
		);

		
		graphView0.addSeries(exampleSeries0); // data
		
		layout = (LinearLayout) findViewById(R.id.graph0);
		layout.addView(graphView0);

		Log.i(TAG, "[onCreate] graph0 registered ");
        
        //---------
		exampleSeries1 = new GraphViewSeries(new GraphViewData[] {});	

		graphView1 = new LineGraphView(
				this // context
				, "ACC-Z" // heading
		);


		graphView1.addSeries(exampleSeries1); // data
		layout = (LinearLayout) findViewById(R.id.graph1);
		layout.addView(graphView1);

		Log.i(TAG, "[onCreate] graph1 registered ");
		
		// ----------
		exampleSeries2 = new GraphViewSeries(new GraphViewData[] {});
		
		graphView2 = new LineGraphView(
				this
				, "ACC-Y"
		);
		//((LineGraphView) graphView).setDrawBackground(true);
		
		graphView2.addSeries(exampleSeries2); // data
		layout = (LinearLayout) findViewById(R.id.graph2);
		layout.addView(graphView2);

		
		// init example series data
		exampleSeries3 = new GraphViewSeries(new GraphViewData[] {});

		graphView3 = new LineGraphView(
				this // context
				, "ACC-X" // heading
		);
		
		graphView3.addSeries(exampleSeries3); // data
		layout = (LinearLayout) findViewById(R.id.graph3);
		layout.addView(graphView3);
		
		
		// Net Acceleration adjustment
		h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        step = 0; //initial value
    }
    
    
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		
//	    Sensor sensor = event.sensor; 
  
	    if (sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
	    	if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME) {  
	            lastSaved = System.currentTimeMillis();	   
//	    	acc_net = mService.mStepDetector.acc_net;	    	
//	    	sensorXYZ = acc_net;  // not sure if this gets a snapshot value of acc_net upon sensor event
 		   
				sensorX = event.values[2];
	//			Log.d("MYAPP", "sensorX " + sensorX);
				sensorY = event.values[1];
				sensorZ = event.values[0];
//				float vSum = 0;
				float vvSum = 0;
				for (int i=0 ; i<3 ; i++) {
	                //final float v = mYOffset + event.values[i] * mScale[1]; // 1 for accelerometer.
					final float vv = event.values[i]*event.values[i]; 
//	                vSum += v;
	                vvSum += vv;
	            }
				//float v = vSum /3;
				
				float v = (float) Math.sqrt(vvSum);
				
				//** no filter (raw)
				
				
                //** Hanning filter 
                
				
				//**  Hanning recursive smoothing technique (filtering)  -added by Eric Sohn
				float v_hr = (float) 0.25*(v + 2* mLastValues_hr[0]  + mLastValues_hr[1]);
                //mLastValues[2] = mLastValues[1]; // shift left
                mLastValues_hr[1] = mLastValues_hr[0]; // shift left
                mLastValues_hr[0] = v_hr; // shift left
				
                //** five-point weighted average method (Eric defined ver)  
                float v_fwa = (float) 0.25*(v +  mLastValues_fwa[0]  + mLastValues_fwa[1] + mLastValues_fwa[2]);
                mLastValues_fwa[2] = mLastValues_fwa[1]; // shift left
                mLastValues_fwa[1] = mLastValues_fwa[0]; // shift left
                mLastValues_fwa[0] = v; // shift left
                
                
                sensorXYZ = v;
                sensorXYZ_HR = v_hr;
                sensorXYZ_fwa = v_fwa;
				sensorStep = mStepValue;
				seriesStep.add(new GraphViewData(dataCount, sensorStep));
				seriesXYZ_HR.add(new GraphViewData(dataCount, sensorXYZ_HR));
				seriesX.add(new GraphViewData(dataCount, sensorX));
				seriesY.add(new GraphViewData(dataCount, sensorY));
				seriesZ.add(new GraphViewData(dataCount, sensorZ));
		    
				dataCount++;
				
				// to write , % operator might slight slow reading rate??
				// time, step, sensorXYZ_HR, sensorXYZ_fwa, sensorXYZ, sensorX, sensorY, sensorZ
				acc= String.valueOf(df.format(lastSaved%100000)) + "," + String.valueOf(df.format(sensorStep)) + "," + String.valueOf(df.format(sensorXYZ_HR)) + "," 
						+ String.valueOf(df.format(sensorXYZ_fwa)) + "," + String.valueOf(df.format(sensorXYZ)) + "," + String.valueOf(df.format(sensorX)) + "," + String.valueOf(df.format(sensorY)) + "," + String.valueOf(df.format(sensorZ));
				
			
		/*		Context context = getApplicationContext();
				float number = (float)Math.round(sensorX * 1000) / 1000;
				//string formattedNumber = Float.toString(number);
				CharSequence text = Float.toString(number);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
		*/		
				if (seriesX.size() > 500) {
					seriesStep.remove(0);
					seriesXYZ_HR.remove(0);
					seriesX.remove(0);
					seriesY.remove(0);
					seriesZ.remove(0);
					graphView0.setViewPort(dataCount - 500, 500);
					graphView1.setViewPort(dataCount - 500, 500);
					graphView2.setViewPort(dataCount - 500, 500);
					graphView3.setViewPort(dataCount - 500, 500);
				}
		    }
		    else {
			// fail! we dont have an accelerometer!
		    	Log.d("MYAPP", "no acc");
		    	Toast.makeText(this, "No accelerometer", Toast.LENGTH_LONG).show();
		    }
		    
		    /*write to file */
		   // WriteFile(filepath,acc);
		    //mSensorLog.logSensorEvent(event);
	    }
	}
	
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		//Do nothing.
	}
   

        
       
    
    @Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
        mUtils.setSpeak(mSettings.getBoolean("speak", false));
        
        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();
        
        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startStepService();
            bindStepService();
        }
        else if (mIsRunning) {
            bindStepService();
        }
        
        mPedometerSettings.clearServiceRunning();//?

        mStepValueView     = (TextView) findViewById(R.id.step_value);
        mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
        
        mStatus  = (TextView) findViewById(R.id.status);
        mlogWifiToggleButton1 = (ToggleButton) findViewById(R.id.logWifiToggleButton1);
        mDatalog = (ToggleButton) findViewById(R.id.datalog);
        //mSpeedValueView    = (TextView) findViewById(R.id.speed_value);
        //mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
       
        mDesiredPaceView   = (TextView) findViewById(R.id.desired_pace_value);

        mIsMetric = mPedometerSettings.isMetric();
        ((TextView) findViewById(R.id.distance_units)).setText(getString(
                mIsMetric
                ? R.string.kilometers
                : R.string.miles
        ));
//        ((TextView) findViewById(R.id.speed_units)).setText(getString(
//                mIsMetric
//                ? R.string.kilometers_per_hour
//                : R.string.miles_per_hour
//        ));
        
        mMaintain = mPedometerSettings.getMaintainOption();
        ((LinearLayout) this.findViewById(R.id.desired_pace_control)).setVisibility(
                mMaintain != PedometerSettings.M_NONE
                ? View.VISIBLE
                : View.GONE
            );
        if (mMaintain == PedometerSettings.M_PACE) {
            mMaintainInc = 5f;
            mDesiredPaceOrSpeed = (float)mPedometerSettings.getDesiredPace();
        }
        else 
        if (mMaintain == PedometerSettings.M_SPEED) {
            mDesiredPaceOrSpeed = mPedometerSettings.getDesiredSpeed();
            mMaintainInc = 0.1f;
        }
        

        // WIFI streaming & logging of ACC, Toggle Button
        
        mlogWifiToggleButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO 
            	 if (!connected) {
                     if (!serverIpAddress.equals("")) {
                     	//text.setText("onclick if");  exception occurs
                     	 mlogWifiToggleButton1.setText("Stop Streaming");
                         Thread cThread = new Thread(new ClientThread());
                         cThread.start();
                     	// to call 
                 		
                         mStatus.setText("Ipadr configured");
                        
                     }
                 }
                 else{
                	 mlogWifiToggleButton1.setText("Start Streaming");
                     mStatus.setText("Streaming");
                     //connectPhones.setText("don't expect here");
                     connected=false;
                     acc_disp=false;
                 }
             }
         });
        

        
        Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed -= mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed += mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        if (mMaintain != PedometerSettings.M_NONE) {
            ((TextView) findViewById(R.id.desired_pace_label)).setText(
                    mMaintain == PedometerSettings.M_PACE
                    ? R.string.desired_pace
                    : R.string.desired_speed
            );
        }
        
        displayDesiredPaceOrSpeed();
        
        
//       startGraphActivity(RealtimeGraph.class);  // Only allow one activity. It is a mess trying to run multiple activity concurrently. 
        
//        // real time graph 
//		((Button) findViewById(R.id.realtimegraph)).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startGraphActivity(RealtimeGraph.class);
//			}
//		});
		
		 //Advanced multiple series graph
		((ToggleButton) findViewById(R.id.datalog)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 if (!writing) {
					//startGraphActivity(AdvancedMultipleSeriesGraph.class);
					 writing = true;
					Thread accThread1 = new Thread(new AccThread());
	                accThread1.start();
	                }
				 else{
						//                	 accThread1.kill();
		        	 mDatalog.setText("Writing stopped");
		        	 writing = false;
				 }
			}
		});
		
	
//       mDatalog.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // TODO 
////	        	 if (!writing) {
////	                    
//	                 	//text.setText("onclick if");  exception occurs
//	            		 writing = true;
//	                	 Thread accThread1 = new Thread(new AccThread());
//	                     accThread1.start();
//	                	 mDatalog.setText("Writing now");
//	                	 
//	                        
//	                     
////	             }
////		         else{
//		//                	 accThread1.kill();
//		        	 mDatalog.setText("Writing stopped");
//		        	 writing = false;
//		             //connectPhones.setText("don't expect here");
////		         }
//             }
//         });
		
        
        //for real time graph
        sManager.registerListener(this, sensor ,SensorManager.SENSOR_DELAY_FASTEST);
		
        
//        mTimer00 = new Runnable() {
//			@Override
//			public void run() {		
//				GraphViewData[] gvd = new GraphViewData[seriesStep.size()];				
//				seriesStep.toArray(gvd);				
//				exampleStepSeries0.resetData(gvd);
//				mHandler2.post(this); //, 100);
//			}
//		};
//		mHandler2.postDelayed(mTimer00, 50);
        
//        
//        mTimer0 = new Runnable() {
//			@Override
//			public void run() {		
//				GraphViewData[] gvd = new GraphViewData[seriesXYZ.size()];				
//				seriesXYZ.toArray(gvd);				
//				exampleSeries0.resetData(gvd);
//				mHandler2.post(this); //, 100);
//			}
//		};
//		mHandler2.postDelayed(mTimer0, 50);
//        
//		mTimer1 = new Runnable() {
//			@Override
//			public void run() {			
//				GraphViewData[] gvd = new GraphViewData[seriesX.size()];				
//				seriesX.toArray(gvd);
//				exampleSeries1.resetData(gvd);
//				mHandler2.post(this); //, 100);
//			}
//		};
//		mHandler2.postDelayed(mTimer1, 50);

//		mTimer2 = new Runnable() {
//			@Override
//			public void run() {
//				
//				GraphViewData[] gvd = new GraphViewData[seriesY.size()];				
//				seriesY.toArray(gvd);
//				exampleSeries2.resetData(gvd);
//
//				mHandler2.post(this);
//			}
//		};
//		mHandler2.postDelayed(mTimer2, 50);

	
//		mTimer3 = new Runnable() {
//			@Override
//			public void run() {
//				
//				GraphViewData[] gvd = new GraphViewData[seriesZ.size()];				
//				seriesZ.toArray(gvd);
//				exampleSeries3.resetData(gvd);
//
//				mHandler2.post(this);
//			}
//		};
//		mHandler2.postDelayed(mTimer3, 50);
//		
		
		
		
    }
    
    
//	private void startGraphActivity(Class<? extends Activity> activity) {
//		Intent intent = new Intent(SensorPedometer.this, activity);
//		//intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);  // to run to activities at the same time...?
//		intent.putExtra("type", "line");
//		
//		startActivity(intent); 
//	}
	
    public class AccThread implements Runnable {

        @Override
        public void run () {

        	while(writing)
            {
//                    Message msg1 = new Message();
                try 
                {
                    WriteFile(filepath,acc);
//                        msg1.what = MSG_DONE;
//                        msg1.obj = "Start to write to SD 'acc.txt'";
                    Thread.sleep(1);  // 2 to 1 doesn't make differnce in sensor sampling rate.  
                } 
                catch (Exception e) 
                {
                	e.printStackTrace();
//                        msg1.what = MSG_ERROR;
//                        msg1.obj = e.getMessage();
                }
//                    uiHandler.sendMessage(msg1);
//                Message msg2 = new Message();
//                msg2.what = MSG_STOP;
//                msg2.obj = "Stop to write to SD 'acc.txt'";
//                uiHandler.sendMessage(msg2);

//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
	                
	            
        	}
        
        }
 
    };
    
    
    // this class is for streaming to ipaddress
    public class ClientThread implements Runnable {
        Socket socket;
        public void run() {
            try {
            	
            	//text.setText("try");
            	//mStatus.setText("run try");
                acc_disp=true;
                //PORT = Integer.parseInt(port.getText().toString());
                PORT = 15000;
                //serverIpAddress=ipAdr.getText().toString();
                serverIpAddress = "192.168.0.106";
                
               
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                //InetAddress serverAddr = InetAddress.getByName("TURBOBEAVER");
                
                socket = new Socket(serverAddr, PORT);
                
                connected = true;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                
                
                while (connected) {
                    out.printf("%10.2f\n", acc_net-240);
                    out.printf("%10.2f\n",  (float)mService.mStepDetector.step);
                    out.flush();
                    Thread.sleep(2);
                }
            } 
            catch (Exception e) {
            	//throw new RuntimeException(e);  //falls here? 
            	Log.e("MYAPP", "exception", e);
            	//statustext.setText("run catch"); // exception occurs
            }
            
            finally{
                try{
                	//mStatus.setText("run finally");
                    acc_disp=false;
                    connected=false;
                    mlogWifiToggleButton1.setText("socket closed");
                    //out.close();
                    socket.close();
                }catch(Exception a){
                }
            }
            
        }
    };
     
     
    
    private void displayDesiredPaceOrSpeed() {
        if (mMaintain == PedometerSettings.M_PACE) {
            mDesiredPaceView.setText("" + (int)mDesiredPaceOrSpeed);
        }
        else {
            mDesiredPaceView.setText("" + mDesiredPaceOrSpeed);
        }
    }
    
    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        // for real time graph
		mHandler2.removeCallbacks(mTimer1);
		mHandler2.removeCallbacks(mTimer2);
		
        if (mIsRunning) {
            unbindStepService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }

        
        super.onPause();
        savePaceSetting();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        // for real time graph

		sManager.unregisterListener(this);
        super.onStop();
    }
    


    protected void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }

    private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
        if (mService != null) {
            if (mMaintain == PedometerSettings.M_PACE) {
                mService.setDesiredPace((int)desiredPaceOrSpeed);
            }
            else
            if (mMaintain == PedometerSettings.M_SPEED) {
                mService.setDesiredSpeed(desiredPaceOrSpeed);
            }
        }
    }
    
    private void savePaceSetting() {
        mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
    }

    private StepService mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();  //return StepService.this

            mService.registerCallback(mCallback);
            mService.reloadSettings();
            
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    

    private void startStepService() {
        if (! mIsRunning) {
            Log.i(TAG, "[SERVICE] Start");
            mIsRunning = true;
            startService(new Intent(SensorPedometer.this,
                    StepService.class));
        }
    }
    
    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        bindService(new Intent(SensorPedometer.this, 
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {
        Log.i(TAG, "[SERVICE] Unbind");
        unbindService(mConnection);
    }
    
    private void stopStepService() {
        Log.i(TAG, "[SERVICE] Stop");
        if (mService != null) {
            Log.i(TAG, "[SERVICE] stopService");
            stopService(new Intent(SensorPedometer.this,
                  StepService.class));
        }
        mIsRunning = false;
    }
    
    private void resetValues(boolean updateDisplay) {
        if (mService != null && mIsRunning) {
            mService.resetValues();                    
        }
        else {
            mStepValueView.setText("0");
            mPaceValueView.setText("0");
            mDistanceValueView.setText("0");
            //mSpeedValueView.setText("0");
            //mCaloriesValueView.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.putInt("pace", 0);
                stateEditor.putFloat("distance", 0);
                stateEditor.putFloat("speed", 0);
                stateEditor.putFloat("calories", 0);
                stateEditor.commit();
            }
        }
    }

    private static final int MENU_SETTINGS = 8;
    private static final int MENU_QUIT     = 9;

    private static final int MENU_PAUSE = 1;
    private static final int MENU_RESUME = 2;
    private static final int MENU_RESET = 3;
    
    /* Creates the menu items */
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mIsRunning) {
            menu.add(0, MENU_PAUSE, 0, R.string.pause)
            .setIcon(android.R.drawable.ic_media_pause)
            .setShortcut('1', 'p');
        }
        else {
            menu.add(0, MENU_RESUME, 0, R.string.resume)
            .setIcon(android.R.drawable.ic_media_play)
            .setShortcut('1', 'p');
        }
        menu.add(0, MENU_RESET, 0, R.string.reset)
        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        .setShortcut('2', 'r');
        menu.add(0, MENU_SETTINGS, 0, R.string.settings)
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setShortcut('8', 's')
        .setIntent(new Intent(this, Settings.class));
        menu.add(0, MENU_QUIT, 0, R.string.quit)
        .setIcon(android.R.drawable.ic_lock_power_off)
        .setShortcut('9', 'q');
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PAUSE:
                unbindStepService();
                stopStepService();
                return true;
            case MENU_RESUME:
                startStepService();
                bindStepService();
                return true;
            case MENU_RESET:
                resetValues(true);
                return true;
            case MENU_QUIT:
                resetValues(false);
                unbindStepService();
                stopStepService();
                mQuitting = true;
                finish();
                return true;
        }
        return false;
    }
 
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
        public void paceChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }
        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
        }
        public void caloriesChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG, (int)(value), 0));
        }
    };
    
    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    private static final int CALORIES_MSG = 5;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) { 
                        mPaceValueView.setText("0");
                    }
                    else {
                        mPaceValueView.setText("" + (int)mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ((int)msg.arg1)/1000f;
                    if (mDistanceValue <= 0) { 
                        mDistanceValueView.setText("0");
                    }
                    else {
                        mDistanceValueView.setText(
                                ("" + (mDistanceValue + 0.000001f)).substring(0, 5)
                        );
                    }
                    break;
//                case SPEED_MSG:
//                    mSpeedValue = ((int)msg.arg1)/1000f;
//                    if (mSpeedValue <= 0) { 
//                        mSpeedValueView.setText("0");
//                    }
//                    else {
//                        mSpeedValueView.setText(
//                                ("" + (mSpeedValue + 0.000001f)).substring(0, 4)
//                        );
//                    }
//                    break;
//                case CALORIES_MSG:
//                    mCaloriesValue = msg.arg1;
//                    if (mCaloriesValue <= 0) { 
//                        mCaloriesValueView.setText("0");
//                    }
//                    else {
//                        mCaloriesValueView.setText("" + (int)mCaloriesValue);
//                    }
//                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
    
//    private SensorEventListener accelerationListener = new SensorEventListener() {
//        
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int acc) {
//        	
//        }
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//        	
//            x = event.values[0];
//            y = event.values[1];
//            z = event.values[2];
//            refreshDisplay();
//        }
//    };
     
    private void refreshDisplay() {
    	//text.setText("refreshDisplay");
        if(acc_disp == true){
        	acc_net = mService.mStepDetector.acc_net;
            String output = String.format("X:%3.2f m/s^2  |  Y:%3.2f m/s^2  |   Z:%3.2f m/s^2", acc_net, acc_net, acc_net);
            mStatus.setText(output);
        }
    }



	// Below here starts write to file.
	
	public void CreateFile(String path)
	{
	    File f = new File(path);
	    try {
	        Log.d("ACTIVITY", "Create a File.");
	        f.createNewFile();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}


	public void WriteFile(String filepath, String str)
	{
	    mBufferedWriter = null;
	
	    if (!FileIsExist(filepath))
	        CreateFile(filepath);
	
	    try 
	    {
	        mBufferedWriter = new BufferedWriter(new FileWriter(filepath, true)); // Writer is to characters when Outputstream is to bytes.  
	        mBufferedWriter.write(str);
	        mBufferedWriter.newLine();
	        mBufferedWriter.flush();
	       // mBufferedWriter.close(); //why close?
	    }
	    catch (IOException e) 
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	public boolean FileIsExist(String filepath)
	{
	    File f = new File(filepath);
	
	    if (! f.exists())
	    {
	        Log.e("ACTIVITY", "File does not exist.");
	        return false;
	    }
	    else
	        return true;
	}

}

