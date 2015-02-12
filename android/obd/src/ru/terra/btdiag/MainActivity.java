package ru.terra.btdiag;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.terra.btdiag.activity.*;
import ru.terra.btdiag.chat.ChatService;
import ru.terra.btdiag.core.InfoService;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.net.core.OBDRest;
import ru.terra.btdiag.net.task.SendTroubleAsyncTask;
import ru.terra.btdiag.obd.io.AbstractGatewayService;
import ru.terra.btdiag.obd.io.ObdGatewayService;
import ru.terra.btdiag.obd.io.ProtocolSelectionAsyncTask;
import ru.terra.btdiag.obd.io.helper.BtObdConnectionHelper;

import javax.inject.Inject;
import java.util.Date;

@ContentView(R.layout.main)
public class MainActivity extends RoboActivity {

    public static final String STATUS_RECEIVER = "ru.terra.btdiag.status_receiver";
    private static final String TAG = MainActivity.class.getName();
    private static final int NO_BLUETOOTH_ID = 0;
    private static final int BLUETOOTH_DISABLED = 1;
    @InjectView(R.id.tvChatStatus)
    public TextView tvChatStatus;
    @InjectView(R.id.tvObdStatus)
    public TextView tvObdStatus;
    @Inject
    public OBDRest obdRest;
    @Inject
    public BtObdConnectionHelper connectionHelper;

    private boolean preRequisites = true;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.d(TAG, className.toString() + " service is bound");
            AbstractGatewayService service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(MainActivity.this);
            Logger.d(TAG, "Starting the live data");
            try {
                service.startService();
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    Toast.makeText(MainActivity.this, "Ошибка инициализации", Toast.LENGTH_LONG).show();
//                    finish();
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Logger.d(TAG, className.toString() + " service is unbound");
        }
    };
    private StatusReceiver statusReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get Bluetooth device
        final BluetoothAdapter btAdapter = BluetoothAdapter
                .getDefaultAdapter();

        preRequisites = btAdapter == null ? false : true;
        if (preRequisites)
            preRequisites = btAdapter.isEnabled();

        if (!preRequisites) {
            showDialog(BLUETOOTH_DISABLED);
            Toast.makeText(this, "BT is disabled, will use Mock service instead", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Blutooth ok", Toast.LENGTH_SHORT).show();
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.auto_connect), false)) {
            bindService(new Intent(this, ObdGatewayService.class), serviceConn, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, ChatService.class));
        }
        startService(new Intent(this, InfoService.class));

        if (PreferenceManager.getDefaultSharedPreferences(this).getString(ConfigActivity.BLUETOOTH_LIST_KEY, "").length() == 0)
            Toast.makeText(this, "Не выбрано устройство bluetooth", Toast.LENGTH_LONG).show();

        IntentFilter statusFilter = new IntentFilter(STATUS_RECEIVER);
        statusFilter.addCategory(Intent.CATEGORY_DEFAULT);
        statusReceiver = new StatusReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, statusFilter);
    }

    private void updateConfig() {
        startActivity(new Intent(this, ConfigActivity.class));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_settings:
                updateConfig();
                return true;
            case R.id.mi_login:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.mi_chat:
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            case R.id.mi_charts:
                startActivity(new Intent(this, ChartsListActivity.class));
                return true;
            case R.id.mi_troubles:
                startActivity(new Intent(this, TroublesActivity.class));
                return true;
        }
        return false;
    }

    public void autoSelectProtocol(View view) {
        new ProtocolSelectionAsyncTask(this, connectionHelper).execute();
    }

    public void testTroubleShoot(View view) {
        Toast.makeText(this, "Тестовая отсылка ошибки", Toast.LENGTH_SHORT).show();
        new SendTroubleAsyncTask(this, obdRest).execute("Test trouble code at " + new Date());
    }

    private class StatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("type", 0) == 0) {
                //obd
                tvObdStatus.setText(intent.getStringExtra("text"));
            } else {
                //chat
                tvChatStatus.setText(intent.getStringExtra("text"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        try {
            unbindService(serviceConn);
        } catch (IllegalArgumentException e) {
        }
    }
}