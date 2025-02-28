package sarao.digital.lakarta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.DecimalFormat;

public class Adaptador_Restaurantes_Inicio extends RecyclerView.Adapter<Adaptador_Restaurantes_Inicio.ViewHolder> {

    private Restaurantes[] losRestaurantes;
    private Publicidad[] laPublicidad;
    private Context contexto;
    private View v;
    private DecimalFormat formato=new DecimalFormat("0.00");
    private Location migps;
    private Server_ComentLikes enviaLike;

    private long mLastClickTime = 0;


    public Adaptador_Restaurantes_Inicio(Restaurantes[] losRestaurantes, Publicidad[] laPublicidad, Context contexto, View v,Location migps) {

        this.contexto = contexto;
        this.losRestaurantes=losRestaurantes;
        this.laPublicidad=laPublicidad;
        this.migps=migps;
        this.v=v;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.barra_restaurante,parent,false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final int nuevaPosicion;

        if(position==1){

            holder.barraRestaurante.setVisibility(View.GONE);
            holder.barraIconos.setVisibility(View.GONE);
            holder.barraPublicidad.setVisibility(View.VISIBLE);

            if(holder.barraPublicidad.getChildCount()<1) {

                View unaPubli = LayoutInflater.from(contexto).inflate(R.layout.barra_publicidad, null);

                ImageView imagen=unaPubli.findViewById(R.id.publi_foto);
                ImageView logo=unaPubli.findViewById(R.id.logo);

                imagen.setImageResource(R.drawable.pizarra);
                logo.setImageResource(R.drawable.logo);

                TextView texto1 = unaPubli.findViewById(R.id.texto1);
                TextView texto2 = unaPubli.findViewById(R.id.texto2);
                TextView pregunta = unaPubli.findViewById(R.id.pregunta);

                texto1.setText(laPublicidad[0].texto1Publi);
                texto2.setText(laPublicidad[0].texto2Publi);
                pregunta.setText(laPublicidad[0].preguntaBoton);

                holder.barraPublicidad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();



                        irApubli();

                    }
                });

                holder.barraPublicidad.addView(unaPubli);
            }

        }else{

            holder.barraRestaurante.setVisibility(View.VISIBLE);
            holder.barraIconos.setVisibility(View.VISIBLE);
            holder.barraPublicidad.setVisibility(View.GONE);

            if(position==0){
                nuevaPosicion = position;
            }else {
                nuevaPosicion = position - 1;
            }


            enviaLike = new Server_ComentLikes(v.getContext(), null);

            Glide.with(v.getContext())
                    .load(losRestaurantes[nuevaPosicion].imagen_principal)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.noimage)
                    .into(holder.imagen);

            Glide.with(v.getContext())
                    .load(losRestaurantes[nuevaPosicion].logo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.noimage)
                    .into(holder.logo);

            holder.nombreRest.setText(losRestaurantes[nuevaPosicion].nombre);
            holder.distancia.setText(dimeDistancia(nuevaPosicion));

            holder.cuantosLike.setText(String.valueOf(losRestaurantes[nuevaPosicion].contaLike));


            if (Inicio.misFavoritos != null) {

                for (int i = 0; i < Inicio.misFavoritos.size(); i++) {

                    if (Inicio.misFavoritos.get(i).equals(losRestaurantes[nuevaPosicion].codigo)) {

                        holder.iconoFavorito.setImageResource(R.drawable.favorito_on);
                        losRestaurantes[nuevaPosicion].favorito = 1;
                        holder.favorito = true;
                    }
                }

            }


            if (!losRestaurantes[nuevaPosicion].tipo_comida.equals("")) {
                holder.tipoComida.setText(losRestaurantes[nuevaPosicion].tipo_comida);
            } else {

                holder.tipoComida.setVisibility(View.GONE);
            }
            if (dimeDistancia(nuevaPosicion).equals("")) {

                holder.distancia.setVisibility(View.GONE);
            }

            holder.contenedorInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent miIntent = new Intent(contexto, Info_Restaurante.class);

                    miIntent.putExtra("QUERESTAURANTE", losRestaurantes[nuevaPosicion]);
                    miIntent.putExtra("PON_PAGINA", "info");
                    miIntent.putExtra("LATITUD", migps.getLatitude());

                    miIntent.putExtra("LONGITUD", migps.getLongitude());

                    miIntent.putExtra("POSICION_REST", nuevaPosicion);
                    miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    System.out.println("LIKE DE ADAPTADOR: " + holder.like);


                    contexto.startActivity(miIntent);

                }
            });

            holder.iconoLike.setImageResource(R.drawable.like_off);
            holder.like = false;

            if (Inicio.misLikes != null && Inicio.misLikes.length() > 0 && !Inicio.userUser.equals("0")) {

                for (int i = 0; i < Inicio.misLikes.length(); i++) {

                    try {

                        if (Inicio.misLikes.get(i).equals(losRestaurantes[nuevaPosicion].codigo)) {

                            holder.iconoLike.setImageResource(R.drawable.like_on);
                            holder.like = true;

                            System.out.println("PONE LIKE");

                            break;
                        }
                    } catch (Exception e) {

                        System.out.println("ERROR AL PONER LIKE " + e.getMessage());

                    }

                }
            } else {

                System.out.println("SIN LIKE " + Inicio.userUser + " " + Inicio.misLikes.length());

            }

            holder.iconoFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (holder.favorito) {

                        holder.iconoFavorito.setImageResource(R.drawable.favorito_off);
                        losRestaurantes[nuevaPosicion].favorito = 0;
                        holder.favorito = false;
                        quitaFavorito(losRestaurantes[nuevaPosicion].codigo);
                    } else {
                        holder.iconoFavorito.setImageResource(R.drawable.favorito_on);
                        losRestaurantes[nuevaPosicion].favorito = 1;
                        holder.favorito = true;

                        guardaFavorito(losRestaurantes[nuevaPosicion].codigo);

                    }

                }
            });

            holder.iconoLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (!Inicio.userUser.equals("0")) {

                        if (compruebaRed()) {

                            if (holder.like) {

                                enviaLike.enviaLike(Inicio.userUser, losRestaurantes[nuevaPosicion].codigo, holder.iconoLike, "quita");
                                holder.iconoLike.setImageResource(R.drawable.like_off);
                                quitaLikeLocal(losRestaurantes[nuevaPosicion].codigo);
                                holder.like = false;

                            } else {

                                enviaLike.enviaLike(Inicio.userUser, losRestaurantes[nuevaPosicion].codigo, holder.iconoLike, "pon");
                                holder.iconoLike.setImageResource(R.drawable.like_on);
                                guardaLikeLocal(losRestaurantes[nuevaPosicion].codigo);
                                holder.like = true;

                            }
                        } else {

                            String mensajeAlerta = contexto.getResources().getString(R.string.sin_internet);
                            ponAlerta(Inicio.mensajePop, mensajeAlerta);

                        }

                    } else {

                        irAlogin_Usuario();
                    }

                }
            });

            // ---- COMENTARIOS ---------------------------

            if (losRestaurantes[nuevaPosicion].permiteComentarios == 1) {

                holder.iconoComent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        Intent miIntent = new Intent(contexto, Info_Restaurante.class);

                        miIntent.putExtra("QUERESTAURANTE", losRestaurantes[nuevaPosicion]);
                        miIntent.putExtra("PON_PAGINA", "comentarios");
                        miIntent.putExtra("LATITUD", migps.getLatitude());

                        miIntent.putExtra("LONGITUD", migps.getLongitude());
                        miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        contexto.startActivity(miIntent);

                    }
                });

                holder.cuantosComent.setText(String.valueOf(losRestaurantes[nuevaPosicion].contaComentario));
            } else {

                holder.iconoComent.setVisibility(View.GONE);
                holder.cuantosComent.setVisibility(View.GONE);
            }

            // -------------------------------------

            holder.barraRestaurante.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (compruebaRed()) {

                        Intent miIntent = new Intent(contexto, Contenedor_Lakarta.class);

                        miIntent.putExtra("QUERESTAURANTE", losRestaurantes[nuevaPosicion]);
                        miIntent.putExtra("KARTA_DESDE_ADMIN", "no");
                        miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        contexto.startActivity(miIntent);

                    } else {

                        String mensajeAlerta = contexto.getResources().getString(R.string.sin_internet);
                        ponAlerta(Inicio.mensajePop, mensajeAlerta);

                    }

                }
            });

        }
    }


    @Override
    public int getItemCount() {

        int numItems;

        if(laPublicidad!=null){

            numItems=laPublicidad.length+losRestaurantes.length;

        }else{

            numItems=losRestaurantes.length;
        }

        return numItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imagen;
        ImageView logo;
        ImageView iconoFavorito, iconoComent, iconoLike;
        TextView nombreRest, distancia, tipoComida, cuantosComent, cuantosLike,contenedorInfo;
        LinearLayout barraRestaurante, barraIconos, barraPublicidad;

        boolean favorito=false;
        boolean like=false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagen=itemView.findViewById(R.id.logo_foto1);
            logo=itemView.findViewById(R.id.logo);
            nombreRest=itemView.findViewById(R.id.nombre_restaurante);
            distancia=itemView.findViewById(R.id.distancia_restaurante);
            tipoComida=itemView.findViewById(R.id.detalle_restaurante);
            barraRestaurante=itemView.findViewById(R.id.barra_restaurante);
            barraIconos=itemView.findViewById(R.id.barra_iconos);
            barraPublicidad=itemView.findViewById(R.id.barra_publicidad);
            contenedorInfo=itemView.findViewById(R.id.info_rest);
            iconoFavorito=itemView.findViewById(R.id.icono_favorito);
            iconoComent=itemView.findViewById(R.id.icono_coment);
            iconoLike=itemView.findViewById(R.id.icono_like);
            cuantosComent=itemView.findViewById(R.id.cuantos_coment);
            cuantosLike=itemView.findViewById(R.id.cuantos_like);


        }


    }


    public String dimeDistancia(int position){

        if(migps.getLatitude()!=0) {

            Location donde=new Location("");

            donde.setLatitude(Double.parseDouble(losRestaurantes[position].latitud));
            donde.setLongitude(Double.parseDouble(losRestaurantes[position].longitud));

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

    private static class usaFavoritos{

        public void Favoritos(){


        }

    }

    public boolean compruebaRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) contexto
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void ponAlerta(Toast pop, String mensajeAlerta){


        try {
        if(!pop.getView().isShown()) {

            pop.setText(mensajeAlerta);

            pop.setGravity(Gravity.CENTER, 0, 0);
            //TextView mensaje = pop.getView().findViewById(android.R.id.message);
            //mensaje.setGravity(Gravity.CENTER);

            pop.show();

        }

        }catch (Exception e){

            pop.setText(mensajeAlerta);

            pop.show();
        }

    }

    public void guardaFavorito(String favorito) {

        Inicio.misFavoritos.add(favorito);

        SharedPreferences guarda = PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor = guarda.edit();


        mieditor.putString("FAVORITOS", Inicio.misFavoritos.toString().replaceAll("\\s",""));


        mieditor.apply();

        /*

        ArrayList<String> misFavoritos=new ArrayList<>();

        String cargado = null;

        try {

            cargado = guarda.getString("FAVORITOS", null);


        } catch (Exception e) {

        }



        if(cargado!=null){

            cargado=cargado.replace("[","").replace("]","");

            misFavoritos =new ArrayList<>(Arrays.asList(cargado.split(",")));


        }



        SharedPreferences.Editor mieditor = guarda.edit();


        misFavoritos.add(favorito);


        mieditor.putString("FAVORITOS", misFavoritos.toString().replaceAll("\\s",""));


        mieditor.apply();

         */


    }

    public void quitaFavorito(String favorito) {

        Inicio.misFavoritos.remove(favorito);

        SharedPreferences guarda = PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor = guarda.edit();

        mieditor.putString("FAVORITOS", Inicio.misFavoritos.toString().replaceAll("\\s",""));

        mieditor.apply();

        System.out.println("FAVORITOS: "+Inicio.misFavoritos);

        /*

        Inicio.misFavoritos.remove(favorito);

        SharedPreferences guarda = PreferenceManager.getDefaultSharedPreferences(contexto);

        ArrayList<String> misFavoritos=new ArrayList<>();

        String cargado = null;


        try {

            cargado = guarda.getString("FAVORITOS", null);


        } catch (Exception e) {

        }



        if(cargado!=null){

            cargado=cargado.replace("[","").replace("]","");

            misFavoritos =new ArrayList<>(Arrays.asList(cargado.split(",")));


        }

        SharedPreferences.Editor mieditor = guarda.edit();


        misFavoritos.remove(favorito);


        mieditor.putString("FAVORITOS", misFavoritos.toString().replaceAll("\\s",""));


        mieditor.apply();

         */


    }

    public void irAlogin_Usuario(){

            Intent miIntent = new Intent(contexto, Login_Usuario.class);

            miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            contexto.startActivity(miIntent);


    }

    public void quitaLikeLocal(String like) {
        try {

            for (int i = 0; i < Inicio.misLikes.length(); i++) {

                if (Inicio.misLikes.get(i).equals(like)) {

                    Inicio.misLikes.remove(i);

                    System.out.println("LIKES ACTUALIZADOS AL QUITAR "+Inicio.misLikes);
                    break;
                }
            }

        }catch (Exception e){


        }



    }

    public void guardaLikeLocal(String like) {

        Inicio.misLikes.put(like);
        System.out.println("LIKES ACTUALIZADOS AL PONER "+Inicio.misLikes);

    }

    private void irApubli(){

        Intent miIntent = new Intent(contexto, Info_Publicidad.class);
        miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        contexto.startActivity(miIntent);
    }





}