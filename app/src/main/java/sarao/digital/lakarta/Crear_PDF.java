package sarao.digital.lakarta;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;


import java.io.ByteArrayOutputStream;
import java.io.File;


public class Crear_PDF extends AppCompatActivity {


    private Toast mensajePop;
    private String mensajeAlerta="";

    TextView textoEditar;

    ImageView imagenQR;

    TextView crearPDF, nomRest, salir;

    AlertDialog enviandoDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_pdf);

        crearPDF=findViewById(R.id.generar_pdf);
        salir=findViewById(R.id.salir);
        nomRest=findViewById(R.id.nombre_restaurante);

        textoEditar=findViewById(R.id.texto_personal);

        imagenQR=findViewById(R.id.codigo_qr);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        Glide.with(getApplicationContext())
                .load(getResources().getString(R.string.imagen_qr))
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.noimage)
                .into(imagenQR);

        nomRest.setText(Menu_Empresa.miRestaurante.nombre);

        textoEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activaBoton(false, textoEditar);

                introduceTexto(textoEditar, textoEditar);
            }
        });


        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        System.out.println("PAQUETE: "+"https://play.google.com/store/apps/details?id="+(getApplicationContext().getPackageName()));

        crearPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkSelfPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);

                }else {

                    generaPDF genera = new generaPDF();

                    genera.execute();

                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==110){

            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                generaPDF genera = new generaPDF();

                genera.execute();

            }

        }
    }

    private class generaPDF extends AsyncTask<String,Integer,String> {  // carga en memoria la base de datos


        @Override
        protected void onPreExecute() {

            enviandoDatos=enviando();

            enviandoDatos.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String texto;

            if(!textoEditar.getText().toString().equals(getResources().getString(R.string.introduce_texto))){

                texto=textoEditar.getText().toString();

            }else{

                texto="";
            }


            String respuesta="nok";

            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File file = new File(pdfPath, "lakarta_qr.pdf");

            try {

                PdfWriter writer = new PdfWriter(String.valueOf(file));
                PdfDocument pdfDocument = new PdfDocument(writer);

                Color rosa=dimeColor(getResources().getColor(R.color.colorRosa1,null));

                Rectangle pagina=pdfDocument.getDefaultPageSize();

                int altura=(int)pagina.getHeight();
                int anchura=(int)pagina.getWidth();

                PdfCanvas canvas = new PdfCanvas(pdfDocument.addNewPage())
                        .setStrokeColor(rosa)
                        .setFillColor(Color.GRAY)
                        .setLineWidth(3)
                        .roundRectangle(50, 50, anchura-100, altura-100, 10)
                        .stroke()
                        .setLineWidth(0);



                Document document = new Document(pdfDocument);

                pdfDocument.setDefaultPageSize(PageSize.A4);

                document.setMargins(70, 70, 70, 70);

                Drawable imagen = getDrawable(R.drawable.logo);
                Drawable gPlay = getDrawable(R.drawable.g_play);

                Bitmap bitmap = ((BitmapDrawable) imagen).getBitmap();
                Bitmap bitmap_gplay = ((BitmapDrawable) gPlay).getBitmap();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byte[] bitmapData = stream.toByteArray();

                ImageData imageData = ImageDataFactory.create(bitmapData);

                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

                bitmap_gplay.compress(Bitmap.CompressFormat.PNG, 100, stream2);

                byte[] bitmapData_gplay = stream2.toByteArray();

                ImageData imageData_gplay = ImageDataFactory.create(bitmapData_gplay);

                Image imagenLogo = new Image(imageData).setWidth(180).setHorizontalAlignment(HorizontalAlignment.CENTER).setMarginTop(10);

                Image imagen_gplay = new Image(imageData_gplay).setWidth(60).setHorizontalAlignment(HorizontalAlignment.LEFT);

                imagen_gplay.setFixedPosition(60, 60);

                Paragraph nombreRest = new Paragraph(Menu_Empresa.miRestaurante.nombre).setTextAlignment(TextAlignment.CENTER).setFontSize(30f).setFontColor(Color.BLACK).setMarginLeft(20).setMarginRight(20);

                Paragraph textoPersonal = new Paragraph(texto).setTextAlignment(TextAlignment.CENTER).setFontSize(15f).setFontColor(Color.BLACK).setMarginLeft(50).setMarginRight(20);

                Paragraph eltexto_uno = new Paragraph(getResources().getString(R.string.mensaje_qr)).
                        setTextAlignment(TextAlignment.CENTER).setFontSize(27f).setFontColor(rosa).setBold();

                Paragraph eltexto_dos = new Paragraph(getResources().
                        getString(R.string.mensaje_qr_2)).setTextAlignment(TextAlignment.CENTER).setFontSize(18f).setMarginTop(20);

                BarcodeQRCode codeQr = new BarcodeQRCode("https://play.google.com/store/apps/details?id="+(getApplicationContext().getPackageName()));



                PdfFormXObject codeQrObject = codeQr.createFormXObject(Color.BLACK, pdfDocument);

                Image qrCode = new Image(codeQrObject).setWidth(220).setHorizontalAlignment(HorizontalAlignment.CENTER);

                document.add(nombreRest);
                document.add(eltexto_uno);
                document.add(imagenLogo);
                document.add(eltexto_dos);
                document.add(qrCode);

                document.add(textoPersonal);

                document.add(imagen_gplay);

                document.close();

                respuesta="ok";


            }catch (Exception e){

                File fi=new File(file.getPath());
                if(fi.exists()) {
                    if(fi.delete()){

                        System.out.println("BORRADO");
                    }else{

                        System.out.println("NO BORRADO");
                    }
                }
            }

            return respuesta;

        }

        protected void onPostExecute(String respuesta){

            enviandoDatos.cancel();

            if(respuesta.equals("ok")) {

                mensajeAlerta = "PDF GUARDADO EN CARPETA DESCARGAS";
                ponAlerta();
                crearPDF.setVisibility(View.GONE);

            }else{

                mensajeAlerta = "ERROR";
                ponAlerta();

            }

        }


    }


    public Color dimeColor(int color) {
        // format: #RRGGBB
        int red =android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);

        Color elColor=new DeviceRgb(red,green,blue);


        return elColor;
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

    private AlertDialog enviando(){

        LayoutInflater inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View alertLayout =inflador.inflate(R.layout.emerg_enviando_datos, null);

        TextView texto=alertLayout.findViewById(R.id.mensaje_envio);

        texto.setText(getString(R.string.creando_pdf));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;

    }
    private void introduceTexto(final TextView elTexto, final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View introTexto = inflater.inflate(R.layout.entrada_texto, null);

        final TextView entradaTexto=introTexto.findViewById(R.id.recoge_texto);
        final ImageView validaTexto=introTexto.findViewById(R.id.valida_texto);

        activaBoton(false,validaTexto);

        AlertDialog.Builder ponTexto = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        ponTexto.setView(introTexto);
        // disallow cancel of AlertDialog on click of back button and outside touch
        ponTexto.setCancelable(true);

        final AlertDialog dialogoTexto = ponTexto.create();

        entradaTexto.setText(elTexto.getText().toString());

        dialogoTexto.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //imm.hideSoftInputFromWindow(entradaTexto.getWindowToken(), 0);

                activaBoton(true,elTexto);

            }
        });

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

                elTexto.setText(entradaTexto.getEditableText().toString());

                activaBoton(true,boton);

                activaBoton(true,elTexto);

                dialogoTexto.cancel();


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

        dialogoTexto.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogoTexto.show();


    }
}