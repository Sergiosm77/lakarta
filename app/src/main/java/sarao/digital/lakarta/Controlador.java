package sarao.digital.lakarta;

import android.view.View;

public class Controlador {


    public Controlador(){



    }


    public void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

}
