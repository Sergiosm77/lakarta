package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class Restaurantes implements Parcelable {

    String nombre;
    String poblacion;
    String email;
    String codigo;
    int telefono;
    String tipo_comida;
    String detalle;
    String tags;
    String logo;
    String imagen_principal;

    int cN;
    int cD;
    int cP;
    int fN;
    int fD;

    int cNP;
    int cDP;
    int fKarta;
    int fDP;

    int tBordes;
    int fBordes;

    int contaComentario;
    int contaLike;

    int permiteComentarios;
    int desdeDondeComentarios;

    String latitud;
    String longitud;
    int online;
    int actualizando;

    int favorito;

    //Location donde;


    public Restaurantes(){}

    public Restaurantes(Parcel parcel){

        nombre=parcel.readString();
        poblacion=parcel.readString();
        email=parcel.readString();
        codigo=parcel.readString();
        telefono=parcel.readInt();
        tipo_comida=parcel.readString();
        detalle=parcel.readString();
        tags=parcel.readString();
        logo=parcel.readString();
        imagen_principal=parcel.readString();

        cN=parcel.readInt();
        cD=parcel.readInt();
        cP=parcel.readInt();
        fN=parcel.readInt();
        fD=parcel.readInt();

        cNP=parcel.readInt();
        cDP=parcel.readInt();
        fKarta=parcel.readInt();
        fDP=parcel.readInt();

        tBordes=parcel.readInt();
        fBordes=parcel.readInt();

        contaComentario=parcel.readInt();
        contaLike=parcel.readInt();

        permiteComentarios=parcel.readInt();
        desdeDondeComentarios=parcel.readInt();

        latitud=parcel.readString();
        longitud=parcel.readString();
        online=parcel.readInt();
        actualizando=parcel.readInt();

        favorito=parcel.readInt();

        //donde=Location.CREATOR.createFromParcel(parcel);



    }


    public static final Parcelable.Creator<Restaurantes> CREATOR = new Parcelable.Creator<Restaurantes>() {
        @Override
        public Restaurantes createFromParcel(Parcel in) {
            return new Restaurantes(in);
        }

        @Override
        public Restaurantes[] newArray(int size) {
            return new Restaurantes[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(nombre);
        parcel.writeString(poblacion);
        parcel.writeString(email);
        parcel.writeString(codigo);
        parcel.writeInt(telefono);
        parcel.writeString(tipo_comida);
        parcel.writeString(detalle);
        parcel.writeString(tags);
        parcel.writeString(logo);
        parcel.writeString(imagen_principal);

        parcel.writeInt(cN);
        parcel.writeInt(cD);
        parcel.writeInt(cP);
        parcel.writeInt(fN);
        parcel.writeInt(fD);

        parcel.writeInt(cNP);
        parcel.writeInt(cDP);
        parcel.writeInt(fKarta);
        parcel.writeInt(fDP);

        parcel.writeInt(tBordes);
        parcel.writeInt(fBordes);

        parcel.writeInt(contaComentario);
        parcel.writeInt(contaLike);

        parcel.writeInt(permiteComentarios);
        parcel.writeInt(desdeDondeComentarios);

        parcel.writeString(latitud);
        parcel.writeString(longitud);
        parcel.writeInt(online);
        parcel.writeInt(actualizando);

        parcel.writeInt(favorito);

        //donde.writeToParcel(parcel, flags);



    }
}
