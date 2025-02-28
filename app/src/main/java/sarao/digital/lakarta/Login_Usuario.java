package sarao.digital.lakarta;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Login_Usuario extends AppCompatActivity {

    Controlador controlador=new Controlador();

    TextView botn_acceso, acceso_titulo,loginAlerta,nuevaCuenta,passOlvidada;
    EditText usuario, contra;
    ProgressBar barraProgreso;
    ImageView llave;
    private Toast mensajePop;
    String mensajeAlerta, cont_usuario,cont_contra, usuarioAnterior;

    Server_RecibeDatos recibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_usuario);

        usuario=findViewById(R.id.usuario);
        contra=findViewById(R.id.contra);
        botn_acceso=findViewById(R.id.boton_acceso);
        barraProgreso=findViewById(R.id.barraprogreso_login);
        llave=findViewById(R.id.imagen_llave);
        acceso_titulo =findViewById(R.id.titulo_acceso);
        loginAlerta=findViewById(R.id.login_alerta);
        nuevaCuenta=findViewById(R.id.nuevo_usuario);
        passOlvidada=findViewById(R.id.pass_olvidado);

        cargaUsuarioAnterior();

        if(!usuarioAnterior.equals("0")){

            usuario.setText(usuarioAnterior);
        }


        nuevaCuenta.setText(getResources().getText(R.string.nuevo_usuario));

        barraProgreso.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        recibe=new Server_RecibeDatos(getApplicationContext());

        acceso_titulo.setText(getResources().getText(R.string.acceso_usuario));

        usuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!loginAlerta.getText().equals("")){

                    loginAlerta.setText("");
                }

            }
        });

        usuario.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){

                    contra.requestFocus();
                    return true;
                }
                return false;
            }
        });

        contra.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    accede();

                    return true;
                }
                return false;
            }
        });

        contra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!loginAlerta.getText().equals("")){

                    loginAlerta.setText("");
                }

            }
        });

        nuevaCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, nuevaCuenta);

                Intent miIntent = new Intent(Login_Usuario.this, Registra_Usuario.class);

                startActivity(miIntent);

                activaBoton(true, nuevaCuenta);


            }
        });

        passOlvidada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, passOlvidada);

                veAcambioPass(passOlvidada);


            }
        });

        botn_acceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controlador.activaBoton(false, botn_acceso);

                accede();

            }
        });


    }

    private void veAcambioPass(View boton){

        Intent miIntent = new Intent(this, Cambia_Password.class);

        miIntent.putExtra("TIPOCAMBIOPASS","olvidado");
        miIntent.putExtra("QUIENCAMBIAPASS","usuario");
        miIntent.putExtra("EMAILCAMBIOPASS","");

        startActivity(miIntent);

        activaBoton(true, boton);

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

    private void accede(){

        loginAlerta.setText("");

        if(usuario.getEditableText().toString().equals("")){

            loginAlerta.setText(getString(R.string.intro_usuario));

            controlador.activaBoton(true,botn_acceso);

        }else {

            if(contra.getEditableText().toString().equals("")){

                loginAlerta.setText(getString(R.string.intro_contra));
                controlador.activaBoton(true,botn_acceso);

            }else {

                cont_usuario = usuario.getEditableText().toString();
                cont_contra = contra.getEditableText().toString();

                loginAlerta.setText("");

                validaUsuario inicia=new validaUsuario();
                inicia.execute();
            }

        }

    }

    private class validaUsuario extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            llave.setVisibility(View.GONE);
            barraProgreso.setVisibility(View.VISIBLE);
            acceso_titulo.setText(R.string.accediendo);

        }

        @Override

        protected String doInBackground(String... strings) {

            if(compruebaConexion()) {

                recibe.compruebaLoginUsuario(cont_usuario, cont_contra);

                int contador = 0;

                while (recibe.respuesta.equals("no") && contador < 10) {

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                return recibe.respuesta;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {

            System.out.println("RESPUESTA USUARIO onpost: "+resultado);

            if(resultado.equals("ok")){

                //guardaLoginUsuario(cont_usuario,cont_contra);


                // ---------- LOGIN CORRECTO ----------------------------------------

                    Intent miIntent = new Intent(Login_Usuario.this, Menu_Usuario.class);

                    Inicio.sesionUserReiniciada =true;

                    startActivity(miIntent);

                    finish();


            }else if(resultado.equals("sinconexion")){


                loginAlerta.setText(getString(R.string.sin_internet));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_usuario);

                controlador.activaBoton(true,botn_acceso);

            }else if(resultado.equals("nok")){

                loginAlerta.setText(getString(R.string.denegado));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_usuario);

                controlador.activaBoton(true,botn_acceso);

            }else{

                loginAlerta.setText(getString(R.string.error_conexion));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_usuario);

                controlador.activaBoton(true,botn_acceso);

            }

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

    @Override
    protected void onResume() {

        super.onResume();
    }

    private void cargaUsuarioAnterior(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        usuarioAnterior=guarda.getString("USUARIO_ANTERIOR","0");

    }
}