package es.articos.midiadiawear;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class MainActivity extends Activity implements DataApi.DataListener, SensorEventListener {

    private TextView mTextView;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        log("conectado a " + bundle);

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        log("suspendida conexion " + i);
                    }


                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        log("Fallo de conexion " + connectionResult);
                    }
                })
                .build();

        //PutDataMapRequest dataMap = PutDataMapRequest.create("/datos");
        //Wearable.DataApi.addListener(mGoogleApiClient, new DataApi.DataListener() {
          //  @Override
            //public void onDataChanged(DataEventBuffer dataEvents) {

           // }
        //});
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(acelerometro != null) {
            mSensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        log("conectado al api en el reloj");
    }

    private void log(String message) {
        Log.d(this.getClass().getName(), message);
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent evt : dataEvents) {
            log("recibidos datos " + evt);
            DataMapItem item = DataMapItem.fromDataItem(evt.getDataItem());
            DataMap datos = item.getDataMap();
            String texto = datos.getString("texto");
            log("recibido texto " + texto);
            Asset imagen = datos.getAsset("imagen");
            mTextView.setText(texto);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //sensorEvent.values
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
