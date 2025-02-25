package com.example.chatlinkup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.homepage2.R;

public class suggestion_d_amis extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestions_d_amis);
        // Récupérer le champ de recherche
        EditText searchEditText = findViewById(R.id.search);

        // Ajouter un TextWatcher pour filtrer les éléments au fur et à mesure que l'utilisateur tape
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Pas besoin de faire quoi que ce soit ici
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Appeler la méthode de filtrage de chaque fragment avec le texte de recherche
                Fragment fragment1 = getSupportFragmentManager().findFragmentById(R.id.fragment1);
                Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragment2);

                if (fragment1 instanceof Invetations) {
                    ((Invetations) fragment1).filterList(charSequence.toString());
                }

                if (fragment2 instanceof Suggestions) {
                    ((Suggestions) fragment2).filterList1(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Pas besoin de faire quoi que ce soit ici
            }
        });
        }
    }

