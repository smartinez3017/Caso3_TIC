package Caso3;

public class MaquinaVirtual extends Thread {

    private final String idMV;
    private final Solicitud solicitud;
    private final ZonaEspera zonaEspera;

    private final ControladorProcesadores controladorCPU;
    private final ControladorMemoria controladorMemoria;
    private final ControladorAlmacenamiento controladorAlmacenamiento;

    private long versionLiberacionParcial = -1;

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

    private boolean reservarRecursos(long versionAntesDelIntento) {

        int cantidadCPU = solicitud.getNumProcesadores();
        int cantidadMemoria = solicitud.getMemoriaGiB();
        int cantidadAlmacenamiento = solicitud.getAlmacenamientoGiB();

        if (!controladorCPU.reservar(cantidadCPU, idMV)) {

            System.out.println("[" + getName() + "] No obtuvo procesadores.");

            return false;
        }

        if (!controladorMemoria.reservar(cantidadMemoria, idMV)) {

            controladorCPU.liberar(idMV);

            versionLiberacionParcial =
                    zonaEspera.notificarLiberacionParcial(versionAntesDelIntento);

            System.out.println("[" + getName()
                    + "] No obtuvo memoria; devolvió los procesadores reservados.");

            return false;
        }

        if (!controladorAlmacenamiento.reservar(cantidadAlmacenamiento, idMV)) {

            controladorMemoria.liberar(idMV);
            controladorCPU.liberar(idMV);

            versionLiberacionParcial =
                    zonaEspera.notificarLiberacionParcial(versionAntesDelIntento);

            System.out.println("[" + getName()
                    + "] No obtuvo almacenamiento; devolvió memoria y procesadores.");

            return false;
        }

        return true;
    }

    private void liberarRecursos() {

        controladorCPU.liberar(idMV);
        controladorMemoria.liberar(idMV);
        controladorAlmacenamiento.liberar(idMV);

        zonaEspera.notificarDisponibilidad();

        System.out.println("[" + getName() + "] Liberó todos sus recursos.");
    }

    private void ejecutar() throws InterruptedException {

        long tiempo = 4000 + (long) (Math.random() * 36001);

        System.out.println("[" + getName()
                + "] Ejecutándose durante " + tiempo + " ms.");

        Thread.sleep(tiempo);
    }

    @Override
    public void run() {

        boolean recursosReservados = false;
        boolean permisoInicialPendiente = true;

        System.out.println("[" + getName() + "] Inicia. Necesita CPU="
                + solicitud.getNumProcesadores()
                + ", memoria=" + solicitud.getMemoriaGiB()
                + " GiB, almacenamiento="
                + solicitud.getAlmacenamientoGiB() + " GiB.");

        try {

            while (true) {

                long versionAntesDelIntento =
                        zonaEspera.obtenerVersionDisponibilidad();

                if (reservarRecursos(versionAntesDelIntento)) {

                    recursosReservados = true;

                    if (permisoInicialPendiente) {

                        zonaEspera.finalizarCreacion();

                        permisoInicialPendiente = false;
                    }

                    zonaEspera.retirar(this);

                    System.out.println("[" + getName()
                            + "] Reservó todos los recursos y puede comenzar.");

                    break;
                }

                if (permisoInicialPendiente) {

                    zonaEspera.agregar(this);

                    permisoInicialPendiente = false;
                }

                long versionParaEsperar = versionAntesDelIntento;

                if (versionLiberacionParcial >= 0) {

                    versionParaEsperar = versionLiberacionParcial;

                    versionLiberacionParcial = -1;
                }

                System.out.println("[" + getName()
                        + "] Espera pasivamente un cambio en la disponibilidad.");

                zonaEspera.esperarCambio(versionParaEsperar);

                System.out.println("[" + getName()
                        + "] Despertó; volverá a intentar desde los procesadores.");
            }

            ejecutar();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println("[" + getName() + "] Fue interrumpida.");

        } finally {

            if (permisoInicialPendiente) {
                zonaEspera.finalizarCreacion();
            }

            zonaEspera.retirar(this);

            if (recursosReservados) {
                liberarRecursos();
            }

            System.out.println("[" + getName() + "] Finalizó.");
        }
    }
}