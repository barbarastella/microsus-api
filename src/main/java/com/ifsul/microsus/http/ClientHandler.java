package com.ifsul.microsus.http;

import com.ifsul.microsus.service.GerenciadorAutenticacao;
import com.ifsul.microsus.service.GerenciadorPacientes;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GerenciadorPacientes gerenciador;
    private final GerenciadorAutenticacao autenticacao;

    public ClientHandler(Socket socket, GerenciadorPacientes gerenciador, GerenciadorAutenticacao autenticacao) {
        this.socket = socket;
        this.gerenciador = gerenciador;
        this.autenticacao = autenticacao;
    }

    // 1. Parsear requisições HTTP (request line, headers, body) diretamente do socket TCP
    @Override
    public void run() {
        try (
            Socket conexaoAberta = this.socket;
                BufferedReader input = new BufferedReader(new InputStreamReader(conexaoAberta.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter output = new PrintWriter(conexaoAberta.getOutputStream(), true, StandardCharsets.UTF_8)
            ) {

            HttpParser parser = new HttpParser();
            String linha;
            Boolean primeiraLinha = true;
            Integer contentLength = 0;

            do {
                linha = input.readLine();

                if (linha == null) {
                    break;
                }

                System.out.println("> " + linha);
                
                if (primeiraLinha) { // request line
                    String [] partes = linha.split(" ");

                    if (partes.length >= 3) {
                        parser.setMethod(partes[0]);
                        parser.setPath(partes[1]);
                        parser.setHttpVersion(partes[2]);
                    }

                    primeiraLinha = false;
                    
                } else if (!linha.isEmpty()) { // headers
                    String [] headerParts = linha.split(":", 2);

                    if (headerParts.length == 2) {
                        parser.setHeader(headerParts[0].trim(), headerParts[1].trim());
                    }

                    if (headerParts[0].trim().equalsIgnoreCase("Content-Length")) {
                        contentLength = Integer.valueOf(headerParts[1].trim());
                    }
                }
            } while (!linha.isEmpty());

            if (contentLength > 0) { // body
                CharBuffer bodyBuffer = CharBuffer.allocate(contentLength);
                input.read(bodyBuffer);
                bodyBuffer.flip();
                parser.setBody(bodyBuffer.toString());
            }
            
            ControladorRotas controlador = new ControladorRotas(gerenciador, autenticacao);
            controlador.processarRequisicao(parser, output);

        } catch (Exception e) {
           System.err.println("Erro no processamento da requisição da thread: " + e.getMessage());
        }
    }
}
