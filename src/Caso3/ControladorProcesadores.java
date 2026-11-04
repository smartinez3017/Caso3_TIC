package Caso3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ControladorProcesadores {

    private List<Integer> procesadores;
    private Map<Integer, String> asignaciones;

    public ControladorProcesadores(int cantidadProcesadores) {

        procesadores = new ArrayList<>();
        asignaciones = new HashMap<>();

        for (int i = 1; i <= cantidadProcesadores; i++) {
            procesadores.add(i);
        }
    }

    public synchronized boolean reservar(int cantidad, String idMV) {

        List<Integer> disponibles = new ArrayList<>();

        for (Integer procesador : procesadores) {
            if (!asignaciones.containsKey(procesador)) {
                disponibles.add(procesador);
            }
        }

        if (disponibles.size() < cantidad) {
            return false;
        }

        for (int i = 0; i < cantidad; i++) {
            asignaciones.put(disponibles.get(i), idMV);
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
}