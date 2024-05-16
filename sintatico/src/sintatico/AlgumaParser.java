import java.util.ArrayList;
import java.util.List;
import AlgumaLex.TipoToken
import AlgumaLex.AlgumaLexico;
import AlgumaLex.Token;

public class AlgumaParser {
    private final static int TAMANHO_BUFFER = 10;
    List<Token> bufferTokens;
    AlgumaLexico lex;
    boolean chegouNoFim = false;

    public AlgumaParser(AlgumaLexico lex) {
        this.lex = lex;
        bufferTokens = new ArrayList<Token>();
        lerToken();
    }

    private void lerToken() {
        if (bufferTokens.size() > 0) {
            bufferTokens.remove(0);
        }
        while (bufferTokens.size() < TAMANHO_BUFFER && !chegouNoFim) {
            Token proximo = lex.proximoToken();
            bufferTokens.add(proximo);
            if (proximo.nome == TipoToken.Fim) {
                chegouNoFim = true;
            }
        }
        System.out.println("Lido:  " + lookahead(1));
    }

    void match(TipoToken tipo) {
        if (lookahead(1).nome == tipo) {
            System.out.println("Match: " + lookahead(1));
            lerToken();
        } else {
            erroSintatico(tipo.toString());
        }
    }

    Token lookahead(int k) {
        if (bufferTokens.isEmpty()) {
            return null;
        }
        if (k - 1 >= bufferTokens.size()) {
            return bufferTokens.get(bufferTokens.size() - 1);
        }
        return bufferTokens.get(k - 1);
    }
    void erroSintatico(String... tokensEsperados) {
        String mensagem = "Erro sintático: esperando um dos seguintes (";
        for(int i=0;i<tokensEsperados.length;i++) {
            mensagem += tokensEsperados[i];
            if(i<tokensEsperados.length-1)
                mensagem += ",";
        }
        mensagem += "), mas foi encontrado " + lookahead(1);
        throw new RuntimeException(mensagem);
    }
}
public void programa() {
    match(TipoToken.Delim);
    match(TipoToken.PCDeclaracoes);
    listaDeclaracoes();
    match(TipoToken.Delim);
    match(TipoToken.PCAlgoritmo);
    listaComandos();
    match(TipoToken.Fim);
}

void listaDeclaracoes() {
    if (lookahead(4).nome == TipoToken.Delim) {
        declaracao();
    } else if (lookahead(4).nome == TipoToken.Var) {
        declaracao();
        listaDeclaracoes();
    } else {
        erroSintatico(TipoToken.Delim.toString(), TipoToken.Var.toString());
    }
}

void declaracao() {
    match(TipoToken.Var);
    match(TipoToken.Delim);
    tipoVar();
}

void tipoVar() {
    if (lookahead(1).nome == TipoToken.PCInteiro) {
        match(TipoToken.PCInteiro);
    } else if (lookahead(1).nome == TipoToken.PCReal) {
        match(TipoToken.PCReal);
    } else {
        erroSintatico("INTEIRO","REAL");
    }
}

void expressaoAritmetica() {
    termoAritmetico();
    expressaoAritmetica2();
}

void expressaoAritmetica2() {
    if (lookahead(1).nome == TipoToken.OpAritSoma || lookahead(1).nome == TipoToken.OpAritSub) {
        expressaoAritmetica2SubRegra1();
        expressaoAritmetica2();
    } else {
    }
}

void expressaoAritmetica2SubRegra1() {
    if (lookahead(1).nome == TipoToken.OpAritSoma) {
        match(TipoToken.OpAritSoma);
        termoAritmetico();
    } else if (lookahead(1).nome == TipoToken.OpAritSub) {
        match(TipoToken.OpAritSub);
        termoAritmetico();
    } else {
        erroSintatico("+","-");
    }
}

void termoAritmetico() {
    fatorAritmetico();
    termoAritmetico2();
}

void termoAritmetico2() {
    if (lookahead(1).nome == TipoToken.OpAritMult || lookahead(1).nome == TipoToken.OpAritDiv) {
        termoAritmetico2SubRegra1();
        termoAritmetico2();
    } else { 
    }
}

void termoAritmetico2SubRegra1() {
    if (lookahead(1).nome == TipoToken.OpAritMult) {
        match(TipoToken.OpAritMult);
        fatorAritmetico();
    } else if (lookahead(1).nome == TipoToken.OpAritDiv) {
        match(TipoToken.OpAritDiv);
        fatorAritmetico();
    } else {
        erroSintatico("*","/");
    }
}

void fatorAritmetico() {
    if (lookahead(1).nome == TipoToken.NumInt) {
        match(TipoToken.NumInt);
    } else if (lookahead(1).nome == TipoToken.NumReal) {
        match(TipoToken.NumReal);
    } else if (lookahead(1).nome == TipoToken.Var) {
        match(TipoToken.Var);
    } else if (lookahead(1).nome == TipoToken.AbrePar) {
        match(TipoToken.AbrePar);
        expressaoAritmetica();
        match(TipoToken.FechaPar);
    } else {
        erroSintatico(TipoToken.NumInt.toString(),TipoToken.NumReal.toString(),TipoToken.Var.toString(),"(");
    }
}

void expressaoRelacional() {
    termoRelacional();
    expressaoRelacional2();
}

void expressaoRelacional2() {
    if (lookahead(1).nome == TipoToken.OpBoolE || lookahead(1).nome == TipoToken.OpBoolOu) {
        operadorBooleano();
        termoRelacional();
        expressaoRelacional2();
    } else { 
    }
}

void termoRelacional() {
    if (lookahead(1).nome == TipoToken.NumInt
            || lookahead(1).nome == TipoToken.NumReal
            || lookahead(1).nome == TipoToken.Var
            || lookahead(1).nome == TipoToken.AbrePar) {
        expressaoAritmetica();
        opRel();
        expressaoAritmetica();
    } else {
        erroSintatico(TipoToken.NumInt.toString(),TipoToken.NumReal.toString(),TipoToken.Var.toString(),"(");
    }
}

void opRel() {
    if (lookahead(1).nome == TipoToken.OpRelDif) {
        match(TipoToken.OpRelDif);
    } else if (lookahead(1).nome == TipoToken.OpRelIgual) {
        match(TipoToken.OpRelIgual);
    } else if (lookahead(1).nome == TipoToken.OpRelMaior) {
        match(TipoToken.OpRelMaior);
    } else if (lookahead(1).nome == TipoToken.OpRelMaiorIgual) {
        match(TipoToken.OpRelMaiorIgual);
    } else if (lookahead(1).nome == TipoToken.OpRelMenor) {
        match(TipoToken.OpRelMenor);
    } else if (lookahead(1).nome == TipoToken.OpRelMenorIgual) {
        match(TipoToken.OpRelMenorIgual);
    } else {
        erroSintatico("<>","=",">",">=","<","<=");
    }
}

//operadorBooleano : 'E' | 'OU';
void operadorBooleano() {
    if (lookahead(1).nome == TipoToken.OpBoolE) {
        match(TipoToken.OpBoolE);
    } else if (lookahead(1).nome == TipoToken.OpBoolOu) {
        match(TipoToken.OpBoolOu);
    } else {
        erroSintatico("E","OU");
    }
}

void listaComandos() {
    comando();
    listaComandosSubRegra1();
}

void listaComandosSubRegra1() {
    if (lookahead(1).nome == TipoToken.PCAtribuir ||
    lookahead(1).nome == TipoToken.PCLer ||
    lookahead(1).nome == TipoToken.PCImprimir ||
    lookahead(1).nome == TipoToken.PCSe ||
    lookahead(1).nome == TipoToken.PCEnquanto ||
    lookahead(1).nome == TipoToken.PCInicio) {
        listaComandos();
    } else {
    }
}
void comando() {
    if (lookahead(1).nome == TipoToken.PCAtribuir) {
        comandoAtribuicao();
    } else if (lookahead(1).nome == TipoToken.PCLer) {
        comandoEntrada();
    } else if (lookahead(1).nome == TipoToken.PCImprimir) {
        comandoSaida();
    } else if (lookahead(1).nome == TipoToken.PCSe) {
        comandoCondicao();
    } else if (lookahead(1).nome == TipoToken.PCEnquanto) {
        comandoRepeticao();
    } else if (lookahead(1).nome == TipoToken.PCInicio) {
        subAlgoritmo();
    } else {
        erroSintatico("ATRIBUIR","LER","IMPRIMIR","SE","ENQUANTO","INICIO");
    }
}

//comandoAtribuicao : 'ATRIBUIR' expressaoAritmetica 'A' VARIAVEL;
void comandoAtribuicao() {
    match(TipoToken.PCAtribuir);
    expressaoAritmetica();
    match(TipoToken.PCA);
    match(TipoToken.Var);
}

//comandoEntrada : 'LER' VARIAVEL;
void comandoEntrada() {
    match(TipoToken.PCLer);
    match(TipoToken.Var);
}

//comandoSaida : 'IMPRIMIR'  (VARIAVEL | CADEIA);
void comandoSaida() {
    match(TipoToken.PCImprimir);
    comandoSaidaSubRegra1();
}

void comandoSaidaSubRegra1() {
    if (lookahead(1).nome == TipoToken.Var) {
        match(TipoToken.Var);
    } else if (lookahead(1).nome == TipoToken.Cadeia) {
        match(TipoToken.Cadeia);
    } else {
        erroSintatico(TipoToken.Var.toString(),TipoToken.Cadeia.toString());
    }
}

void comandoCondicao() {
    match(TipoToken.PCSe);
    expressaoRelacional();
    match(TipoToken.PCEntao);
    comando();
    comandoCondicaoSubRegra1();
}

void comandoCondicaoSubRegra1() {
    if (lookahead(1).nome == TipoToken.PCSenao) {
        match(TipoToken.PCSenao);
        comando();
    } else {
    }
}

void comandoRepeticao() {
    match(TipoToken.PCEnquanto);
    expressaoRelacional();
    comando();
}

void subAlgoritmo() {
    match(TipoToken.PCInicio);
    listaComandos();
    match(TipoToken.PCFim);
}
