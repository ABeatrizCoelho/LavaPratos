import java.util.Random;
import java.util.logging.Logger;

/**
 * Consumidor: retira pratos do escorredor e enxuga.
 */
public class Enxugador implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Enxugador.class.getName());

    private static final int PASSO_CONTAGEM = 10_000_000;
    private static final int MIN_PASSOS = 3;  // 30.000.000
    private static final int MAX_PASSOS = 10; // 100.000.000

    private final String nome;
    private final Escorredor escorredor;
    private final Random random = new Random();
    private volatile boolean done = false;

    public Enxugador(String nome, Escorredor escorredor) {
        this.nome = nome;
        this.escorredor = escorredor;
    }

    /** Condição de término: só termina depois de esvaziar o escorredor. */
    public void parar() {
        done = true;
    }

    @Override
    public void run() {
        LOGGER.fine(nome + " começou a trabalhar");
        Prato prato = retirarPrato();
        while (prato != null) {
            enxugar(prato);
            prato = retirarPrato();
        }
        LOGGER.fine(nome + " terminou");
    }

    /** Retorna um prato, ou null se for hora de encerrar (done e escorredor vazio). */
    private Prato retirarPrato() {
        synchronized (escorredor) {
            while (escorredor.estaVazio()) {
                if (done) {
                    return null;
                }
                LOGGER.fine(nome + " descansando: escorredor vazio");
                try {
                    escorredor.wait();
                } catch (InterruptedException e) {
                    // Fomos acordados pelo App.parar(); volta ao while e confere o done.
                    LOGGER.fine(nome + " foi interrompido");
                }
            }
            Prato prato = escorredor.retirar();

            // Notifica só quando o escorredor estava cheio (agora tem uma vaga):
            // acorda um lavador que estava esperando.
            if (escorredor.getQuantidade() == escorredor.getCapacidade() - 1) {
                escorredor.notify();
            }
            return prato;
        }
    }

    private void enxugar(Prato prato) {
        // sorteia 3..10 passos de 10 milhões -> 30M, 40M, ..., 100M
        int passos = MIN_PASSOS + random.nextInt(MAX_PASSOS - MIN_PASSOS + 1);
        LOGGER.fine(nome + " enxugando " + prato);
        Simulacao.contarAte((long) passos * PASSO_CONTAGEM);
        LOGGER.fine(nome + " terminou de enxugar " + prato);
    }
}
