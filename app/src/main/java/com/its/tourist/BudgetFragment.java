package com.its.tourist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jem.rubberpicker.RubberRangePicker;

import java.util.Objects;

/**
 * @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 *  BudgetFragment
 *  Classe usata per gestire il budget dell'utente
 */
public class BudgetFragment extends Fragment {

    private TextView seekbarEnd, seekbarStart;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    public BudgetFragment(){ }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GlobalVariable global = GlobalVariable.getInstance();

        // Da questo Fragment, il tasto Back non avrà messaggi di chiusura
        global.setBackPeople(false);

        // Per poter usare il pulsante indietro normalmente quando vi è in MapActivity
        if(Objects.requireNonNull(getActivity()).getClass() == MapActivity.class) {
            global.setBackPeopleMap(true);
        }

        gestionePicker();
        toTime();
    }


    /**
     *  Metodo per la visulizzazione del budget dell'utente
     *  Il budget viene visualizzato tramite una libreria specifica, inerente al Range Picker,
     *  all'interno di due TextView, cioè il minimo e il massimo disponibile
     */
    private void gestionePicker() {
        RubberRangePicker rubberRangePicker = Objects.requireNonNull(getView()).findViewById(R.id.seekbar);
        seekbarStart = getView().findViewById(R.id.txtSeekbarStart);
        seekbarEnd = getView().findViewById(R.id.txtSeekbarEnd);

        seekbarStart.setText(String.valueOf(0));
        seekbarEnd.setText(String.valueOf(1));

        //noinspection NullableProblems
        rubberRangePicker.setOnRubberRangePickerChangeListener(new RubberRangePicker
                .OnRubberRangePickerChangeListener() {
            @Override
            public void onProgressChanged(RubberRangePicker rubberRangePicker, int startThumbValue,
                                          int endThumbValue, boolean b) {
                if (b && (startThumbValue != Integer.parseInt(seekbarStart.getText().toString()) ||
                        endThumbValue != Integer.parseInt(seekbarEnd.getText().toString()))) {
                    if(endThumbValue == 200 && startThumbValue == 200){
                        startThumbValue -= 1;
                    } else if (startThumbValue == endThumbValue) {
                        endThumbValue += 1;
                    }
                    seekbarStart.setText(String.valueOf(startThumbValue));
                    seekbarEnd.setText(String.valueOf(endThumbValue));
                }
            }

            @Override
            public void onStartTrackingTouch(RubberRangePicker rubberRangePicker, boolean b) { }

            @Override
            public void onStopTrackingTouch(RubberRangePicker rubberRangePicker, boolean b) { }

        });
    }


    /**
     *  Metodo per la gestione dei pulsanti
     *  In base alla scelta se gratis o no, verrà passato il flag rispettivo
     */
    private void toTime() {
        Button avanti = Objects.requireNonNull(getView()).findViewById(R.id.btnAvanti);
        Button free = Objects.requireNonNull(getView()).findViewById(R.id.btnFree);
        avanti.setOnClickListener(v -> toTimeFragment(false));
        free.setOnClickListener(v -> toTimeFragment(true));
    }


    /**
     *  Metodo per la gestione del salvataggio del budget e della prosecuzione
     *  Se l'utente avrà premuto avanti, passeranno al prossimo Fragment i valori effettivi del
     *  budget, altrimenti massimo e minimo saranno entrambi 0
     *  @param free Flag per sapere quale scelta ha effettuato l'utente
     */
    private void toTimeFragment(boolean free) {
        assert getFragmentManager() != null;
        assert this.getArguments() != null;
        Bundle bundle = new Bundle();
        Bundle peopleData = this.getArguments();
        TimeFragment timeFragment = new TimeFragment();
        if (free) {
            bundle.putInt("startBudget", 0);
            bundle.putInt("endBudget", 0);
        } else {
            bundle.putInt("startBudget", Integer.parseInt(seekbarStart.getText().toString()));
            bundle.putInt("endBudget", Integer.parseInt(seekbarEnd.getText().toString()));
        }
        bundle.putString("numberOfPeople", peopleData.getString("numberOfPeople"));
        timeFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(Objects.requireNonNull(getActivity()).getClass() == MapActivity.class) {
            fragmentTransaction.replace(R.id.frame_map, timeFragment);
        } else {
            fragmentTransaction.replace(R.id.frame_main, timeFragment);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
