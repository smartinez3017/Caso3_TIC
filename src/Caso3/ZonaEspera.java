package Caso3;

import java.util.ArrayList;
import java.util.List;

public class ZonaEspera {

    private final List<MaquinaVirtual> maquinasEnEspera;
    private final int capacidad;
    private final int umbral;

    private long versionDisponibilidad;

    private int creacionesEnCurso;
    private boolean esperaTotalActiva;

    public ZonaEspera(int capacidad, int umbral) {

        if (capacidad <= 0 || umbral <= 0 || umbral > capacidad) {
            throw new IllegalArgumentException("Capacidad o umbral inválido.");
        }

        this.maquinasEnEspera = new ArrayList<>();
        this.capacidad = capacidad;
        this.umbral = umbral;

        this.versionDisponibilidad = 0;
        this.creacionesEnCurso = 0;
        this.esperaTotalActiva = false;
    }

    public synchronized void esperarPermisoProcesamiento(int umbralSolicitado)
            throws InterruptedException {

        if (umbralSolicitado != umbral) {
            throw new IllegalArgumentException("El umbral del gestor no coincide con el de la zona.");
        }

        while (true) {

            int pendientes = cantidadPendienteInterna();

            if (esperaTotalActiva) {

                if (pendientes == 0) {

                    esperaTotalActiva = false;

                    System.out.println("[Zona de espera] Todas las MVs pendientes se resolvieron. Se habilita un nuevo grupo.");

                    notifyAll();

                } else {
                    wait();
                    continue;
                }
            }

            pendientes = cantidadPendienteInterna();

            if (pendientes >= umbral) {

                esperaTotalActiva = true;

                System.out.println("[Zona de espera] Se alcanzó el umbral ("
                        + umbral + "). Los gestores esperan a que se resuelvan las MVs pendientes.");

                wait();
                continue;
            }

            creacionesEnCurso++;

            if (cantidadPendienteInterna() >= umbral) {

                esperaTotalActiva = true;

                System.out.println("[Zona de espera] Se alcanzó el umbral. No se admiten más solicitudes.");
            }

            return;
        }
    }

    public synchronized void cancelarPermisoProcesamiento() {

        decrementarCreacionesEnCurso();
        notifyAll();
    }

    public synchronized void finalizarCreacion() {

        decrementarCreacionesEnCurso();
        notifyAll();
    }

    public synchronized void agregar(MaquinaVirtual mv)
            throws InterruptedException {

        if (maquinasEnEspera.contains(mv)) {
            return;
        }

        while (maquinasEnEspera.size() >= capacidad) {
            wait();
        }

        maquinasEnEspera.add(mv);

        decrementarCreacionesEnCurso();

        notifyAll();
    }

    public synchronized void retirar(MaquinaVirtual mv) {

        if (maquinasEnEspera.remove(mv)) {
            notifyAll();
        }
    }

    public synchronized int cantidadEnEspera() {
        return cantidadPendienteInterna();
    }

    public synchronized void esperarHastaVacia()
            throws InterruptedException {

        while (cantidadPendienteInterna() > 0) {
            wait();
        }
    }

    public synchronized void notificarDisponibilidad() {

        versionDisponibilidad++;
        notifyAll();
    }

    public synchronized long notificarLiberacionParcial(long versionAntesDelIntento) {

        boolean huboOtroCambio =
                versionDisponibilidad != versionAntesDelIntento;

        versionDisponibilidad++;
        notifyAll();

        return huboOtroCambio
                ? versionAntesDelIntento
                : versionDisponibilidad;
    }

    public synchronized long obtenerVersionDisponibilidad() {
        return versionDisponibilidad;
    }

    public synchronized void esperarCambio(long versionAnterior)
            throws InterruptedException {

        while (versionDisponibilidad == versionAnterior) {
            wait();
        }
    }

    public synchronized boolean estaVacia() {
        return cantidadPendienteInterna() == 0;
    }

    public synchronized int getUmbral() {
        return umbral;
    }

    private int cantidadPendienteInterna() {
        return maquinasEnEspera.size() + creacionesEnCurso;
    }

    private void decrementarCreacionesEnCurso() {

        if (creacionesEnCurso <= 0) {
            throw new IllegalStateException(
                    "No hay una creación en curso para finalizar."
            );
        }

        creacionesEnCurso--;
    }
}