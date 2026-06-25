package br.com.unipe; //mesmo pacote das classes de grafo

import java.util.ArrayList;    //listas dinamicas usadas nos resultados e na BFS
import java.util.Collections;  //Collections.unmodifiableList para proteger o cache
import java.util.Comparator;   //ordenacao das sugestoes por amigos em comum
import java.util.HashSet;      //conjunto para marcar visitados (busca rapida, sem repeticao)
import java.util.LinkedList;   //usada como fila (FIFO) na busca em largura
import java.util.List;         //tipo de retorno das missoes 2, 4 e 5
import java.util.Map;          //Map.Entry retornado pelo dijkstra
import java.util.Queue;        //interface da fila da BFS
import java.util.Set;          //conjuntos de vizinhos e de visitados

/*
motor de analises e recomendacoes da rede social profissional.
recebe um Grafo nao-direcionado e ponderado (perfis = vertices,
conexoes = arestas, afinidade = pesos) e responde as 5 missoes do projeto.
*/
public class LinkedInAnalyzer {

    private final Grafo redeSocial; //instancia do grafo que representa toda a rede de contatos

    //cache interno da missao 4: garante que o dijkstra rode apenas uma vez por par
    private String cacheOrigem;          //ultimo par (origem, destino) calculado
    private String cacheDestino;
    private List<String> cacheRota;      //rota correspondente (nomes ordenados)
    private int cacheCusto;              //custo acumulado correspondente

    //missao 1 - construtor da analise
    /*
    guarda a instancia do grafo para que as demais missoes possam consulta-la.
    parametro redeSocial: o grafo ja montado com perfis e conexoes.
    */
    public LinkedInAnalyzer(Grafo redeSocial) {
        this.redeSocial = redeSocial; //armazena a referencia da rede para uso nas analises
    }

    //missao 2 - sugestao de conexoes (amigos de 2 grau)
    /*
    sugere amigos de amigos que o usuario ainda nao tem como contato direto,
    ordenados do maior para o menor numero de amigos em comum.
    parametro nomeUsuario: nome da pessoa para quem queremos gerar sugestoes.
    retorna lista de Sugestao (nome + amigos em comum), ja ordenada.
    */
    public List<Sugestao> sugerirConexoes(String nomeUsuario) {
        //localiza o perfil do usuario; erro claro caso o nome nao exista na rede
        Vertice usuario = redeSocial.encontraVertice(nomeUsuario).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeUsuario + " nao encontrado."));

        //contatos diretos (1 grau) do usuario: nao podem virar sugestao
        Set<Vertice> amigosDiretos = new HashSet<>(usuario.getAdjacencias());

        //acumula, para cada candidato (amigo de amigo), quantos amigos em comum tem
        List<Sugestao> sugestoes = new ArrayList<>();

        //varre os amigos de 2 grau: para cada amigo direto, olha os amigos dele
        for (Vertice amigoDireto : usuario.getAdjacencias()) {
            for (Vertice candidato : amigoDireto.getAdjacencias()) {
                boolean eOProprioUsuario   = candidato.equals(usuario);                  //regra: nao sugerir a si mesmo
                boolean jaEAmigoDireto     = amigosDiretos.contains(candidato);          //regra: nao sugerir contato de 1 grau
                boolean jaFoiContabilizado = contemNome(sugestoes, candidato.getNome()); //evita duplicar candidato

                if (eOProprioUsuario || jaEAmigoDireto || jaFoiContabilizado) {
                    continue; //candidato invalido ou ja tratado: pula
                }

                //conta os amigos em comum entre o usuario e este candidato
                int amigosEmComum = contaAmigosEmComum(amigosDiretos, candidato);
                sugestoes.add(new Sugestao(candidato.getNome(), amigosEmComum));
            }
        }

        //ordena do maior para o menor numero de amigos em comum (decrescente)
        sugestoes.sort(Comparator.comparingInt(Sugestao::amigosEmComum).reversed());
        return sugestoes; //estrutura final com nomes sugeridos e amigos em comum
    }

    //conta quantos contatos diretos do usuario tambem sao amigos do candidato
    private int contaAmigosEmComum(Set<Vertice> amigosDoUsuario, Vertice candidato) {
        int total = 0; //contador de amizades compartilhadas
        for (Vertice amigoDoCandidato : candidato.getAdjacencias()) { //olha cada amigo do candidato
            if (amigosDoUsuario.contains(amigoDoCandidato)) { //esse amigo tambem e amigo do usuario?
                total++; //sim: e um amigo em comum
            }
        }
        return total; //quantidade de amigos em comum
    }

    //verifica se um candidato (por nome) ja esta presente na lista de sugestoes
    private boolean contemNome(List<Sugestao> sugestoes, String nome) {
        return sugestoes.stream().anyMatch(sugestao -> sugestao.nome().equals(nome)); //true se ja houver o nome
    }

    //missao 3 - grau de separacao (BFS: conta conexoes, ignora pesos)
    /*
    descobre em quantos passos de conexao direta/indireta duas pessoas estao
    separadas na rede, usando busca em largura (BFS) que nao considera pesos.
    parametro nomeOrigem: ponto de partida.
    parametro nomeDestino: ponto de chegada.
    retorna o numero de saltos (arestas) entre os dois perfis,
    ou -1 se nao houver conexao entre eles.
    */
    public int grauDeSeparacao(String nomeOrigem, String nomeDestino) {
        Vertice origem = redeSocial.encontraVertice(nomeOrigem).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeOrigem + " nao encontrado."));
        Vertice destino = redeSocial.encontraVertice(nomeDestino).orElseThrow(
                () -> new IllegalArgumentException("Vertice " + nomeDestino + " nao encontrado."));

        if (origem.equals(destino)) {
            return 0; //mesma pessoa: zero passos
        }

        //BFS classica: fila de vertices + distancia acumulada em numero de arestas
        Queue<Vertice> fila = new LinkedList<>();
        Map<Vertice, Integer> distancia = new java.util.HashMap<>();

        fila.add(origem);
        distancia.put(origem, 0); //origem esta a 0 passos de si mesma

        while (!fila.isEmpty()) {
            Vertice atual = fila.poll(); //processa o proximo na ordem de chegada
            int passos = distancia.get(atual); //quantos saltos ate aqui

            for (Vertice vizinho : atual.getAdjacencias()) {
                if (distancia.containsKey(vizinho)) {
                    continue; //ja visitado: ignora
                }
                int novaDistancia = passos + 1; //mais um salto para chegar no vizinho
                distancia.put(vizinho, novaDistancia);

                if (vizinho.equals(destino)) {
                    return novaDistancia; //achou o destino: retorna a quantidade de passos
                }
                fila.add(vizinho); //ainda nao chegou: continua explorando
            }
        }

        return -1; //destino inalcancavel: perfis sem conexao
    }

    //missao 4 - rota e custo de maior afinidade (dijkstra + cache)
    /*
    executa o dijkstra apenas se o par (origem, destino) for diferente do
    ultimo calculado; caso contrario, reutiliza os valores armazenados.
    assim, rotaDeMaiorAfinidade e custoDeMaiorAfinidade compartilham o mesmo
    resultado sem rodar o algoritmo duas vezes.
    */
    private void calcularSeNecessario(String origem, String destino) {
        if (origem.equals(cacheOrigem) && destino.equals(cacheDestino)) {
            return; //cache valido: nao recalcula
        }

        //chama o dijkstra uma vez e salva o par (rota, custo) em cache
        Map.Entry<List<String>, Integer> resultado = redeSocial.dijkstra(origem, destino);
        cacheOrigem  = origem;
        cacheDestino = destino;
        cacheRota    = resultado.getKey();   //lista de nomes ordenada (vazia se inalcancavel)
        cacheCusto   = resultado.getValue(); //custo total (-1 se inalcancavel)
    }

    /*
    retorna a sequencia ordenada de nomes que formam a rota de maior afinidade
    (menor custo ponderado) entre origem e destino.
    retorna lista de nomes da rota, ou lista vazia se os perfis forem inalcancaveis.
    */
    public List<String> rotaDeMaiorAfinidade(String origem, String destino) {
        calcularSeNecessario(origem, destino); //garante que o cache esta atualizado
        return Collections.unmodifiableList(cacheRota); //protege a lista contra modificacoes externas
    }

    /*
    retorna o custo acumulado (soma dos pesos) da rota de maior afinidade
    entre origem e destino.
    retorna o custo total, ou -1 se os perfis forem inalcancaveis.
    */
    public int custoDeMaiorAfinidade(String origem, String destino) {
        calcularSeNecessario(origem, destino); //garante que o cache esta atualizado
        return cacheCusto; //custo ja calculado e guardado pelo metodo privado
    }

    //missao 5 - mapear grupos isolados (componentes conexos via BFS)
    /*
    varre toda a rede e agrupa os perfis em componentes conexos:
    cada grupo reune as pessoas que se alcancam entre si, mas que estao
    totalmente isoladas dos demais grupos.
    retorna lista de grupos; cada grupo e uma lista de nomes de perfis.
    */
    public List<List<String>> mapearGruposIsolados() {
        Set<Vertice> visitados = new HashSet<>();      //marca quem ja foi atribuido a algum grupo
        List<List<String>> grupos = new ArrayList<>(); //resultado: cada elemento e um componente conexo

        for (Vertice inicio : redeSocial.getVertices()) { //varre todos os perfis da rede
            if (visitados.contains(inicio)) {
                continue; //ja pertence a um grupo descoberto anteriormente: pula
            }

            //BFS a partir de inicio descobre todos os vertices do mesmo componente
            List<String> grupo = new ArrayList<>();
            Queue<Vertice> fila = new LinkedList<>();
            fila.add(inicio);
            visitados.add(inicio);

            while (!fila.isEmpty()) {
                Vertice atual = fila.poll(); //processa o proximo na ordem de chegada
                grupo.add(atual.getNome());  //adiciona ao grupo atual

                for (Vertice vizinho : atual.getAdjacencias()) {
                    if (!visitados.contains(vizinho)) { //vizinho ainda nao categorizado?
                        visitados.add(vizinho); //marca como visitado antes de enfileirar (evita duplicatas)
                        fila.add(vizinho);      //enfileira para explorar os vizinhos dele tambem
                    }
                }
            }

            grupos.add(grupo); //componente completo: adiciona a lista de grupos
        }

        return grupos; //cada elemento e um componente conexo da rede
    }
}