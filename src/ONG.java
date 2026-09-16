import java.util.ArrayList;

public class ONG {
    String nome;
    String descricao;
    ArrayList<String> necessidades=new ArrayList<>();
    public ONG(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    public void addNecessidade(String necessidade){
        necessidades.add(necessidade);
    }
    public ArrayList<String> getNecessidades() {
        return necessidades;
    }
}
