package es.articos.midiadiawear;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Map;

/**
 * Created by cgarcia on 6/11/14.
 */
public class DataService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent event : dataEvents) {
            Map<String, DataItemAsset> assets = event.getDataItem().getAssets();
            DataItemAsset imagen = assets.get("data");
            DataItemAsset texto = assets.get("texto");
            //imagen.
        }
    }
}
