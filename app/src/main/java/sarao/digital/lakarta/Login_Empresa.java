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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Login_Empresa extends AppCompatActivity {

    Controlador controlador=new Controlador();

    TextView botn_acceso, acceso_titulo,loginAlerta,nuevaCuenta,passOlvidada;
    EditText usuario, contra;
    ProgressBar barraProgreso;
    ImageView llave;
    private Toast mensajePop;
    String mensajeAlerta, cont_usuario,cont_contra;

    String usuarioAnterior;

    Kartas[] laKartaNivel3,laKartaNivel2,laKartaNivel1;

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

        nuevaCuenta.setText(getResources().getText(R.string.nueva_empresa));

        barraProgreso.setVisibility(View.GONE);

        recibe=new Server_RecibeDatos(getApplicationContext());

        acceso_titulo.setText(getResources().getText(R.string.acceso_empresa));

        cargaEmpresaAnterior();

        if(!usuarioAnterior.equals("0")){

            usuario.setText(usuarioAnterior);
        }

        passOlvidada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, passOlvidada);

                veAcambioPass(passOlvidada);


            }
        });

        nuevaCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent miIntent = new Intent(Login_Empresa.this, Registra_Empresa.class);

                startActivity(miIntent);


            }
        });

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

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        botn_acceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controlador.activaBoton(false, botn_acceso);

                accede();

                //creaCodLogin();

            }
        });


    }

    private void accede(){

        loginAlerta.setText("");

        if(usuario.getText().toString().equals("")){

            loginAlerta.setText(getString(R.string.intro_usuario));

            controlador.activaBoton(true,botn_acceso);

        }else {

            if(contra.getText().toString().equals("")){

                loginAlerta.setText(getString(R.string.intro_contra));
                controlador.activaBoton(true,botn_acceso);

            }else {

                cont_usuario = usuario.getText().toString();
                cont_contra = contra.getText().toString();

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

                recibe.compruebaLoginEmpresa(cont_usuario, cont_contra);

                int contador = 0;

                while (recibe.respuesta.equals("no") && contador < 10) {

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                System.out.println("RESPUESTA PARA LOGIN: " + recibe.respuesta);

                return recibe.respuesta;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {

            if(resultado.equals("ok")){

                Inicio.sesionUserReiniciada =true;
                guardaLogin(cont_usuario,cont_contra);

                verMiusuario();

            }else if(resultado.equals("sinconexion")){


                loginAlerta.setText(getString(R.string.sin_internet));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_empresa);

                controlador.activaBoton(true,botn_acceso);

            }else if(resultado.equals("nok")){

                loginAlerta.setText(getString(R.string.denegado));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_empresa);

                controlador.activaBoton(true,botn_acceso);

            }else{

                loginAlerta.setText(getString(R.string.error_conexion));

                barraProgreso.setVisibility(View.GONE);
                llave.setVisibility(View.VISIBLE);
                acceso_titulo.setText(R.string.acceso_empresa);

                controlador.activaBoton(true,botn_acceso);

            }

        }
    }

    private void veAcambioPass(View boton){

        Intent miIntent = new Intent(this, Cambia_Password.class);

        miIntent.putExtra("TIPOCAMBIOPASS","olvidado");
        miIntent.putExtra("QUIENCAMBIAPASS","empresa");
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

    public void misCategorias(JSONArray datosNivel1){


        try{

            laKartaNivel1=new Kartas[datosNivel1.length()];

            for(int i=0;i<datosNivel1.length();i++) {

                laKartaNivel1[i] = new Kartas();

                JSONObject objectNivel1 = datosNivel1.getJSONObject(i);

                laKartaNivel1[i].cod_restaurante = objectNivel1.getString("cod_restaurante");

                laKartaNivel1[i].cod_nivel = objectNivel1.getString("cod_nivel1");
                laKartaNivel1[i].nombre_nivel = objectNivel1.getString("nivel1_nombre");
                laKartaNivel1[i].imagen_nivel = objectNivel1.getString("nivel1_imagen");
                laKartaNivel1[i].detalle_nivel = objectNivel1.getString("nivel1_detalle");
                laKartaNivel1[i].tipoMenu = Integer.parseInt(objectNivel1.getString("tipo_menu"));
                laKartaNivel1[i].orden_nivel = objectNivel1.getInt("nivel1_orden");
                if (!objectNivel1.getString("nivel1_precio").equals("null")) {
                    laKartaNivel1[i].precio_nivel = objectNivel1.getDouble("nivel1_precio");

                }

            }

        }catch (JSONException e){

            System.out.println("ERROR AL PONER PLATOS: "+e.getMessage());

        }

    }

    public void misMenus(JSONArray datosNivel2, JSONArray datosNivel3, JSONArray datosNivel4){


        try{

            laKartaNivel2=new Kartas[datosNivel2.length()];

            for(int i=0;i<datosNivel2.length();i++) {

                laKartaNivel2[i] = new Kartas();

                JSONObject objectNivel2 = datosNivel2.getJSONObject(i);

                laKartaNivel2[i].cod_restaurante = objectNivel2.getString("cod_restaurante");

                laKartaNivel2[i].cod_nivel_sup = objectNivel2.getString("cod_nivel1");
                laKartaNivel2[i].cod_nivel = objectNivel2.getString("cod_nivel2");
                laKartaNivel2[i].nombre_nivel = objectNivel2.getString("nivel2_nombre");
                laKartaNivel2[i].imagen_nivel = objectNivel2.getString("nivel2_imagen");
                laKartaNivel2[i].detalle_nivel = objectNivel2.getString("nivel2_detalle");
                laKartaNivel2[i].orden_nivel = objectNivel2.getInt("nivel2_orden");
                laKartaNivel2[i].esmenu = objectNivel2.getInt("esmenu");
                if (!objectNivel2.getString("nivel2_precio").equals("null")) {
                    laKartaNivel2[i].precio_nivel = objectNivel2.getDouble("nivel2_precio");

                }

            }

        }catch (JSONException e){

            System.out.println("ERROR AL PONER PLATOS: "+e.getMessage());

        }

        if(datosNivel3.length()>0) {

            try {

                laKartaNivel3 = new Kartas[datosNivel3.length()];

                JSONObject objectNivel3;

                for (int i = 0; i < datosNivel3.length(); i++) {

                    laKartaNivel3[i] = new Kartas();

                    objectNivel3 = datosNivel3.getJSONObject(i);

                    laKartaNivel3[i].cod_restaurante = objectNivel3.getString("cod_restaurante");

                    laKartaNivel3[i].cod_nivel = objectNivel3.getString("cod_nivel3");
                    laKartaNivel3[i].cod_nivel_sup = objectNivel3.getString("cod_nivel2");
                    laKartaNivel3[i].nombre_nivel = objectNivel3.getString("nivel3_nombre");
                    laKartaNivel3[i].imagen_nivel = objectNivel3.getString("nivel3_imagen");
                    laKartaNivel3[i].detalle_nivel = objectNivel3.getString("nivel3_detalle");
                    laKartaNivel3[i].cantidad_nivel = objectNivel3.getInt("nivel3_cantidad");
                    if (!objectNivel3.getString("nivel3_precio").equals("null")) {
                        laKartaNivel3[i].precio_nivel = objectNivel3.getDouble("nivel3_precio");

                    }

                    int contador = 0;

                    for (int e = 0; e < datosNivel4.length(); e++) {

                        JSONObject objectNivel4 = datosNivel4.getJSONObject(e);

                        if (laKartaNivel3[i].cod_nivel.equals(objectNivel4.getString("cod_nivel3"))) {

                            contador++;
                        }

                    }

                    laKartaNivel3[i].cod_subnivel=new String[contador];
                    laKartaNivel3[i].nombre_subnivel=new String[contador];
                    laKartaNivel3[i].imagen_subnivel=new String[contador];
                    laKartaNivel3[i].detalle_subnivel=new String[contador];
                    laKartaNivel3[i].precio_subnivel=new double[contador];
                    laKartaNivel3[i].visible=new int[contador];

                    contador=0;

                    for (int e = 0; e < datosNivel4.length(); e++) {

                        JSONObject objectNivel4 = datosNivel4.getJSONObject(e);

                        if (laKartaNivel3[i].cod_nivel.equals(objectNivel4.getString("cod_nivel3"))) {

                            laKartaNivel3[i].cod_subnivel[contador]=objectNivel4.getString("cod_nivel4");
                            laKartaNivel3[i].nombre_subnivel[contador]=objectNivel4.getString("nivel4_nombre");
                            laKartaNivel3[i].imagen_subnivel[contador]=objectNivel4.getString("nivel4_imagen");
                            laKartaNivel3[i].detalle_subnivel[contador]=objectNivel4.getString("nivel4_detalle");
                            if(!objectNivel4.getString("nivel4_precio").equals("null")) {
                                laKartaNivel3[i].precio_subnivel[contador]=objectNivel4.getDouble("nivel4_precio");

                            }
                            laKartaNivel3[i].visible[contador]=objectNivel4.getInt("visible");
                            contador++;

                        }

                    }

                }

            } catch (JSONException e) {

                System.out.println("ERROR AL PONER PLATOS OP: " + e.getMessage());

            }
        }else{
            laKartaNivel3 = new Kartas[0];

        }

    }

    public void verMiusuario(){

        Intent miIntent = new Intent(Login_Empresa.this, Menu_Empresa.class);

        startActivity(miIntent);

        finish();

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

    public void guardaLogin(String user, String pass){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor mieditor=guarda.edit();

            mieditor.putString("USER_EMPRESA", user);
            mieditor.putString("PASS_EMPRESA", pass);

        mieditor.apply();

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

    private void creaCodLogin(){

        Random random = new Random();
        StringBuilder codigo= new StringBuilder();
        char n;
        int contador=0;

        while(contador<20) {

            n=((char)random.nextInt(255));

            if(Character.toUpperCase((char) n) >= 'A'
                    && Character.toUpperCase((char) n) <= 'Z'){

                codigo.append(n);
                contador++;

            }

        }

        contador=0;

        StringBuilder numeros= new StringBuilder();

        String alphabet = "0123456789ABCDEFGHIJ";
        String desplaza="23456789";
        int cuantos=alphabet.length();
        int cuantosDespl=desplaza.length();

        while(contador<20) {

            n=alphabet.charAt(random.nextInt(cuantos));

            if(!numeros.toString().contains(String.valueOf(n))){

                numeros.append(n);
                codigo.append(n);
                contador++;

            }

        }

        n=desplaza.charAt(random.nextInt(cuantosDespl));

        codigo.append(n);

        System.out.println(codigo.toString());

        codifica("hola", codigo.toString());
        codifica("password", codigo.toString());

    }

    private void codifica(String palabra, String clave){

        String aleatorio=clave.substring(0,20);
        String orden=clave.substring(20,40);
        int desfase=Integer.parseInt(clave.substring(40,41));

        System.out.println("PRIMERO "+aleatorio+" SEGUNDO "+orden+" DESFASE "+desfase);

        StringBuilder codificado=new StringBuilder();
        int posicion;
        for(int i=0;i<20;i++) {
            char caracter = orden.charAt(i);
            if(caracter=='A'){

                posicion=10;

            }else if(caracter=='B'){

                posicion=11;
            }else if(caracter=='C'){

                posicion=12;
            }else if(caracter=='D'){

                posicion=13;
            }else if(caracter=='E'){

                posicion=14;
            }else if(caracter=='F'){

                posicion=15;
            }else if(caracter=='G'){

                posicion=16;
            }else if(caracter=='H'){

                posicion=17;
            }else if(caracter=='I'){

                posicion=18;
            }else if(caracter=='J'){

                posicion=19;
            }else{

                posicion=Integer.parseInt(String.valueOf(caracter));
            }

            if(palabra.length()<posicion+1){

                codificado.append(aleatorio.charAt(posicion));

            }else{

                codificado.append((char)((int)palabra.charAt(posicion)+desfase));

            }

        }

        System.out.println("CODIFICADO "+palabra+": "+codificado);

    }

    private void cargaEmpresaAnterior(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        usuarioAnterior=guarda.getString("EMPRESA_ANTERIOR","0");

    }



}