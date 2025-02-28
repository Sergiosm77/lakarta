package sarao.digital.lakarta;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.DecimalFormat;

public class fragment_karta extends Fragment {

    RecyclerView reciclaKartas;
    Adaptador_Karta adaptaKartas;

    private final Kartas kartaPoner;
    private final Kartas[] niveles3karta;

    CardView contenedorImagen;

    final DecimalFormat form = new DecimalFormat("0.00");

    private double precioFinal;
    int cantidad,cantidadNivel4;
    int[] elegidoTotalEste,totalAelegir;

    static BBDD_Helper helper;

    boolean menuCargado;

    LayoutInflater inflador;

    int[] scroll;

    TextView nomCriterio,detalleCriterio,precioCriterio;
    ImageView imagenCriterio;
    AppBarLayout barraApp;
    CollapsingToolbarLayout barraCollapse;

    static float px;

    public fragment_karta(Kartas kartaPoner,Kartas[] niveles3karta) {

        this.kartaPoner=kartaPoner;
        this.niveles3karta=niveles3karta;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_karta, container, false);

        nomCriterio = v.findViewById(R.id.criterio_menu);
        detalleCriterio = v.findViewById(R.id.criterio_detalle);
        precioCriterio = v.findViewById(R.id.precio_menu);
        imagenCriterio=v.findViewById(R.id.imagen_criterio);
        contenedorImagen=v.findViewById(R.id.contenedor_cardview);
        barraApp=v.findViewById(R.id.appbarLayout);
        barraCollapse=v.findViewById(R.id.barraCollapse);

        nomCriterio.setText(kartaPoner.nombre_nivel);
        detalleCriterio.setText(kartaPoner.detalle_nivel);

        barraCollapse.setTitle(kartaPoner.nombre_nivel);
        barraCollapse.setExpandedTitleTextAppearance(R.style.tituloExpandido);
        barraCollapse.setCollapsedTitleTextAppearance(R.style.tituloComprimido);


        if(Contenedor_Lakarta.queRestaurante.fKarta!=0) {

            v.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fKarta);

        }

        if(Contenedor_Lakarta.queRestaurante.fBordes!=0) {
            barraApp.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fBordes);
            barraCollapse.setContentScrimColor(Contenedor_Lakarta.queRestaurante.fBordes);

        }

        if(Contenedor_Lakarta.queRestaurante.tBordes!=0) {
            nomCriterio.setTextColor(Contenedor_Lakarta.queRestaurante.tBordes);
            detalleCriterio.setTextColor(Contenedor_Lakarta.queRestaurante.tBordes);
            precioCriterio.setTextColor(Contenedor_Lakarta.queRestaurante.tBordes);
            barraCollapse.setCollapsedTitleTextColor(Contenedor_Lakarta.queRestaurante.tBordes);
        }


        if(kartaPoner.mostrar_imagen==1) {

            Glide.with(this)
                    .load(kartaPoner.imagen_nivel)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.no_photo)
                    .into(imagenCriterio);

        }else{

            contenedorImagen.setVisibility(View.GONE);
        }

        imagenCriterio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Contenedor_Lakarta.blurview.setVisibility(View.VISIBLE);

                activaBoton(false, imagenCriterio);

                verFoto(kartaPoner, imagenCriterio);
            }
        });

        if(kartaPoner.precio_nivel==0){

            precioCriterio.setText("");
        }else{

            precioCriterio.setText(form.format(kartaPoner.precio_nivel)+"€");
        }

        //rutacontenedor = v.findViewById(R.id.pantalla_platos);
        //rutaTitulo = v.findViewById(R.id.titulo_plato);

        px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                getResources().getDisplayMetrics()
        );


        //helper = new BBDD_Helper(getActivity());

        try {

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);


            reciclaKartas = v.findViewById(R.id.recicladorKartas);
            reciclaKartas.setLayoutManager(layoutManager);
            //adaptaRestaurantes = new Adaptador_Restaurantes_Inicio(imagenes, nombres,latitudes,longitudes, getApplicationContext(),new View(getApplicationContext()),restaurantes);
            adaptaKartas = new Adaptador_Karta(niveles3karta,kartaPoner, getContext(),new View(getContext()));
            reciclaKartas.setAdapter(adaptaKartas);


        }catch (Exception e){

        }





        return v;

    }

    @Override
    public void onPause() {
        //barraApp.setExpanded(true);
        super.onPause();
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

    private void verFoto(final Kartas criterio, final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);


        inflador = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View alertLayout = inflador.inflate(R.layout.ver_foto, null);

        final ImageView queFoto = alertLayout.findViewById(R.id.ver_foto);
        TextView nombreCriterio=alertLayout.findViewById(R.id.nombre_categoria);
        TextView detalleCriterio=alertLayout.findViewById(R.id.detalle_categoria);


        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true, boton);

            }
        });

        queFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true, boton);
                dialog.cancel();

            }
        });

        String nombreconprecio;
        if(criterio.precio_nivel!=0){

            nombreconprecio=criterio.nombre_nivel+" "+form.format(criterio.precio_nivel)+"€";
        }else{

            nombreconprecio=criterio.nombre_nivel;
        }
        nombreCriterio.setText(nombreconprecio);
        if(criterio.detalle_nivel.equals("")){

            detalleCriterio.setVisibility(View.GONE);
        }else {

            detalleCriterio.setText(criterio.detalle_nivel);
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if (!criterio.imagen_nivel.equals("null")) {

                    Glide.with(getContext())
                            .load(criterio.imagen_nivel)
                            .error(R.drawable.no_photo)
                            .into(queFoto);

                } else {

                    queFoto.setImageResource(R.drawable.no_photo);
                }



            }
        });

        dialog.show();


    }
}

class SingleScrollDirectionEnforcer  implements RecyclerView.OnItemTouchListener {

    private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    private int scrollPointerId = -1;
    private int initialTouchX = 0;
    private int initialTouchY = 0;
    private int dx = 0;
    private int dy = 0;


    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scrollPointerId = e.getPointerId(0);
                initialTouchX = (int)(e.getX() + 0.5f);
                initialTouchY = (int)(e.getY() + 0.5f);

            case MotionEvent.ACTION_POINTER_DOWN:
                int actionIndex = e.getActionIndex();
                scrollPointerId = e.getPointerId(actionIndex);
                initialTouchX = (int)(e.getX(actionIndex) + 0.5f);
                initialTouchY = (int)(e.getY(actionIndex) + 0.5f);

            case MotionEvent.ACTION_MOVE:
                int index = e.findPointerIndex(scrollPointerId);
                if (index >= 0 && scrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    int x = (int)(e.getX(index) + 0.5f);
                    int y = (int)(e.getY(index) + 0.5f);
                    dx = x - initialTouchX;
                    dy = y - initialTouchY;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


}
