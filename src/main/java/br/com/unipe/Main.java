package br.com.unipe; // mesmo pacote das classes do projeto

import java.util.List; // usado para imprimir os resultados das missões

public class Main {
    public static void main(String[] args) {
        // Rede social = grafo NÃO-direcionado (false) e PONDERADO (true), conforme o enunciado.
        Grafo redeSocial = new Grafo(false, true);

        // --- Cadastro dos perfis (vértices) ---
        // Rede principal:
        redeSocial.adicionaVertices("Ana", "Bruno", "Carlos", "Daniela", "Eduardo", "Fernanda");
        // Grupo isolado 1 e grupo isolado 2 (sub-redes separadas da principal):
        redeSocial.adicionaVertices("Gabriel", "Hugo", "Igor", "Juliana");

        // --- Cadastro das conexões (arestas) e suas afinidades (pesos) ---
        // Peso baixo = muita afinidade; peso alto = pouca afinidade.
        redeSocial.addAresta("Ana", "Bruno", 1);       // trabalham muito próximos
        redeSocial.addAresta("Ana", "Carlos", 2);
        redeSocial.addAresta("Ana", "Daniela", 8);     // conexão fraca (peso alto)
        redeSocial.addAresta("Bruno", "Eduardo", 1);
        redeSocial.addAresta("Carlos", "Eduardo", 1);
        redeSocial.addAresta("Daniela", "Fernanda", 5);
        redeSocial.addAresta("Eduardo", "Fernanda", 1);
        redeSocial.addAresta("Gabriel", "Hugo", 1);    // grupo isolado 1
        redeSocial.addAresta("Igor", "Juliana", 1);    // grupo isolado 2

        // Cria o motor de análises (Missão 1: construtor recebe e guarda o grafo).
        LinkedInAnalyzer analisador = new LinkedInAnalyzer(redeSocial);

        // ===================== MISSÃO 2: Sugestão de conexões =====================
        System.out.println("==== Missão 2: Sugestões de conexão para Ana ====");
        List<Sugestao> sugestoesParaAna = analisador.sugerirConexoes("Ana"); // amigos de 2º grau
        sugestoesParaAna.forEach(System.out::println); // imprime cada sugestão (nome + amigos em comum)

        // ===================== MISSÃO 3: Grau de separação =====================
        System.out.println("\n==== Missão 3: Grau de separação ====");
        System.out.println("Ana -> Fernanda: " + analisador.grauDeSeparacao("Ana", "Fernanda") + " passo(s)");
        System.out.println("Ana -> Eduardo: " + analisador.grauDeSeparacao("Ana", "Eduardo") + " passo(s)");
        System.out.println("Ana -> Igor (grupo isolado): " + analisador.grauDeSeparacao("Ana", "Igor") + " (-1 = sem conexão)");

        // ===================== MISSÃO 4: Rota de maior afinidade =====================
        System.out.println("\n==== Missão 4: Rota de maior afinidade (menor custo) ====");
        List<String> rotaAnaFernanda = analisador.rotaDeMaiorAfinidade("Ana", "Fernanda"); // deve achar custo 3
        System.out.println("Ana -> Fernanda: " + rotaAnaFernanda
                + " | custo total = " + analisador.custoDeMaiorAfinidade("Ana", "Fernanda"));
        List<String> rotaAnaIgor = analisador.rotaDeMaiorAfinidade("Ana", "Igor"); // inalcançável -> custo -1
        System.out.println("Ana -> Igor: " + rotaAnaIgor
                + " | custo total = " + analisador.custoDeMaiorAfinidade("Ana", "Igor"));

        // ===================== MISSÃO 5: Grupos isolados (sub-redes) =====================
        System.out.println("\n==== Missão 5: Grupos isolados (componentes conexos) ====");
        List<List<String>> subRedes = analisador.mapearGruposIsolados(); // varre a rede inteira
        for (int i = 0; i < subRedes.size(); i++) { // enumera cada sub-rede encontrada
            System.out.println("Sub-rede " + (i + 1) + ": " + subRedes.get(i));
        }
    }
}