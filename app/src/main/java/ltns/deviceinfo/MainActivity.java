package ltns.deviceinfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ltns.deviceinfolib.DeviceInfoManager;
import ltns.deviceinfolib.collector.NfcInfoCollector;
import ltns.deviceinfolib.collector.PhoneBasicInfoCollector;
import ltns.deviceinfolib.collector.SensorInfoCollector;
import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;
import ltns.deviceinfolib.listener.DeviceInfoCollectListener;

public class MainActivity extends AppCompatActivity {
    private static final int ERROR = 0;
    private static final int ALL_DONE = 1;
    private static final int ALL_AUTO_COMPLETED = 2;
    private static final int SINGLE_SUCCEED = 3;
    private static final int SINGLE_FAILED = 4;
    private TextView tv;
    private Button btn;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SINGLE_SUCCEED:
                    tv.setText(tv.getText() + "\n" + ((BaseDeviceInfoCollector) msg.obj).getJsonInfo());
                    break;
                case SINGLE_FAILED:
                    tv.setText(tv.getText() + "\n" + msg.obj.toString());
                    break;
                case ALL_DONE:
                    Toast.makeText(MainActivity.this, "采集完成", Toast.LENGTH_SHORT).show();
                    tv.setText(tv.getText() + "\n\n\nAll Done:\n"+((DeviceInfoManager) msg.obj).getDeviceJsonInfo());
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
                tv.setText("");
                collectDeviceInfo();
            }
        });
    }

    private DeviceInfoCollectListener mDeviceInfoCollectListener = new DeviceInfoCollectListener() {
        @Override
        public void onStart() {
            Toast.makeText(MainActivity.this, "开始采集", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSingleSuccess(BaseDeviceInfoCollector mCollector) {
            Message m = new Message();
            m.what = SINGLE_SUCCEED;
            m.obj = mCollector;
            mHandler.sendMessage(m);

        }

        @Override
        public void onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo) {
            Message m = new Message();
            m.what = SINGLE_FAILED;
            m.obj =mErrorInfo+"-->"+mCollector.getJsonInfo();
            mHandler.sendMessage(m);
        }

        @Override
        public void onAllDone(DeviceInfoManager mDeviceInfoManager) {
            Message m = new Message();
            m.what = ALL_DONE;
            m.obj = mDeviceInfoManager;
            mHandler.sendMessage(m);
        }

        @Override
        public void onAutoAllDone(DeviceInfoManager mDeviceInfoManager) {
        }
    };

    private void collectDeviceInfo() {
        DeviceInfoManager.NewInstance(this)
                .addCollector(new PhoneBasicInfoCollector(this, "basic"))
//                .addCollector(new SimInfoCollector(this, "sim"))
//                .addCollector(new CpuInfoCollector(this, "cpu"))
//                .addCollector(new BoardInfoCollector(this, "board"))
//                .addCollector(new BatteryInfoCollector(this, "battery"))
//                .addCollector(new StorageInfoCollector(this, "storage"))
//                .addCollector(new CameraInfoCollector(this, "camera",true))
//                .addCollector(new ScreenInfoCollector(this, "screen"))
//                .addCollector(new UiInfoCollector(this, "ui"))
                .addCollector(new SensorInfoCollector(this, "sensor"))
                .addCollector(new NfcInfoCollector(this, "nfc"))
                .autoStartManualCollection(true)
                .bindListener(mDeviceInfoCollectListener)
                .start();
    }
}
