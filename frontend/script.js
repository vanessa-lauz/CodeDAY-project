const form = document.getElementById("formVoluntario");

const nome = document.getElementById("nome");
const idade = document.getElementById("idade");

const texto = document.getElementById("texto");
const resultado = document.getElementById("resultado");
const status = document.getElementById("status");

const btnProximo = document.getElementById("btnProximo");
const btnVoltar = document.getElementById("btnVoltar");
const botao = document.getElementById("botao");

const passos = document.querySelectorAll(".passo");
const indicadores = document.querySelectorAll(".passo-indicador");

let aguardandoEsclarecimento = false;
let textoAnterior = "";

btnProximo.addEventListener("click", () => {
    status.textContent = "";

    if (nome.value.trim() === "") {
        status.textContent = "Digite seu nome.";
        nome.focus();
        return;
    }

    if (idade.value === "") {
        status.textContent = "Informe sua idade.";
        idade.focus();
        return;
    }

    const idadeNumero = Number(idade.value);

    if (idadeNumero < 14 || idadeNumero > 110) {
        status.textContent = "Informe uma idade válida.";
        idade.focus();
        return;
    }

    passos.forEach(passo => {
        passo.hidden = passo.dataset.passo !== "2";
    });

    indicadores.forEach(indicador => {
        if (indicador.dataset.indicador === "2") {
            indicador.classList.add("ativo");
        } else {
            indicador.classList.remove("ativo");
        }
    });

    texto.focus();
});

btnVoltar.addEventListener("click", () => {
    status.textContent = "";

    passos.forEach(passo => {
        passo.hidden = passo.dataset.passo !== "1";
    });

    indicadores.forEach(indicador => {
        if (indicador.dataset.indicador === "1") {
            indicador.classList.add("ativo");
        } else {
            indicador.classList.remove("ativo");
        }
    });
});

form.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const textoDigitado = texto.value.trim();

    if (textoDigitado === "") {
        status.textContent = "Digite como você gostaria de ajudar.";
        texto.focus();
        return;
    }

    let textoParaEnviar = textoDigitado;

    if (aguardandoEsclarecimento) {
        textoParaEnviar =
            "A pessoa informou anteriormente: " +
            textoAnterior +
            ". Ela esclareceu que quer ajudar " +
            textoDigitado +
            ". Classifique considerando as duas informações.";
    }

    status.textContent = "Buscando uma recomendação...";
    resultado.innerHTML = "";
    botao.disabled = true;
    botao.textContent = "Buscando...";

    try {
        const resposta = await fetch(
            "https://codeday-project-2.onrender.com/classificar",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nome: nome.value.trim(),
                    idade: Number(idade.value),
                    texto: textoParaEnviar
                })
            }
        );

        const dados = await resposta.json();
       if (!resposta.ok) {
           if (dados.erro === "limite_gemini") {
               resultado.innerHTML = `
                   <div class="card-ong">
                       <h2>Serviço de IA temporariamente indisponível</h2>
                       <p>${dados.mensagem}</p>
                       <p>A cota de uso da IA foi atingida. O restante do sistema continua funcionando normalmente.</p>
                   </div>
               `;
               status.textContent = "";
               return;
           }

           throw new Error(dados.erro || "Erro ao consultar o servidor.");
       }
        if (dados.precisaEsclarecimento) {
            textoAnterior = textoDigitado;
            aguardandoEsclarecimento = true;

            resultado.innerHTML = `
                <h2>Precisamos de mais uma informação</h2>
                <p>${dados.mensagem}</p>
            `;

            status.textContent = "";
            texto.value = "";
            return;
        }

        if (dados.mensagem) {
            resultado.innerHTML = `
                <h2>Não encontramos uma oportunidade</h2>
                <p>${dados.mensagem}</p>
            `;

            status.textContent = "";
            return;
        }

        status.textContent = "";
        aguardandoEsclarecimento = false;
        textoAnterior = "";

        resultado.innerHTML = `
            <h2>Recomendamos:</h2>
            <div class="card-ong">
                <h3>${dados.ong}</h3>
                <p>${dados.descricao}</p>
                <h4>Necessidades:</h4>
                <ul>
                    ${dados.necessidades
                        .map(necessidade => `<li>${necessidade}</li>`)
                        .join("")}
                </ul>
            </div>
        `;

    } catch (erro) {
        console.error(erro);
        status.textContent = "Não foi possível obter uma recomendação.";

    } finally {
        botao.disabled = false;
        botao.textContent = "Encontrar onde ajudar";
    }
});