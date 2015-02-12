package ru.terra.btdiag.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.inject.Inject;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import org.acra.ACRA;
import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.terra.btdiag.R;
import ru.terra.btdiag.core.constants.ObdCommandNames;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.obd.io.AbstractGatewayService;
import ru.terra.btdiag.obd.io.ObdCommandJob;
import ru.terra.btdiag.obd.io.ObdGatewayService;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jjoe64.graphview.GraphView.GraphViewData;

/**
 * Date: 23.12.14
 * Time: 12:29
 */
@ContentView(R.layout.a_chart_info)
public class ChartActivity extends RoboActivity {
    private static final String TAG = ChartActivity.class.getName();

    @InjectView(R.id.llChart)
    LinearLayout llChart;
    @Inject
    private PowerManager powerManager;
    private boolean isServiceBound;
    private AbstractGatewayService service;
    private Class<? extends ObdCommand> command;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.d(TAG, className.toString() + " service is bound");
            isServiceBound = true;
            service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(ChartActivity.this);
            Toast.makeText(ChartActivity.this, "Подключение успешно", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Logger.d(TAG, className.toString() + " service is unbound");
            isServiceBound = false;
        }
    };

    private PowerManager.WakeLock wakeLock = null;

    private final Runnable mQueueCommands = new Runnable() {
        public void run() {
            if (service != null) {
                if (!service.isRunning())
                    if (!service.startService()) {
                        finish();
                        return;
                    }
                if (service.getCurrentQueueSize() == 0)
                    if (isServiceBound) {
                        try {
                            service.queueJob(new ObdCommandJob(command.newInstance()));
                        } catch (InstantiationException e) {
                            ACRA.getErrorReporter().handleException(e);
                        } catch (IllegalAccessException e) {
                            ACRA.getErrorReporter().handleException(e);
                        }
                    }
            }
            new Handler().postDelayed(mQueueCommands, 10);
        }
    };
    private LineGraphView graphView;
    private GraphViewSeries graphViewSeries;
    private int count = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AvailableCommandNames commandName = AvailableCommandNames.valueOf(getIntent().getStringExtra("command"));
        command = ObdCommandNames.getCommand(commandName);
        if (command == null) {
            Toast.makeText(this, "Command name " + commandName.getValue() + " is not recognized", Toast.LENGTH_LONG).show();
            finish();
        }
        graphViewSeries = new GraphViewSeries(new GraphViewData[]{});
        graphView = new LineGraphView(
                this // context
                , commandName.name() // heading
        );
        graphView.addSeries(graphViewSeries); // data

        graphView.setScalable(true);
        graphView.setScrollable(true);
        llChart.addView(graphView);
    }

    public void stateUpdate(final ObdCommand job) {
        final String cmdName = job.getName();
        final String cmdResult = job.getFormattedResult();

        Logger.d(TAG, "Command result = " + cmdResult);

        Double res = 0d;

        List<String> numbers = new LinkedList<String>();
        try {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(cmdResult);
            while (m.find()) {
                numbers.add(m.group());
            }
        } catch (NullPointerException e) {
            Logger.e(TAG, "Error while parsing result", e);
        }
        if (numbers.size() > 0)
            res = Double.parseDouble(numbers.get(0));
        try {
            GraphViewData data = new GraphViewData(count++, res);
            graphViewSeries.appendData(data, true);
        } catch (Exception e) {
            Logger.e(TAG, "Unable to parse command result", e);

        }
        if (job instanceof TroubleCodesObdCommand) {
            Toast.makeText(this, "Найдена ошибка: " + job.getFormattedResult(), Toast.LENGTH_LONG).show();
        }

        Logger.d(TAG, "Command " + cmdName + " result = " + cmdResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLiveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "Resuming..");
    }

    private void startLiveData() {
        Logger.d(TAG, "Starting live data..");
        doBindService();
        new Handler().post(mQueueCommands);
    }

    private void doBindService() {
        if (!isServiceBound) {
            Logger.d(TAG, "Binding OBD service..");
            bindService(new Intent(this, ObdGatewayService.class), serviceConn, Context.BIND_AUTO_CREATE);
        }
    }

    private void doUnbindService() {
        if (isServiceBound) {
            Logger.d(TAG, "Unbinding OBD service..");
            unbindService(serviceConn);
            isServiceBound = false;
        }
    }
}
