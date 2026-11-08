
public final class Simulacao {


    private static volatile long resultado;

    private Simulacao() {
    }

    public static void contarAte(long limite) {
        long acumulador = 0;
        for (long i = 0; i < limite; i++) {
            acumulador = acumulador * 31 + i;
        }
        resultado = acumulador;
    }
}
