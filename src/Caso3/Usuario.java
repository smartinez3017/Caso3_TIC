package Caso3;


public class Usuario extends Thread {
    private int idUsuario;
    private int numSolicitudes;
    private BuzonEntrada buzon;
    private int maxProcesadores;
    private int maxMemoria;
    private int maxAlmacenamiento;

    public Usuario(int idUsuario,int base,BuzonEntrada buzon,int procesadores, int memoria,int almacenamiento) {
        this.idUsuario=idUsuario;
        this.numSolicitudes = base*idUsuario;
        this.buzon = buzon;
        maxProcesadores = (int) Math.round(procesadores*0.75);
        maxMemoria = (int) Math.round(memoria*0.75);
        maxAlmacenamiento = (int) Math.round(almacenamiento*0.75);
    }
    @Override
    public void run() {

        System.out.println("[" + getName()
                + "] Inicia; generará " + numSolicitudes + " solicitudes.");

        try {

            for (int i = 1; i <= numSolicitudes; i++) {

                Solicitud solicitud = generarSolicitud(i);

                System.out.println("[" + getName() + "] Generó "
                        + solicitud.getIdSolicitud()
                        + " (CPU=" + solicitud.getNumProcesadores()
                        + ", memoria=" + solicitud.getMemoriaGiB()
                        + " GiB, almacenamiento="
                        + solicitud.getAlmacenamientoGiB() + " GiB).");

                depositar(solicitud);

                System.out.println("[" + getName()
                        + "] Depositó " + solicitud.getIdSolicitud()
                        + " en el buzón.");
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println("[" + getName()
                    + "] Fue interrumpido antes de generar todas sus solicitudes.");

        } finally {

            informarTerminacion();

            System.out.println("[" + getName()
                    + "] Terminó de producir solicitudes.");
        }
    }
    private Solicitud generarSolicitud(int numero) {
        String id =idUsuario+"-"+numero;
        int procesadores = (int)(Math.random()*maxProcesadores)+1;
        int memoria = (int)(Math.random()*maxMemoria)+1;
        int almacenamiento = ( int)(Math.random()*maxAlmacenamiento)+1;

        memoria = redondearPotenciaDeDos(memoria);
        almacenamiento = redondearMultiploDeDos(almacenamiento);
        
        return new Solicitud(id, idUsuario, procesadores,memoria, almacenamiento);
    }
    
    private int redondearPotenciaDeDos(int numero) {
        int resultado = 1;

        while (resultado * 2 <= numero) {
            resultado = resultado * 2;
        }

        return resultado;
    }

    // Si el valor generado es 1, se conserva como excepción,
    // ya que no existe un múltiplo positivo de 2 inferior
    // para los demás valores, se redondea al múltiplo de 2 inferior
    private int redondearMultiploDeDos(int numero) {
        if (numero == 1) {
            return 1;
        }

        return numero - numero % 2;
    }

    private void depositar(Solicitud solicitud) throws InterruptedException {
        buzon.depositar(solicitud);
    }

    private void informarTerminacion() {
        buzon.informarTerminacion();
    }
}

