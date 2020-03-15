package com.maps.reto1map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddMarkers extends AppCompatActivity {

    private EditText editText_name;
    private TextView textView_adress;
    private Button Btn_agregar;
    private Marcador marcador;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_markers);

        editText_name = findViewById(R.id.editText_name);
        textView_adress = findViewById(R.id.textView_adress);
        Btn_agregar = findViewById(R.id.Btn_agregar);

        marcador = (Marcador) getIntent().getExtras().getSerializable("marcador");

        textView_adress.setText(marcador.getDireccion());

        Btn_agregar.setOnClickListener(
                (v) -> {
                    if(editText_name.getText().toString()!=null) {
                        Intent i = new Intent();
                        marcador.setTitulo(editText_name.getText().toString());
                        i.putExtra("marcador", marcador);
                        setResult(RESULT_OK, i);
                        finish();
                    }
                }
        );


    }
}
