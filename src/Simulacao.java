/**
 * Simula o tempo gasto em uma tarefa usando um laço de repetição que conta
 * até um limite (como pede a especificação).
 */
public final class Simulacao {

    // volatile: obriga o resultado a ser realmente gravado, então o
    // compilador JIT não pode descartar o laço inteiro por "não servir pra nada"
    private static volatile long resultado;

    private Simulacao() {
    }

    public static void contarAte(long limite) {
        long acumulador = 0;
        for (long i = 0; i < limite; i++) {
            // Cada volta depende da anterior, então o JIT não consegue
            // trocar o laço por uma conta pronta (como fazia com contador++).
            acumulador = acumulador * 31 + i;
        }
        resultado = acumulador;
    }
}
