const botao = document.getElementById("botao");
const texto = document.getElementById("texto");
const resultado = document.getElementById("resultado");
const status = document.getElementById("status");

botao.addEventListener("click", async () => {

    const textoDigitado = texto.value.trim();

    if (textoDigitado === "") {
        status.textContent = "Digite como você gostaria de ajudar.";
        return;
    }

    status.textContent = "Buscando uma recomendação...";
    resultado.innerHTML = "";

    try {

        const resposta = await fetch(
            "https://codeday-project.onrender.com/classificar",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    texto: textoDigitado
                })
            }
        );

        const dados = await resposta.json();

        if (!resposta.ok) {
            throw new Error(dados.erro || "Erro ao consultar o servidor.");
        }
        if (dados.mensagem) {
            resultado.innerHTML = `<h2>Não encontramos uma oportunidade</h2><p>${dados.mensagem}</p>`;
            status.textContent = "";
            return;
        }
        status.textContent = "";

        resultado.innerHTML = `
            <h2>Recomendamos:</h2>

            <h3>${dados.ong}</h3>

            <p>${dados.descricao}</p>

            <h4>Necessidades:</h4>

            <ul>
                ${dados.necessidades
                    .map(necessidade => `<li>${necessidade}</li>`)
                    .join("")}
            </ul>
        `;

    } catch (erro) {

        status.textContent =
            "Não foi possível obter uma recomendação.";

        console.error(erro);
    }
});