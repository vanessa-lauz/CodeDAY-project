const botao = document.getElementById("botao");
const texto = document.getElementById("texto");
const resultado = document.getElementById("resultado");
const status = document.getElementById("status");

let aguardandoEsclarecimento = false;
let textoAnterior = "";

botao.addEventListener("click", async () => {

    const textoDigitado = texto.value.trim();

    if (textoDigitado === "") {
        status.textContent = "Digite como você gostaria de ajudar.";
        return;
    }

    let textoParaEnviar = textoDigitado;

    if (aguardandoEsclarecimento) {
        textoParaEnviar = textoAnterior + ". A pessoa esclareceu que quer ajudar " + textoDigitado + ".";
    }

    status.textContent = "Buscando uma recomendação...";
    resultado.innerHTML = "";

    try {

        const resposta = await fetch("https://codeday-project2.onrender.com/classificar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ texto: textoParaEnviar })
        });

        const dados = await resposta.json();

        if (!resposta.ok) throw new Error(dados.erro || "Erro ao consultar o servidor.");

        if (dados.precisaEsclarecimento) {
            textoAnterior = textoDigitado;
            aguardandoEsclarecimento = true;
            resultado.innerHTML = `<h2>Precisamos de mais uma informação</h2><p>${dados.mensagem}</p>`;
            status.textContent = "";
            texto.value = "";
            return;
        }

        if (dados.mensagem) {
            resultado.innerHTML = `<h2>Não encontramos uma oportunidade</h2><p>${dados.mensagem}</p>`;
            status.textContent = "";
            return;
        }

        status.textContent = "";
        aguardandoEsclarecimento = false;
        textoAnterior = "";

        resultado.innerHTML = `
            <h2>Recomendamos:</h2>
            <h3>${dados.ong}</h3>
            <p>${dados.descricao}</p>
            <h4>Necessidades:</h4>
            <ul>${dados.necessidades.map(necessidade => `<li>${necessidade}</li>`).join("")}</ul>
        `;

    } catch (erro) {
        status.textContent = "Não foi possível obter uma recomendação.";
        console.error(erro);
    }
});