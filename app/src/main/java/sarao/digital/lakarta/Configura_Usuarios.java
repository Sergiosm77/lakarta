package sarao.digital.lakarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Configura_Usuarios extends AppCompatActivity {

    Context contexto;

    ProgressBar barraProgreso;
    private LayoutInflater inflador;

    LinearLayout cajaDistancia;

    EditText cuantosKilometros;

    RadioGroup grupoPoderComentar ,grupoDesdeDonde;

    TextView salir, enviarCambios;

    String user, alses, alsesk;

    private Toast mensajePop;
    private String mensajeAlerta="";

    Server_EnvioDatos serverEnviaDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_usuarios);

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        salir=findViewById(R.id.salir);
        enviarCambios=findViewById(R.id.enviar_cambios);
        cuantosKilometros=findViewById(R.id.cantidad_km);
        cajaDistancia=findViewById(R.id.caja_dsede_donde);
        grupoPoderComentar=findViewById(R.id.grupo_permitir);
        grupoDesdeDonde=findViewById(R.id.grupo_desde_donde);

        barraProgreso = findViewById(R.id.barraprogreso_usuario);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        contexto=this;

        barraProgreso.setVisibility(View.GONE);

        cuantosKilometros.setVisibility(View.GONE);

        cargaUserEmpresa();

        serverEnviaDatos =new Server_EnvioDatos(null,this,this,inflador);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        activaBoton(false, enviarCambios);

        cargaPantalla();

    }

    private void cargaPantalla(){

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,salir);

                finish();

            }
        });

        enviarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, enviarCambios);

                int puede=0, desde=0;

                if(grupoPoderComentar.getCheckedRadioButtonId()==R.id.radioButton0){

                    puede=1;
                }

                if(puede==1){

                    if(grupoDesdeDonde.getCheckedRadioButtonId()==R.id.radioButton2){

                        desde=0;
                    }else if(grupoDesdeDonde.getCheckedRadioButtonId()==R.id.radioButton3){

                        desde=1;

                    }else{

                        if(Integer.parseInt(cuantosKilometros.getText().toString())>0){

                            desde=Integer.parseInt(cuantosKilometros.getText().toString())+100;

                        }else{

                            mensajeAlerta=getResources().getString(R.string.pon_distancia_mayor_cero);
                            ponAlerta();

                        }

                    }
                }
                if(compruebaConexion()) {
                    cargaUserEmpresa();
                    serverEnviaDatos.enviaCambiosConfigUser(user, alses, alsesk, puede, desde);
                }else{

                    mensajeAlerta=getResources().getString(R.string.sin_internet);
                    ponAlerta();
                    activaBoton(true, enviarCambios);
                }

            }
        });

        if(Menu_Empresa.miRestaurante.permiteComentarios==1) {

            grupoPoderComentar.check(R.id.radioButton0);

            cuantosKilometros.setVisibility(View.GONE);

            if(Menu_Empresa.miRestaurante.desdeDondeComentarios==0) {

                grupoDesdeDonde.check(R.id.radioButton2);

            }else if(Menu_Empresa.miRestaurante.desdeDondeComentarios==1){

                grupoDesdeDonde.check(R.id.radioButton3);

            }else{

                grupoDesdeDonde.check(R.id.radioButton4);
                if(Menu_Empresa.miRestaurante.desdeDondeComentarios>100){

                    cuantosKilometros.setVisibility(View.VISIBLE);

                    cuantosKilometros.setText(String.valueOf(Menu_Empresa.miRestaurante.desdeDondeComentarios-100));

                }

            }

        }else{

            grupoPoderComentar.check(R.id.radioButton1);
            cajaDistancia.setVisibility(View.GONE);
        }



        grupoPoderComentar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radioButton0:

                       cajaDistancia.setVisibility(View.VISIBLE);
                        grupoDesdeDonde.setVisibility(View.VISIBLE);
                        grupoDesdeDonde.check(R.id.radioButton2);
                        cuantosKilometros.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton1:

                        cajaDistancia.setVisibility(View.GONE);
                        grupoDesdeDonde.setVisibility(View.GONE);
                        cuantosKilometros.setVisibility(View.GONE);
                        break;

                }

                activaBoton(true, enviarCambios);

            }
        });

        grupoDesdeDonde.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radioButton2:

                        cuantosKilometros.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton3:

                        cuantosKilometros.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton4:

                        System.out.println("PONE");

                        cuantosKilometros.setVisibility(View.VISIBLE);
                        break;

                }

                activaBoton(true, enviarCambios);

            }
        });

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

    private void ponAlerta(){


        try {
            if (!mensajePop.getView().isShown()) {

                mensajePop.setText(mensajeAlerta);

                mensajePop.setGravity(Gravity.CENTER, 0, 0);
                //TextView mensaje = mensajePop.getView().findViewById(android.R.id.message);
                //mensaje.setGravity(Gravity.CENTER);

                mensajePop.show();

            }

        }catch (Exception e){

            mensajePop.setText(mensajeAlerta);

            mensajePop.show();
        }

    }


    public boolean compruebaConexion(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo estadoRed = connectivityManager.getActiveNetworkInfo();

        if (estadoRed == null || !estadoRed.isConnected()) {

            return false;

        }else{

            return true;
        }


    }

    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


    }


}

