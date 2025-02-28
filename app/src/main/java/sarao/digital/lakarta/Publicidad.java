package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class Publicidad implements Parcelable {

    String nombrePublicidad;
    String texto1Publi;
    String texto2Publi;
    String preguntaBoton;


    public Publicidad() {

    }


    public Publicidad(Parcel parcel){

        nombrePublicidad=parcel.readString();
        texto1Publi=parcel.readString();
        texto2Publi=parcel.readString();
        preguntaBoton=parcel.readString();


}


    public static final Creator<Publicidad> CREATOR = new Creator<Publicidad>() {
        @Override
        public Publicidad createFromParcel(Parcel in) {
            return new Publicidad(in);
        }

        @Override
        public Publicidad[] newArray(int size) {
            return new Publicidad[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(nombrePublicidad);
        parcel.writeString(texto1Publi);
        parcel.writeString(texto2Publi);
        parcel.writeString(preguntaBoton);

    }
}
