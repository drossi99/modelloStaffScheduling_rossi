package domain;

import utility.InputDati;
import utility.LettoreIstanzaDaFile;
import view.OutputDati;

import java.io.File;
import java.util.ArrayList;
import java.time.LocalTime;

public class GeneratoreIstanze {

    public static IstanzaProblema generazioneIstanzaGuidata() {
        String nomeIstanza = GeneratoreDatiIstanze.assegnaNomeManuale();
        int numeroDipendenti = GeneratoreDatiIstanze.assegnaNumeroDipendentiManuale();
        Frazionamento frazionamentoGiornata = GeneratoreDatiIstanze.sceltaNumeroIntervalli();
        int numeroCategorieDipendenti = GeneratoreDatiIstanze.assegnaNumeroCategorieDipendentiManuale();
        double[] array_pagheOrarieNormalizzato = GeneratoreDatiIstanze.assegnaPagheManuale(numeroCategorieDipendenti);
        int[] array_isFullTime = GeneratoreDatiIstanze.assegnaPartFullTimeManuale(numeroDipendenti);
        int[] array_categoriaDipendente = GeneratoreDatiIstanze.assegnaCategoriaManuale(numeroDipendenti, numeroCategorieDipendenti);
        int[] parametriTipici = GeneratoreDatiIstanze.assegnaParametriTipici();
        int numeroGiorni = parametriTipici[0];
        LocalTime[] orarioAttivita = GeneratoreDatiIstanze.assegnaOrarioAttivita();
        int[] array_oreUomoMinime = GeneratoreDatiIstanze.assegnOreUomoMinimeManuale(numeroDipendenti, numeroGiorni);

        return new IstanzaProblema(nomeIstanza, numeroDipendenti, frazionamentoGiornata, numeroCategorieDipendenti, array_pagheOrarieNormalizzato, array_isFullTime, array_categoriaDipendente, parametriTipici, orarioAttivita, array_oreUomoMinime);
    }

    public static IstanzaProblema generazioneIstanzaCasuale(int numeroIstanzeGiaEsistenti) {
        int numeroParametriTipici = 8;
        int numeroOrariAttivita = 2;
        String nomeIstanza = GeneratoreDatiIstanze.assegnaNomeCasuale(numeroIstanzeGiaEsistenti);
        int numeroDipendenti = GeneratoreDatiIstanze.assegnaNumeroDipendentiCasuale();
        Frazionamento frazionamentoGiornata = GeneratoreDatiIstanze.assegnaFrazionamentoCasuale();
        int numeroCategorieDipendenti = GeneratoreDatiIstanze.assegnaNumeroCategorieDipendentiCasuale();
        double[] array_pagheOrarieNormalizzato = GeneratoreDatiIstanze.assegnaPagheCasuale(numeroCategorieDipendenti);
        int[] array_isFullTime = GeneratoreDatiIstanze.assegnaPartFullTimeCasuale(numeroDipendenti);
        int[] array_categoriaDipendente = GeneratoreDatiIstanze.assegnaCategoriaCasuale(numeroDipendenti, numeroCategorieDipendenti);

        int[] parametriTipici = GeneratoreDatiIstanze.assegnaParametriTipiciDefault(numeroParametriTipici);
        int numeroGiorni = parametriTipici[0];
        LocalTime[] orarioAttivita = GeneratoreDatiIstanze.assegnaOrarioAttivitaDefault(numeroOrariAttivita);
        int[] array_oreUomoMinime = GeneratoreDatiIstanze.assegnOreUomoMinimeCasuale(numeroDipendenti, numeroGiorni, parametriTipici[6]);

        IstanzaProblema istanzaCreata = new IstanzaProblema(nomeIstanza, numeroDipendenti, frazionamentoGiornata, numeroCategorieDipendenti, array_pagheOrarieNormalizzato, array_isFullTime, array_categoriaDipendente, parametriTipici, orarioAttivita, array_oreUomoMinime);
        return istanzaCreata;
    }

    public static IstanzaProblema generazioneIstanzaDaFile() {
        ArrayList<String> listaAttributi = new ArrayList<>();
        try {
            File fileIstanza = InputDati.selezionaFile();
            listaAttributi = GeneratoreDatiIstanze.estraiListaDaFile(fileIstanza);
        } catch (Exception e) {
            OutputDati.stampaErrore("C'è stato un errore nella lettura del file");
        }
        return LettoreIstanzaDaFile.leggiIstanzaDaLista(listaAttributi);
    }
}
