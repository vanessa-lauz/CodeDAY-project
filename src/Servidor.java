import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Servidor {
    public static void main(String[] args) throws Exception {
        // Cadastro das ONGS
        ArrayList<ONG> ongs = new ArrayList<>();
        ONG aspa = new ONG("ASPA - Associação Santanense de Proteção aos Animais", "Atuação voltada à proteção e ao cuidado de animais");
        aspa.addNecessidade("ração");
        aspa.addNecessidade("produtos de limpeza");

        ONG apae = new ONG("APAE - Associação de Pais e Amigos dos Excepcionais", "Atendimento e inclusão de pessoas com deficiência");
        apae.addNecessidade("materiais de limpeza");
        apae.addNecessidade("roupas");


        ONG liga = new ONG("Liga Feminina de Combate ao Câncer de Sant'Ana do Livramento", "Assistência a pessoas em situação de vulnerabilidade que enfrentam o câncer");
        liga.addNecessidade("roupas");
        liga.addNecessidade("medicamentos");
        liga.addNecessidade("cestas básicas");

        ongs.add(aspa);
        ongs.add(apae);
        ongs.add(liga);

        // OBJETOS QUE FAZEM O TRABALHO
        ClassificadorIA classificador = new ClassificadorIA();
        Recomendador recomendador = new Recomendador(ongs);
        // Cria o servidor
        int porta = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", porta), 0);
        server.createContext("/", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String caminho = exchange.getRequestURI().getPath();
            if (caminho.equals("/") || caminho.equals("/index.html")) {
                enviarArquivo(exchange, "frontend/index.html", "text/html; charset=UTF-8");
            } else if (caminho.equals("/style.css")) {
                enviarArquivo(exchange, "frontend/style.css", "text/css; charset=UTF-8");
            } else if (caminho.equals("/script.js")) {
                enviarArquivo(exchange, "frontend/script.js", "application/javascript; charset=UTF-8");

            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        });
        // CRIA O ENDPOINT
        server.createContext("/classificar", exchange -> {
            adicionarCors(exchange);
            // Responde às requisições de pré-verificação do navegador
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            // Só aceitamos POST
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                enviarResposta(exchange, 405, "{\"erro\":\"Método não permitido\"}");
                return;
            }
            try {
                // RECEBE O JSON DO FRONTEND
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String texto = extrairTexto(body);
                if (texto == null || texto.isBlank()) {
                    enviarResposta(exchange, 400, "{\"erro\":\"Texto não informado\"}");
                    return;
                }
                System.out.println("Texto recebido: " + texto);
                // API Gemini classifica se é uma categoria existente ou fora do escopo
                String categoria = classificador.classificar(texto);
                System.out.println("Categoria identificada: " + categoria);
                if (categoria.equals("fora do escopo")) {
                    enviarResposta(exchange, 200, "{\"mensagem\":\"Não encontramos uma oportunidade compatível. Tente descrever como gostaria de ajudar.\"}");
                    return;
                }
                if (categoria.equals("fora do escopo")) {
                    enviarResposta(exchange, 200,"{\"mensagem\":\"Não encontramos uma oportunidade compatível. Tente descrever como gostaria de ajudar.\"}");
                    return;
                }
                // Procura ONG compatível
                ONG ong = recomendador.recomendar(categoria);
                if (ong == null) {
                    enviarResposta(exchange, 404, "{\"erro\":\"Nenhuma ONG encontrada\"}");
                    return;
                }
                // MONTA RESPOSTA
                String resposta = montarJson(ong, categoria);
                // DEVOLVE PARA O FRONTEND
                enviarResposta(exchange, 200, resposta);
            } catch (Exception e) {
                e.printStackTrace();
                enviarResposta(exchange, 500, "{\"erro\":\"Erro interno do servidor\"}");
            }
        });
        // Inicia o servidor
        server.start();
        System.out.println();
        System.out.println("=================================");
        System.out.println("Servidor iniciado!");
        System.out.println("Servidor iniciado na porta " + porta);
        System.out.println("Endpoint: POST /classificar");
        System.out.println("=================================");
    }
    // PEGA O TEXTO DENTRO DO JSON
    private static String extrairTexto(String body) {
        String marcador = "\"texto\":\"";
        int inicio = body.indexOf(marcador);
        if (inicio == -1) {
            return null;
        }
        inicio += marcador.length();
        int fim = body.indexOf("\"", inicio);
        if (fim == -1) {
            return null;
        }
        return body.substring(inicio, fim);
    }
    // MONTA O JSON QUE SERÁ DEVOLVIDO
    private static void enviarArquivo(
            com.sun.net.httpserver.HttpExchange exchange,
            String caminho,
            String tipoConteudo) throws IOException {
        try {
            java.nio.file.Path arquivo =
                    java.nio.file.Path.of(caminho);
            byte[] conteudo =
                    java.nio.file.Files.readAllBytes(arquivo);
            exchange.getResponseHeaders().set(
                    "Content-Type",
                    tipoConteudo
            );
            exchange.sendResponseHeaders(
                    200,
                    conteudo.length
            );
            exchange.getResponseBody().write(conteudo);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            exchange.sendResponseHeaders(404, -1);
        }
    }
    private static String montarJson(ONG ong, String categoria) {
        StringBuilder necessidades = new StringBuilder();
        for (int i = 0; i < ong.getNecessidades().size(); i++) {
            if (i > 0) {
                necessidades.append(",");
            }
            necessidades
                    .append("\"")
                    .append(
                            escaparJson(
                                    ong.getNecessidades().get(i)
                            )
                    )
                    .append("\"");
        }
        return """
                {
                  "categoria": "%s",
                  "ong": "%s",
                  "descricao": "%s",
                  "necessidades": [%s]
                }
                """.formatted(
                escaparJson(categoria),
                escaparJson(ong.nome),
                escaparJson(ong.descricao),
                necessidades);
    }
    // ENVIA RESPOSTA
    private static void enviarResposta(HttpExchange exchange, int status, String resposta) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output =exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
    // PERMITE O FRONTEND ACESSAR O BACKEND
    private static void adicionarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    // EVITA QUE CARACTERES DO TEXTO QUEBREM O JSON
    private static String escaparJson(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}