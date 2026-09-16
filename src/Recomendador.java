import java.util.ArrayList;

public class Recomendador {

    private ArrayList<ONG> ongs;

    public Recomendador(ArrayList<ONG> ongs) {
        this.ongs = ongs;
    }

    public ONG recomendar(String categoria) {

        for (ONG ong : ongs) {

            if (categoria.equals("animais")
                    && ong.nome.startsWith("ASPA")) {
                return ong;
            }

            if (categoria.equals("pessoas_com_deficiencia")
                    && ong.nome.startsWith("APAE")) {
                return ong;
            }

            if (categoria.equals("combate_ao_cancer")
                    && ong.nome.startsWith("Liga")) {
                return ong;
            }
        }

        return null;
    }
}