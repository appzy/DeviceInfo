package ltns.deviceinfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ltns.deviceinfolib.DeviceInfoManager;
import ltns.deviceinfolib.collector.BaseDeviceInfoCollector;
import ltns.deviceinfolib.collector.BoardInfoCollector;
import ltns.deviceinfolib.collector.SimInfoCollector;
import ltns.deviceinfolib.listener.DeviceInfoCollectListener;

public class MainActivity extends AppCompatActivity {
    private static final int ERROR = 0;
    private static final int ALL_COMPLETED = 1;
    private static final int ALL_AUTO_COMPLETED = 2;
    private static final int SINGLE_COMPLETED = 3;
    private static final int SINGLE_FAILED = 4;
    private TextView tv;
    private Button btn;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SINGLE_COMPLETED:
                    break;
                case SINGLE_FAILED:
                    break;
                case ALL_COMPLETED:
                    tv.setText(((DeviceInfoManager) msg.obj).getDeviceJsonInfo());
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tv = (TextView) findViewById(R.id.textView);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectDeviceInfo();
            }
        });
    }

    private DeviceInfoCollectListener mDeviceInfoCollectListener = new DeviceInfoCollectListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onSingleSuccess(BaseDeviceInfoCollector mCollector) {

        }

        @Override
        public void onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo) {
        }

        @Override
        public void onAllDone(DeviceInfoManager mDeviceInfoManager) {
            Message m = new Message();
            m.what = ALL_COMPLETED;
            m.obj = mDeviceInfoManager;
            mHandler.sendMessage(m);
        }

        @Override
        public void onAutoAllDone(DeviceInfoManager mDeviceInfoManager) {
        }
    };

    private void collectDeviceInfo() {
        DeviceInfoManager.NewInstance(this)
                .addCollector(new BoardInfoCollector(MainActivity.this, "board"))
                .addCollector(new SimInfoCollector(MainActivity.this, "sim"))
                .autoStartManualCollection(true)
                .bindListener(mDeviceInfoCollectListener)
                .start();
    }
}
