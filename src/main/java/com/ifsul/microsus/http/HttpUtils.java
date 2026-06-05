package com.ifsul.microsus.http;

import com.ifsul.microsus.model.Paciente;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public class HttpUtils {
    
    // 2. Montar respostas HTTP válidas com status codes, headers e body corretos
    public static void enviarResposta(PrintWriter out, int statusCode, String statusText, String contentType, String body) {
        if (out == null) {
            return;
        }

        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;

        out.print("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.print("Content-Type: " + contentType + "\r\n"); // application/json ou text/html
        out.print("Content-Length: " + contentLength + "\r\n");
        out.print("Connection: close\r\n");
        out.print("\r\n");
        out.print(body);
        out.flush();
    }

    public static String construirJsonPaciente(Paciente p) {
        String prog = p.getPrognostico() != null ? "\"" + p.getPrognostico() + "\"" : "null";
        
        return String.format(
                "{\"id\": %d, \"nome\": \"%s\", \"sintoma\": \"%s\", \"prioridade\": \"%s\", \"estado\": \"%s\", \"horaChegada\": \"%s\", \"prognostico\": %s}",
                p.getId(), p.getNome(), p.getSintoma(), p.getPrioridade().name(), p.getEstado().name(), p.getHoraChegada().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), prog
        );
    }

    public static String extrairValorJson(String json, String chave) {
        if (json == null) {
            return null;
        }
        
        String busca = "\"" + chave + "\"";
        int inicioChave = json.indexOf(busca);
        
        if (inicioChave == -1) {
            return null;
        }

        int inicioDoisPontos = json.indexOf(":", inicioChave);
        int inicioValor = json.indexOf("\"", inicioDoisPontos);
        
        if (inicioValor == -1) {
            return null;
        }

        int fimValor = json.indexOf("\"", inicioValor + 1);
        
        if (fimValor == -1) {
            return null;
        }

        return json.substring(inicioValor + 1, fimValor);
    }

    public static int extrairIdDoPath(String path) {
        String[] partes = path.split("/");

        for (String parte : partes) {
            if (parte.matches("\\d+")) {
                return Integer.parseInt(parte);
            }
        }

        return -1;
    }
}
