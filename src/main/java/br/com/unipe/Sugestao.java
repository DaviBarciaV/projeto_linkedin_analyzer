package br.com.unipe; // mesmo pacote das demais classes do grafo

/**
 * Estrutura de retorno da sugestão de conexões (Missão 2 - amigos de 2º grau).
 * Cada item associa o nome de uma pessoa sugerida à quantidade de amigos
 * que ela tem em comum com o usuário pesquisado.
 *
 * @param nome           nome da pessoa sugerida (amigo de amigo).
 * @param amigosEmComum  quantos contatos diretos os dois compartilham.
 */
public record Sugestao(String nome, int amigosEmComum) { // record imutável só de dados

    @Override
    public String toString() { // formatação amigável para exibir a sugestão no console
        return "%s (%d amigos em comum)".formatted(nome, amigosEmComum); // ex.: "Eduardo (2 amigos em comum)"
    }
}