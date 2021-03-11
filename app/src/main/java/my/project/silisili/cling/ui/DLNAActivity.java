package my.project.silisili.cling.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;

import java.util.Collection;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.silisili.R;
import my.project.silisili.cling.Intents;
import my.project.silisili.cling.adapter.DevicesAdapter;
import my.project.silisili.cling.control.ClingPlayControl;
import my.project.silisili.cling.control.callback.ControlCallback;
import my.project.silisili.cling.control.callback.ControlReceiveCallback;
import my.project.silisili.cling.entity.ClingDevice;
import my.project.silisili.cling.entity.ClingDeviceList;
import my.project.silisili.cling.entity.DLANPlayState;
import my.project.silisili.cling.entity.IDevice;
import my.project.silisili.cling.entity.IResponse;
import my.project.silisili.cling.listener.BrowseRegistryListener;
import my.project.silisili.cling.listener.DeviceListChangedListener;
import my.project.silisili.cling.service.ClingUpnpService;
import my.project.silisili.cling.service.manager.ClingManager;
import my.project.silisili.cling.service.manager.DeviceManager;
import my.project.silisili.cling.util.OtherUtils;
import my.project.silisili.main.base.BaseActivity;
import my.project.silisili.main.base.Presenter;
import my.project.silisili.R;
import my.project.silisili.main.base.BaseActivity;

public class DLNAActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = DLNAActivity.class.getSimpleName();
    /** 连接设备状态: 播放状态 */
    public static final int PLAY_ACTION = 0xa1;
    /** 连接设备状态: 暂停状态 */
    public static final int PAUSE_ACTION = 0xa2;
    /** 连接设备状态: 停止状态 */
    public static final int STOP_ACTION = 0xa3;
    /** 连接设备状态: 转菊花状态 */
    public static final int TRANSITIONING_ACTION = 0xa4;
    /** 获取进度 */
    public static final int GET_POSITION_INFO_ACTION = 0xa5;
    /** 投放失败 */
    public static final int ERROR_ACTION = 0xa5;

    private Context mContext;
    private Handler mHandler = new InnerHandler();

    @BindView(R.id.lv_devices)
    ListView mDeviceList;
    @BindView(R.id.tv_selected)
    TextView mTVSelected;
    @BindView(R.id.seekbar_progress)
    SeekBar mSeekProgress;
    @BindView(R.id.seekbar_volume)
    SeekBar mSeekVolume;
//    private Switch mSwitchMute;

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    private ArrayAdapter<ClingDevice> mDevicesAdapter;
    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /** 用于监听发现设备 */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");

            ClingManager.getInstance().setUpnpService(null);
        }
    };
    private String playUrl; // 视频播放地址
    private long duration; // 视屏长度 TODO:视频进度拖动、回显

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_dlna;
    }

    @Override
    protected void init() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.6f;
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = (int) Math.round(outMetrics.widthPixels / 1.2);
        attributes.height = (int) Math.round(outMetrics.heightPixels / 1.2);
        getWindow().setAttributes(attributes);
        mContext = this;
        playUrl = getIntent().getExtras().getString("playUrl");
        duration = getIntent().getExtras().getLong("duration");
        initView();
        initListeners();
        bindServices();
        registerReceivers();
        hideNavBar();
    }

    @Override
    protected void initBeforeView() {

    }

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(DLNAActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
        // Bind System service
        //        Intent systemServiceIntent = new Intent(MainActivity.this, SystemService.class);
        //        bindService(systemServiceIntent, mSystemServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        unbindService(mUpnpServiceConnection);
        // Unbind System service
        //        unbindService(mSystemServiceConnection);
        // UnRegister Receiver
        unregisterReceiver(mTransportStateBroadcastReceiver);

        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
    }

    private void initView() {
        mDevicesAdapter = new DevicesAdapter(mContext);
        mDeviceList.setAdapter(mDevicesAdapter);

        // 片源的时间
        mSeekProgress.setMax((int) duration);

        // 设置最大音量
        mSeekVolume.setMax(100);
    }

    private void initListeners() {
/*        mRefreshLayout.setOnRefreshListener(() -> {
            mRefreshLayout.setRefreshing(true);
            mDeviceList.setEnabled(false);

            mRefreshLayout.setRefreshing(false);
            refreshDeviceList();
            mDeviceList.setEnabled(true);
        });*/
        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            // 选择连接设备
            ClingDevice item = mDevicesAdapter.getItem(position);
            if (OtherUtils.isNull(item)) {
                return;
            }

            ClingManager.getInstance().setSelectedDevice(item);

            Device device = item.getDevice();
            if (OtherUtils.isNull(device)) {
                return;
            }

            String selectedDeviceName = String.format(getString(R.string.selectedText), device.getDetails().getFriendlyName());
            mTVSelected.setText(selectedDeviceName);
        });

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                runOnUiThread(() -> mDevicesAdapter.add((ClingDevice) device));
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                runOnUiThread(() -> mDevicesAdapter.remove((ClingDevice) device));
            }
        });
        // 静音开关
   /*     mSwitchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mClingPlayControl.setMute(isChecked, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "setMute success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "setMute fail");
                    }
                });
            }
        });*/

        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);
    }

    /**
     * 刷新设备
     */
    private void refreshDeviceList() {
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesAdapter.clear();
            mDevicesAdapter.addAll(devices);
        }
    }

    @OnClick({R.id.play, R.id.pause, R.id.stop, R.id.exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                play();
                break;
            case R.id.pause:
                pause();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.exit:
                finish();
                break;
        }
    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    public void getPositionInfo() {
        mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {

            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    /**
     * 播放视频
     */
    private void play() {
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == DLANPlayState.STOP) {
            mClingPlayControl.playNew(playUrl, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                    ClingManager.getInstance().registerAVTransport(mContext);
                    ClingManager.getInstance().registerRenderingControl(mContext);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        }
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Start Seek");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Stop Seek");
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekbar_progress: // 进度
                int currentProgress = seekBar.getProgress() * 1000; // 转为毫秒
                mClingPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "seek success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "seek fail");
                    }
                });
                break;
            case R.id.seekbar_volume:   // 音量
                int currentVolume = seekBar.getProgress();
                mClingPlayControl.setVolume(currentVolume, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "volume success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "volume fail");
                    }
                });
                break;
        }
    }

    /******************* end progress changed listener *************************/

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    Toast.makeText(mContext, "正在投放", Toast.LENGTH_SHORT).show();
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);

                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);

                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);

                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(mContext, "正在连接", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(mContext, "投放失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
    }
}