package Caso3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ControladorAlmacenamiento {

    private int totalBloques;
    private int tamBloqueKiB;
    private Map<Integer, String> asignaciones;

    public ControladorAlmacenamiento(int almacenamientoGiB, int tamBloqueKiB) {

        if (almacenamientoGiB <= 0 || tamBloqueKiB <= 0) {
            throw new IllegalArgumentException("Los valores deben ser positivos");
        }

        long almacenamientoKiB = (long) almacenamientoGiB * 1024 * 1024;

        this.totalBloques = (int) (almacenamientoKiB / tamBloqueKiB);
        this.tamBloqueKiB = tamBloqueKiB;
        this.asignaciones = new HashMap<>();
    }

    public synchronized boolean reservar(int almacenamientoGiB, String idMV) {

        if (almacenamientoGiB < 0 || idMV == null || idMV.isBlank()) {
            throw new IllegalArgumentException("Solicitud o identificador inválido");
        }

        long almacenamientoKiB = (long) almacenamientoGiB * 1024 * 1024;

        int bloquesNecesarios = (int) Math.round(
                (double) almacenamientoKiB / tamBloqueKiB
        );

        int bloquesLibres = totalBloques - asignaciones.size();

        if (bloquesLibres < bloquesNecesarios) {
            return false;
        }

        List<Integer> disponibles = new ArrayList<>();

        for (int i = 1; i <= totalBloques && disponibles.size() < bloquesNecesarios; i++) {

            if (!asignaciones.containsKey(i)) {
                disponibles.add(i);
            }
        }

        if (disponibles.size() < bloquesNecesarios) {
            return false;
        }

        for (Integer bloque : disponibles) {
            asignaciones.put(bloque, idMV);
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