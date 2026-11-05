package Caso3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ControladorMemoria {

    private int totalPaginas;
    private int tamPaginaKiB;
    private Map<Integer, String> asignaciones;

    public ControladorMemoria(int memoriaGiB, int tamPaginaKiB) {

        if (memoriaGiB <= 0 || tamPaginaKiB <= 0) {
            throw new IllegalArgumentException("Los valores deben ser positivos");
        }

        long memoriaKiB = (long) memoriaGiB * 1024 * 1024;
        long paginas = memoriaKiB / tamPaginaKiB;

        if (paginas == 0 || paginas > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Capacidad de memoria inválida");
        }

        this.totalPaginas = (int) paginas;
        this.tamPaginaKiB = tamPaginaKiB;
        this.asignaciones = new HashMap<>();
    }

    public synchronized boolean reservar(int memoriaGiB, String idMV) {

        if (memoriaGiB <= 0 || idMV == null || idMV.isBlank()) {
            throw new IllegalArgumentException("Solicitud o identificador inválido");
        }

        long memoriaKiB = (long) memoriaGiB * 1024 * 1024;

        long paginasNecesariasLong = Math.round(
                (double) memoriaKiB / tamPaginaKiB
        );

        if (paginasNecesariasLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Solicitud de memoria demasiado grande");
        }

        int paginasNecesarias = (int) paginasNecesariasLong;

        int paginasLibres = totalPaginas - asignaciones.size();

        if (paginasLibres < paginasNecesarias) {
            return false;
        }

        List<Integer> disponibles = new ArrayList<>();

        for (int i = 1; i <= totalPaginas && disponibles.size() < paginasNecesarias; i++) {

            if (!asignaciones.containsKey(i)) {
                disponibles.add(i);
            }
        }

        if (disponibles.size() < paginasNecesarias) {
            return false;
        }

        for (Integer pagina : disponibles) {
            asignaciones.put(pagina, idMV);
        }

        return true;
    }

    public synchronized void liberar(String idMV) {

        Iterator<Map.Entry<Integer, String>> iterador =
                asignaciones.entrySet().iterator();

        while (iterador.hasNext()) {

            Map.Entry<Integer, String> asignacion = iterador.next();

            if (asignacion.getValue().equals(idMV)) {
                iterador.remove();
            }
        }
    }

    public synchronized boolean estaTodoLibre() {
        return asignaciones.isEmpty();
    }

    public synchronized int cantidadAsignada() {
        return asignaciones.size();
    }
}