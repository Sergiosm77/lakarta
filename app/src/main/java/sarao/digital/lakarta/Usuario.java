package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class Usuario implements Parcelable {

    String nombre;
    String email;
    String avatar;
    int puede_comentar;


    public Usuario() {

    }


    public Usuario(Parcel parcel){

        nombre=parcel.readString();
        email=parcel.readString();
        avatar=parcel.readString();
        puede_comentar=parcel.readInt();


}


    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(nombre);
        parcel.writeString(email);
        parcel.writeString(avatar);
        parcel.writeInt(puede_comentar);

    }
}
