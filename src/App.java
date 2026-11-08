import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cria e controla a aplicação Lavador de Pratos.
 * Uso: java App [capacidade] [qtdLavadores] [qtdEnxugadores]
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    // Durante o desenvolvimento use Level.FINE; para entregar, Level.INFO.
    private static final Level NIVEL_LOG = Level.INFO;
    private static final long TEMPO_EXECUCAO_MS = 60_000; // 1 minuto

    private final Escorredor escorredor;
    private final int qtdLavadores;
    private final int qtdEnxugadores;

    private final List<Lavador> lavadores = new ArrayList<>();
    private final List<Enxugador> enxugadores = new ArrayList<>();
    private final List<Thread> threadsLavadores = new ArrayList<>();
    private final List<Thread> threadsEnxugadores = new ArrayList<>();

    public App(int capacidade, int qtdLavadores, int qtdEnxugadores) {
        this.escorredor = new Escorredor(capacidade);
        this.qtdLavadores = qtdLavadores;
        this.qtdEnxugadores = qtdEnxugadores;
    }

    public void iniciar() {
        LOGGER.info("Iniciando: " + qtdLavadores + " lavador(es), "
                + qtdEnxugadores + " enxugador(es), escorredor com "
                + escorredor.getCapacidade() + " lugares");

        for (int i = 1; i <= qtdLavadores; i++) {
            Lavador lavador = new Lavador("Lavador-" + i, escorredor);
            lavadores.add(lavador);
            threadsLavadores.add(new Thread(lavador));
        }
        for (int i = 1; i <= qtdEnxugadores; i++) {
            Enxugador enxugador = new Enxugador("Enxugador-" + i, escorredor);
            enxugadores.add(enxugador);
            threadsEnxugadores.add(new Thread(enxugador));
        }

        threadsLavadores.forEach(Thread::start);
        threadsEnxugadores.forEach(Thread::start);
    }

    public void parar() throws InterruptedException {
        LOGGER.info("Encerrando os trabalhos...");

        // 1º: para os lavadores (ninguém coloca mais pratos no escorredor)
        lavadores.forEach(Lavador::parar);
        threadsLavadores.forEach(Thread::interrupt); // acorda quem está no wait()
        for (Thread thread : threadsLavadores) {
            thread.join();
        }

        // 2º: os enxugadores esvaziam o escorredor e só então terminam
        enxugadores.forEach(Enxugador::parar);
        threadsEnxugadores.forEach(Thread::interrupt);
        for (Thread thread : threadsEnxugadores) {
            thread.join();
        }

        LOGGER.info("Trabalhos encerrados. Pratos no escorredor: "
                + escorredor.getQuantidade());
    }

    private static void configurarLog(Level nivel) {
        Logger raiz = Logger.getLogger("");
        raiz.setLevel(nivel);
        for (Handler handler : raiz.getHandlers()) {
            handler.setLevel(nivel);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        configurarLog(NIVEL_LOG);

        int capacidade = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        int qtdLavadores = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        int qtdEnxugadores = args.length > 2 ? Integer.parseInt(args[2]) : 2;

        App app = new App(capacidade, qtdLavadores, qtdEnxugadores);
        app.iniciar();
        Thread.sleep(TEMPO_EXECUCAO_MS);
        app.parar();
    }
}
