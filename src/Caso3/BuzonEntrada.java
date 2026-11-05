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

        boolean avisoMostrado = false;

        while (solicitudes.size() >= capacidad) {

            if (!avisoMostrado) {
                System.out.println("[Buzón] Está lleno; "
                        + Thread.currentThread().getName()
                        + " espera para depositar.");
                avisoMostrado = true;
            }

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

        if (usuariosActivos > 0) {
            usuariosActivos--;
        }

        notifyAll();
    }

    public synchronized boolean terminado() {
        return usuariosActivos==0 && solicitudes.isEmpty();
    }

    public synchronized boolean estaVacio() {
        return solicitudes.isEmpty();
    }

    public synchronized int cantidadEnBuzon() {
        return solicitudes.size();
    }

    public synchronized int getUsuariosActivos() {
        return usuariosActivos;
    }

}
