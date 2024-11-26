import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// Classe representando um pedido
class Pedido {
    private static int contador = 0;
    private final int id;
    private final String produto;

    public Pedido(String produto) {
        this.id = ++contador;
        this.produto = produto;
    }

    public int getId() {
        return id;
    }

    public String getProduto() {
        return produto;
    }
}

// Thread que gera pedidos
class GeradorDePedidos implements Runnable {
    private final BlockingQueue<Pedido> pedidos;

    public GeradorDePedidos(BlockingQueue<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public void run() {
        String[] produtos = {"Notebook", "Smartphone", "Monitor", "Teclado", "Mouse"};
        try {
            for (int i = 0; i < 10; i++) {
                Pedido pedido = new Pedido(produtos[i % produtos.length]);
                pedidos.put(pedido);
                System.out.println("[Gerador] Pedido criado: " + pedido.getId() + " - " + pedido.getProduto());
                Thread.sleep(500); // Simula tempo de geração
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Thread que processa pedidos
class ProcessadorDePedidos implements Runnable {
    private final BlockingQueue<Pedido> pedidos;
    private final BlockingQueue<Pedido> pedidosProcessados;

    public ProcessadorDePedidos(BlockingQueue<Pedido> pedidos, BlockingQueue<Pedido> pedidosProcessados) {
        this.pedidos = pedidos;
        this.pedidosProcessados = pedidosProcessados;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Pedido pedido = pedidos.poll(2, TimeUnit.SECONDS); // Espera por pedidos
                if (pedido == null) break; // Encerra se não houver mais pedidos

                System.out.println("[Processador] Processando pedido: " + pedido.getId());
                Thread.sleep(1000); // Simula tempo de processamento
                pedidosProcessados.put(pedido);
                System.out.println("[Processador] Pedido processado: " + pedido.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Thread que envia notificações
class Notificador implements Runnable {
    private final BlockingQueue<Pedido> pedidosProcessados;

    public Notificador(BlockingQueue<Pedido> pedidosProcessados) {
        this.pedidosProcessados = pedidosProcessados;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Pedido pedido = pedidosProcessados.poll(2, TimeUnit.SECONDS); // Espera por pedidos processados
                if (pedido == null) break; // Encerra se não houver mais pedidos processados

                System.out.println("[Notificador] Enviando notificação para pedido: " + pedido.getId());
                Thread.sleep(500); // Simula tempo de envio de notificação
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Classe principal
public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Pedido> pedidos = new LinkedBlockingQueue<>();
        BlockingQueue<Pedido> pedidosProcessados = new LinkedBlockingQueue<>();

        // Criando threads
        Thread gerador = new Thread(new GeradorDePedidos(pedidos));
        Thread processador = new Thread(new ProcessadorDePedidos(pedidos, pedidosProcessados));
        Thread notificador = new Thread(new Notificador(pedidosProcessados));

        // Iniciando threads
        gerador.start();
        processador.start();
        notificador.start();

        // Aguardando término das threads
        gerador.join();
        processador.join();
        notificador.join();

        System.out.println("[Main] Processamento concluído.");
    }
}
