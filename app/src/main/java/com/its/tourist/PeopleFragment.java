package com.its.tourist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Objects;

/**
 * @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 *  PeopleFragment
 *  Classe per la gestione del numero di persone che viaggiano assieme
 */
public class PeopleFragment extends Fragment {

    private GlobalVariable global;

    public PeopleFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_people, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = GlobalVariable.getInstance();

        // L'utente avrà la possibilità di uscire dall'app tramite messaggio quando si trova in questo Fragment
        global.setBackPeople(true);

        // Per la gestione dell'handler all'interno di MainActivity, il delay così non verrà ripetuto
        global.setHandlerPeopleFalse();
        chooseImageGroup();
        backHendler();
    }


    /**
     * Metodo per la gestione del bottone back
     * Se PeopleFragment viene creato tramite MapActivity, premendo il pulsante indietro, si ritornerà
     * alla suddetta Activity, nascondendo il FrameLayout al suo interno
     */
    private void backHendler() {
        if(Objects.requireNonNull(getActivity()).getClass() == MapActivity.class) {
            Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
            getView().requestFocus();
            Objects.requireNonNull(getView()).setOnKeyListener((v, keyCode, event) -> {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().findViewById(R.id.frame_map).setVisibility(View.GONE);
                    global.setBackPeopleMap(false);
                    return true;
                }
                return false;
            });
        }
    }


    /**
     *  Metodo per la gestione del numero di persone
     *  In base alla scelta dell'utente, verranno salvate il numero di persone in viaggio
     */
    private void chooseImageGroup(){
        ImageView imgSingolo = Objects.requireNonNull(getView()).findViewById(R.id.imgViewSingolo);
        imgSingolo.setOnClickListener(v -> toBudget("singolo"));

        ImageView imgCoppia = getView().findViewById(R.id.imgViewCoppia);
        imgCoppia.setOnClickListener(v -> toBudget("coppia"));

        ImageView imgGruppo = getView().findViewById(R.id.imgViewGruppo);
        imgGruppo.setOnClickListener(v -> toBudget("gruppo"));
    }


    /**
     *  Metodo per la gestione del salvataggio del numero di persone e della prosecuzione
     *  Prima di visualizzare il prossimo Fragment, verranno salvate il numero di persone
     *  all'interno del prossimo Fragment tramite Bundle
     *  @param txtPeople Il numero di persone selezionate
     */
    private void toBudget(String txtPeople) {
        assert getFragmentManager() != null;
        Bundle bundle = new Bundle();
        BudgetFragment budgetFragment = new BudgetFragment();
        bundle.putString("numberOfPeople", txtPeople);
        budgetFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(Objects.requireNonNull(getActivity()).getClass() == MapActivity.class) {
            fragmentTransaction.replace(R.id.frame_map, budgetFragment);
        } else {
            fragmentTransaction.replace(R.id.frame_main, budgetFragment);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
