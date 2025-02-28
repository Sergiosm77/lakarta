package sarao.digital.lakarta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Info_Publicidad extends AppCompatActivity {

    TextView salir, irAregistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publi_info);

        salir=findViewById(R.id.salir);
        irAregistro=findViewById(R.id.registro);

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              finish();
            }
        });

        irAregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, irAregistro);

               irAregistro();

            }
        });

    }

    private void irAregistro(){

        Intent miIntent = new Intent(getApplication(), Registra_Empresa.class);

        startActivity(miIntent);

        finish();

    }



    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

}
