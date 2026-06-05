package com.ifsul.microsus.model;

import java.time.LocalDateTime;

public class Paciente {

    private Integer id;
    private String nome;
    private String sintoma;
    private Prioridade prioridade; // enum
    private Estado estado; // enum
    private LocalDateTime horaChegada;
    private String prognostico;

    public Paciente(String nome, String sintoma, Prioridade prioridade) {
        this.nome = nome;
        this.sintoma = sintoma;
        this.prioridade = prioridade;
        this.estado = Estado.EM_FILA; // POST /pacientes cria o paciente já EM_FILA
        this.horaChegada = LocalDateTime.now();
        this.prognostico = null;
    }

    public Paciente(Integer id, String nome, String sintoma, Prioridade prioridade, Estado estado, LocalDateTime horaChegada, String prognostico) {
        this.id = id;
        this.nome = nome;
        this.sintoma = sintoma;
        this.prioridade = prioridade;
        this.estado = estado;
        this.horaChegada = horaChegada;
        this.prognostico = prognostico;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSintoma() {
        return sintoma;
    }

    public void setSintoma(String sintoma) {
        this.sintoma = sintoma;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getHoraChegada() {
        return horaChegada;
    }

    public void setHoraChegada(LocalDateTime horaChegada) {
        this.horaChegada = horaChegada;
    }

    public String getPrognostico() {
        return prognostico;
    }

    public void setPrognostico(String prognostico) {
        this.prognostico = prognostico;
    }
    
    @Override
    public String toString() {
        return "Paciente {" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", sintoma='" + sintoma + '\'' +
                ", prioridade=" + prioridade +
                ", estado=" + estado +
                '}';
    }
}
