import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ClassificadorIA {

    private final String apiKey;

    public ClassificadorIA() {
        apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chave da API não encontrada.");
        }
    }

    public String classificar(String textoVoluntario) throws Exception {

        String json = """
                {
                  "model": "gemini-3.6-flash",
                  "input": "Classifique o texto abaixo em UMA destas categorias: animais, pessoas_com_deficiencia ou combate_ao_cancer. Responda SOMENTE com o nome da categoria, sem explicação. Texto: %s"
                }
                """.formatted(textoVoluntario);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://generativelanguage.googleapis.com/v1/interactions"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Erro na API Gemini. Status: " + response.statusCode()
                            + "\n" + response.body()
            );
        }

        String resposta = response.body();

        int inicio = resposta.lastIndexOf("\"text\":\"") + 8;
        int fim = resposta.indexOf("\"", inicio);

        if (inicio < 8 || fim == -1) {
            throw new RuntimeException("Não foi possível extrair a categoria da resposta da IA.");
        }

        String categoria = resposta.substring(inicio, fim).trim().toLowerCase();
        categoria = categoria.replace(".", "");

        if (!categoria.equals("animais")
                && !categoria.equals("pessoas_com_deficiencia")
                && !categoria.equals("combate_ao_cancer")) {

            throw new RuntimeException(
                    "Categoria inválida retornada pela IA: " + categoria
            );
        }

        return categoria;
    }
}