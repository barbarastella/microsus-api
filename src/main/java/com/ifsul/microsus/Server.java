package com.ifsul.microsus;

import com.ifsul.microsus.http.ClientHandler;
import com.ifsul.microsus.service.GerenciadorPacientes;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORTA = 5555;
    private final GerenciadorPacientes gerenciador;

    public Server() {
        this.gerenciador = new GerenciadorPacientes();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("\n\n---------- Inicializando MicroSUS ----------");
            System.out.println("     ----- http://localhost:" + PORTA + " -----\n");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[CONEXÃO] Novo cliente conectado: " + socket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(socket, gerenciador);
                new Thread(handler).start(); // permite multithreading
            }
        } catch (IOException e) {
            System.err.println("Erro ao inicializar servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server servidor = new Server();
        servidor.iniciar();
    }
}
