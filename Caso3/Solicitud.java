package Caso3;

public class Solicitud {
    private String idSolicitud;
    private int idUsuario;
    private int numProcesadores;
    private int memoriaGiB;
    private int almacenamientoGiB;
    public Solicitud(String idSolicitud, int idUsuario,int numProcesadores, int memoriaGiB,int almacenamientoGiB) {
        this.idSolicitud=idSolicitud;
        this.idUsuario=idUsuario;
        this.numProcesadores =numProcesadores;
        this.memoriaGiB= memoriaGiB;
        this.almacenamientoGiB =almacenamientoGiB;
    }
    public String getIdSolicitud() {
        return idSolicitud;
    }
    public int getIdUsuario() {
        return idUsuario;
    }
    public int getNumProcesadores() {
        return numProcesadores;
    }
    public int getMemoriaGiB() {
        return memoriaGiB;
    }
    public int getAlmacenamientoGiB() {
        return almacenamientoGiB;
    }
}

