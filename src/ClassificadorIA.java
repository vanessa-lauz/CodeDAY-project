import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

public class ClassificadorIA {
    private final String apiKey;
    public ClassificadorIA() {
        apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chave da API não encontrada.");
        }
    }
    public String classificar(String textoVoluntario) throws Exception {
        try {
            String json = """
                {
                  "model": "gemini-3.6-flash",
                  "input": "Você é o classificador do sistema Conecta Voluntário. Classifique o texto em UMA categoria: animais, pessoas com deficiencia, combate ao cancer, indefinido ou fora do escopo.
                    
                               Animais: menciona animais, gatos, cães, cachorros ou pets.
                               Pessoas com deficiencia: menciona pessoas com deficiência ou PCD.
                               Combate ao cancer: menciona câncer ou pessoas com câncer.
                               Indefinido: quer ajudar/doar ou menciona algo para doação, mas não informa para quem.
                               Fora do escopo: quer ajudar um público que não pertence às três categorias, como mulheres, crianças ou idosos.
                    
                               Exemplos: Gosto de gatos = animais; Tenho medicamentos = indefinido; Quero doar ração = indefinido; Quero doar ração para gatos = animais; Quero ajudar mulheres = fora do escopo; Quero ajudar pessoas com deficiência = pessoas com deficiencia; Quero ajudar pessoas com câncer = combate ao cancer.
                    
                               Responda SOMENTE com o nome da categoria. Texto: %s"
                }
                """.formatted(textoVoluntario);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://generativelanguage.googleapis.com/v1/interactions"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            System.out.println("JSON enviado: " + json);
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                System.out.println("Gemini HTTP 429 - limite de uso atingido.");
                System.out.println("Resposta Gemini: " + response.body());
                throw new RuntimeException("LIMITE_GEMINI");
            }
            if (response.statusCode() != 200) {
                System.out.println("Gemini HTTP " + response.statusCode());
                System.out.println("Resposta Gemini: " + response.body());

                return classificacaoLocal(textoVoluntario);
            }
            String resposta = response.body();
            int inicio = resposta.lastIndexOf("\"text\":\"") + 8;
            int fim = resposta.indexOf("\"", inicio);
            if (inicio < 8 || fim == -1) {
                return classificacaoLocal(textoVoluntario);
            }
            String categoria = resposta.substring(inicio, fim)
                    .trim()
                    .toLowerCase()
                    .replace(".", "");
            if (!categoria.equals("animais")
                    && !categoria.equals("pessoas com deficiencia")
                    && !categoria.equals("combate ao cancer")
                    && !categoria.equals("indefinido")
                    && !categoria.equals("fora do escopo")) {
                return classificacaoLocal(textoVoluntario);
            }
            return categoria;
        } catch (Exception e) {
            if ("LIMITE_GEMINI".equals(e.getMessage())) {
                throw e;
            }
            e.printStackTrace();
            System.out.println("Gemini indisponível. Usando modo de demonstração.");
            return classificacaoLocal(textoVoluntario);
        }
    }
    private String classificacaoLocal(String texto) {
        String textoMinusculo = texto.toLowerCase();
        if (textoMinusculo.contains("animal")
                || textoMinusculo.contains("bicho")
                || textoMinusculo.contains("ração")
                || textoMinusculo.contains("cachorro")
                || textoMinusculo.contains("pet")
                || textoMinusculo.contains("gato")) {
            return "animais";
        }
        if (textoMinusculo.contains("câncer")
                || textoMinusculo.contains("cancer")
                || textoMinusculo.contains("medicamento")
                || textoMinusculo.contains("medicamentos")) {
            return "combate ao cancer";
        }
        if (textoMinusculo.contains("deficiência")
                || textoMinusculo.contains("deficiencia")
                || textoMinusculo.contains("pcd")
                || textoMinusculo.contains("pessoas")){
            return "pessoas com deficiencia";
        }
        return "fora do escopo";
    }
}