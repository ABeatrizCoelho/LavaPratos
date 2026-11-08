import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa um prato sujo. O número de série é sequencial e o nível de
 * sujeira é sorteado no construtor (30% BAIXO, 60% MEDIO, 10% ENGORDURADO).
 */
public class Prato {

    public enum NivelSujeira {
        BAIXO, MEDIO, ENGORDURADO
    }

    // AtomicInteger: vários lavadores criam pratos ao mesmo tempo, então o
    // contador precisa ser seguro para uso concorrente.
    private static final AtomicInteger CONTADOR = new AtomicInteger(0);
    private static final Random RANDOM = new Random();

    private final int numeroSerie;
    private final NivelSujeira nivelSujeira;

    public Prato() {
        this.numeroSerie = CONTADOR.incrementAndGet();

        int sorteio = RANDOM.nextInt(100); // 0 a 99
        if (sorteio < 30) {
            this.nivelSujeira = NivelSujeira.BAIXO;        // 0-29  -> 30%
        } else if (sorteio < 90) {
            this.nivelSujeira = NivelSujeira.MEDIO;        // 30-89 -> 60%
        } else {
            this.nivelSujeira = NivelSujeira.ENGORDURADO;  // 90-99 -> 10%
        }
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public NivelSujeira getNivelSujeira() {
        return nivelSujeira;
    }

    @Override
    public String toString() {
        return "Prato #" + numeroSerie + " (" + nivelSujeira + ")";
    }
}
