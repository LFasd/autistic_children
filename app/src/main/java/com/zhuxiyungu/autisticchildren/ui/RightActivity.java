package com.zhuxiyungu.autisticchildren.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.zhuxiyungu.autisticchildren.R;
import com.zhy.autolayout.AutoLayoutActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import butterknife.ButterKnife;

/**
 * Created by null on 17-2-18.
 */

public class RightActivity extends AutoLayoutActivity implements View.OnClickListener{


    private Context context;
    private Intent intent;
    private final String RIGHT = "恭喜你，答对了！";
    private SpeechSynthesizer mTts;
    private static final String TAG = "laoluo";
  /*  private TextView mTvApSsid;
    public TextView mget;
    private TextView msend;
    private TextView monline;
    private EditText mEdtApPassword;
    private Button mBtnConfirm;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;*/
    /*private LinearLayout mBack;
    private CheckBox mCheckboxIsSsidHidden;*/
    private EspWifiAdminSimple mWifiAdmin;

    private String remo_ip;
    private static final int UDP_SERVER_PORT = 2012;
    private String ApSsid;
    //代表一个套字节发送和接收数据
    private DatagramSocket My_UDP_Socket;
    private UDP_SEND udp_client;
    ExecutorService exec2;
    private int Online=0;
    private String Myip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置为全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.right_activity_layout);
        ButterKnife.bind(this);
        //设置屏幕为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        context = this;

        mWifiAdmin = new EspWifiAdminSimple(this);


        intent = getIntent();

        mTts = SpeechSynthesizer.createSynthesizer(context, null);
        setSynthesisParameters();
        mTts.startSpeaking(RIGHT, synthesizerListener);


        handler.postDelayed(runnable, 500);

        UDP_init();
        start_MOTOR();
        countDown();
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {

                if(Online>0){
                    Online--;
                }

                if(Online==0){
                    /*monline.setText("设备不在线，请打开设备，或者配置");
                    monline.setTextColor(Color.parseColor("red"));*/
                    handler.postDelayed(this, 1000);//没在线的时候，1秒钟检测一次
                }else{
                    handler.postDelayed(this, 5000);//发现设备之后，5秒钟检测一次
                }
                sent_udp("online");//所谓心跳，就是app发送一个"online"字符串，广播到局域网中

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };




    //进入页面时，判断手机是否处于wifi状态中，配置必须要在wifi环境中，
    @Override
    protected void onResume() {
        super.onResume();
        ApSsid = mWifiAdmin.getWifiConnectedSsid();
    }




    //转动装置的马达，请确保设备在线后调用（即收到心跳online后，再调用）
    public void start_MOTOR()
    {
        String tosend="good";
        sent_udp(tosend);

        /*SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate)+":"+tosend;
        msend.setText(str);*/
    }




    @Override
    public void onClick(View v) {

        /*//wifi配置
        if (v == mBtnConfirm) {
            Log.v("laoluo", "click me! OK");
            String apSsid = ApSsid;
            String apPassword = mEdtApPassword.getText().toString();

            String apBssid = mWifiAdmin.getWifiConnectedBssid();

            Boolean isSsidHidden = mCheckboxIsSsidHidden.isChecked();
            String isSsidHiddenStr;

            if (isSsidHidden) {
                isSsidHiddenStr = "YES";
            } else {
                isSsidHiddenStr = "NO";
            }
            String taskResultCountStr = "1";
            new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
        }*/

        /*//例子，手机发送一个"good"数据到装置，装置转动一次
        else if (v == btn1) {
            Log.i("btn1 :::::::", "OK");
            start_MOTOR();

        }*/

    }



    //配置wifi使用
    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    //配置wifi使用
    private IEsptouchListener myListener = new IEsptouchListener() {
        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };
    //配置wifi使用
    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;
        private IEsptouchTask mEsptouchTask;
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(RightActivity.this);
            mProgressDialog.setMessage("正在配置中, 耐心等待...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        //配置wifi使用
        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, RightActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        //配置wifi使用
        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("确认");
            IEsptouchResult DevResult = result.get(0);

            if ((!DevResult.isCancelled()) && (DevResult.isSuc())) {
//                remo_ip = DevResult.getInetAddress().getHostAddress().toString();
                sent_udp("online");
                sent_udp("online");
                sent_udp("online");
                mProgressDialog.setMessage("配置成功！！！ mac地址:" + DevResult.getBssid() + "\n");
            } else {
                mProgressDialog.setMessage("配置失败，请保证设备进入黄灯快闪的配置状态，并在需要配置的路由器附近");
            }
        }
    }







    /*
    *
    * 以下是udp部分的工作业务逻辑
    *
    * */
    public void UDP_init()
    {
        My_UDP_Socket = null;
        try {
            My_UDP_Socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(new UDP_Receive(My_UDP_Socket), "thread1");
        thread.start();
    }


    public void sent_udp(String tosend)
    {
        Thread thread = new Thread(new UDP_SEND(My_UDP_Socket,tosend,UDP_SERVER_PORT), "thread1");
        thread.start();
    }



    //udp接收
    public class UDP_Receive implements Runnable {

        private byte[] msg = new byte[1024];

        private DatagramPacket get_Packet;
        private DatagramSocket the_socket;

        public UDP_Receive(DatagramSocket new_socket) {
            this.the_socket = new_socket;
        }

        @Override
        public void run() {
            get_Packet = new DatagramPacket(msg, msg.length);
            String getdata;
            while(true) try {
                the_socket.receive(get_Packet);//此处阻塞

                getdata = new String(get_Packet.getData(), 0 ,get_Packet.getLength());
                getdata = getdata.replaceAll("[\u0000-\u001f]", "");
                Log.i("msg sever received", getdata);


                SocketAddress dev_address = get_Packet.getSocketAddress();
                Myip = dev_address.toString();

                RightActivity.this.runOnUiThread(new ChangeText(getdata));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    class ChangeText implements Runnable {
        String text;
        ChangeText(String text) {
            this.text = text;
        }
        @Override
        public void run() {
            Online=2;

            /*monline.setText("设备已经在线");
            monline.setTextColor(Color.parseColor("green"));*/

            /*if(!this.text.equals("online")) {
                SimpleDateFormat formatter=new SimpleDateFormat("HH:mm:ss");
                Date curDate =  new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate)+":"+this.text;
                mget.setText(str);
            }*/
        }
    }


    //udp发送广播
    public class UDP_SEND implements Runnable {

        private DatagramPacket bcast_send_packet;
        private DatagramSocket the_socket;

        private int re_port;
        private String buf;

        public UDP_SEND(DatagramSocket new_socket,String buf,int re_port) {
            this.the_socket = new_socket;
            this.buf = buf;
            this.re_port = re_port;
        }


        @Override
        public void run() {

            InetAddress cast_local = null;
            try {
                cast_local = InetAddress.getByName("255.255.255.255");//向局域网广播
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            bcast_send_packet = new DatagramPacket(buf.getBytes(), buf.length(), cast_local, re_port);
            //广播数据到局域网
            try {
                the_socket.send(bcast_send_packet);
                Log.i("msg send:", buf);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }



    // TODO: 这个过程为出糖果的过程,需要单开一个线程处理对应的业务逻辑
    public void countDown() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(context, ChildModelActivity.class));
                finish();
            }
        }, 6000);
    }

    //2.合成语音参数设置
    public void setSynthesisParameters() {
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "30");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量,范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    private SynthesizerListener synthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
}
