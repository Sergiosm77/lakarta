package sarao.digital.lakarta;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.CALL_PHONE;
import static android.widget.Toast.makeText;

public class Info_Restaurante extends AppCompatActivity {

    Restaurantes queRestaurante;

    ImageView imagenRestaurante, logoRestaurante,ponInfo, ponComentarios, daleLike;

    final private int cantidadComentariosMostrar=20;
    private int comentariosYaCargados;

    private long mLastClickTime = 0;

    BottomNavigationView barraBottom;

    boolean like=false;

    int posScroll;

    EditText elComentario;
    TextView enviarComentario,cancelaRespuesta, contaComent,contaLike;

    String enviaCodCom="";
    String quePagina;

    String nombreEmpresa, miCodRest;

    JSONArray losComentarios, lasRespuestas;

    Herramientas herramientas;

    // ---------- comentarios layout ----

    View contenido;

    LinearLayout contenedorComentarios,contenedorRespuestasEnvio, verMasComentarios;

    // -------------------

    LinearLayout cajaComentario, barraProgreso;
    LinearLayout contendorInfo;
    Location migps;

    private DecimalFormat formato=new DecimalFormat("0.00");

    LinearLayout llamar;

    InputMethodManager inputManager;

    LayoutInflater inflater;

    TextView nombreRestaurante,irAkarta, noComment;

    NestedScrollView scrollInfo;

    Usuario miUsuario;
    String user, pass;
    String userEmpresa, passEmpresa;

    Server_ComentLikes enviaORecibeComentario;

    Server_RecibeDatos serverRecibeDatos;

    private Toast mensajePop;
    private String mensajeAlerta="";

    int posicionRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_rest);

        Bundle miBundle=this.getIntent().getExtras();

        migps=new Location("");

        if(miBundle!=null) {

            queRestaurante = miBundle.getParcelable("QUERESTAURANTE");
            quePagina=miBundle.getString("PON_PAGINA");
            System.out.println("COGE LONGITUD: "+ miBundle.getDouble("LONGITUD"));
            migps.setLatitude(miBundle.getDouble("LATITUD"));
            migps.setLongitude(miBundle.getDouble("LONGITUD"));
            posicionRest=miBundle.getInt("POSICION_REST");

        }else{

            finish();
        }

        cargaEmpresaGuardado();

        inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        barraBottom=findViewById(R.id.bottom_bar);

        imagenRestaurante=findViewById(R.id.info_rest_imagen);
        logoRestaurante=findViewById(R.id.logo_usuario);
        irAkarta=findViewById(R.id.info_ver_karta);
        nombreRestaurante=findViewById(R.id.info_rest_nombre);
        ponInfo=findViewById(R.id.pon_info);
        daleLike=findViewById(R.id.like);
        ponComentarios=findViewById(R.id.pon_comentarios);
        scrollInfo=findViewById(R.id.scroll_info);
        noComment=findViewById(R.id.no_comment);
        cajaComentario=findViewById(R.id.caja_comentario);
        cancelaRespuesta=findViewById(R.id.cancela_respuesta);
        contaComent=findViewById(R.id.cuantos_coment);
        contaLike=findViewById(R.id.cuantos_like);

        enviarComentario=findViewById(R.id.enviar_comentario);

        elComentario=findViewById(R.id.el_comentario);

        enviarComentario.setEnabled(false);
        enviarComentario.setAlpha(0.4f);

        contendorInfo=findViewById(R.id.contenedor_info);



        llamar=findViewById(R.id.info_llamar);

        inflater = getLayoutInflater();

        mensajePop = Toast.makeText(this, mensajeAlerta, Toast.LENGTH_SHORT);

        enviaORecibeComentario =new Server_ComentLikes(this, inflater);

        serverRecibeDatos =new Server_RecibeDatos(this);

        miUsuario=new Usuario();

        cargaUsuarioGuardado();



        //miUsuario= serverRecibeDatos.miUsuario;

        Glide.with(getApplicationContext())
                .load(queRestaurante.imagen_principal)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.noimage)
                .into(imagenRestaurante);

        Glide.with(getApplicationContext())
                .load(queRestaurante.logo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.user)
                .into(logoRestaurante);

        // --------------- COMENTARIOS -------------------------------

        if(queRestaurante.permiteComentarios==1) {
            contaComent.setText(String.valueOf(queRestaurante.contaComentario));

            ponComentarios.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    barraBottom.setVisibility(View.VISIBLE);

                    quitarTinte();

                    ponComentarios.setColorFilter(Color.BLUE);

                    ponComentarios();


                }
            });
        }else{

            contaComent.setVisibility(View.GONE);
            ponComentarios.setVisibility(View.GONE);
            noComment.setVisibility(View.GONE);
            cajaComentario.setVisibility(View.GONE);
        }

        // ----------------------------------------------------------

        elComentario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){



                        enviarComentario.setEnabled(true);
                        enviarComentario.setAlpha(1f);
                }else{

                        enviarComentario.setEnabled(false);
                        enviarComentario.setAlpha(0.4f);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        nombreRestaurante.setText(queRestaurante.nombre);

        contaLike.setText(String.valueOf(queRestaurante.contaLike));


        cancelaRespuesta.setVisibility(View.GONE);

        irAkarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, irAkarta);

                Ira_Lakarta(queRestaurante);

            }
        });

        if(queRestaurante.telefono!=0){

            llamar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent llama = new Intent(Intent.ACTION_CALL);
                    llama.setData(Uri.parse("tel:"+queRestaurante.telefono));

                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{CALL_PHONE}, 1);

                        // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    } else {
                        //You already have permission
                        try {
                            startActivity(llama);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

        }else{

            llamar.setVisibility(View.GONE);
        }

        ponInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // ---- restaura cometnarios --------------
                barraBottom.setVisibility(View.GONE);
                elComentario.setText("");
                String textoRespuesta=getResources().getString(R.string.escribe_comentario);
                elComentario.setHint(textoRespuesta);
                enviaCodCom="";

                quitarTinte();

                ponInfo.setColorFilter(Color.BLUE);

                ponInfo();

            }
        });

        noComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("NO COMMENT "+user+" "+miUsuario.puede_comentar);

                if( user==null || user.equals("0") || miUsuario.puede_comentar==1){

                   irALogin_Usuario();
                }

            }
        });

        daleLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                enviaLike(posicionRest);

            }
        });

        enviarComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, enviarComentario);

                boolean compruebaDistancia=false;

                if(queRestaurante.desdeDondeComentarios>0){

                    compruebaDistancia=true;

                }

                if(!compruebaDistancia) {

                    enviaComentario();


                }else{

                    if(compruebaPermisoGPS(enviarComentario)){

                        if(migps.getLatitude()==0) {
                            enviaCuandoGPS(enviarComentario);
                        }else{

                            Location locRest=new Location("");
                            locRest.setLatitude(Double.parseDouble(queRestaurante.latitud));
                            locRest.setLongitude(Double.parseDouble(queRestaurante.longitud));

                            System.out.println("DISTANCIA: "+migps.distanceTo(locRest));

                            int distancia;

                            if(queRestaurante.desdeDondeComentarios==1){

                                distancia=100;

                            }else{

                                distancia=(queRestaurante.desdeDondeComentarios-100)*1000;

                            }

                            if(migps.distanceTo(locRest)<distancia){

                                enviaComentario();

                            }else{
                                mensajeAlerta="ESTAS DEMASIADO LEJOS";
                                ponAlerta();
                                activaBoton(true, enviarComentario);

                            }
                        }

                    }

                }

            }
        });

        cancelaRespuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelaRespuesta.setVisibility(View.GONE);

                inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                String textoRespuesta=getResources().getString(R.string.escribe_comentario);
                elComentario.setHint(textoRespuesta);
                elComentario.setText("");
                enviaCodCom="";
            }
        });



        if(quePagina.equals("info")) {

            ponInfo.callOnClick();

        }else if(quePagina.equals("comentarios")){

            ponComentarios.callOnClick();

        }

    }

    @Override
    protected void onResume() {

        cargaUserEmpresa();
        cargaUserUsuario();

        if((user.equals("0") || miUsuario.puede_comentar==2) && (userEmpresa.equals("0") || !miCodRest.equals(queRestaurante.codigo))){

            cajaComentario.setVisibility(View.GONE);
            noComment.setVisibility(View.VISIBLE);

            if(miUsuario.puede_comentar==2){

                noComment.setText(getResources().getString(R.string.no_puedes_comentar));
            }
        }else{

            cajaComentario.setVisibility(View.VISIBLE);
            noComment.setVisibility(View.GONE);

        }


        if(Inicio.misLikes!=null && Inicio.misLikes.length()>0){

            try {
                for (int i = 0; i < Inicio.misLikes.length(); i++) {

                    if (queRestaurante.codigo.equals(Inicio.misLikes.get(i))){

                        System.out.println("COINCIDE: "+queRestaurante.codigo+" "+Inicio.misLikes.get(i));


                        daleLike.setImageResource(R.drawable.like_on);
                        like=true;
                        break;
                    }
                }
            }catch (Exception e){

                System.out.println("ERROR AL POCER LIKE: "+e.getMessage());
            }
        }

        super.onResume();
    }

    public void enviaLike(int posicion) {

        if(!user.equals("0")) {

            if(compruebaConexion()) {

                if (like) {

                    enviaORecibeComentario.enviaLike(user, queRestaurante.codigo, daleLike, "quita");
                    daleLike.setImageResource(R.drawable.like_off);
                    quitaLikeLocal(queRestaurante.codigo);
                    like = false;


                } else {

                    enviaORecibeComentario.enviaLike(user, queRestaurante.codigo, daleLike, "pon");
                    daleLike.setImageResource(R.drawable.like_on);
                    guardaLikeLocal(queRestaurante.codigo);
                    like = true;


                }
            }else{

                mensajeAlerta =this.getResources().getString(R.string.sin_internet);
                ponAlerta();

            }

        }else{

            irALogin_Usuario();
        }

        System.out.println("ENVIA LIKE");

    }

    public void enviaComentario() {

        if (elComentario.getHint().equals(getResources().getString(R.string.escribe_comentario))) {

            enviaCodCom = "";
        }

        if (elComentario.getText().length() > 0) {

            if(enviaCodCom.equals("")) {

                if(!userEmpresa.equals("0") && user.equals("0")){

                    if(miCodRest.equals(queRestaurante.codigo)) {
                        empresaEnviaComentario();
                    }

                }else if(userEmpresa.equals("0") && !user.equals("0")){

                    usuarioEnviaComentario();

                }else if(!userEmpresa.equals("0") && !user.equals("0")){

                    if(miCodRest.equals(queRestaurante.codigo)) {
                        preguntaAquien("comentario");
                    }else{
                        usuarioEnviaComentario();
                    }
                }



            }else{

                if(!userEmpresa.equals("0") && user.equals("0")){

                    if(miCodRest.equals(queRestaurante.codigo)) {
                        empresaEnviaRespuesta();
                    }

                }else if(userEmpresa.equals("0") && !user.equals("0")){

                    usuarioEnviaRespuesta();

                }else if(!userEmpresa.equals("0") && !user.equals("0")){

                    if(miCodRest.equals(queRestaurante.codigo)) {
                        preguntaAquien("respuesta");
                    }else{

                        usuarioEnviaRespuesta();
                    }
                }

            }

            inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }



        System.out.println("ENVIA COMENTARIO");

    }

    private void usuarioEnviaComentario(){

        if(compruebaConexion()) {

            enviaORecibeComentario.enviaComentario(user, miUsuario.avatar,
                    elComentario.getText().toString(), "normal",
                    queRestaurante.codigo, "", elComentario, contenedorComentarios,
                    contenedorRespuestasEnvio, cancelaRespuesta, scrollInfo, 0);
        }else{

            mensajeAlerta = getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    private void usuarioEnviaRespuesta(){

        if(compruebaConexion()) {
            enviaORecibeComentario.enviaComentario(user, miUsuario.avatar,
                    elComentario.getText().toString(), "respuesta",
                    queRestaurante.codigo, enviaCodCom, elComentario,
                    contenedorComentarios,contenedorRespuestasEnvio,cancelaRespuesta,scrollInfo,posScroll);
        }else{

            mensajeAlerta = getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    private void empresaEnviaComentario(){

        if(compruebaConexion()) {

            enviaORecibeComentario.enviaComentarioEmpresa(userEmpresa,
                    elComentario.getText().toString(), "normal",
                    queRestaurante.codigo, "", elComentario, contenedorComentarios,
                    contenedorRespuestasEnvio, cancelaRespuesta, scrollInfo, 0);
        }else{

            mensajeAlerta = getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    private void empresaEnviaRespuesta(){

        if(compruebaConexion()) {
            enviaORecibeComentario.enviaComentarioEmpresa(userEmpresa,
                    elComentario.getText().toString(), "respuesta",
                    queRestaurante.codigo, enviaCodCom, elComentario,
                    contenedorComentarios,contenedorRespuestasEnvio,cancelaRespuesta,scrollInfo,posScroll);
        }else{

            mensajeAlerta = getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    public void ponComentarios() {

        comentariosYaCargados=0;

        contendorInfo.removeAllViews();
        scrollInfo.scrollTo(0,0);

        contenido = inflater.inflate(R.layout.info_comentarios, null);

        contenedorComentarios = contenido.findViewById(R.id.contenedor_comentarios);
        verMasComentarios=contenido.findViewById(R.id.ver_mas_comentarios);
        contendorInfo.addView(contenido);
        barraProgreso=contenido.findViewById(R.id.barraprogreso);
        verMasComentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                masComentarios();
            }
        });

        recibeComentarios inicia=new recibeComentarios();
        inicia.execute();

    }

    private class recibeComentarios extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            barraProgreso.setVisibility(View.VISIBLE);
            verMasComentarios.setVisibility(View.GONE);
            activaBoton(false, ponInfo);
            activaBoton(false, daleLike);

        }

        @Override

        protected String doInBackground(String... strings) {

            if(compruebaConexion()) {

                enviaORecibeComentario.recibeComentarios(queRestaurante.codigo);

                int contador = 0;

                while (enviaORecibeComentario.recibido.equals("no") && contador < 10) {

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                System.out.println("RESPUESTA PARA LOGIN: " + enviaORecibeComentario.recibido);

                return enviaORecibeComentario.recibido;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);
            activaBoton(true, ponInfo);
            activaBoton(true, daleLike);



            if(resultado.equals("ok")){

                //enviaORecibeComentario.cargaComentariosGuardados();
                //comentariosEste=enviaORecibeComentario.comentariosRest;
                losComentarios=enviaORecibeComentario.losComentarios;
                lasRespuestas=enviaORecibeComentario.lasRespuestas;

                if(losComentarios!=null){

                    contaComent.setText(String.valueOf(losComentarios.length()));
                }

              masComentarios();


            }else if(resultado.equals("vacio")){

                mensajeAlerta = getString(R.string.sin_comentarios);
                ponAlerta();

            }else if(resultado.equals("sinconexion")){

                mensajeAlerta = getString(R.string.sin_internet);
                ponAlerta();

            }else{

                mensajeAlerta = getString(R.string.error_conexion);
                ponAlerta();

            }

        }
    }

    private void masComentarios(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Date fechaHoy=new Date();

        String diaHoy=sdf.format(fechaHoy);


        int hastaQueComentario;

        if (losComentarios.length() <= cantidadComentariosMostrar) {

            verMasComentarios.setVisibility(View.GONE);
            hastaQueComentario=losComentarios.length();

        }else{

            verMasComentarios.setVisibility(View.VISIBLE);
            hastaQueComentario=comentariosYaCargados+cantidadComentariosMostrar;
        }


            if (losComentarios.length() > 0) {


                for (int i = comentariosYaCargados; i < hastaQueComentario; i++) {

                    String esteUsuario, codigoComentario, fechaComentario, esteComentario, codAvatar;

                    try{

                        esteUsuario = losComentarios.getJSONObject(i).getString("usuario");
                        codigoComentario = losComentarios.getJSONObject(i).getString("cod_comentario");
                        fechaComentario = losComentarios.getJSONObject(i).getString("fecha");
                        esteComentario=losComentarios.getJSONObject(i).getString("comentario");
                        codAvatar=losComentarios.getJSONObject(i).getString("avatar");

                    }catch (Exception e){

                        verMasComentarios.setVisibility(View.GONE);
                        System.out.println("NO HAY MAS COMENTARIOS "+e.getMessage());
                        break;

                    }

                    View comentarios = inflater.inflate(R.layout.unidad_comentario, null);

                    TextView usuario = comentarios.findViewById(R.id.nombre_usuario);
                    ImageView avatar=comentarios.findViewById(R.id.avatar);
                    TextView comentario = comentarios.findViewById(R.id.comentario_usuario);
                    TextView cuando = comentarios.findViewById(R.id.cuando);
                    TextView tituloRespuestas = comentarios.findViewById(R.id.titulo_respuestas);
                    TextView ponRespuesta = comentarios.findViewById(R.id.responder_comentario);
                    CardView fondoAvatar=comentarios.findViewById(R.id.fondo_avatar);

                    if(!codAvatar.equals("no") && codAvatar.length()>6){

                        try{

                            if(codAvatar.startsWith("chico")){

                                avatar.setImageResource(R.drawable.boy);

                                avatar.setColorFilter((Integer.parseInt(codAvatar.substring(5,14))-1000000000));
                                fondoAvatar.setCardBackgroundColor(Integer.parseInt(codAvatar.substring(14))-1000000000);

                            }else if(codAvatar.startsWith("chica")){

                                avatar.setImageResource(R.drawable.girl);
                                avatar.setColorFilter((Integer.parseInt(codAvatar.substring(5,14))-1000000000));
                                fondoAvatar.setCardBackgroundColor(Integer.parseInt(codAvatar.substring(14))-1000000000);

                            }else if(codAvatar.startsWith("http")){

                                Glide.with(getApplicationContext())
                                        .load(codAvatar)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .error(R.drawable.user)
                                        .into(avatar);

                                usuario.setTextColor(Color.BLUE);

                            }

                        }catch (Exception e){
                            System.out.println("ERROR EN AVATAR ");
                        }
                    }

                    final LinearLayout contenedorRespuestas = comentarios.findViewById(R.id.contenedor_respuestas);

                    if (!fechaComentario.equals("null") && fechaComentario != null) {

                        String texto = dimeCuando(fechaComentario, diaHoy);
                        cuando.setText(texto);

                    } else {

                        cuando.setText("");
                    }

                    usuario.setText(esteUsuario);
                    comentario.setText(esteComentario);

                    final String respondoAusuario=esteUsuario;
                    final String resppondoAcodigo=codigoComentario;

                    ponRespuesta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();

                            cancelaRespuesta.setVisibility(View.VISIBLE);


                            barraBottom.animate().translationY(0).setDuration(200);

                            elComentario.requestFocus();
                            //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            //imm.showSoftInput(elComentario, InputMethodManager.SHOW_IMPLICIT);
                            String textoRespuesta = getResources().getString(R.string.escribe_respuesta) + " " + respondoAusuario;
                            elComentario.setHint(textoRespuesta);
                            enviaCodCom = resppondoAcodigo;
                            contenedorRespuestasEnvio = contenedorRespuestas;
                            posScroll = scrollInfo.getScrollY();
                            System.out.println("PONE COD COMENTARIO: " + enviaCodCom);
                        }
                    });


                    if (lasRespuestas.length() > 0) {

                        int contador = 0;

                        String textoTitulo = getResources().getString(R.string.escribe_respuestas) + " " + esteUsuario;
                        tituloRespuestas.setText(textoTitulo);

                        for (int r = 0; r < lasRespuestas.length(); r++) {

                            String codComentario,codRespuesta, nombreUsuario, respuestaUsuario, fechaRespuesta, respAvatar;

                            try{

                                codComentario=losComentarios.getJSONObject(i).getString("cod_comentario");
                                codRespuesta=lasRespuestas.getJSONObject(r).getString("cod_comentario");
                                nombreUsuario=lasRespuestas.getJSONObject(r).getString("usuario");
                                respuestaUsuario=lasRespuestas.getJSONObject(r).getString("comentario");
                                fechaRespuesta=lasRespuestas.getJSONObject(r).getString("fecha");
                                respAvatar=lasRespuestas.getJSONObject(r).getString("avatar");

                            }catch (Exception e){

                                System.out.println("NO HAY MAS RESPUESTAS "+e.getMessage());
                                break;
                            }

                            if (codComentario.equals(codRespuesta)) {

                                View respuesta = inflater.inflate(R.layout.unidad_comentario_respuesta, null);

                                TextView respUsuario = respuesta.findViewById(R.id.nombre_usuario);
                                TextView respComentario = respuesta.findViewById(R.id.comentario_usuario);
                                TextView cuandoRespuesta = respuesta.findViewById(R.id.cuando);
                                ImageView avatarRespuesta=respuesta.findViewById(R.id.avatar);
                                CardView fondoAvatarResp=respuesta.findViewById(R.id.fondo_avatar);

                                respUsuario.setText(nombreUsuario);
                                respComentario.setText(respuestaUsuario);

                                if(respAvatar.startsWith("chico")){

                                    avatarRespuesta.setImageResource(R.drawable.boy);
                                    avatarRespuesta.setColorFilter((Integer.parseInt(respAvatar.substring(5,14))-1000000000));
                                    fondoAvatarResp.setCardBackgroundColor(Integer.parseInt(respAvatar.substring(14))-1000000000);

                                }else if(respAvatar.startsWith("chica")){

                                    avatarRespuesta.setImageResource(R.drawable.girl);
                                    avatarRespuesta.setColorFilter((Integer.parseInt(respAvatar.substring(5,14))-1000000000));
                                    fondoAvatarResp.setCardBackgroundColor(Integer.parseInt(respAvatar.substring(14))-1000000000);

                                }else if(respAvatar.startsWith("http")){

                                    Glide.with(getApplicationContext())
                                            .load(respAvatar)
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .error(R.drawable.user)
                                            .into(avatarRespuesta);

                                    respUsuario.setTextColor(Color.BLUE);

                                }

                                if (!fechaRespuesta.equals("null") && fechaRespuesta != null) {

                                    String texto = dimeCuando(fechaRespuesta, diaHoy);
                                    cuandoRespuesta.setText(texto);

                                } else {

                                    cuandoRespuesta.setText("");
                                }

                                contador++;
                                contenedorRespuestas.addView(respuesta);

                            }
                        }

                        if (contador == 0) {

                            tituloRespuestas.setVisibility(View.GONE);

                        }

                    } else {

                        tituloRespuestas.setVisibility(View.GONE);
                    }

                    contenedorComentarios.addView(comentarios);
                }

                if(losComentarios.length()>comentariosYaCargados){

                    comentariosYaCargados=comentariosYaCargados+cantidadComentariosMostrar;
                }
            }else{

                mensajeAlerta = getString(R.string.sin_comentarios);
                ponAlerta();
            }


    }

    private String dimeCuando(String fechaComentario, String diaHoy){

        String cuando="";

        try {

            //Timestamp laFechaComentario=Timestamp.valueOf(fechaComentario);
            //Timestamp elDiaHoy=Timestamp.valueOf(diaHoy);

            long laFechaComentario=Timestamp.valueOf(fechaComentario).getTime();
            long elDiaHoy=Timestamp.valueOf(diaHoy).getTime();

            if(TimeUnit.MILLISECONDS.toDays(elDiaHoy-laFechaComentario)<30){

                if(TimeUnit.MILLISECONDS.toHours(elDiaHoy-laFechaComentario)<24){

                    if(TimeUnit.MILLISECONDS.toMinutes(elDiaHoy-laFechaComentario)<60){

                        if(TimeUnit.MILLISECONDS.toMinutes(elDiaHoy-laFechaComentario)>3) {
                            cuando=getString(R.string.hace)+" "+TimeUnit.MILLISECONDS.toMinutes(elDiaHoy-laFechaComentario)+" "+getString(R.string.minutos);

                        }else{

                            cuando=getString(R.string.un_momento);

                        }

                    }else{

                        cuando=getString(R.string.hace)+" "+TimeUnit.MILLISECONDS.toHours(elDiaHoy-laFechaComentario)+" "+getString(R.string.horas);

                    }

                }else{

                    cuando=getString(R.string.hace)+" "+TimeUnit.MILLISECONDS.toDays(elDiaHoy-laFechaComentario)+" "+getString(R.string.dias);
                }
            }else{

                cuando=getString(R.string.hace)+" "+(TimeUnit.MILLISECONDS.toDays(elDiaHoy-laFechaComentario)/30)+" "+getString(R.string.meses);


            }



        }catch (Exception e){
            System.out.println("ERROR FECHA "+e.getCause());

        }

        return cuando;

    }

    public void veAregistro(){

        Intent miIntent = new Intent(Info_Restaurante.this, Login_Usuario.class);

        startActivity(miIntent);
    }

    public void ponInfo(){

        contendorInfo.removeAllViews();
        scrollInfo.scrollTo(0,0);

        View contenido = inflater.inflate(R.layout.info_informacion, null);


        TextView detalleRestaurante=contenido.findViewById(R.id.info_rest_detalle);
        TextView tagsRestaurante=contenido.findViewById(R.id.info_rest_tags);
        TextView tipoComida=contenido.findViewById(R.id.info_tipo_comida);
        TextView telefonoRestaurante=contenido.findViewById(R.id.info_rest_telefono);
        TextView distancia=contenido.findViewById(R.id.info_rest_distancia);
        LinearLayout cajaTags=contenido.findViewById(R.id.caja_tags);

        LinearLayout contenedorLlamadas=contenido.findViewById(R.id.contenedor_llamada);

        detalleRestaurante.setText(queRestaurante.detalle);

        tipoComida.setText(queRestaurante.tipo_comida);

        if(queRestaurante.tags.equals("")){

            tagsRestaurante.setVisibility(View.GONE);
            cajaTags.setVisibility(View.GONE);

        }else {
            tagsRestaurante.setText(queRestaurante.tags);
        }

        if(dimeDistancia().equals("")){

            distancia.setVisibility(View.GONE);

        }else{

            distancia.setText(dimeDistancia());
        }

        if(queRestaurante.telefono==0){

            contenedorLlamadas.setVisibility(View.GONE);

        }else{

            telefonoRestaurante.setText(String.valueOf(queRestaurante.telefono));
        }

        contendorInfo.addView(contenido);

    }

    public void Ira_Lakarta(Restaurantes elRestaurante){

        if(compruebaConexion()) {

            Intent miIntent = new Intent(this, Contenedor_Lakarta.class);

            miIntent.putExtra("QUERESTAURANTE", elRestaurante);
            miIntent.putExtra("KARTA_DESDE_ADMIN", "no");

            startActivity(miIntent);

            finish();

        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

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

    public String dimeDistancia(){

        if(migps.getLatitude()!=0) {

            Location donde=new Location("");

            donde.setLatitude(Double.parseDouble(queRestaurante.latitud));
            donde.setLongitude(Double.parseDouble(queRestaurante.longitud));

            String distanciaRes = "";

            if (donde.distanceTo(migps) > 1000) {

                distanciaRes = formato.format(donde.distanceTo(migps) / 1000) + " Km";

            } else {

                distanciaRes = (int) (donde.distanceTo(migps) / 1) + " metros";
            }

            return distanciaRes;


        }else{

            return "";

        }

    }

    public void quitarTinte(){

        ponInfo.setColorFilter(Color.GRAY);
        ponComentarios.setColorFilter(Color.GRAY);

    }

    private void introduceComentario(final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View introTexto = inflater.inflate(R.layout.entrada_comentario, null);

        final TextView entradaTexto=introTexto.findViewById(R.id.recoge_texto);
        final ImageView validaTexto=introTexto.findViewById(R.id.valida_texto);

        activaBoton(false,validaTexto);
        entradaTexto.setHint(getResources().getString(R.string.debes_poner_comentario));

        AlertDialog.Builder ponTexto = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        ponTexto.setView(introTexto);
        // disallow cancel of AlertDialog on click of back button and outside touch
        ponTexto.setCancelable(true);

        final AlertDialog dialogoTexto = ponTexto.create();


        entradaTexto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!validaTexto.isEnabled()) {

                    activaBoton(true, validaTexto);
                }

            }
        });

        validaTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                    if(entradaTexto.getText().length()<2){

                        mensajeAlerta=getResources().getString(R.string.debes_poner_comentario);
                        ponAlerta();

                    }else{

                        System.out.println("ENVIA COMENTARIO");
                        activaBoton(true,boton);

                        dialogoTexto.cancel();
                    }



            }
        });

        entradaTexto.setOnFocusChangeListener(new View.OnFocusChangeListener() { // ABRE EL TECLADO -----------------
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialogoTexto.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        entradaTexto.requestFocus(); //Asegurar que editText tiene focus

        dialogoTexto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogoTexto.show();

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

    public void cargaUsuarioGuardado(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        miUsuario=new Usuario();

        try {

            JSONArray usuarioCargado = new JSONArray(guarda.getString("USUARIO_GUARDADO", "0"));

            JSONObject objectUser = usuarioCargado.getJSONObject(0);

            miUsuario.email = objectUser.getString("email");
            miUsuario.avatar = objectUser.getString("avatar");
            miUsuario.puede_comentar = objectUser.getInt("puede_comentar");

        }catch (Exception e){

            System.out.println("ERROR AL CARGAR USUARIO: "+e.getMessage());

        }

    }

    public void cargaUserUsuario(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_USUARIO","0");
        pass=guarda.getString("PASS_USUARIO","0");

        System.out.println("CARGAR USER");

    }

    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        userEmpresa=guarda.getString("USER_EMPRESA","0");
        passEmpresa=guarda.getString("PASS_EMPRESA","0");

        System.out.println("CARGAR USER EMPRESA");


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

    public void quitaLikeLocal(String like) {
        try {

            for (int i = 0; i < Inicio.misLikes.length(); i++) {

                if (Inicio.misLikes.get(i).equals(like)) {

                    Inicio.misLikes.remove(i);
                    Inicio.likesActualizados=true;
                    System.out.println("LIKES ACTUALIZADOS AL QUITAR "+Inicio.misLikes);
                    break;
                }
            }

        }catch (Exception e){


        }



    }

    public void guardaLikeLocal(String like) {

        Inicio.misLikes.put(like);
        Inicio.likesActualizados=true;
        System.out.println("LIKES ACTUALIZADOS AL PONER "+Inicio.misLikes);

    }

    private void irALogin_Usuario(){

        Intent miIntent = new Intent(this, Login_Usuario.class);

        startActivity(miIntent);

        System.out.println("VA A LOGIN");
    }

    public boolean compruebaPermisoGPS(final View boton){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            //System.out.println("NO TIENE ACCESO A LA LOCALIZACION 2");

            preguntaPorGPS(boton);

            return false;


        }else{

            return true;
        }

    }

    private void preguntaPorGPS(final View boton){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button continuar=alertLayout.findViewById(R.id.pedido_guardar);
        Button salir=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);
        ImageView imagen=alertLayout.findViewById(R.id.icono_alerta);

        continuar.setText(getResources().getString(R.string.continuar));
        salir.setText(getResources().getString(R.string.salir));
        pregunta.setText(getResources().getString(R.string.pregunta_activar_gps));
        imagen.setImageResource(R.drawable.gps);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ActivityCompat.requestPermissions(Info_Restaurante.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, 100);

                dialog.cancel();
                activaBoton(true, boton);

            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(true, boton);

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("RESULTADO " + requestCode);
        if (requestCode == 100) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (migps.getLatitude() == 0) {

                    enviaCuandoGPS(enviarComentario);

                } else {

                    Location locRest = new Location("");
                    locRest.setLatitude(Double.parseDouble(queRestaurante.latitud));
                    locRest.setLongitude(Double.parseDouble(queRestaurante.longitud));

                    System.out.println("DISTANCIA: " + migps.distanceTo(locRest));

                    int distancia;

                    if (queRestaurante.desdeDondeComentarios == 1) {

                        distancia = 100;

                    } else {

                        distancia = (queRestaurante.desdeDondeComentarios - 100) * 1000;

                    }

                    if (migps.distanceTo(locRest) < distancia) {

                        enviaComentario();

                    } else {
                        mensajeAlerta = "ESTAS DEMASIADO LEJOS";
                        ponAlerta();

                    }

                }

            }

        }
    }

    public void enviaCuandoGPS(View boton){

        System.out.println("ENVIA CUANDO GPS");

        View alertLayout = inflater.inflate(R.layout.emerg_enviando_datos, null);

        TextView mensaje=alertLayout.findViewById(R.id.mensaje_envio);

        mensaje.setText(getResources().getString(R.string.buscando_posicion));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        activaBoton(true, boton);

        esperaTiempo espera=new esperaTiempo();
        espera.execute(dialog);

    }

    private class esperaTiempo extends AsyncTask<AlertDialog,Integer,String> {

        AlertDialog alerta;

        @Override
        protected String doInBackground(AlertDialog... alerta) {

            this.alerta=alerta[0];
            int contador=20;

            while(contador>0 && migps.getLongitude()==0) {

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println(e);
                }

                contador--;

            }

            if(migps.getLongitude()==0){

                return "nok";

            }else{
                return "ok";

            }

        }

        protected void onPostExecute(String resultado) {

            if(resultado.equals("ok")) {

                Location locRest = new Location("");
                locRest.setLatitude(Double.parseDouble(queRestaurante.latitud));
                locRest.setLongitude(Double.parseDouble(queRestaurante.longitud));

                System.out.println("DISTANCIA: " + migps.distanceTo(locRest));

                int distancia;

                if (queRestaurante.desdeDondeComentarios == 1) {

                    distancia = 100;

                } else {

                    distancia = (queRestaurante.desdeDondeComentarios - 100) * 1000;

                }

                if (migps.distanceTo(locRest) < distancia) {

                    alerta.cancel();
                    enviaComentario();

                } else {
                    mensajeAlerta = "ESTAS DEMASIADO LEJOS";
                    ponAlerta();
                    alerta.cancel();

                }
            }else{

                mensajeAlerta = "NO SE HA PODIDO COMROBAR TU UBICACIÓN, INtÉNTALO DE NUEVO";
                ponAlerta();
                alerta.cancel();
            }


        }
    }

    private void preguntaAquien(final String que){

        final View alertLayout = inflater.inflate(R.layout.emerg_elige_quien_comenta, null);

        final RadioGroup opciones=alertLayout.findViewById(R.id.opciones);
        final RadioButton aUsuario=alertLayout.findViewById(R.id.radioButton0);
        RadioButton aEmpresa=alertLayout.findViewById(R.id.radioButton1);
        TextView aceptar=alertLayout.findViewById(R.id.aceptar);

        aUsuario.setText(user);
        aEmpresa.setText(nombreEmpresa);

        aUsuario.setChecked(true);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(opciones.getCheckedRadioButtonId()==R.id.radioButton0){

                    if(que.equals("comentario")){

                        usuarioEnviaComentario();

                    }else{

                        usuarioEnviaRespuesta();
                    }

                }else if(opciones.getCheckedRadioButtonId()==R.id.radioButton1){

                    if(que.equals("comentario")){

                        empresaEnviaComentario();

                    }else{

                        empresaEnviaRespuesta();

                    }

                }

                dialog.cancel();


            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

    }

    public void cargaEmpresaGuardado(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);


        try {

            String valor=guarda.getString("EMPRESA_GUARDADA", "0");

            if(valor!=null) {
                JSONObject objectUser = new JSONObject(valor);

                nombreEmpresa = objectUser.getString("nombre");
                miCodRest = objectUser.getString("codRest");
            }else{

                nombreEmpresa="";
                miCodRest="";
            }


        }catch (Exception e){

            System.out.println("ERROR AL CARGAR USUARIO: "+e.getMessage());

        }

    }

}
