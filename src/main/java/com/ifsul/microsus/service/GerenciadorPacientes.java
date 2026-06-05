package com.ifsul.microsus.service;

import com.ifsul.microsus.model.Estado;
import com.ifsul.microsus.model.Paciente;
import com.ifsul.microsus.model.Prioridade;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GerenciadorPacientes {

    private static final String DATA_TXT = "C:\\Users\\babis\\Downloads\\microsus_db.txt";

    private Map<Integer, Paciente> bancoPacientes;
    private AtomicInteger contadorId;

    public GerenciadorPacientes() {
        // pacote Concurrent para lidar com multithreading
        // ConcurrentHashMap: permite acessos simultâneos ao map
        // AtomicInteger: garante atomicidade das operações sobre o contador
        this.bancoPacientes = new ConcurrentHashMap<>();
        this.contadorId = new AtomicInteger(1);

        carregarDadosDoArquivo();
    }

    public Paciente cadastrarPaciente(Paciente paciente) {
        int novoId = contadorId.getAndIncrement();
        paciente.setId(novoId);
        
        bancoPacientes.put(novoId, paciente);
        salvarDadosNoArquivo();

        return paciente;
    }

    public Paciente buscarPaciente(int id) {
      for (Paciente p : bancoPacientes.values()) {
            
            if (p.getId() == id) {
                return p;
            }
        }
      
      return null;
    }

    // 4. Gerenciar estruturas de dados em memória com fila de prioridade
    public List<Paciente> obterFilaOrdenada() {
        return bancoPacientes.values()
                .stream()
                .filter(paciente -> paciente.getEstado() == Estado.EM_FILA) // impede que pacientes com estado != EM_FILA possam ser chamados
                .sorted((pcte1, pcte2) -> {
                    int compPrioridade = pcte1.getPrioridade().compareTo(pcte2.getPrioridade()); // considera a ordem de declaração no enum

                    if (compPrioridade != 0) {
                        return compPrioridade;
                    }

                    return pcte1.getHoraChegada().compareTo(pcte2.getHoraChegada()); // FIFO
                })
                .collect(Collectors.toList());
    }

    // 5. Implementar e respeitar uma máquina de estados por paciente.
    public synchronized Paciente chamarProximo() {
        List<Paciente> fila = obterFilaOrdenada();
        
        if (fila.isEmpty()) {
            return null;
        }

        Paciente proximo = fila.get(0);
        proximo.setEstado(Estado.EM_ATENDIMENTO); // POST /chamar transiciona para EM_ATENDIMENTO

        salvarDadosNoArquivo();
        return proximo;
    }

    public synchronized Paciente finalizarAtendimento(int id, String prognostico) throws IllegalStateException, IllegalArgumentException {
        Paciente paciente = buscarPaciente(id);

        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não encontrado."); // 404 Not Found
        }

        if (paciente.getEstado() == Estado.EM_FILA) {
            throw new IllegalStateException("Paciente está EM_FILA, não é possível finalizar sem antes chamar para atendimento."); // 409 Conflict
        }

        if (paciente.getEstado() == Estado.ATENDIDO) {
            throw new IllegalStateException("Paciente já está ATENDIDO, não é possível mudar de estado."); // 409 Conflict
        }

        paciente.setEstado(Estado.ATENDIDO); // POST /pacientes/:id/finalizar transiciona para ATENDIDO
        paciente.setPrognostico(prognostico);

        salvarDadosNoArquivo();
        return paciente;
    }

    public Map<String, Long> obterEstatisticas() {
        return Map.of(
                "totalGeral", (long) bancoPacientes.size(),
                
                "emFila", bancoPacientes.values().stream().filter(pcte -> pcte.getEstado() == Estado.EM_FILA).count(),
                "emAtendimento", bancoPacientes.values().stream().filter(pcte -> pcte.getEstado() == Estado.EM_ATENDIMENTO).count(),
                "atendidos", bancoPacientes.values().stream().filter(pcte -> pcte.getEstado() == Estado.ATENDIDO).count(),
                
                "prioridadeVermelho", bancoPacientes.values().stream().filter(pcte -> pcte.getPrioridade() == Prioridade.VERMELHO).count(),
                "prioridadeAmarelo", bancoPacientes.values().stream().filter(pcte -> pcte.getPrioridade() == Prioridade.AMARELO).count(),
                "prioridadeVerde", bancoPacientes.values().stream().filter(pcte -> pcte.getPrioridade() == Prioridade.VERDE).count()
        );
    }

    private synchronized void salvarDadosNoArquivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_TXT))) {

            File arquivo = new File(DATA_TXT);

            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }

            for (Paciente pcte : bancoPacientes.values()) {
                String linha = String.format("%d;%s;%s;%s;%s;%s;%s",
                        pcte.getId(),
                        pcte.getNome(),
                        pcte.getSintoma(),
                        pcte.getPrioridade().name(),
                        pcte.getEstado().name(),
                        pcte.getHoraChegada().toString(),
                        pcte.getPrognostico() != null ? pcte.getPrognostico() : "null"
                );

                writer.println(linha);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no arquivo: " + e.getMessage());
        }
    }

    private void carregarDadosDoArquivo() {
        File arquivo = new File(DATA_TXT);

        if (!arquivo.exists()) {
            System.out.println("Arquivo não encontrado, criando novo microsus_db.txt.");
            
            try {
                arquivo.createNewFile();
                System.out.println("microsus_db.txt criado em " + arquivo.getAbsolutePath());

            } catch (IOException e) {
                System.err.println("Erro ao criar arquivo inicial: " + e.getMessage());
            }

            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            int maiorIdEncontrado = 0;
            String linha;

            while ((linha = reader.readLine()) != null) {
                String [] partes = linha.split(";");

                if (partes.length == 7) {
                    int id = Integer.parseInt(partes[0]);
                    String nome = partes[1];
                    String sintoma = partes[2];
                    Prioridade prioridade = Prioridade.valueOf(partes[3]);
                    Estado estado = Estado.valueOf(partes[4]);
                    LocalDateTime horaChegada = LocalDateTime.parse(partes[5]);
                    String prognostico = partes[6].equals("null") ? null : partes[6];

                    Paciente pacienteRestaurado = new Paciente(id, nome, sintoma, prioridade, estado, horaChegada, prognostico);
                    bancoPacientes.put(id, pacienteRestaurado);

                    if (id > maiorIdEncontrado) {
                        maiorIdEncontrado = id;
                    }
                }
            }

            contadorId.set(maiorIdEncontrado + 1);
            System.out.println("Dados carregados com sucesso! | Total de pacientes: " + bancoPacientes.size());

        } catch (Exception e) {
            System.err.println("Erro ao carregar o arquivo de dados: " + e.getMessage());
        }
    }
}
