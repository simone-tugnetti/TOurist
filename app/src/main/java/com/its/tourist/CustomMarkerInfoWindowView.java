package com.its.tourist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


/**
 *  @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 *  CustomMarkerInfoWindowView
 *  Classe utilizzata per modificare all'interno della mappa le finestre che appaiono quando viene
 *  premuto un Marker
 */
public class CustomMarkerInfoWindowView implements GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    private Activity context;


    /**
     *  Metodo costruttore
     *  Viene richiesta un'Activity, un contesto, alla quale collegarsi per modificare il suo interno
     *  @param context L'Activity collegata
     */
    CustomMarkerInfoWindowView(Activity context) {
        this.context = context;
    }


    /**
     *  Metodo per la gestione delle finestre di ogni marker
     *  Prende in considerazione ogni singolo marker e lo scandisce, ricavandone le informazioni,
     *  per poi inserirle all'interno di una View modificata con uno specifico layout
     *  @param marker Il marker preso in carico
     *  @return View
     */
    @Override
    public View getInfoWindow(Marker marker) {
        @SuppressLint("InflateParams")
        View markerItemView = context.getLayoutInflater().inflate(R.layout.layout_markers, null);
        TextView titoloCard = markerItemView.findViewById(R.id.titoloCard);
        TextView infoCard = markerItemView.findViewById(R.id.infoCard);
        ImageView imageCard = markerItemView.findViewById(R.id.imageCard);
        assert marker.getTag() != null;
        MarkerTags markerTags = (MarkerTags) marker.getTag();
        Bitmap image = markerTags.getImage();

        titoloCard.setText(marker.getTitle());
        infoCard.setText(marker.getSnippet());

        if(image != null) {
            imageCard.setImageBitmap(image);
        } else {
            imageCard.setVisibility(View.GONE);
        }

        return markerItemView;
    }


    /**
     *  Metodo per la gestione dei contenuti della finestra del marker
     *  Prende in considerazione ogni singolo marker e lo scandisce, ricavandone le informazioni,
     *  per poi inserirle all'interno della finestra di default.
     *  Se ritorna null, passa di default a getInfoWindow.
     *  @param marker Il marker preso in carico
     *  @return View
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     *  Metodo per la gestione del click sulla finestra del marker
     *  Quando verrà premuta la finestra di un marker, quest'ultima aprirà il Dialer
     *  con il numero telefonico specifico di quel determinato posto
     *  @param marker Il marker preso in carico
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        assert marker.getTag() != null;
        MarkerTags markerTags = (MarkerTags) marker.getTag();
        String phone = markerTags.getPhone();
        if(!phone.equals("")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"+phone));
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Numero inesistente!", Toast.LENGTH_LONG).show();
        }
    }
}

/**
 *  MarkerTags
 *  Classe utilizzata per poter salvare più dati all'interno del tag di un singolo marker
 */
class MarkerTags {

    private Bitmap image;
    private String phone;

    MarkerTags() {
        image = null;
        phone = "";
    }

    void setImage(Bitmap image) {
        this.image = image;
    }

    Bitmap getImage(){
        return image;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    String getPhone(){
        return phone;
    }

}
