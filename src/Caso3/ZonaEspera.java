package Caso3;

import java.util.ArrayList;
import java.util.List;

public class ZonaEspera {

    private List<MaquinaVirtual> maquinasEnEspera;
    private int capacidad;
    private int umbral;
    private long versionDisponibilidad;

    public ZonaEspera(int capacidad, int umbral) {

        this.maquinasEnEspera = new ArrayList<>();
        this.capacidad = capacidad;
        this.umbral = umbral;
        this.versionDisponibilidad = 0;
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
    }

    public synchronized void retirar(MaquinaVirtual mv) {

        if (maquinasEnEspera.remove(mv)) {
            notifyAll();
        }
    }

    public synchronized int cantidadEnEspera() {
        return maquinasEnEspera.size();
    }

    public synchronized void esperarHastaVacia()
            throws InterruptedException {

        while (!maquinasEnEspera.isEmpty()) {
            wait();
        }
    }

    public synchronized void notificarDisponibilidad() {
        versionDisponibilidad++;
        notifyAll();
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
}