package es.articos.midiadiawear;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        log("conectado a " + bundle);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        log("suspendida conexion" + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        log("Fallo de conexion " + connectionResult);
                    }
                })
                .build();

        Button boton = (Button)findViewById(R.id.button);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("enviando datos");
                PutDataMapRequest dataMap = PutDataMapRequest.create("/datos");
                Asset asset = Asset.createFromUri(Uri.parse("http://midiadia.blob.core.windows.net/products/e66b9de3-1426-4559-a5b6-e77f52276fe4_Small.png"));
                dataMap.getDataMap().putAsset("imagen", asset);
                String kcal = "10";
                dataMap.getDataMap().putString("texto", kcal);
                PutDataRequest request = dataMap.asPutDataRequest();
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                log("datos enviados" + request);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        log("Conectado a la API");
    }

    private void log(String message) {
        Log.d(this.getClass().getName(), message);
    }

    private void caloriesChanged(Integer calories) {
        new GetProductsForCaloriesTask().execute(calories);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class GetProductsForCaloriesTask extends AsyncTask<Integer, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(Integer... calories) {
            Integer newCalories = calories[0];

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
            ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

            DefaultHttpClient client = new DefaultHttpClient(cm, httpParams);
            client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

            HttpGet get = new HttpGet("/URI");

            try {
                HttpResponse response = client.execute(get);

                if (response.getEntity() != null) {
                    String entityContents = EntityUtils.toString(response.getEntity(), "utf-8");
                    JSONArray result = new JSONArray(entityContents);
                    if (result != null && result.length() > 0) {
                        return result.getJSONObject(0);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null && jsonObject.has("ProductId")) {
                String productId = jsonObject.optString("productId", null);

                if (productId != null) {
                    String imageURL = "http://midiadia.blob.core.windows.net/products/" +  productId + "_Small.png";
                    ImageLoader loader = ImageLoader.getInstance();

                    Bitmap bitmap = loader.loadImageSync(imageURL);

                }

            }
        }
    }
}
