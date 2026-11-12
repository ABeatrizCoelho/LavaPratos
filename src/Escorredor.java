import java.util.logging.Logger;

/**
 * Buffer limitado implementado como fila FIFO circular (sem usar Collections).
 *
 * Os métodos NÃO são synchronized: quem chama (Lavador/Enxugador) já deve
 * estar dentro de um bloco synchronized (escorredor), porque precisa
 * verificar e alterar o escorredor de forma atômica e usar wait/notify.
 */
public class Escorredor {

    private static final Logger LOGGER = Logger.getLogger(Escorredor.class.getName());

    private final Object monitorVagas = new Object();
    private final Object monitorPratos = new Object();
    public Object getMonitorVagas() { return monitorVagas; }
    public Object getMonitorPratos() { return monitorPratos; }
    private final Prato[] pratos;
    private int inicio;     // posição do próximo prato a sair
    private int fim;        // posição onde o próximo prato vai entrar
    private int quantidade; // quantos pratos há no escorredor agora

    public Escorredor(int capacidade) {
        if (capacidade <= 0) throw new IllegalArgumentException("Capacidade deve ser positiva");
        this.pratos = new Prato[capacidade];
    }

    public void colocar(Prato prato) {
        pratos[fim] = prato;
        fim = (fim + 1) % pratos.length; // volta ao início do array (circular)
        quantidade++;
        LOGGER.fine(prato + " colocado no escorredor (" + quantidade + " pratos)");

        if (estaCheio()) {
            LOGGER.info("Escorredor CHEIO: " + quantidade + " pratos");
        }
    }

    public Prato retirar() {
        Prato prato = pratos[inicio];
        pratos[inicio] = null;
        inicio = (inicio + 1) % pratos.length;
        quantidade--;
        LOGGER.fine(prato + " retirado do escorredor (" + quantidade + " pratos)");

        if (estaVazio()) {
            LOGGER.info("Escorredor VAZIO: " + quantidade + " pratos");
        }
        return prato;
    }

    public boolean estaCheio() {
        return quantidade == pratos.length;
    }

    public boolean estaVazio() {
        return quantidade == 0;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getCapacidade() {
        return pratos.length;
    }
}
