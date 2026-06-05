package com.ifsul.microsus.http;

import com.ifsul.microsus.model.Paciente;
import com.ifsul.microsus.model.Prioridade;
import com.ifsul.microsus.service.GerenciadorPacientes;
import java.io.PrintWriter;
import java.util.List;

public class ControladorRotas {

    private final GerenciadorPacientes gerenciador;

    public ControladorRotas(GerenciadorPacientes gerenciador) {
        this.gerenciador = gerenciador;
    }

    // 3. Implementar roteamento de requisições (método + path → lógica)
    public void processarRequisicao(HttpParser parser, PrintWriter output) {
        String method = parser.getMethod();
        String path = parser.getPath();

        if (method == null || path == null) {
            HttpUtils.enviarResposta(output, 400, "Bad Request", "text/plain", "Requisição inválida");
            return;
        }

        System.out.println("\n-> Roteando: " + method + " " + path);

        try {
            if (method.equals("GET") && path.equals("/fila")) {
                // teste do multithreading
                System.out.println("[Thread: " + Thread.currentThread().getName() + "] Rota sem carregamento chamada.");

                handleGetFila(output);
            }

            if (method.equals("POST") && path.equals("/pacientes")) {
                handlePostPacientes(parser, output);
            }

            if (method.equals("GET") && path.matches("^/pacientes/\\d+$")) {
                handleGetPacienteById(path, output);
            }

            HttpUtils.enviarResposta(output, 404, "Not Found", "application/json",
                    "{\"erro\": \"Rota não encontrada\"}");

        } catch (Exception e) {
            System.err.println("Erro ao processar rota: " + e.getMessage());
            HttpUtils.enviarResposta(output, 500, "Internal Server Error", "application/json",
                    "{\"erro\": \"Erro interno no servidor\"}");
        }
    }

    private void handlePostPacientes(HttpParser parser, PrintWriter output) {
        String body = parser.getBody();

        if (body == null || body.isEmpty()) {
            HttpUtils.enviarResposta(output, 400, "Bad Request", "application/json",
                    "{\"erro\": \"Corpo da requisiçãoo vazio.\"}");
            return;
        }

        try {
            String nome = HttpUtils.extrairValorJson(body, "nome");
            String sintoma = HttpUtils.extrairValorJson(body, "sintoma");
            String prioridadeStr = HttpUtils.extrairValorJson(body, "prioridade");

            if (nome == null || sintoma == null || prioridadeStr == null) {
                HttpUtils.enviarResposta(output, 400, "Bad Request", "application/json",
                        "{\"erro\": \"Campos ausentes: requer nome, sintoma e prioridade.\"}"); // 400 Bad Request
                return;
            }

            Prioridade prioridade = Prioridade.valueOf(prioridadeStr.toUpperCase());
            Paciente novoPaciente = new Paciente(nome, sintoma, prioridade);

            gerenciador.cadastrarPaciente(novoPaciente);

            String jsonRes = HttpUtils.construirJsonPaciente(novoPaciente);
            HttpUtils.enviarResposta(output, 201, "Created", "application/json", jsonRes); // 201 Created
        } catch (IllegalArgumentException e) {
            HttpUtils.enviarResposta(output, 400, "Bad Request", "application/json",
                    "{\"erro\": \"Prioridade inválida: use somente VERMELHO, AMARELO, VERDE.\"}");
        }
    }

    // 6. Retornar respostas que um navegador consiga interpretar (HTML para GETs
    // visuais)
    private void handleGetFila(PrintWriter output) {
        List<Paciente> fila = gerenciador.obterFilaOrdenada();
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'><title>Fila do MicroSUS</title>");
        sb.append(
                "<style>body{font-family: Arial;} table{width: 100%; border-collapse: collapse;} th,td{border: 1px solid #ccc; padding: 10px; text-align: left;} .VERMELHO{background-color: #ffcccc;} .AMARELO{background-color: #ffffcc;} .VERDE{background-color: #ccffcc;}</style>");
        sb.append("</head><body><h1>MicroSUS - Fila de espera</h1>");

        if (fila.isEmpty()) {
            sb.append("<p>A fila está vazia no momento.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Nome</th><th>Prioridade</th><th>Sintoma</th><th>Chegada</th></tr>");

            for (Paciente p : fila) {
                sb.append("<tr class='").append(p.getPrioridade().name()).append("'>");
                sb.append("<td>").append(p.getId()).append("</td>");
                sb.append("<td>").append(p.getNome()).append("</td>");
                sb.append("<td>").append(p.getPrioridade().name()).append("</td>");
                sb.append("<td>").append(p.getSintoma()).append("</td>");
                sb.append("<td>")
                        .append(p.getHoraChegada().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")))
                        .append("</td>");
                sb.append("</tr>");
            }

            sb.append("</table>");
        }

        sb.append("</body></html>");

        HttpUtils.enviarResposta(output, 200, "OK", "text/html; charset=UTF-8", sb.toString()); // 200 OK, Content-Type:
                                                                                                // text/html
    }

    private void handleGetPacienteById(String path, PrintWriter output) {
        int id = HttpUtils.extrairIdDoPath(path);
        Paciente paciente = gerenciador.buscarPaciente(id);

        if (paciente == null) {
            HttpUtils.enviarResposta(output, 404, "Not Found", "application/json",
                    "{\"erro\": \"Paciente não encontrado.\"}"); // 404 Not Found
        } else {
            HttpUtils.enviarResposta(output, 200, "OK", "application/json", HttpUtils.construirJsonPaciente(paciente)); // 200
                                                                                                                        // OK
        }
    }
}
