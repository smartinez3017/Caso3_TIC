package Caso3;

import java.util.ArrayList;
import java.util.List;

public class GestorSolicitudes extends Thread {

    private final int idGestor;
    private final BuzonEntrada buzon;
    private final ZonaEspera zonaEspera;
    private final int umbral;

    private final ControladorProcesadores controladorCPU;
    private final ControladorMemoria controladorMemoria;
    private final ControladorAlmacenamiento controladorAlmacenamiento;

    private final List<MaquinaVirtual> maquinasCreadas;

    public GestorSolicitudes(
            int idGestor,
            BuzonEntrada buzon,
            ZonaEspera zonaEspera,
            int umbral,
            ControladorProcesadores controladorCPU,
            ControladorMemoria controladorMemoria,
            ControladorAlmacenamiento controladorAlmacenamiento) {

        if (idGestor <= 0 || umbral <= 0) {
            throw new IllegalArgumentException("Identificador o umbral inválido.");
        }

        this.idGestor = idGestor;
        this.buzon = buzon;
        this.zonaEspera = zonaEspera;
        this.umbral = umbral;

        this.controladorCPU = controladorCPU;
        this.controladorMemoria = controladorMemoria;
        this.controladorAlmacenamiento = controladorAlmacenamiento;

        this.maquinasCreadas = new ArrayList<>();
    }

    @Override
    public void run() {

        System.out.println("[" + getName() + "] Inicia el procesamiento de solicitudes.");

        while (!buzon.terminado()) {

            if (buzon.estaVacio()) {
                Thread.yield();
                continue;
            }

            boolean permisoPendiente = false;

            try {

                zonaEspera.esperarPermisoProcesamiento(umbral);

                permisoPendiente = true;

                Solicitud solicitud = buzon.retirar();

                if (solicitud == null) {

                    if (buzon.terminado()) {
                        break;
                    }

                    Thread.yield();
                    continue;
                }

                procesarSolicitud(solicitud);

                permisoPendiente = false;

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println("[" + getName() + "] Fue interrumpido.");

                break;

            } finally {

                if (permisoPendiente) {
                    zonaEspera.cancelarPermisoProcesamiento();
                }
            }
        }

        System.out.println("[" + getName()
                + "] Termina: todos los usuarios finalizaron y el buzón está vacío.");
    }

    private void procesarSolicitud(Solicitud solicitud) {

        System.out.println("[" + getName() + "] Retiró la solicitud "
                + solicitud.getIdSolicitud() + " del buzón.");

        MaquinaVirtual mv = crearMaquinaVirtual(solicitud);

        mv.setName("MV-" + solicitud.getIdSolicitud());

        iniciarMaquinaVirtual(mv);
    }

    private MaquinaVirtual crearMaquinaVirtual(Solicitud solicitud) {

        return new MaquinaVirtual(
                solicitud,
                zonaEspera,
                controladorCPU,
                controladorMemoria,
                controladorAlmacenamiento
        );
    }

    private void iniciarMaquinaVirtual(MaquinaVirtual mv) {

        maquinasCreadas.add(mv);

        System.out.println("[" + getName()
                + "] Crea e inicia la MV " + mv.getName() + ".");

        mv.start();
    }

    public List<MaquinaVirtual> obtenerMaquinasCreadas() {

        return new ArrayList<>(maquinasCreadas);
    }
}