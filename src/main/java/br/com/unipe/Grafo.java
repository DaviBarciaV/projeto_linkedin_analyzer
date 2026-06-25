package br.com.unipe;

import java.util.*;
import java.util.AbstractMap;

public class Grafo {
    private final List<Aresta> arestas;
    private final List<Vertice> vertices;
    private boolean eDirigido;
    private int ordem;
    private int tamanho;
    private final boolean ePonderado;

    public Grafo() {
        this(false, false);
    }

    public Grafo(boolean eDirigido, boolean ePonderado) {
        this.eDirigido = eDirigido;
        this.ePonderado = ePonderado;
        arestas = new ArrayList<>();
        vertices = new ArrayList<>();
    }

    public void adicionaVertices(String... nomes) {
        for (String nome : nomes) {
            vertices.add(new Vertice(nome));
            ordem++;
        }
    }

    public void addAresta(String v1, String v2) {
        arestas.add(criaAresta("", v1, v2, null));
    }

    public void addAresta(String v1, String v2, int peso) {
        arestas.add(criaAresta("", v1, v2, peso));
    }

    public void addAresta(String nome, String v1, String v2) {
        arestas.add(criaAresta(nome, v1, v2, null));
    }

    public void addAresta(String nome, String v1, String v2, int peso) {
        arestas.add(criaAresta(nome, v1, v2, peso));
    }

    private Aresta criaAresta(String nomeAresta, String nomeVertice1, String nomeVertice2, Integer peso) {
        Vertice v1 = encontraVertice(nomeVertice1).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeVertice1 + " não encontrado."));
        Vertice v2 = encontraVertice(nomeVertice2).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeVertice2 + " não encontrado."));
        if (!eDirigido) {
            infereSeGrafoEDirecionado(v1, v2);
        }
        aumentaGrauDosVertices(v1, v2);
        resolveAdjacencias(v1, v2);
        tamanho++;
        return new Aresta(nomeAresta, v1, v2, peso);
    }

    private void resolveAdjacencias(Vertice v1, Vertice v2) {
        v1.adicionaAdjacencia(v2); // v1 envia p v2
        v2.adicionaAdjacente(v1); // v2 recebe de v1
        if (!eDirigido) {
            v1.adicionaAdjacente(v2);
            v2.adicionaAdjacencia(v1);
        }
    }

    private void aumentaGrauDosVertices(Vertice v1, Vertice v2) {
        if (eDirigido) {
            v1.aumentaOutDegree();
            v2.aumentaInDegree();
        } else {
            v1.aumentaGrau();
            v2.aumentaGrau();
        }
    }

    private void infereSeGrafoEDirecionado(Vertice v1, Vertice v2) {
        if (eSelfLoop(v1, v2)) {
            reprocessamentoParaDigrafo();
        } else {
            for (Aresta aresta : arestas) {
                if (eViaMaoDupla(v1, v2, aresta) || eArestaDuplicada(v2, v1, aresta)) {
                    reprocessamentoParaDigrafo();
                    break;
                }
            }
        }
    }

    private static boolean eArestaDuplicada(Vertice v1, Vertice v2, Aresta aresta) {
        return aresta.getVerticeOrigem().equals(v1) && aresta.getVerticeDestino().equals(v2);
    }

    private static boolean eViaMaoDupla(Vertice v1, Vertice v2, Aresta aresta) {
        return aresta.getVerticeOrigem().equals(v2) && aresta.getVerticeDestino().equals(v1);
    }

    private static boolean eSelfLoop(Vertice v1, Vertice v2) {
        return v1.getNome().equals(v2.getNome());
    }

    public Optional<Vertice> encontraVertice(String nome) {
        for (Vertice vertice : vertices) {
            if (vertice.getNome().equalsIgnoreCase(nome)) {
                return Optional.of(vertice);
            }
        }
        return Optional.empty();
    }

    private void reprocessamentoParaDigrafo() {
        eDirigido = true;
        System.out.println("Reprocessamento para digrafo necessário. O grafo agora é direcionado.");
        limpezaGrausEAdjacencias();
        recalculaGrausEAdjacencias();
    }

    private void recalculaGrausEAdjacencias() {
        arestas.forEach(aresta -> {
            Vertice origem = aresta.getVerticeOrigem();
            Vertice destino = aresta.getVerticeDestino();
            aumentaGrauDosVertices(origem, destino);
            resolveAdjacencias(origem, destino);
        });
    }

    private void limpezaGrausEAdjacencias() {
        vertices.forEach(vertice -> {
            vertice.resetaGraus();
            vertice.resetaAdjacenciasEAdjacentes();
        });
    }

    public String exibeGrausDosVertices() {
        StringBuilder graus = new StringBuilder();
        for (Vertice vertice : vertices) {
            graus.append(vertice.exibeGraus());
        }
        return graus.toString();
    }

    public String exibeAdjacencias() {
        StringBuilder adjacencias = new StringBuilder();
        for (Vertice vertice : vertices) {
            adjacencias.append("\n").append(vertice.getNome()).append(": ").append(vertice.getAdjacencias());
        }
        return adjacencias.toString();
    }

    public String exibeAdjacentes() {
        StringBuilder adjacencias = new StringBuilder();
        for (Vertice vertice : vertices) {
            adjacencias.append("\n").append(vertice.getNome()).append(": ").append(vertice.getAdjacentes());
        }
        return adjacencias.toString();
    }

    public void exibeMatrizAdjacencia() {
        List<Vertice> verticesOrdenados = vertices.stream().sorted(Comparator.comparing(Vertice::getNome)).toList();

        StringBuilder matriz = new StringBuilder("\nMatriz de Adjacência\n");
        matriz.append("\t");
        verticesOrdenados.forEach(v -> matriz.append(v.getNome()).append("\t"));
        matriz.append("\n");

        for (Vertice vertice : verticesOrdenados) { // read-only
            matriz.append(vertice.getNome()).append("\t");
            List<Vertice> adjacencias = vertice.getAdjacencias();
            for (Vertice outroVertice : verticesOrdenados) {
                matriz.append(adjacencias.contains(outroVertice) ? "1" : "0").append("\t");
            }
            matriz.append("\n");
        }

        System.out.println(matriz);
    }

    public void exibeMatrizIncidencia() {
        List<Vertice> verticesOrdenados = vertices.stream().sorted(Comparator.comparing(Vertice::getNome)).toList();
        StringBuilder matriz = new StringBuilder("\nMatriz de Incidência\n\t");
        arestas.forEach(a -> matriz.append(a.getNome()).append("\t"));
        matriz.append("\n");
        for (Vertice vertice : verticesOrdenados) {
            matriz.append(vertice.getNome()).append("\t");
            for (Aresta aresta : arestas) {
                Vertice origem = aresta.getVerticeOrigem();
                Vertice destino = aresta.getVerticeDestino();
                String valor;
                if (origem.equals(vertice) && destino.equals(vertice)) {
                    valor = " 2";
                } else if (origem.equals(vertice)) {
                    valor = eDirigido ? "-1" : "1";
                } else if (destino.equals(vertice)) {
                    valor = " 1";
                } else { // caso contrário
                    valor = " 0";
                }
                matriz.append(valor).append("\t");
            }
            matriz.append("\n");
        }
        System.out.println(matriz);
    }

    public List<String> dfsIterativo(String origem, String destino) {
        Vertice verticeOrigem = encontraVertice(origem).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + origem + " não encontrado."));
        Vertice verticeDestino = destino == null ? null
                : encontraVertice(destino).orElseThrow(
                        () -> new IllegalArgumentException("Vertice " + destino + " não encontrado."));

        Stack<Vertice> pilha = new Stack<>();
        List<Vertice> visitados = new ArrayList<>();
        StringBuilder percurso = new StringBuilder("Percurso = ");

        visitados.add(verticeOrigem);
        pilha.push(verticeOrigem);

        percurso.append(verticeOrigem.getNome()).append(", ");

        while (!pilha.isEmpty()) {
            Vertice atual = pilha.peek();

            if (atual.equals(verticeDestino)) {
                break;
            }

            List<Vertice> adjacencias = atual.getAdjacencias();
            List<Vertice> adjacenciasOrdenadas = adjacencias.stream().sorted(Comparator.comparing(Vertice::getNome))
                    .toList();

            // Pegue a primeira adjacência não visitada
            Optional<Vertice> proximo = adjacenciasOrdenadas.stream().filter(a -> !visitados.contains(a)).findFirst();

            if (proximo.isPresent()) {
                Vertice adjacencia = proximo.get();
                visitados.add(adjacencia);
                percurso.append(adjacencia.getNome()).append(", ");
                pilha.push(adjacencia); // avança para o primeiro vizinho não visitado
            } else {
                pilha.pop(); // vértice esgotado: remove da pilha
            }
        }

        System.out.println(percurso);
        return visitados.stream().map(Vertice::getNome).toList();
    }

    public List<String> dfsRecursivo(String origem, String destino, List<Vertice> visitados) {
        final List<Vertice> visitadosAtual = visitados != null ? visitados : new ArrayList<>();

        Vertice v = encontraVertice(origem).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + origem + " não encontrado."));
        visitadosAtual.add(v);

        if (origem.equals(destino)) {
            return visitadosAtual.stream().map(Vertice::getNome).toList();
        }

        // itera os vizinhos um a um — após backtrack, os já visitados são pulados pelo
        // contains()
        // espelhando o peek() + findFirst() do iterativo
        for (Vertice adj : v.getAdjacencias()) {
            if (visitadosAtual.contains(adj)) {
                continue;
            }

            dfsRecursivo(adj.getNome(), destino, visitadosAtual);

            // se destino foi encontrado em algum ramo, propaga o resultado
            if (destino != null && visitadosAtual.stream().anyMatch(x -> x.getNome().equals(destino))) {
                return visitadosAtual.stream().map(Vertice::getNome).toList();
            }
        }

        // vértice esgotado (sem vizinhos não visitados): retorna o percurso até aqui
        return visitadosAtual.stream().map(Vertice::getNome).toList();
    }

    public int encontraComprimentoCaminho(String... caminho) {
        if (!ePonderado) {
            return caminho.length - 1; // qtd de arestas percorridas
        }
        int comprimento = 0;
        List<Aresta> arestasPercorridas = new ArrayList<>();

        for (int i = 0; i < caminho.length - 1; i++) {
            int indiceAtual = i;
            Vertice origem = encontraVertice(caminho[indiceAtual]).orElseThrow(
                    () -> new IllegalArgumentException("Vertice " + caminho[indiceAtual] + " não encontrado."));
            Vertice destino = encontraVertice(caminho[indiceAtual + 1]).orElseThrow(
                    () -> new IllegalArgumentException("Vertice " + caminho[indiceAtual + 1] + " não encontrado."));
            Optional<Aresta> aresta = arestas.stream()
                    .filter(a -> a.getVerticeOrigem().equals(origem) && a.getVerticeDestino().equals(destino))
                    .findFirst();
            if (aresta.isPresent()) {
                if (arestasPercorridas.contains(aresta.get())) {
                    throw new IllegalArgumentException("Aresta repetida!");
                }
                arestasPercorridas.add(aresta.get());
                comprimento += aresta.get().getPeso();
            }
        }
        return comprimento;
    }

    public boolean eConexo() {
        for (Vertice v : vertices)
            if (v.getInDegree() == 0 || v.getOutDegree() == 0) {
                return false;
            }

        for (Vertice v : vertices) {
            List<String> caminho = dfsIterativo(v.getNome(), null);
            if (caminho.size() < vertices.size()) {
                return false;
            }
        }
        return true;
    }

    public boolean eConexoSimplificado() {
        if (vertices.stream().anyMatch(v -> v.getInDegree() == 0 || v.getOutDegree() == 0)) {
            return false;
        }
        return vertices.stream().noneMatch(v -> dfsIterativo(v.getNome(), null).size() < vertices.size());
    }

    public List<String> greedySearch(String nomeVerticeOrigem, String nomeVerticeDestino) {
        List<Vertice> verticesVisitados = new ArrayList<>();
        int comprimentoCaminho = 0;

        Vertice verticeOrigem = encontraVertice(nomeVerticeOrigem).orElseThrow();
        Vertice verticeDestino = encontraVertice(nomeVerticeDestino).orElseThrow();

        verticesVisitados.add(verticeOrigem);
        Vertice atual = verticeOrigem;

        while (!atual.equals(verticeDestino)) {
            Vertice verticeAlvo = atual;

            // Otimização: Pegamos direto os vizinhos sem iterar sobre arestas do grafo
            // inteiro
            List<Vertice> adjacencias = verticeAlvo.getAdjacencias();
            if (adjacencias == null || adjacencias.isEmpty()) {
                System.out.println("Caminho não encontrado. Busca falhou em: " + atual.getNome());
                return null;
            }

            // Busca a aresta não percorrida com o menor peso baseada nos vizinhos do
            // vértice atual
            List<Aresta> arestasVizinhas = new ArrayList<>();
            for (Vertice vizinho : adjacencias) {
                if (!verticesVisitados.contains(vizinho)) {
                    arestasVizinhas.addAll(obtemArestasParaVizinho(verticeAlvo, vizinho));
                }
            }

            // Se não houver arestas vizinhas, significa que não há caminho para o destino
            if (arestasVizinhas.isEmpty()) {
                System.out.println("Caminho não encontrado. Busca falhou em: " + atual.getNome());
                return null;
            }

            // Pega a aresta com o menor peso
            Aresta melhorAresta = arestasVizinhas.stream()
                    .min(Comparator.comparing(Aresta::getPeso))
                    .orElseThrow();

            comprimentoCaminho += melhorAresta.getPeso() != null ? melhorAresta.getPeso() : 0;
            atual = obtemVerticeOposto(melhorAresta, verticeAlvo);
            verticesVisitados.add(atual);

            System.out.println("Percorrendo aresta " + melhorAresta.getNome() +
                    " (peso " + melhorAresta.getPeso() +
                    ") para o vértice " + atual.getNome());
        }

        List<String> nomesVisitados = verticesVisitados.stream().map(Vertice::getNome).toList();

        System.out.println("Destino " + verticeDestino.getNome() + " encontrado! Busca concluída com sucesso.");
        System.out.println("Caminho: " + String.join(" -> ", nomesVisitados));
        System.out.println("Comprimento do caminho: " + comprimentoCaminho);

        return nomesVisitados;
    }


    private List<Aresta> obtemArestasParaVizinho(Vertice atual, Vertice vizinho) {
        return arestas.stream()
                .filter(a -> (a.getVerticeOrigem().equals(atual) && a.getVerticeDestino().equals(vizinho)) ||
                        (!eDirigido && a.getVerticeDestino().equals(atual) && a.getVerticeOrigem().equals(vizinho)))
                .toList();
    }

    private Vertice obtemVerticeOposto(Aresta aresta, Vertice vertice) {
        return aresta.getVerticeOrigem().equals(vertice) ? aresta.getVerticeDestino() : aresta.getVerticeOrigem();
    }

    //metodos adicionados para o projeto da rede social (usados pela LinkedInAnalyzer)

    /*
    devolve a lista de vertices da rede em modo somente-leitura.
    a LinkedInAnalyzer precisa varrer todos os perfis para achar grupos
    isolados, mas nao deve alterar a estrutura interna do grafo.
    */
    public List<Vertice> getVertices() {
        return Collections.unmodifiableList(vertices); //expoe a lista sem permitir add/remove externos
    }

    /*
    retorna o peso da conexao entre dois vertices vizinhos.
    como a rede e nao-direcionada, a aresta pode ter sido cadastrada em qualquer
    sentido (origem->destino ou destino->origem), por isso checamos os dois lados.
    se houver arestas paralelas, fica a de menor peso (maior afinidade).
    retorna null se os vertices nao forem ligados.
    */
    private Integer obtemPesoEntre(Vertice origem, Vertice destino) {
        return arestas.stream() //percorre todas as arestas do grafo
                .filter(aresta -> ligaOsDoisVertices(aresta, origem, destino)) //mantem as arestas que ligam os dois vertices
                .map(Aresta::getPeso) //troca a aresta pelo seu peso
                .filter(Objects::nonNull) //descarta pesos nulos
                .min(Integer::compareTo) //entre arestas paralelas, pega a de menor peso
                .orElse(null); //null sinaliza sem conexao direta
    }

    /*
    diz se uma aresta conecta exatamente o par (a, b), ignorando o sentido,
    ja que numa rede de amizades a conexao e mutua.
    */
    private boolean ligaOsDoisVertices(Aresta aresta, Vertice a, Vertice b) {
        Vertice origem = aresta.getVerticeOrigem(); //ponta cadastrada como origem
        Vertice destino = aresta.getVerticeDestino(); //ponta cadastrada como destino
        boolean noSentidoDireto = origem.equals(a) && destino.equals(b); //a -> b
        boolean noSentidoInverso = origem.equals(b) && destino.equals(a); //b -> a
        return noSentidoDireto || noSentidoInverso; //qualquer um dos sentidos serve
    }

    /*
    algoritmo de menor caminho para grafos ponderados (dijkstra).
    encontra a rota de menor soma de pesos (maior afinidade acumulada) entre
    origem e destino. e a base da missao 4.
    o retorno e um par (Map.Entry) onde:
    - getKey() retorna a lista ordenada de nomes da rota (vazia se inalcancavel)
    - getValue() retorna o custo total acumulado (-1 se inalcancavel)
    usamos AbstractMap.SimpleImmutableEntry por ser um par imutavel
    nativo do Java, sem precisar criar nenhuma classe auxiliar.
    */
    public Map.Entry<List<String>, Integer> dijkstra(String nomeOrigem, String nomeDestino) {
        //valida e obtem os vertices de partida e chegada
        Vertice origem = encontraVertice(nomeOrigem).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeOrigem + " nao encontrado."));
        Vertice destino = encontraVertice(nomeDestino).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeDestino + " nao encontrado."));

        //menor distancia conhecida da origem ate cada vertice (comeca infinita)
        Map<Vertice, Integer> distancias = new HashMap<>();
        //para cada vertice, guarda de onde chegamos com a menor distancia
        Map<Vertice, Vertice> antecessores = new HashMap<>();
        //vertices cujo menor caminho ja foi definitivamente fechado
        Set<Vertice> finalizados = new HashSet<>();

        //inicializa todas as distancias como infinito, exceto a origem (distancia 0)
        for (Vertice vertice : vertices) {
            distancias.put(vertice, Integer.MAX_VALUE); //MAX_VALUE representa ainda inalcancavel
        }
        distancias.put(origem, 0); //da origem para ela mesma o custo e zero

        //fila de prioridade que sempre entrega o vertice aberto de menor distancia acumulada
        PriorityQueue<Vertice> fila = new PriorityQueue<>(Comparator.comparingInt(distancias::get));
        fila.add(origem); //comecamos a explorar pela origem

        while (!fila.isEmpty()) { //enquanto houver vertices a explorar
            Vertice atual = fila.poll(); //remove o vertice aberto mais proximo da origem

            if (atual.equals(destino)) { //otimizacao: chegou no destino, pode parar
                break;
            }
            if (!finalizados.add(atual)) { //se ja foi finalizado, ignora entrada duplicada da fila
                continue;
            }

            //relaxa cada vizinho: tenta melhorar a distancia passando pelo vertice atual
            for (Vertice vizinho : atual.getAdjacencias()) {
                if (finalizados.contains(vizinho)) { //vizinho ja fechado nao precisa ser reavaliado
                    continue;
                }
                Integer peso = obtemPesoEntre(atual, vizinho); //custo da aresta atual -> vizinho
                if (peso == null) { //sem peso valido nao ha como percorrer essa conexao
                    continue;
                }
                int novaDistancia = distancias.get(atual) + peso; //distancia acumulada via atual
                if (novaDistancia < distancias.get(vizinho)) { //achamos um caminho melhor?
                    distancias.put(vizinho, novaDistancia); //atualiza a menor distancia do vizinho
                    antecessores.put(vizinho, atual); //registra que chegamos nele a partir de atual
                    fila.add(vizinho); //reabre o vizinho com a nova distancia para reexplorar
                }
            }
        }

        //se a distancia ao destino continua infinita, nao existe rota entre os perfis
        if (distancias.get(destino) == Integer.MAX_VALUE) {
            return new AbstractMap.SimpleImmutableEntry<>(List.of(), -1); //rota vazia e custo sentinela -1
        }

        //reconstroi a rota do destino ate a origem seguindo os antecessores
        List<String> rota = reconstroiRota(origem, destino, antecessores);
        return new AbstractMap.SimpleImmutableEntry<>(rota, distancias.get(destino)); //rota + custo total
    }

    /*
    remonta a sequencia de nomes da origem ate o destino caminhando de tras para
    frente pelos antecessores calculados no dijkstra e invertendo no final.
    */
    private List<String> reconstroiRota(Vertice origem, Vertice destino, Map<Vertice, Vertice> antecessores) {
        LinkedList<String> rota = new LinkedList<>(); //LinkedList permite inserir na frente em O(1)
        Vertice passo = destino; //comeca no destino e volta ate a origem
        while (passo != null) { //antecessor da origem e null, encerrando o laco
            rota.addFirst(passo.getNome()); //insere no inicio para sair em ordem origem -> destino
            if (passo.equals(origem)) { //chegou na origem: rota completa
                break;
            }
            passo = antecessores.get(passo); //recua para o vertice anterior na melhor rota
        }
        return rota; //lista de nomes ja na ordem correta
    }

    @Override
    public String toString() {
        return """
                direcionado = %s,
                ordem = %d,
                tamanho = %d,
                vertices = %s,
                arestas = %s,
                graus = %s,
                adjacencias = %s,
                adjacentes = %s
                }""".formatted(eDirigido ? "sim" : "não", ordem, tamanho, vertices, arestas, exibeGrausDosVertices(),
                exibeAdjacencias(), exibeAdjacentes());
    }
}
