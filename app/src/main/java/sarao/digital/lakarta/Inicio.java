package sarao.digital.lakarta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import android.net.ConnectivityManager;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Inicio extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public Location migps=new Location("");
    private LocationManager locManager;
    private ConnectivityManager connectivityManager;

    Localizacion miLoc;

    static String userUser, userEmp;
    static boolean likesActualizados=false;

    private long mLastClickTime = 0;

    boolean pausado=false;

    public static boolean likesCargados=false;
    public static boolean sesionUserReiniciada =false;

    public static boolean sesionEmpresaIniciada=false;

    ProgressBar barraProgreso, iconoBuscando;

    CardView viendoFavoritos;
    ImageView quitaFavoritos;

    boolean permisoGPS=false;

    public static List<String> misFavoritos;
    public static JSONArray misLikes;

    String ciudadElegida="";
    boolean ordenaPorNombre=false;
    boolean favoritosActivado=false;
    boolean iniciando=false;

    Menu nav_Menu;
    MenuItem logUs, confUs, logEm, conEm;

    NavigationView navegacion;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    Toolbar miBarraMenu;

    LayoutInflater inflador;
    TextView ordenaPor;
    TextView nombreLocalidad;

    RecyclerView reciclaRestaurantes;

    SearchView buscar;

    Adaptador_Restaurantes_Inicio adaptaRestaurantes;

    Restaurantes[] restaurantes,restaurantesEstaciudad;
    Ciudades[] ciudades;
    Publicidad[] laPublicidad;

    public static Toast mensajePop;
    private String mensajeAlerta="";

    private ArrayList<OrdenaCosas> restOrdenado,ciuOrdenado,restBuscado;

    private SwipeRefreshLayout swipe;

    boolean funcionaGps;

    LinearLayout contenedorRestaurantes;


    iniciaCuandoGps inicia;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        System.out.println("CREA MENUS");

        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        MenuItem menuItem=menu.findItem(R.id.buscar);
        buscar=(SearchView)menuItem.getActionView();
        MenuItem logUsuario=menu.findItem(R.id.grupo_login_usuario);
        MenuItem logEmpresa=menu.findItem(R.id.grupo_login_empresa);
        //MenuItem misFavoritos=menu.findItem(R.id.mis_favoritos);
        menu.setGroupVisible(R.id.caja_favoritos,false);
        menu.setGroupVisible(R.id.caja_politica,false);
        menu.setGroupVisible(R.id.caja_compartir,false);
        //MenuItem compartir=menu.findItem(R.id.compartir);
        //MenuItem politica=menu.findItem(R.id.politica);

        logEmpresa.setVisible(false);
        logUsuario.setVisible(false);
        //misFavoritos.setVisible(false);
        //politica.setVisible(false);
        //compartir.setVisible(false);

        buscar.setQueryHint(getResources().getString(R.string.buscar_restaurante));

        buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                buscaRestaurante(nombreLocalidad.getText().toString(),buscar.getQuery());

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        buscar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                ponRestaurantes(restOrdenado,nombreLocalidad.getText().toString() );
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        locManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);


        miLoc=new Localizacion();

        navegacion=findViewById(R.id.nav_view);
        nav_Menu = navegacion.getMenu();

        drawer=findViewById(R.id.drawer_layout);

        miBarraMenu=findViewById(R.id.toolbar_inicio);
        viendoFavoritos=findViewById(R.id.boton_viendo_favoritos);
        quitaFavoritos=findViewById(R.id.quita_favoritos);

        barraProgreso=findViewById(R.id.barraprogreso_inicio);

        nombreLocalidad=findViewById(R.id.ciudad_actual);

        ordenaPor=findViewById(R.id.orden_por);
        iconoBuscando=findViewById(R.id.icono_buscando);


        contenedorRestaurantes=findViewById(R.id.contenedor_restaurantes);

        swipe=findViewById(R.id.swipe);

        misLikes=new JSONArray();

        nav_Menu.findItem(R.id.buscar).setVisible(false);

        logUs=nav_Menu.findItem(R.id.grupo_login_usuario).getSubMenu().findItem(R.id.login_usuario);
        confUs=nav_Menu.findItem(R.id.grupo_login_usuario).getSubMenu().findItem(R.id.config_usuario);
        conEm=nav_Menu.findItem(R.id.grupo_login_empresa).getSubMenu().findItem(R.id.config_empresa);
        logEm=nav_Menu.findItem(R.id.grupo_login_empresa).getSubMenu().findItem(R.id.login_empresa);

        // Inicia valores -------------------------------------

        compruebaInicioSesiones();
        cargamisFavoritos();

        iniciaGPS();

        viendoFavoritos.setVisibility(View.GONE);

        //rutacontenedor=findViewById(R.id.pantalla_sitioscercanos);
        inflador=(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        //miBarraMenu.setNavigationIcon(R.drawable.icon_info);
        setSupportActionBar(miBarraMenu);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        navegacion.bringToFront();

        toggle = new ActionBarDrawerToggle(this, drawer, miBarraMenu, 0, 0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navegacion.setNavigationItemSelectedListener(this);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        ordenaPor.setText(getString(R.string.buscando_gps));

        nombreLocalidad.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              activaBoton(false,nombreLocalidad);

              cambiarCiudad(nombreLocalidad);

          }
      });

        quitaFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viendoFavoritos.setVisibility(View.GONE);
                favoritosActivado=false;
                ordenaRestaurantes(nombreLocalidad.getText().toString());
                ponRestaurantes(restOrdenado,nombreLocalidad.getText().toString());

            }
        });

        activaBoton(false,ordenaPor);

        if(ciudades==null && restaurantes==null){

            Bundle miBundle=this.getIntent().getExtras();

            if(miBundle!=null) {
                Parcelable[] datos_ciu = miBundle.getParcelableArray("CIUDADES");
                Parcelable[] datos_rest = miBundle.getParcelableArray("RESTAURANTES");
                Parcelable[] datos_publi = miBundle.getParcelableArray("PUBLICIDAD");

                if(datos_ciu!=null && datos_rest!=null) {

                    ciudades = Arrays.copyOf(datos_ciu, datos_ciu.length, Ciudades[].class); // copiamos el array del paquete en el array ciudades
                    restaurantes = Arrays.copyOf(datos_rest, datos_rest.length, Restaurantes[].class);

                }

                if(datos_publi!=null) {

                    laPublicidad = Arrays.copyOf(datos_publi, datos_publi.length, Publicidad[].class);

                }else{

                    //TODO

                    //laPublicidad=null;

                    laPublicidad=new Publicidad[1];

                    laPublicidad[0]=new Publicidad();


                    laPublicidad[0].nombrePublicidad = "PRUEBA PUBLI";
                    laPublicidad[0].texto1Publi = "¿Eres una empresa?";
                    laPublicidad[0].texto2Publi = "¿Te gustaría que tu carta apareciera aquí?";
                    laPublicidad[0].preguntaBoton = "Sí, quiero más información";



                }

            }

        }

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                System.out.println("BUSCAR: "+buscar.getQuery());

                if(!compruebaRed() && !compruebaGps()){

                    System.out.println("NINGUNA RED");

                    ordenaPor.setText(getString(R.string.buscando_gps));
                    iconoBuscando.setVisibility(View.VISIBLE);
                }

                if(buscar.getQuery().length()==0) {

                    swipe.setEnabled(false);

                    System.out.println("HACE SWIPE");

                    inicia = new iniciaCuandoGps();
                    inicia.execute();

                }else{

                    swipe.setEnabled(true);

                    System.out.println("NO HACE SWIPE");
                }
                swipe.setRefreshing(false);

            }
        });

        inicia=new iniciaCuandoGps();

        inicia.execute();

    }

    public void iniciaGPS(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //System.out.println("NO TIENE ACCESO A LA LOCALIZACION 1");
            //ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION }, 1222);

            permisoGPS=false;

            ordenaPor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    pidePermisoGps();
                }
            });

        }else {

            permisoGPS=true;

            if(compruebaRed() && compruebaGps()) {

                ordenaPor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        if (restOrdenado.size() > 1) {

                            cargamisFavoritos();

                            if (ordenaPor.getText().equals(getString(R.string.ordena_nombre))) {

                                //Collections.sort(restOrdenado, new OrdenarRestaurantesNombre());


                                if (restOrdenado.size() > 0 && funcionaGps) {

                                    ordenaPor.setText(getString(R.string.ordena_distancia));
                                    ordenaPorNombre = true;
                                    ordenaRestaurantes(nombreLocalidad.getText().toString());
                                    ponRestaurantes(restOrdenado, nombreLocalidad.getText().toString());

                                }
                            } else if (ordenaPor.getText().equals(getString(R.string.ordena_distancia))) {

                                //Collections.sort(restOrdenado, new OrdenarRestaurantesDistancia());

                                if (restOrdenado.size() > 0) {

                                    ordenaPorNombre = false;
                                    ordenaPor.setText(getString(R.string.ordena_nombre));
                                    ordenaRestaurantes(nombreLocalidad.getText().toString());
                                    ponRestaurantes(restOrdenado, nombreLocalidad.getText().toString());

                                }
                            }

                        }

                    }
                });

                if (ordenaPor.getText().equals(getString(R.string.sin_acceso_gps))) {

                    ordenaPor.setText(getString(R.string.buscando_gps));
                    iconoBuscando.setVisibility(View.VISIBLE);
                }

            }

                try {

                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, miLoc);
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, miLoc);

                    //funcionaGps=true;
                    //System.out.println("inicia GPS: "+funcionaGps);

                } catch (Exception e) {

                    //funcionaGps=false;

                }
        }
    }

    public void onResume(){

        System.out.println("RESUME");

        if(sesionUserReiniciada) {
            compruebaInicioSesiones();
        }

        if (reciclaRestaurantes != null && likesActualizados) {

            reciclaRestaurantes.getAdapter().notifyDataSetChanged();
            likesActualizados=false;

            // System.out.println("ACTUALIZA LIKES");
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        pausado=true;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
       if (!buscar.isIconified()) {
            buscar.setIconified(true);
        } else {
            super.onBackPressed();
        }


    }

    @Override
    public void onStop() {

        locManager.removeUpdates(miLoc);  // ------------- DETIENE EL GPS ----------------------

        super.onStop();
    }

    @Override
    protected void onRestart() {

      iniciaGPS();

       System.out.println("RESTART");
        super.onRestart();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.login_empresa){

            irAlogin_Empresa();

        }


        if(id==R.id.config_empresa){

            irAconfig_Empresa();

        }

        if(id==R.id.login_usuario){

            irAlogin_Usuario();

        }


        if(id==R.id.config_usuario){

            irAconfig_Usuario();

        }

        if(id==R.id.mis_favoritos){

            System.out.println("FAVORITOS: "+misFavoritos+" "+misFavoritos.size());

            if(misFavoritos.size()==0){

                mensajeAlerta=getResources().getString(R.string.sin_favoritos);
                ponAlerta();
            }else {


                favoritosActivado = true;
                viendoFavoritos.setVisibility(View.VISIBLE);
                ordenaRestaurantes(nombreLocalidad.getText().toString());
                ponRestaurantes(restOrdenado, nombreLocalidad.getText().toString());
            }

        }

        if(id==R.id.politica){

            irApolitica();

        }

        if(id==R.id.compartir){

            compartir();

        }

        drawer.closeDrawers();
        return true;
    }


    public class Localizacion implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            migps.setLatitude(loc.getLatitude());
            migps.setLongitude(loc.getLongitude());

            System.out.println("CAMBIA localizacion: "+migps.getLongitude());

/*
            if(migps.getLongitude()!=0) {

                System.out.println("inicia gps");

                if(ordenaPor.getText().toString().equals(getString(R.string.buscando_gps)) && !iniciando){

                    inicia=new iniciaCuandoGps();

                    inicia.execute();
                }
            }
*/
            System.out.println("funiona gps: "+funcionaGps);

            if(migps.getLongitude()!=0 && !funcionaGps) {

                funcionaGps=true;

                if((ordenaPor.getText().toString().equals(getString(R.string.buscando_gps)) || ordenaPor.getText().toString().equals(getString(R.string.sin_acceso_gps)) )&& !iniciando){

                    inicia=new iniciaCuandoGps();

                    inicia.execute();
                }
            }


        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado

            System.out.println("PROVIDER DISABLED: "+provider);
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado

            System.out.println("PROVIDER ENABLED: "+provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    System.out.println("LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    System.out.println( "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    System.out.println( "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    private class iniciaCuandoGps extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {

            iniciando=true;

            int contador=0;

            if(permisoGPS) {

                if (!compruebaGps()) {

                    System.out.println("NINGUNA RED EN INICIO: "+compruebaRed()+" "+compruebaGps());

                    funcionaGps=false;

                    return "nogps";

                }else {

                    while (migps.getLatitude() == 0 && contador < 20) {

                        try {
                            Thread.sleep(300);
                            contador++;
                        } catch (Exception e) {
                            System.out.println("ERROR GPS " + e.getMessage());

                        }

                    }

                    if (contador == 20) {

                        funcionaGps = false;

                        return "nok";
                    }

                    funcionaGps = true;
                    System.out.println(" GPS " + compruebaGps() + " RED " + compruebaRed());

                    return "ok";
                }

            }else{

                return "sinpermisogps";
            }

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            if(resultado.equals("ok")){

                if(ordenaPor.getText().toString().equals(getString(R.string.buscando_gps)) || ordenaPor.getText().toString().equals(getString(R.string.sin_acceso_gps))){

                    ordenaPor.setText(getString(R.string.ordena_nombre));
                    iconoBuscando.setVisibility(View.GONE);

                }

            }else if(resultado.equals("sinpermisogps")){

                ordenaPor.setText(getString(R.string.sin_acceso_gps));
                iconoBuscando.setVisibility(View.GONE);

            }else {

                ordenaPor.setText(getString(R.string.sin_acceso_gps));
                iconoBuscando.setVisibility(View.GONE);

            }

            if (ciudadElegida.equals("")) {

                if(resultado.equals("ok")) {

                    buscaMiCiudad();
                }else{

                    nombreLocalidad.setText(leeUltimaCiudad());

                }

            }else{

                nombreLocalidad.setText(ciudadElegida);
            }

            ordenaRestaurantes(nombreLocalidad.getText().toString());
            if(restOrdenado.size()>0) {
                ponRestaurantes(restOrdenado, nombreLocalidad.getText().toString());
            }

            activaBoton(true,ordenaPor);
            swipe.setEnabled(true);

            iniciando=false;

        }
    }

    public void buscaMiCiudad(){

        OrdenaCosas ciuOrden;

        ordenaCiudades();

        Collections.sort(ciuOrdenado, new OrdenarCiudadesDistancia());

        ciuOrden = ciuOrdenado.get(0);

        String queciudad=ciuOrden.getNombre();

        ciudadElegida=queciudad;

        nombreLocalidad.setText(queciudad);

        guardaUltimaCiudad(queciudad);

    }

    public void buscaRestaurante(String ciudad,CharSequence queBuscar){

        OrdenaCosas esteRestaurante;

        restBuscado=new ArrayList<>();

        String cadenaNormalize = Normalizer.normalize(queBuscar, Normalizer.Form.NFD);
        String cadenaSinAcentos = cadenaNormalize.replaceAll("[^\\p{ASCII}]", "");

        String[] palabras = cadenaSinAcentos.toLowerCase().split("[ .,]+");


        for(int i=0; i<restOrdenado.size();i++){

            esteRestaurante=(OrdenaCosas)restOrdenado.get(i);

            String normNombreBuscado = Normalizer.normalize(esteRestaurante.getNombre(), Normalizer.Form.NFD);
            String nombreBuscado = normNombreBuscado.replaceAll("[^\\p{ASCII}]", "");

            String normTipocomidaBuscado = Normalizer.normalize(esteRestaurante.getTipoComida(), Normalizer.Form.NFD);
            String tipocomidaBuscado = normTipocomidaBuscado.replaceAll("[^\\p{ASCII}]", "");

            String normTagBuscado = Normalizer.normalize(esteRestaurante.getTag(), Normalizer.Form.NFD);
            String tagBuscado = normTagBuscado.replaceAll("[^\\p{ASCII}]", "");


            for (String palabra : palabras) {
                if (nombreBuscado.toLowerCase().contains(palabra) || tipocomidaBuscado.toLowerCase().contains(palabra) || tagBuscado.toLowerCase().contains(palabra)) {
                    restBuscado.add(esteRestaurante);

                }
            }

        }

        if(restBuscado.size()>0) {

            ponRestaurantes(restBuscado, ciudad);
        }else{

            mensajeAlerta =getResources().getString(R.string.busqueda_nula);

            ponAlerta();
        }

    }

    public void ordenaRestaurantes(String ciudad){

        restOrdenado = new ArrayList<>();

        System.out.println("VA A ORDENAR RESTAURANTES");

        Location donde=new Location("");

        if(!ciudad.equals("Ver todas")) {

            for (int i = 0; i < restaurantes.length; i++) {

                if (restaurantes[i].poblacion.equals(ciudad)) {

                    if (funcionaGps && !ordenaPorNombre) {

                        donde.setLatitude(Double.parseDouble(restaurantes[i].latitud));
                        donde.setLongitude(Double.parseDouble(restaurantes[i].longitud));

                        if(favoritosActivado){
                            for(int f=0;f<misFavoritos.size();f++){

                                if(misFavoritos.get(f).equals(restaurantes[i].codigo)){

                                    restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, (int) donde.distanceTo(migps),restaurantes[i].tags));
                                }
                            }
                        }else {

                            restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, (int) donde.distanceTo(migps),restaurantes[i].tags));
                        }

                    } else {

                        if(favoritosActivado) {
                            for (int f = 0; f < misFavoritos.size(); f++) {

                                if (misFavoritos.get(f).equals(restaurantes[i].codigo)) {

                                    restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, 0,restaurantes[i].tags));

                                }
                            }
                        }else {

                            restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, 0,restaurantes[i].tags));
                        }

                    }
                }

            }
        }else{

            for (int i = 0; i < restaurantes.length; i++) {

                    if (funcionaGps && !ordenaPorNombre) {

                        donde.setLatitude(Double.parseDouble(restaurantes[i].latitud));
                        donde.setLongitude(Double.parseDouble(restaurantes[i].longitud));

                        if(favoritosActivado) {
                            for (int f = 0; f < misFavoritos.size(); f++) {

                                if (misFavoritos.get(f).equals(restaurantes[i].codigo)) {

                                    restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, (int) donde.distanceTo(migps),restaurantes[i].tags));
                                }
                            }
                        }else {

                            restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, (int) donde.distanceTo(migps),restaurantes[i].tags));
                        }

                    } else {

                        if(favoritosActivado) {
                            for (int f = 0; f < misFavoritos.size(); f++) {

                                if (misFavoritos.get(f).equals(restaurantes[i].codigo)) {

                                    restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, 0, restaurantes[i].tags));
                                }
                            }
                        }else {

                            restOrdenado.add(new OrdenaCosas(restaurantes[i].codigo, restaurantes[i].nombre, restaurantes[i].tipo_comida, 0,restaurantes[i].tags));
                        }

                    }

            }


        }

        System.out.println("HA ORDENADO: "+restOrdenado.size());

        if(funcionaGps && !ordenaPorNombre) {

            Collections.sort(restOrdenado, new OrdenarRestaurantesDistancia());
        }else if(ordenaPorNombre){

            Collections.sort(restOrdenado, new OrdenarRestaurantesNombre());
        }

    }

    public void ordenaCiudades(){

        ciuOrdenado = new ArrayList<>();

        for(int i=0; i<ciudades.length;i++) {

            ciuOrdenado.add(new OrdenaCosas(null, ciudades[i].nombre_ciudad, "", (int) ciudades[i].donde_ciudad.distanceTo(migps),""));

        }

        Collections.sort(ciuOrdenado, new OrdenarCiudadesDistancia());

    }

    public void ponRestaurantes(ArrayList<OrdenaCosas> enOrden, String ciudad){

        OrdenaCosas unrest;

        if(!ciudadElegida.equals("Ver todas")) {

            restaurantesEstaciudad = new Restaurantes[enOrden.size()];

            int contador = 0;

            for (int i = 0; i < enOrden.size(); i++) {

                unrest = (OrdenaCosas) enOrden.get(i);


                for (int e = 0; e < restaurantes.length; e++) {

                    if (unrest.getCodigo().equals(restaurantes[e].codigo)) {

                        if (restaurantes[e].poblacion.equals(ciudad)) {

                            restaurantesEstaciudad[contador] = restaurantes[e];
                            contador++;

                        /*

                        imagenes.add(restaurantes[e].logo);
                        nombres.add(restaurantes[e].nombre);
                        latitudes.add(restaurantes[e].latitud);
                        longitudes.add(restaurantes[e].longitud);

                         */

                        }

                        break;
                    }


                }

            }
        }else{

            restaurantesEstaciudad = new Restaurantes[enOrden.size()];

            int contador = 0;

            for (int i = 0; i < enOrden.size(); i++) {

                unrest = (OrdenaCosas) enOrden.get(i);

                for (int e = 0; e < restaurantes.length; e++) {

                    if (unrest.getCodigo().equals(restaurantes[e].codigo)) {

                            restaurantesEstaciudad[contador] = restaurantes[e];
                            contador++;


                        break;
                    }


                }

            }

        }

        if(restaurantesEstaciudad.length==0){

            contenedorRestaurantes.removeAllViews();

            reciclaRestaurantes.setAdapter(null);

            View unaPubli = inflador.inflate(R.layout.barra_publicidad, null);
            View sinRest=inflador.inflate(R.layout.sin_restaurantes,null);

            contenedorRestaurantes.addView(sinRest);

            ImageView imagen=unaPubli.findViewById(R.id.publi_foto);
            ImageView logo=unaPubli.findViewById(R.id.logo);

            logo.setImageResource(R.drawable.logo);
            imagen.setImageResource(R.drawable.pizarra);

            TextView texto1 = unaPubli.findViewById(R.id.texto1);
            TextView texto2 = unaPubli.findViewById(R.id.texto2);
            TextView pregunta = unaPubli.findViewById(R.id.pregunta);

            texto1.setText(laPublicidad[0].texto1Publi);
            texto2.setText(laPublicidad[0].texto2Publi);
            pregunta.setText(laPublicidad[0].preguntaBoton);

            unaPubli.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();



                    irApubli();

                }
            });

            contenedorRestaurantes.addView(unaPubli);


        }else {

            contenedorRestaurantes.removeAllViews();

            try {

                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                reciclaRestaurantes = findViewById(R.id.recicladorInicio);
                reciclaRestaurantes.setLayoutManager(layoutManager);
                //adaptaRestaurantes = new Adaptador_Restaurantes_Inicio(imagenes, nombres,latitudes,longitudes, getApplicationContext(),new View(getApplicationContext()),restaurantes);
                adaptaRestaurantes = new Adaptador_Restaurantes_Inicio(restaurantesEstaciudad, laPublicidad, getApplicationContext(), new View(getApplicationContext()), migps);
                reciclaRestaurantes.setAdapter(adaptaRestaurantes);

/*
            if (reciclaRestaurantes != null) {
                ((reciclaRestaurantes.getLayoutManager())).scrollToPosition(ContenedorInicio.posicionReciclador);
                //progressBarSitios.setVisibility(View.GONE);

            }

 */

            } catch (Exception e) {

                System.out.println("ERROR: " + e.getMessage());

            }

        }


    }

    private void irApubli(){

        Intent miIntent = new Intent(this, Info_Publicidad.class);
        miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(miIntent);
    }

    private void cambiarCiudad(final View boton){

        InputMethodManager introduce = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        introduce.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_elige_ciudad, null);
        final TextView todasCiudades=alertLayout.findViewById(R.id.todas);

        LinearLayout rutaContenedor=alertLayout.findViewById(R.id.lista_ciudades);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        alert.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                activaBoton(true,boton);

                dialog.cancel();

            }
        });


        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //imm.hideSoftInputFromWindow(entradaTexto.getWindowToken(), 0);

                activaBoton(true,boton);

            }
        });

        todasCiudades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(!nombreLocalidad.getText().toString().equals(todasCiudades.getText().toString())) {

                    nombreLocalidad.setText("Ver todas");
                    ciudadElegida = "Ver todas";
                    ordenaRestaurantes("Ver todas");
                    ponRestaurantes(restOrdenado, "Ver todas");
                }

                activaBoton(true,boton);
                dialog.cancel();

            }
        });

        for(int i=0;i<ciudades.length;i++){

            LinearLayout barra_ciudades=(LinearLayout) inflador.inflate(R.layout.barra_ciudades, null);

            TextView nom_ciudad=barra_ciudades.findViewById(R.id.nombre_ciudad);
            final String queciudad=ciudades[i].nombre_ciudad;
            nom_ciudad.setText(queciudad);
            rutaContenedor.addView(barra_ciudades);

            barra_ciudades.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if(!nombreLocalidad.getText().toString().equals(queciudad)) {

                        nombreLocalidad.setText(queciudad);
                        ciudadElegida = queciudad;
                        ordenaRestaurantes(queciudad);
                        ponRestaurantes(restOrdenado, queciudad);

                    }

                    activaBoton(true,boton);
                    dialog.cancel();

                }
            });

        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBlanco,null));

    }

    public void Ira_Lakarta(Restaurantes elRestaurante){

        if(compruebaRed() && compruebaGps()) {

            Intent miIntent = new Intent(this, Contenedor_Lakarta.class);

            miIntent.putExtra("CIUDADES", ciudades);
            miIntent.putExtra("QUERESTAURANTE", elRestaurante);

            startActivity(miIntent);
        }else{


            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    private boolean compruebaGps(){

        //LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            return false;

        }else{

            return true;
        }


    }

    public boolean compruebaRed(){

        NetworkInfo estadoRed = connectivityManager.getActiveNetworkInfo();

        if (estadoRed == null || !estadoRed.isConnected()) {

            return false;

        }else{

            return true;
        }


    }



    public void guardaUltimaCiudad(String ciudad){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("ULTIMACIUDAD",ciudad);

        mieditor.apply();

    }

    public String leeUltimaCiudad(){


        SharedPreferences guarda = PreferenceManager.getDefaultSharedPreferences(this);
        return guarda.getString("ULTIMACIUDAD", ciudades[0].nombre_ciudad);

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

    private class OrdenaCosas  {

        private String codigo;
        private String nombre;
        private String tipoComida;
        private String tag;
        private int distancia;

        public OrdenaCosas(String codigo, String nombre, String tipoComida, int distancia,String tag) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.tipoComida = tag;
            this.tag = tipoComida;
            this.distancia = distancia;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getTipoComida() {
            return tipoComida;
        }

        public String getTag() {
            return tag;
        }

        public void setTipoComida(String tipoComida) {
            this.tipoComida = tipoComida;
        }

        public int getDistancia() {
            return distancia;
        }

        public void setDistancia(int distancia) {
            this.distancia = distancia;
        }

        @Override
        public String toString() {
            return this.getCodigo() + "  -  " + this.getNombre() + "  -  " + this.getTipoComida()+ "  -  " + this.getDistancia();
        }
    }

    class OrdenarRestaurantesDistancia implements Comparator<OrdenaCosas> {

        @Override
        public int compare(OrdenaCosas o1, OrdenaCosas o2) {
            return new Integer(o1.getDistancia()).compareTo(new Integer(o2.getDistancia()));
        }
    }

    class OrdenarRestaurantesNombre implements Comparator<OrdenaCosas> {

        @Override
        public int compare(OrdenaCosas o1, OrdenaCosas o2) {
            return o1.getNombre().compareTo(o2.getNombre());
        }
    }

    class OrdenarCiudadesDistancia implements Comparator<OrdenaCosas> {

        @Override
        public int compare(OrdenaCosas o1, OrdenaCosas o2) {
            return new Integer(o1.getDistancia()).compareTo(new Integer(o2.getDistancia()));
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

    public void cargaUserUsuario(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        userUser=guarda.getString("USER_USUARIO","0");



    }

    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        userEmp=guarda.getString("USER_EMPRESA","0");


    }

    public void irAlogin_Empresa(){

        if(compruebaRed()) {

            Intent miIntent = new Intent(this, Login_Empresa.class);

            startActivity(miIntent);
        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    public void irAlogin_Usuario(){

        if(compruebaRed()) {

            Intent miIntent = new Intent(this, Login_Usuario.class);

            startActivity(miIntent);
        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    public void irAconfig_Empresa(){

        if(compruebaRed()) {

            Intent miIntent = new Intent(this, Menu_Empresa.class);

            startActivity(miIntent);
        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    public void irAconfig_Usuario(){

            Intent miIntent = new Intent(this, Menu_Usuario.class);

            startActivity(miIntent);

    }

    public void irApolitica(){

        Intent miIntent = new Intent(this, politica_privacidad.class);

        miIntent.putExtra("POLITICA","menu_inicio");

        startActivity(miIntent);

    }

    public void compruebaInicioSesiones(){

        sesionUserReiniciada =false;

        cargaUserEmpresa();
        cargaUserUsuario();
        cargaMisLikes();

        System.out.println("USUARIO ES 0, sesionUser="+ sesionUserReiniciada +" pausado="+pausado);

        if(pausado){

            pausado=false;

            if (reciclaRestaurantes != null) {


                reciclaRestaurantes.getAdapter().notifyDataSetChanged();

                System.out.println("ACTUALIZA ADAPTADOR");

            }
/*
            if(restOrdenado.size()>0) {

                System.out.println("REINICIA 1");

                ponRestaurantes(restOrdenado, nombreLocalidad.getText().toString());

            }


 */
        }

        if(userUser.equals("0")){

            logUs.setVisible(true);
            confUs.setVisible(false);


        }else{

            logUs.setVisible(false);
            confUs.setVisible(true);

        }

        if(userEmp.equals("0")){

            logEm.setVisible(true);
            conEm.setVisible(false);

        }else{

            logEm.setVisible(false);
            conEm.setVisible(true);


        }
    }

    public void cargaMisLikes(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        try {

            JSONArray  cargamisLikes = new JSONArray(guarda.getString("MIS_LIKES", "0"));

            System.out.println("CONTENIDO LIKES: " + cargamisLikes);

            if(cargamisLikes.length()>0) {

                for (int i = 0; i < cargamisLikes.length(); i++) {

                    JSONObject objectLikes = cargamisLikes.getJSONObject(i);

                    misLikes.put(i, objectLikes.getString("cod_restaurante"));

                }

                System.out.println("CARGA LIKES: " + misLikes);
            }else{

                misLikes=new JSONArray();
            }

            likesCargados=true;

        }catch (Exception e){

            misLikes=new JSONArray();
            likesCargados=true;


        }


    }


    public void cargamisFavoritos(){

        SharedPreferences carga= PreferenceManager.getDefaultSharedPreferences(this);

        String cargado=null;

        //misFavoritos = new ArrayList<>();

        try {

            cargado=carga.getString("FAVORITOS", null);


        }catch (Exception e){


        }

        if(cargado!=null) {

            cargado=cargado.replace("[","").replace("]","");

            misFavoritos =new ArrayList<>(Arrays.asList(cargado.split(",")));

        }else{

            misFavoritos = new ArrayList<>();
        }

        System.out.println("CARGA MIS FAVORITOS: "+misFavoritos);

    }

    private void pidePermisoGps(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION }, 1222);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1222) {

            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                System.out.println("PERMISO CONCEDIDO");

               iniciaGPS();

            }else{

                avisoPermisos();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void avisoPermisos(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button cancelar=alertLayout.findViewById(R.id.pedido_guardar);
        Button ver_permisos=alertLayout.findViewById(R.id.pedido_noguardar);
        ImageView icono=alertLayout.findViewById(R.id.icono_alerta);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        String texto=getResources().getString(R.string.sin_permiso_ubicacion);

        icono.setImageResource(R.drawable.gps);
        pregunta.setText(texto);

        cancelar.setText(getResources().getString(R.string.cancelar));
        ver_permisos.setText(R.string.ver_permisos);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        ver_permisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.cancel();

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();

            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void compartir(){

        String mensaje;

        try{

            mensaje=getString(R.string.comparte_mensaje)+"https://play.google.com/store/apps/details?id="+(this.getPackageName());

        }catch(NullPointerException e){

            mensaje=getString(R.string.comparte_mensaje)+"https://play.google.com/store/apps/details?id="+(this.getPackageName());

        }

        Intent compartir = new Intent(android.content.Intent.ACTION_SEND);
        compartir.setType("text/plain");
        compartir.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.comparte_subject));
        compartir.putExtra(android.content.Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(compartir, getString(R.string.comparte_via)));


    }

}

