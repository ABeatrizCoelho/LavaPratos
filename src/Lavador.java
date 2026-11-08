import java.util.logging.Logger;


public class Lavador implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Lavador.class.getName());

    private static final long CONTAGEM_BAIXO = 30_000_000L;
    private static final long CONTAGEM_MEDIO = 50_000_000L;
    private static final long CONTAGEM_ENGORDURADO = 100_000_000L;

    private final String nome;
    private final Escorredor escorredor;
    private volatile boolean done = false;

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
                Prato prato = new Prato();
                lavar(prato);
                guardar(prato);
            }
        } catch (InterruptedException e) {
            LOGGER.fine(nome + " foi interrompido");
        }
        LOGGER.fine(nome + " terminou");
    }


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
        Simulacao.contarAte(limite);
        LOGGER.fine(nome + " terminou de lavar " + prato);
    }

    private void guardar(Prato prato) throws InterruptedException {
        synchronized (escorredor) {
            while (escorredor.estaCheio()) {
                LOGGER.fine(nome + " aguardando para guardar " + prato);
                escorredor.wait();
            }
            escorredor.colocar(prato);
            if (escorredor.getQuantidade() == 1) {
                escorredor.notify();
            }
        }
    }
}
