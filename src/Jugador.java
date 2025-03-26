import java.util.*;
import javax.swing.JPanel;

public class Jugador {

    private static final int DISTANCIA = 40;
    private static final int MARGEN = 10;
    private static final int TOTAL_CARTAS = 10;
    private static final String ENCABEZADO_MENSAJE = "Se encontraron los siguientes grupos:\n";
    private static final int MINIMO_GRUPO = 2;
    private final Carta[] cartas = new Carta[TOTAL_CARTAS];
    private final Random r = new Random();

    public void repartir() {
        for (int i = 0; i < TOTAL_CARTAS; i++) {
            cartas[i] = new Carta(r);
        }
    }

    public void mostrar(JPanel pnl) {
        pnl.removeAll();
        int x = MARGEN + (TOTAL_CARTAS - 1) * DISTANCIA;
        for (Carta carta : cartas) {
            carta.mostrar(pnl, x, MARGEN);
            x -= DISTANCIA;
        }
        pnl.repaint();
    }

    public String getGrupos() {
        Map<NombreCarta, Integer> contadores = new HashMap<>();
        Map<Pinta, List<Carta>> cartasPorPinta = new HashMap<>();

        for (Carta carta : cartas) {
            contadores.merge(carta.getNombre(), 1, Integer::sum);
            cartasPorPinta.computeIfAbsent(carta.getPinta(), k -> new ArrayList<>()).add(carta);
        }

        StringBuilder mensaje = new StringBuilder();
        contadores.forEach((nombre, cantidad) -> {
            if (cantidad >= MINIMO_GRUPO) {
                mensaje.append(Grupo.values()[cantidad]).append(" de ").append(nombre).append("\n");
            }
        });

        List<String> escaleras = new ArrayList<>();
        cartasPorPinta.forEach((pinta, lista) -> {
            lista.sort(Comparator.comparingInt(c -> c.getNombre().ordinal()));

            List<String> escalera = new ArrayList<>();
            for (int i = 0; i < lista.size(); i++) {
                if (!escalera.isEmpty() && lista.get(i).getNombre().ordinal() != NombreCarta.valueOf(escalera.get(escalera.size() - 1)).ordinal() + 1) {
                    if (escalera.size() >= 3)
                        escaleras.add("Escalera de " + pinta + ": " + String.join(", ", escalera));
                    escalera.clear();
                }
                escalera.add(lista.get(i).getNombre().name());
            }
            if (escalera.size() >= 3) escaleras.add("Escalera de " + pinta + ": " + String.join(", ", escalera));

            if (lista.stream().anyMatch(c -> c.getNombre() == NombreCarta.KING) &&
                    lista.stream().anyMatch(c -> c.getNombre() == NombreCarta.AS)) {

                List<String> escaleraCiclica = new ArrayList<>();
                escaleraCiclica.add("KING");
                escaleraCiclica.add("AS");

                for (Carta carta : lista) {
                    if (carta.getNombre() != NombreCarta.KING && carta.getNombre() != NombreCarta.AS) {
                        escaleraCiclica.add(carta.getNombre().name());
                    }
                }

                if (escaleraCiclica.size() >= 3) {
                    escaleras.add("Escalera de " + pinta + ": " + String.join(", ", escaleraCiclica));
                }
            }
        });

        int puntaje = Arrays.stream(cartas)
                .filter(carta -> contadores.get(carta.getNombre()) == 1) // Solo cartas que aparecen 1 vez
                .mapToInt(carta -> switch (carta.getNombre()) {
                    case AS, JACK, QUEEN, KING -> 10;
                    default -> carta.getNombre().ordinal() + 1;
                })
                .sum();

        if (!mensaje.isEmpty()) mensaje.insert(0, ENCABEZADO_MENSAJE);
        if (!escaleras.isEmpty()) mensaje.append(String.join("\n", escaleras)).append("\n");
        mensaje.append("Puntaje de cartas no usadas en figuras: ").append(puntaje);

        return mensaje.toString();
    }
}