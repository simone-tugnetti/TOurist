package com.its.tourist;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 *  TimeFragment
 *  Classe usata per gestire il tempo che l'utente ha a disposizione per visitare la città
 */
public class TimeFragment extends Fragment {

    private Calendar myCalendar;
    private TextView txtCalendar, txtStartTime, txtEndTime;
    private int hour, minute;
    private GlobalVariable global;


    public TimeFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = GlobalVariable.getInstance();

        txtCalendar = Objects.requireNonNull(getView()).findViewById(R.id.txtData);
        txtStartTime = getView().findViewById(R.id.timeBegin);
        txtEndTime = getView().findViewById(R.id.timeEnd);
        myCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALIAN);
        hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        minute = myCalendar.get(Calendar.MINUTE);

        disableDalleAlle();
        setTime(txtStartTime,true);
        setTime(txtEndTime,false);
        setDateCalendar();

        toMap();
    }


    /**
     *  Metodo per la gestione del calendario
     *  Viene visualizzato un calendario, tramite Date Picker, dal quale sarà possibile selezionare,
     *  dalla data attuale, un possibile giorno di arrivo in città, per poi visulaizzarlo in una TextView
     */
    private void setDateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
        txtCalendar.setText(sdf.format(myCalendar.getTime()));
        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);
            txtCalendar.setText(sdf.format(myCalendar.getTime()));
        };
        ImageView calendar = Objects.requireNonNull(getView()).findViewById(R.id.imgViewCalendar);
        calendar.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                    date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }


    /**
     *  Metodo per la gestione dell'orario
     *  Viene visualizzato un orologio, tramite Time Picker, dal quale sarà possibile selezionare
     *  un possibile orario di inizio e fine della visita in città, per poi visulaizzarlo in una TextView
     *  @param txtTime La TextView alla quale verrà selezionato e visualizzato l'orario
     *  @param start Flag per determinare se la TextView utilizzata è quella di inizio, per settarla
     *              all'orario attuale
     */
    @SuppressLint("DefaultLocale")
    private void setTime(TextView txtTime, boolean start) {
        if (start) {
            txtTime.setText(String.format("%02d:%02d", hour, minute));
        }
        txtTime.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (timePicker, selectedHour, selectedMinute) ->
                    txtTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute)),
                    hour, minute, true);
            timePickerDialog.show();
        });
    }


    /**
     *  Metodo per la gestione della possibilità di visita per tutta la durata odierna
     *  Quando lo Switch è abilitato, l'utente ha la possibilità di poter visitare per tutto il
     *  giorno, o per quel che ne rimane, la città di Torino, altrimenti vengono resettate le
     *  TextView e può essere di nuovo possibile scegliere un orario
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void disableDalleAlle() {
        Switch allDay = Objects.requireNonNull(getView()).findViewById(R.id.switchGiorno);
        allDay.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                txtStartTime.setEnabled(false);
                txtEndTime.setEnabled(false);
                txtStartTime.setText("00:00");
                txtEndTime.setText("23:59");
            } else {
                txtStartTime.setEnabled(true);
                txtEndTime.setEnabled(true);
                txtStartTime.setText(String.format("%02d:%02d", hour, minute));
                txtEndTime.setText("00:00");
            }
        });
    }


    /**
     *  Metodo per la gestione del pulsante Avanti
     *  Se l'orario di inizio e di fine coincidono, così da avere un range temporale di 0 min,
     *  verrà visualizzato un Toast di avvertimento,
     *  altriemnti si passerà alla sezione per i permessi di geoclocalizzazione
     */
    private void toMap() {
        Button avanti = Objects.requireNonNull(getView()).findViewById(R.id.btnAvanti3);
        avanti.setOnClickListener(v -> {
            if (txtStartTime.getText().toString().equals(txtEndTime.getText().toString())) {
                Toast.makeText(getActivity(), "Inserisci un range temporale valido!",
                        Toast.LENGTH_LONG).show();
            } else {
                permessi();
            }
        });
    }


    /**
     *  Metodo che gestisce i permessi di geolocalizzazione
     *  Utilizzando una libreria per semplificare la procedura, chiamata Dexter, è possibile poter
     *  richiedere all'utente i permessi per poter utilizzare la mappa nella prossima Activity.
     *  Se l'utente darà i permessi, si passerà al salvataggio dei dati e al passaggio verso MapActivity.
     *  Se l'utente negherà i permessi, apparirà un Toast di errore e al prossimo avvio dell'app
     *  verranno richiesti nuovamente.
     *  Se l'utente negherà permanentemente i permessi, apparirà un messaggio di errore per poi far
     *  chiudere l'app, costringendo l'utente a dare i permessi solamente tramite le impostazioni
     *  dello smartphone.
     */
    private void permessi() {
        Dexter.withActivity(getActivity())
                .withPermission(ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        toMapActivity();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            new AlertDialog
                                    .Builder(Objects.requireNonNull(getActivity()))
                                    .setTitle("Accesso ai permessi")
                                    .setMessage("L'accesso alla localizzazione è permanentemente negato." +
                                            "\nRecarsi nelle impostazioni per attivare il servizio")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("OK", (dialog, which) -> getActivity().finish())
                                    .show();
                        } else {
                            Toast.makeText(getActivity(), "Permesso negato", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }


    /**
     *  Metodo per salvare i dati complessivi e passare a MapActivity
     *  Vengono infine salvati i dati raccolti precedentemente dai Fragment all'interno del
     *  Singleton per poi avviare la mappa contenuta all'interno di MapActivity
     */
    private void toMapActivity() {
        assert this.getArguments() != null;
        Bundle bundle = this.getArguments();
        global.setBudgetStart(bundle.getInt("startBudget"));
        global.setBudgetEnd(bundle.getInt("endBudget"));
        global.setTypePerson(bundle.getString("numberOfPeople"));
        global.setCalendarDay(myCalendar.get(Calendar.DAY_OF_WEEK));
        global.setTimeStart(txtStartTime.getText().toString());
        global.setTimeEnd(txtEndTime.getText().toString());
        Intent intent = new Intent(getActivity(), MapActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }

}
