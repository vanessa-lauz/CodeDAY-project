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
          "input": "Você é o classificador do sistema Conecta Voluntário. Analise o texto do usuário e classifique a intenção principal em UMA destas categorias: animais, pessoas com deficiencia, combate ao cancer, indefinido ou fora do escopo. Use animais quando o usuário quiser ajudar animais ou instituições de proteção animal, incluindo doações de alimentos, ração, roupas, agasalhos, medicamentos, produtos de limpeza ou dinheiro. Use pessoas com deficiencia quando quiser ajudar pessoas com deficiência ou instituições relacionadas, incluindo doações de alimentos, roupas, agasalhos, medicamentos, materiais de limpeza ou dinheiro. Use combate ao cancer quando quiser ajudar pessoas afetadas pelo câncer ou instituições relacionadas, incluindo doações de alimentos, roupas, agasalhos, medicamentos ou dinheiro. Ajuda financeira pode ser destinada a qualquer uma das três categorias quando o usuário indicar a área que deseja ajudar. Use indefinido quando a pessoa demonstra vontade de ajudar, mas não fornece informações suficientes para identificar uma área. Use fora do escopo quando o texto não tiver relação com ajudar pessoas, animais ou organizações. Considere palavras com significado semelhante, como agasalho e roupa. Responda SOMENTE com o nome da categoria, sem explicação. Texto: %s"
        }
        """.formatted(textoVoluntario);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1/interactions"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            System.out.println("JSON enviado: " + json);
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

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