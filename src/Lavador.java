import java.util.logging.Logger;

public class Lavador implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Lavador.class.getName());
    private static final long CONTAGEM_BAIXO = 30_000_000L;
    private static final long CONTAGEM_MEDIO = 50_000_000L;
    private static final long CONTAGEM_ENGORDURADO = 100_000_000L;

    private final String nome;
    private final Escorredor escorredor;
    private volatile boolean done;

    public Lavador(String nome, Escorredor escorredor) {
        this.nome = nome;
        this.escorredor = escorredor;
    }

    public void parar() { done = true; }

    @Override
    public void run() {
        LOGGER.fine(nome + " começou a trabalhar");
        try {
            while (!done) {
                esperarEspaco();
                if (done) break;
                Prato prato = new Prato();
                lavar(prato);
                // Depois de iniciar a lavagem, sempre guardar o prato.
                guardar(prato);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.fine(nome + " terminou");
    }

    private void esperarEspaco() throws InterruptedException {
        Object vagas = escorredor.getMonitorVagas();
        synchronized (vagas) {
            while (!done) {
                synchronized (escorredor) {
                    if (!escorredor.estaCheio()) return;
                }
                LOGGER.fine(nome + " aguardando: escorredor cheio");
                vagas.wait();
            }
        }
    }

    private void lavar(Prato prato) {
        long limite;
        switch (prato.getNivelSujeira()) {
            case BAIXO: limite = CONTAGEM_BAIXO; break;
            case MEDIO: limite = CONTAGEM_MEDIO; break;
            default: limite = CONTAGEM_ENGORDURADO; break;
        }
        LOGGER.fine(nome + " lavando " + prato);
        Simulacao.contarAte(limite);
        LOGGER.fine(nome + " terminou de lavar " + prato);
    }

    private void guardar(Prato prato) {
        Object vagas = escorredor.getMonitorVagas();
        Object pratos = escorredor.getMonitorPratos();
        boolean interrompido = false;
        boolean guardado = false;
        while (!guardado) {
            synchronized (vagas) {
                synchronized (pratos) {
                    synchronized (escorredor) {
                        if (!escorredor.estaCheio()) {
                            boolean estavaVazio = escorredor.estaVazio();
                            escorredor.colocar(prato);
                            guardado = true;
                            if (estavaVazio) pratos.notify();
                        }
                    }
                }
                if (!guardado) {
                    try { vagas.wait(); }
                    catch (InterruptedException e) { interrompido = true; }
                }
            }
        }
        if (interrompido) Thread.currentThread().interrupt();
    }
}
