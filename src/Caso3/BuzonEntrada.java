package Caso3;
import java.util.ArrayList;

public class BuzonEntrada {
    private ArrayList<Solicitud> solicitudes;
    private int capacidad;
    private int usuariosActivos;
    public BuzonEntrada(int capacidad, int numUsuarios) {
        this.capacidad=capacidad;
        this.usuariosActivos= numUsuarios;
        this.solicitudes =new ArrayList<Solicitud>();
    }
    public synchronized void depositar(Solicitud solicitud)
            throws InterruptedException {
        while (solicitudes.size() >= capacidad) {
            wait();
        }
        solicitudes.add(solicitud);
        notifyAll();
    }
    public synchronized Solicitud retirar() {
        if (solicitudes.isEmpty()) {
            return null;
        }
        Solicitud solicitud = solicitudes.remove(0);
        notifyAll();
        return solicitud;
    }

    public synchronized void informarTerminacion() {
        usuariosActivos--;
        notifyAll();
    }

    public synchronized boolean terminado() {
        return usuariosActivos==0 && solicitudes.isEmpty();
    }
}
