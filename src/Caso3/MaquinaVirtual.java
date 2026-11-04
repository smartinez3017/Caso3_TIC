package Caso3;

public class MaquinaVirtual extends Thread {

    private String idMV;
    private Solicitud solicitud;
    private ZonaEspera zonaEspera;

    private ControladorProcesadores controladorCPU;
    private ControladorMemoria controladorMemoria;
    private ControladorAlmacenamiento controladorAlmacenamiento;

    public MaquinaVirtual(
            Solicitud solicitud,
            ZonaEspera zonaEspera,
            ControladorProcesadores controladorCPU,
            ControladorMemoria controladorMemoria,
            ControladorAlmacenamiento controladorAlmacenamiento) {

        this.idMV = solicitud.getIdSolicitud();
        this.solicitud = solicitud;
        this.zonaEspera = zonaEspera;
        this.controladorCPU = controladorCPU;
        this.controladorMemoria = controladorMemoria;
        this.controladorAlmacenamiento = controladorAlmacenamiento;
    }

    private boolean reservarRecursos() {

        int cantidadCPU = solicitud.getNumProcesadores();
        int cantidadMemoria = solicitud.getMemoriaGiB();
        int cantidadAlmacenamiento = solicitud.getAlmacenamientoGiB();

        if (!controladorCPU.reservar(cantidadCPU, idMV)) {
            return false;
        }

        if (!controladorMemoria.reservar(cantidadMemoria, idMV)) {
            controladorCPU.liberar(idMV);
            return false;
        }

        if (!controladorAlmacenamiento.reservar(cantidadAlmacenamiento, idMV)) {
            controladorMemoria.liberar(idMV);
            controladorCPU.liberar(idMV);
            return false;
        }

        return true;
    }

    private void liberarRecursos() {

        controladorCPU.liberar(idMV);
        controladorMemoria.liberar(idMV);
        controladorAlmacenamiento.liberar(idMV);

        zonaEspera.notificarDisponibilidad();
    }

    private void ejecutar() throws InterruptedException {

        long tiempo = 4000 + (long) (Math.random() * 36001);

        Thread.sleep(tiempo);
    }

    @Override
    public void run() {

        boolean recursosReservados = false;

        try {

            while (true) {

                long version = zonaEspera.obtenerVersionDisponibilidad();

                if (reservarRecursos()) {

                    recursosReservados = true;
                    zonaEspera.retirar(this);
                    break;

                } else {

                    zonaEspera.agregar(this);
                    zonaEspera.esperarCambio(version);
                }
            }

            ejecutar();

        } catch (InterruptedException e) {

            interrupt();

        } finally {

            zonaEspera.retirar(this);

            if (recursosReservados) {
                liberarRecursos();
            }
        }
    }
}