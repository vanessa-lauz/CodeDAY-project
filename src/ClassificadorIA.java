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

        try {

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
                System.out.println("Gemini indisponível. Usando modo de demonstração.");
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
                    && !categoria.equals("pessoas_com_deficiencia")
                    && !categoria.equals("combate_ao_cancer")) {

                return classificacaoLocal(textoVoluntario);
            }

            return categoria;

        } catch (Exception e) {

            System.out.println("Gemini indisponível. Usando modo de demonstração.");
            return classificacaoLocal(textoVoluntario);
        }
    }

    private String classificacaoLocal(String texto) {

        String textoMinusculo = texto.toLowerCase();

        if (textoMinusculo.contains("animal")
                || textoMinusculo.contains("ração")
                || textoMinusculo.contains("cachorro")
                || textoMinusculo.contains("gato")) {

            return "animais";
        }

        if (textoMinusculo.contains("câncer")
                || textoMinusculo.contains("cancer")
                || textoMinusculo.contains("medicamento")
                || textoMinusculo.contains("medicamentos")) {

            return "combate_ao_cancer";
        }

        if (textoMinusculo.contains("deficiência")
                || textoMinusculo.contains("deficiencia")
                || textoMinusculo.contains("pcd")) {

            return "pessoas_com_deficiencia";
        }

        return "animais";
    }
}