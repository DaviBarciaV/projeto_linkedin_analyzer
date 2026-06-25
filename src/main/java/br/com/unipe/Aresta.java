package br.com.unipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Aresta {
    private String nome;
    private Vertice verticeOrigem;
    private Vertice verticeDestino;
    private Integer peso;

    public Aresta(String nome, Vertice verticeOrigem, Vertice verticeDestino, Integer peso) {
        this.nome = nome;
        this.verticeOrigem = verticeOrigem;
        this.verticeDestino = verticeDestino;
        this.peso = peso;
    }

    public Aresta(String nome, Vertice verticeOrigem, Vertice verticeDestino) {
        this(nome, verticeOrigem, verticeDestino, null);
    }

    public Aresta(Vertice verticeOrigem, Vertice verticeDestino, Integer peso) {
        this(null, verticeOrigem, verticeDestino, peso);
    }

    public Aresta(Vertice verticeOrigem, Vertice verticeDestino) {
        this(null, verticeOrigem, verticeDestino, null);
    }

    public String getNome() {
        return nome != null ? nome : "";
    }

    @Override
    public String toString() {
        return "\n" + getNome() + "{" + verticeOrigem.getNome() + "," + verticeDestino.getNome() + "}";
    }
}
