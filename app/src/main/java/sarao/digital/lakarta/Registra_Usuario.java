package sarao.digital.lakarta;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registra_Usuario extends AppCompatActivity {

    LocationManager mlocManager;
    String mensajeAlerta;
    Toast mensajePop;
    EditText nombreUsr,emailUsr,contraUsr,contraUsrRpt,codigoUsr;
    TextView registrar,leePolitica,volver;
    ImageView iconoNombre, iconoEmail, iconoContra, iconoContra2, chico, chica;
    ScrollView scrollAlta;
    String avatar="no";

    public static ImageView iconoPolitica;
    public static Boolean politicaOk=false;

    int cambiaColorChico, cambiaColorChica, fondoAvatarColorChico,fondoAvatarColorChica, avatarElegido;

    int[] loscolores;

    String nombreEnviar,emailEnviar,contraEnviar,codigoEnviar;

    LinearLayout cajaCodigo, cajaRegistro, fondoAvatares;

    CardView fondoAvatarChico, fondoAvatarChica;

    NuevoUsuario nuevoUsuario;

    boolean nombreOk=false,emailOk=false,contraOk=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_usuario);

        cambiaColorChica=18;
        cambiaColorChico=18;
        fondoAvatarColorChico=18;
        fondoAvatarColorChica=18;

        avatarElegido=0;

        mlocManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        nombreUsr=findViewById(R.id.nombre);
        emailUsr=findViewById(R.id.email);
        contraUsr=findViewById(R.id.contra);
        contraUsrRpt=findViewById(R.id.contra2);
        codigoUsr=findViewById(R.id.codigo);

        registrar=findViewById(R.id.registrar);
        leePolitica=findViewById(R.id.leer_politica);
        volver=findViewById(R.id.volver);

        iconoNombre=findViewById(R.id.icono_nombre);
        iconoContra=findViewById(R.id.icono_contra);
        iconoEmail=findViewById(R.id.icono_email);
        iconoContra2=findViewById(R.id.icono_contra2);
        iconoPolitica=findViewById(R.id.icono_politica);
        cajaCodigo=findViewById(R.id.caja_codigo);
        cajaRegistro=findViewById(R.id.caja_registro);
        scrollAlta=findViewById(R.id.scroll_alta_user);
        chico=findViewById(R.id.chico);
        chica=findViewById(R.id.chica);
        fondoAvatarChico=findViewById(R.id.fondo_avatar_chico);
        fondoAvatarChica=findViewById(R.id.fondo_avatar_chica);
        fondoAvatares=findViewById(R.id.fondo_avatares);

        cajaCodigo.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        nuevoUsuario=new NuevoUsuario(this,this,getLayoutInflater());

          cargaColores();

               leePolitica.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        activaBoton(false, leePolitica);

                        irApolitica(leePolitica, "politica_usuario");

                    }
                });

                   fondoAvatares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(avatarElegido==1) {

                    fondoAvatarColorChico++;

                    if (fondoAvatarColorChico > 19) {

                        fondoAvatarColorChico = 0;

                    }

                    int elColorFondo = getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChico]).getColor(0, 0);

                    int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChico]).getColor(0, 0);

                    fondoAvatarChico.setCardBackgroundColor(elColorFondo);

                    avatar="chico"+(elColor+1000000000)+(elColorFondo+1000000000);

                    //miNuevoAvatar="chico"+elColor;

                }else if(avatarElegido==2) {

                    fondoAvatarColorChica++;

                    if (fondoAvatarColorChica > 19) {

                        fondoAvatarColorChica = 0;

                    }

                    int elColorFondo = getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChica]).getColor(0, 0);

                    int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChica]).getColor(0, 0);

                    fondoAvatarChica.setCardBackgroundColor(elColorFondo);

                    avatar="chica"+(elColor+1000000000)+(elColorFondo+1000000000);


                }


            }
        });

                   volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activaBoton(false, volver);

                if(nuevoUsuario.enviarCodigo){

                    preguntaSiSalir();
                }else {

                    finish();

                }



            }
        });

        chico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                avatarElegido=1;

                if(fondoAvatarChica.getScaleX()==1.0) {

                    fondoAvatarChica.setCardBackgroundColor(getResources().getColor(R.color.colorGrisSuave, null));
                    chica.setColorFilter(getResources().getColor(R.color.colorBlanco, null));
                    fondoAvatarChica.animate().scaleX(0.7f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(0.7f).setDuration(200);
                }

                if(fondoAvatarChico.getScaleX()!=1.0){

                    //chico.animate().scaleX(1.2f).setDuration(200);
                    //chico.animate().scaleY(1.2f).setDuration(200);

                    ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                            fondoAvatarChico,
                            PropertyValuesHolder.ofFloat("scaleX", 1f),
                            PropertyValuesHolder.ofFloat("scaleY", 1f));
                    scaleDown.setDuration(200);

                    //scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
                    scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

                    scaleDown.start();

                }

                cambiaColorChico++;
                if(cambiaColorChico>19) {

                    cambiaColorChico=0;

                }

                int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChico]).getColor(0, 0);

                chico.setColorFilter(elColor);

                int elColorFondo=getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChico]).getColor(0, 0);

                fondoAvatarChico.setCardBackgroundColor(elColorFondo);

                avatar="chico"+(elColor+1000000000)+(elColorFondo+1000000000);

            }
        });

        chica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                avatarElegido=2;

                if(fondoAvatarChico.getScaleX()==1.0) {

                    fondoAvatarChico.setCardBackgroundColor(getResources().getColor(R.color.colorGrisSuave, null));
                    chico.setColorFilter(getResources().getColor(R.color.colorBlanco, null));
                    fondoAvatarChico.animate().scaleX(0.7f).setDuration(200);
                    fondoAvatarChico.animate().scaleY(0.7f).setDuration(200);
                }

                if(fondoAvatarChica.getScaleX()!=1.0){

                    fondoAvatarChica.animate().scaleX(1.2f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(1.2f).setDuration(200);
                    fondoAvatarChica.animate().scaleX(1f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(1f).setDuration(200);

                }

                cambiaColorChica++;
                if(cambiaColorChica>19) {

                    cambiaColorChica=0;

                }

                int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChica]).getColor(0, 0);

                chica.setColorFilter(elColor);
                int elColorFondo=getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChica]).getColor(0, 0);

                fondoAvatarChica.setCardBackgroundColor(elColorFondo);

                avatar="chica"+(elColor+1000000000)+(elColorFondo+1000000000);
            }
        });

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, registrar);

                if(!nuevoUsuario.enviarCodigo) {

                    if (nombreOk && emailOk && contraOk && politicaOk) {

                        nombreEnviar=nombreUsr.getText().toString();
                        emailEnviar= emailUsr.getText().toString();
                        contraEnviar=contraUsr.getText().toString();

                        nuevoUsuario.enviaNuevoUsuario(nombreEnviar, emailEnviar,avatar,contraEnviar,iconoNombre, iconoEmail, scrollAlta, registrar, cajaCodigo, cajaRegistro,fondoAvatares,chica,chico);

                    } else {

                        mensajeAlerta = "Datos incompletos";
                        ponAlerta();
                        activaBoton(true, registrar);
                    }
                }else{

                    if(codigoUsr.getText().toString().length()>0) {

                        codigoEnviar =codigoUsr.getText().toString();
                        nuevoUsuario.activaUsuario(nombreEnviar,codigoEnviar,registrar);

                    }else{

                        mensajeAlerta = getResources().getString(R.string.codigo_incorrecto);
                        ponAlerta();
                        activaBoton(true, registrar);

                    }

                }
            }
        });

        nombreUsr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>5){

                    iconoNombre.setImageResource(R.drawable.ok);
                    nombreOk=true;

                }else{
                    iconoNombre.setImageResource(R.drawable.delete);
                    nombreOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        emailUsr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>5){

                    // Patrón para validar el email
                    Pattern pattern = Pattern
                            .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

                    // El email a validar

                    Matcher mather = pattern.matcher(s.toString());

                    if (mather.find()) {
                        iconoEmail.setImageResource(R.drawable.ok);

                        emailOk=true;

                    } else {
                        iconoEmail.setImageResource(R.drawable.delete);
                        emailOk=false;

                    }
                }else{
                    iconoEmail.setImageResource(R.drawable.delete);
                    emailOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contraUsr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                contraUsrRpt.setText("");
                contraOk=false;

                if(s.length()>5){

                    iconoContra.setImageResource(R.drawable.ok);


                }else{
                    iconoContra.setImageResource(R.drawable.delete);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contraUsrRpt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(contraUsr.getText().toString().contentEquals(s) && s.length()>5){

                    iconoContra2.setImageResource(R.drawable.ok);
                    contraOk=true;

                }else{
                    iconoContra2.setImageResource(R.drawable.delete);
                    contraOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        if(nuevoUsuario.enviarCodigo){

            preguntaSiSalir();
        }else{

            super.onBackPressed();
        }

    }

    private void irApolitica(TextView boton, String tipoPolitica){

        Intent miIntent = new Intent(this, politica_privacidad.class);

        miIntent.putExtra("POLITICA",tipoPolitica);

        startActivity(miIntent);

        activaBoton(true, boton);
    }

    public void cargaColores(){

        loscolores=new int[20];

        loscolores[0]=R.array.reds;
        loscolores[1]=R.array.pinks;
        loscolores[2]=R.array.purples;
        loscolores[3]=R.array.deep_purples;
        loscolores[4]=R.array.indigos;
        loscolores[5]=R.array.blues;
        loscolores[6]=R.array.light_blues;
        loscolores[7]=R.array.cyans;
        loscolores[8]=R.array.teals;
        loscolores[9]=R.array.greens;
        loscolores[10]=R.array.light_greens;
        loscolores[11]=R.array.limes;
        loscolores[12]=R.array.yellows;
        loscolores[13]=R.array.ambers;
        loscolores[14]=R.array.oranges;
        loscolores[15]=R.array.deep_oranges;
        loscolores[16]=R.array.browns;
        loscolores[17]=R.array.greys;
        loscolores[18]=R.array.blanco;
        loscolores[19]=R.array.negro;

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

    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    private void preguntaSiSalir(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button salir=alertLayout.findViewById(R.id.pedido_guardar);
        final Button cancelar=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView laPregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        laPregunta.setText(getResources().getString(R.string.salir_sin_guardar));

        salir.setText(getResources().getString(R.string.salir));
        cancelar.setText(getResources().getString(R.string.continuar));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, salir);

                nuevoUsuario.descartaUsuario(nombreEnviar,salir);
                dialog.cancel();

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cancelar);

                activaBoton(true, volver);

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }



}

