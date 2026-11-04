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
        try {
            for (int i = 1; i <= numSolicitudes; i++) {
                Solicitud solicitud = generarSolicitud(i);
                depositar(solicitud);
            }
        } catch (InterruptedException e) {
            interrupt();
        } finally {
            informarTerminacion();
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

    private int redondearMultiploDeDos(int numero) {
        return numero - (numero % 2);
    }

    private void depositar(Solicitud solicitud) throws InterruptedException {
        buzon.depositar(solicitud);
    }

    private void informarTerminacion() {
        buzon.informarTerminacion();
    }
}

