import java.util.logging.Logger;

/**
 * Produtor: pega um prato sujo (pilha infinita), lava e coloca no escorredor.
 */
public class Lavador implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Lavador.class.getName());

    private static final long CONTAGEM_BAIXO = 30_000_000L;
    private static final long CONTAGEM_MEDIO = 50_000_000L;
    private static final long CONTAGEM_ENGORDURADO = 100_000_000L;

    private final String nome;
    private final Escorredor escorredor;
    private volatile boolean done = false; // volatile: lida por outra thread (App)

    public Lavador(String nome, Escorredor escorredor) {
        this.nome = nome;
        this.escorredor = escorredor;
    }

    /** Condição de término: pede para o lavador parar. */
    public void parar() {
        done = true;
    }

    @Override
    public void run() {
        LOGGER.fine(nome + " começou a trabalhar");
        try {
            while (!done) {
                esperarEspaco();
                if (done) {
                    break;
                }
                Prato prato = new Prato(); // pilha de pratos sujos "infinita"
                lavar(prato);
                guardar(prato);
            }
        } catch (InterruptedException e) {
            LOGGER.fine(nome + " foi interrompido");
        }
        LOGGER.fine(nome + " terminou");
    }

    /** Só segue adiante quando existe espaço livre no escorredor. */
    private void esperarEspaco() throws InterruptedException {
        synchronized (escorredor) {
            while (escorredor.estaCheio() && !done) {
                LOGGER.fine(nome + " aguardando: escorredor cheio");
                escorredor.wait();
            }
        }
    }

    private void lavar(Prato prato) {
        long limite;
        switch (prato.getNivelSujeira()) {
            case BAIXO:
                limite = CONTAGEM_BAIXO;
                break;
            case MEDIO:
                limite = CONTAGEM_MEDIO;
                break;
            default:
                limite = CONTAGEM_ENGORDURADO;
                break;
        }
        LOGGER.fine(nome + " lavando " + prato);
        Simulacao.contarAte(limite); // lavar fora do synchronized: não trava os outros
        LOGGER.fine(nome + " terminou de lavar " + prato);
    }

    private void guardar(Prato prato) throws InterruptedException {
        synchronized (escorredor) {
            // Outro lavador pode ter ocupado a vaga enquanto eu lavava,
            // por isso confirmo de novo antes de colocar.
            while (escorredor.estaCheio()) {
                LOGGER.fine(nome + " aguardando para guardar " + prato);
                escorredor.wait();
            }
            escorredor.colocar(prato);

            // Notifica só quando o escorredor estava vazio (agora tem 1 prato):
            // acorda um enxugador que estava descansando.
            if (escorredor.getQuantidade() == 1) {
                escorredor.notify();
            }
        }
    }
}
