
# **Como a classe `Thread` pode violar o Princípio de Substituição de Liskov (LSP)**

O **Princípio de Substituição de Liskov (LSP)**, parte dos princípios **SOLID**, afirma que **uma classe derivada deve ser substituível por sua classe base sem alterar o comportamento esperado do programa**. 

Se um código funciona com a classe base, ele deve continuar funcionando com qualquer subclasse dela.

---

## **Como a classe `Thread` pode violar o LSP?**

### **1. Alteração do comportamento esperado**
Quando uma subclasse de `Thread` modifica métodos como `start()` ou `run()` de forma inesperada, ela pode quebrar as expectativas de quem utiliza a classe base. 

#### **Exemplo de violação:**
```java
class MinhaThread extends Thread {
    @Override
    public void start() {
        // Comportamento alterado inesperadamente
        System.out.println("Thread não será iniciada normalmente.");
    }
}

public class Main {
    public static void main(String[] args) {
        Thread t = new MinhaThread();
        t.start(); // Comportamento diferente do esperado
    }
}
```

No exemplo acima:
- A subclasse `MinhaThread` altera o comportamento do método `start()`.
- O programa que espera o comportamento padrão de `Thread` (iniciar a execução de `run()`) não funciona corretamente.

---

## **Como evitar a violação do LSP com threads?**

A melhor prática é **evitar estender diretamente a classe `Thread`** e, em vez disso, implementar a interface **`Runnable`**. Isso separa a lógica da tarefa do gerenciamento de threads, respeitando o LSP.

#### **Exemplo correto:**
```java
class MinhaTarefa implements Runnable {
    @Override
    public void run() {
        System.out.println("Executando tarefa...");
    }
}

public class Main {
    public static void main(String[] args) {
        Thread t = new Thread(new MinhaTarefa());
        t.start(); // Comportamento previsível
    }
}
```

### Por que funciona:
- A lógica da tarefa está isolada na classe `MinhaTarefa`.
- O gerenciamento da thread é feito pela classe `Thread`, mantendo seu comportamento padrão.



## **Resumo**

A classe `Thread` pode violar o **Princípio de Substituição de Liskov (LSP)** quando:
1. Seus métodos (`start()`, `run()`) são sobrescritos de forma que contradizem o comportamento esperado.
2. Ela combina responsabilidades que deveriam estar separadas.




## Aplicação de runnable em spring boot

1 - Primeiro, habilite o suporte para métodos assíncronos:


```java
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuração para habilitar métodos assíncronos
}

```

2 - Crie uma classe para executar uma tarefa:
```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MinhaTarefaAsync {

    @Async
    public void executarTarefa() {
        System.out.println("Tarefa assíncrona em execução: " + Thread.currentThread().getName());
    }
}

```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MinhaTarefaController {

    @Autowired
    private MinhaTarefaAsync minhaTarefaAsync;

    @GetMapping("/executar")
    public String executar() {
        minhaTarefaAsync.executarTarefa();
        return "Tarefa enviada para execução!";
    }
}

```






# Simulação de Processamento de Pedidos com Threads

Este projeto demonstra o uso de threads em Java para simular um sistema de processamento de pedidos em um comércio eletrônico. O exemplo utiliza múltiplas threads que realizam tarefas interdependentes, como geração, processamento e notificação de pedidos.

---

## Funcionalidades

- **Geração de pedidos:** Uma thread cria pedidos e os coloca em uma fila compartilhada.
- **Processamento de pedidos:** Outra thread processa os pedidos gerados e os coloca em outra fila.
- **Envio de notificações:** Uma terceira thread envia notificações para os pedidos processados.
- Uso de **BlockingQueue** para comunicação segura entre threads.

---

## Requisitos

- Java 8 ou superior.

---

## Como Executar

1. **Clone ou copie o repositório.**
2. **Compile o código:**
   ```bash
   javac Main.java



```java
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

```
Saida: 

[Processador] Processando pedido: 1

[Gerador] Pedido criado: 1 - Notebook

[Gerador] Pedido criado: 2 - Smartphone

[Processador] Pedido processado: 1

[Processador] Processando pedido: 2

[Notificador] Enviando notificação para pedido: 1

[Gerador] Pedido criado: 3 - Monitor

[Gerador] Pedido criado: 4 - Teclado

[Notificador] Enviando notificação para pedido: 2

[Processador] Pedido processado: 2

[Processador] Processando pedido: 3

[Gerador] Pedido criado: 5 - Mouse

[Gerador] Pedido criado: 6 - Notebook

[Processador] Pedido processado: 3

[Notificador] Enviando notificação para pedido: 3

[Processador] Processando pedido: 4

[Gerador] Pedido criado: 7 - Smartphone

[Gerador] Pedido criado: 8 - Monitor

[Processador] Pedido processado: 4

[Processador] Processando pedido: 5

[Notificador] Enviando notificação para pedido: 4

[Gerador] Pedido criado: 9 - Teclado

[Gerador] Pedido criado: 10 - Mouse

[Processador] Pedido processado: 5

[Processador] Processando pedido: 6

[Notificador] Enviando notificação para pedido: 5

[Processador] Pedido processado: 6

[Notificador] Enviando notificação para pedido: 6

[Processador] Processando pedido: 7

[Processador] Pedido processado: 7

[Processador] Processando pedido: 8

[Notificador] Enviando notificação para pedido: 7

[Processador] Pedido processado: 8

[Notificador] Enviando notificação para pedido: 8

[Processador] Processando pedido: 9

[Processador] Pedido processado: 9

[Notificador] Enviando notificação para pedido: 9

[Processador] Processando pedido: 10

[Processador] Pedido processado: 10

[Notificador] Enviando notificação para pedido: 10

[Main] Processamento concluído.
