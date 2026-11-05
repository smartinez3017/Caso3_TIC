package Caso3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        String nombreArchivo = args.length > 0 ? args[0] : "config.txt";

        try {

            Properties configuracion = cargarConfiguracion(nombreArchivo);

            int numeroUsuarios = obtenerEntero(configuracion, "nu");
            int base = obtenerEntero(configuracion, "base");
            int numeroGestores = obtenerEntero(configuracion, "ng");

            int tamBuzon = obtenerEntero(configuracion, "tam1");
            int tamZonaEspera = obtenerEntero(configuracion, "tam2");

            int cantidadProcesadores = obtenerEntero(configuracion, "procesadores");

            int memoriaGiB = obtenerEntero(configuracion, "memoria");
            int tamPaginaKiB = obtenerEntero(configuracion, "pagina");

            int almacenamientoGiB = obtenerEntero(configuracion, "almacenamiento");
            int tamBloqueKiB = obtenerEntero(configuracion, "bloque");

            // El enunciado establece un umbral, pero no lo enumera
            // expresamente entre los parámetros configurables
            // asi que si no se especifica, utilizamos tam2.

            int umbral = configuracion.getProperty("umbral") == null
                    ? tamZonaEspera
                    : obtenerEntero(configuracion, "umbral");

            validarConfiguracion(
                    numeroUsuarios,
                    base,
                    numeroGestores,
                    tamBuzon,
                    tamZonaEspera,
                    umbral,
                    cantidadProcesadores,
                    memoriaGiB,
                    tamPaginaKiB,
                    almacenamientoGiB,
                    tamBloqueKiB
            );

            // Crear una única instancia de cada objeto compartido

            BuzonEntrada buzon =
                    new BuzonEntrada(tamBuzon, numeroUsuarios);

            ZonaEspera zonaEspera =
                    new ZonaEspera(tamZonaEspera, umbral);

            ControladorProcesadores controladorCPU =
                    new ControladorProcesadores(cantidadProcesadores);

            ControladorMemoria controladorMemoria =
                    new ControladorMemoria(memoriaGiB, tamPaginaKiB);

            ControladorAlmacenamiento controladorAlmacenamiento =
                    new ControladorAlmacenamiento(
                            almacenamientoGiB,
                            tamBloqueKiB
                    );

            // Crear usuarios

            Usuario[] usuarios = new Usuario[numeroUsuarios];

            for (int i = 0; i < numeroUsuarios; i++) {

                usuarios[i] = new Usuario(
                        i + 1,
                        base,
                        buzon,
                        cantidadProcesadores,
                        memoriaGiB,
                        almacenamientoGiB
                );

                usuarios[i].setName("Usuario-" + (i + 1));
            }

            // Crear gestores

            GestorSolicitudes[] gestores =
                    new GestorSolicitudes[numeroGestores];

            for (int i = 0; i < numeroGestores; i++) {

                gestores[i] = new GestorSolicitudes(
                        i + 1,
                        buzon,
                        zonaEspera,
                        umbral,
                        controladorCPU,
                        controladorMemoria,
                        controladorAlmacenamiento
                );

                gestores[i].setName("Gestor-" + (i + 1));
            }

            long solicitudesEsperadas =
                    (long) base * numeroUsuarios * (numeroUsuarios + 1L) / 2;

            // Mostrar configuración inicial

            System.out.println("====================================================");
            System.out.println(" SIMULADOR DE GESTIÓN DE RECURSOS PARA MÁQUINAS VIRTUALES");
            System.out.println("====================================================");

            System.out.println("Configuración:");

            System.out.println("  Usuarios: " + numeroUsuarios
                    + " (solicitudes esperadas: " + solicitudesEsperadas + ")");

            System.out.println("  Gestores: " + numeroGestores);

            System.out.println("  Buzón: capacidad " + tamBuzon);

            System.out.println("  Zona de espera: capacidad "
                    + tamZonaEspera + " | umbral " + umbral);

            System.out.println("  Procesadores: " + cantidadProcesadores);

            System.out.println("  Memoria: " + memoriaGiB
                    + " GiB | página: " + tamPaginaKiB + " KiB");

            System.out.println("  Almacenamiento: " + almacenamientoGiB
                    + " GiB | bloque: " + tamBloqueKiB + " KiB");

            System.out.println("----------------------------------------------------");
            System.out.println("INICIO DE LA EJECUCIÓN");
            System.out.println("----------------------------------------------------");

            // Iniciar productores y consumidores

            for (Usuario usuario : usuarios) {
                usuario.start();
            }

            for (GestorSolicitudes gestor : gestores) {
                gestor.start();
            }

            // Esperar a que terminen todos los usuarios

            boolean interrupcionDuranteJoin = false;

            for (Usuario usuario : usuarios) {

                interrupcionDuranteJoin |= unirSinAbandonar(usuario);
            }

            // Esperar a que terminen todos los gestores

            for (GestorSolicitudes gestor : gestores) {

                interrupcionDuranteJoin |= unirSinAbandonar(gestor);
            }

            // Recuperar todas las máquinas virtuales creadas

            List<MaquinaVirtual> todasLasMVs = new ArrayList<>();

            for (GestorSolicitudes gestor : gestores) {

                todasLasMVs.addAll(gestor.obtenerMaquinasCreadas());
            }

            // Esperar a que todas las máquinas virtuales terminen

            for (MaquinaVirtual mv : todasLasMVs) {

                interrupcionDuranteJoin |= unirSinAbandonar(mv);
            }

            // Comprobar condiciones finales

            boolean usuariosFinalizados = todosTerminaron(usuarios);

            boolean gestoresFinalizados = todosTerminaron(gestores);

            boolean maquinasFinalizadas = todasTerminaron(todasLasMVs);

            boolean buzonVacio =
                    buzon.estaVacio() && buzon.getUsuariosActivos() == 0;

            boolean zonaVacia = zonaEspera.estaVacia();

            boolean cpuLibre = controladorCPU.estaTodoLibre();

            boolean memoriaLibre = controladorMemoria.estaTodoLibre();

            boolean almacenamientoLibre =
                    controladorAlmacenamiento.estaTodoLibre();

            // Mostrar resumen

            System.out.println("\n====================================================");
            System.out.println(" RESUMEN Y VALIDACIÓN DEL ESTADO FINAL");
            System.out.println("====================================================");

            System.out.println("Solicitudes esperadas: " + solicitudesEsperadas);

            System.out.println("Máquinas virtuales creadas: " + todasLasMVs.size());

            System.out.println("Usuarios finalizados: " + usuariosFinalizados);

            System.out.println("Gestores finalizados: " + gestoresFinalizados);

            System.out.println("Todas las MVs finalizaron: " + maquinasFinalizadas);

            System.out.println("Buzón vacío y sin usuarios activos: " + buzonVacio);

            System.out.println("Zona de espera vacía: " + zonaVacia
                    + " (pendientes registrados: "
                    + zonaEspera.cantidadEnEspera() + ")");

            System.out.println("Procesadores completamente libres: " + cpuLibre
                    + " (asignados: "
                    + controladorCPU.cantidadAsignada() + ")");

            System.out.println("Memoria completamente libre: " + memoriaLibre
                    + " (páginas asignadas: "
                    + controladorMemoria.cantidadAsignada() + ")");

            System.out.println("Almacenamiento completamente libre: "
                    + almacenamientoLibre
                    + " (bloques asignados: "
                    + controladorAlmacenamiento.cantidadAsignada() + ")");

            boolean validacionCorrecta =
                    usuariosFinalizados
                    && gestoresFinalizados
                    && maquinasFinalizadas
                    && buzonVacio
                    && zonaVacia
                    && cpuLibre
                    && memoriaLibre
                    && almacenamientoLibre
                    && todasLasMVs.size() == solicitudesEsperadas;

            System.out.println("----------------------------------------------------");

            if (validacionCorrecta) {

                System.out.println("RESULTADO: todas las condiciones finales se cumplieron.");

            } else {

                System.out.println("RESULTADO: hay condiciones finales que requieren revisión.");
            }

            System.out.println("====================================================");

            if (interrupcionDuranteJoin) {

                Thread.currentThread().interrupt();

                System.err.println(
                        "El hilo principal recibió una interrupción durante join(); "
                        + "se completó la espera y se restauró el estado de interrupción."
                );
            }

        } catch (IOException e) {

            System.err.println("No se pudo leer el archivo de configuración '"
                    + nombreArchivo + "': " + e.getMessage());

        } catch (IllegalArgumentException e) {

            System.err.println("Error en la configuración: " + e.getMessage());
        }
    }

    private static Properties cargarConfiguracion(String nombreArchivo)
            throws IOException {

        Properties configuracion = new Properties();

        Path ruta = Path.of(nombreArchivo);

        try (InputStream entrada = Files.newInputStream(ruta)) {

            configuracion.load(entrada);
        }

        return configuracion;
    }

    private static int obtenerEntero(Properties configuracion, String clave) {

        String valor = configuracion.getProperty(clave);

        if (valor == null || valor.isBlank()) {

            throw new IllegalArgumentException(
                    "Falta el parámetro obligatorio '" + clave + "'."
            );
        }

        try {

            return Integer.parseInt(valor.trim());

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El parámetro '" + clave + "' debe ser un número entero."
            );
        }
    }

    private static void validarConfiguracion(
            int nu,
            int base,
            int ng,
            int tam1,
            int tam2,
            int umbral,
            int procesadores,
            int memoria,
            int pagina,
            int almacenamiento,
            int bloque) {

        if (nu <= 0 || base <= 0 || ng <= 0
                || tam1 <= 0 || tam2 <= 0
                || procesadores <= 0 || memoria <= 0
                || pagina <= 0 || almacenamiento <= 0
                || bloque <= 0) {

            throw new IllegalArgumentException(
                    "Todos los parámetros deben ser mayores que cero."
            );
        }

        if (umbral <= 0 || umbral > tam2) {

            throw new IllegalArgumentException(
                    "El umbral debe estar entre 1 y tam2."
            );
        }

        long memoriaKiB = (long) memoria * 1024 * 1024;

        long totalPaginas = memoriaKiB / pagina;

        if (totalPaginas <= 0 || totalPaginas > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "La cantidad total de páginas no cabe en el tipo int del controlador."
            );
        }

        long almacenamientoKiB = (long) almacenamiento * 1024 * 1024;

        long totalBloques = almacenamientoKiB / bloque;

        if (totalBloques <= 0 || totalBloques > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "La cantidad total de bloques no cabe en el tipo int del controlador."
            );
        }

        long solicitudesTotales =
                (long) base * nu * (nu + 1L) / 2;

        if (solicitudesTotales > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "La cantidad total de solicitudes es demasiado grande."
            );
        }
    }

    private static boolean todosTerminaron(Thread[] hilos) {

        for (Thread hilo : hilos) {

            if (hilo.isAlive()) {
                return false;
            }
        }

        return true;
    }

    private static boolean todasTerminaron(List<MaquinaVirtual> maquinas) {

        for (MaquinaVirtual mv : maquinas) {

            if (mv.isAlive()) {
                return false;
            }
        }

        return true;
    }

    private static boolean unirSinAbandonar(Thread hilo) {

        boolean interrumpido = false;

        while (hilo.isAlive()) {

            try {

                hilo.join();

            } catch (InterruptedException e) {

                interrumpido = true;
            }
        }

        return interrumpido;
    }
}