import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        // CADASTRO DAS ONGs
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

        // LISTA DE ONGs
        ArrayList<ONG> ongs = new ArrayList<>();
        ongs.add(aspa);
        ongs.add(apae);
        ongs.add(liga);

        // TEXTO DO VOLUNTÁRIO
        String textoVoluntario = JOptionPane.showInputDialog(null, "Como gostaria de ajudar?");
        if (textoVoluntario == null || textoVoluntario.isBlank()) {
            JOptionPane.showMessageDialog(null, "Nenhum texto foi informado.");
            return;
        }
        // IA
        ClassificadorIA classificador = new ClassificadorIA();
        Recomendador recomendador = new Recomendador(ongs);
        try {
            String categoria = classificador.classificar(textoVoluntario);
            System.out.println("Categoria identificada pela IA: " + categoria);
            ONG ong = recomendador.recomendar(categoria);
            if (ong == null) {
                JOptionPane.showMessageDialog(null, "Não encontramos uma ONG compatível.");
                return;
            }
            // RESULTADO
            StringBuilder resultado = new StringBuilder();
            resultado.append("ONG RECOMENDADA:\n\n");
            resultado.append(ong.nome).append("\n\n");
            resultado.append("Descrição:\n");
            resultado.append(ong.descricao).append("\n\n");
            resultado.append("Necessidades:\n");
            for (String necessidade : ong.getNecessidades()) {
                resultado.append("• ")
                        .append(necessidade)
                        .append("\n");
            }
            resultado.append("\nCategoria identificada pela IA: ");
            resultado.append(categoria);
            JOptionPane.showMessageDialog(null, resultado.toString(), "Conecta Voluntário", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao consultar a IA:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}