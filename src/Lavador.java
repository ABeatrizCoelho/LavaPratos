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
                // Um prato que já começou a ser lavado deve ser guardado,
                // mesmo se a parada for solicitada durante a lavagem.
                guardar(prato);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.fine(nome + " terminou");
    }

    private void esperarEspaco() throws InterruptedException {
        Object vaga = escorredor.getMonitorVagas();
        synchronized (vaga) {
            while (!done) {
                synchronized (escorredor) {
                    if (!escorredor.estaCheio()) return;
                }
                LOGGER.fine(nome + " aguardando: escorredor cheio");
                vaga.wait();
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
        Object vaga = escorredor.getMonitorVagas();
        Object pratos = escorredor.getMonitorPratos();
        boolean interrompido = false;
        boolean guardado = false;
        while (!guardado) {
            synchronized (vaga) {
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
                    try { vaga.wait(); }
                    catch (InterruptedException e) { interrompido = true; }
                }
            }
        }
        if (interrompido) Thread.currentThread().interrupt();
    }
}
