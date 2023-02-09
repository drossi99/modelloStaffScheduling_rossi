package domain;

import utility.EstrazioniCasuali;
import utility.InputDati;
import utility.LettoreIstanzaDaFile;
import utility.MyMenu;
import view.OutputDati;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class GeneratoreDatiIstanze {
    public static final int MIN_NUMERO_DIPENDENTI = 1;
    public static final int MAX_NUMERO_DIPENDENTI = 250;
    private static final String TITOLO_SCELTA_INTERVALLI = "Inserisci il numero corrispondente al frazionamento desiderato:";
    private static final String[] VOCI_SCELTA_INTERVALLI = {"ogni 15 minuti -> 96 intervalli", "ogni mezz'ora -> 48 intervalli", "ogni ora -> 24 intervalli"};
    static final int PERCENTUALE_PARTTIME = 50;
    static final int MIN_NUMERO_CATEGORIE = 1;
    static final int MAX_NUMERO_CATEGORIE = 15;
    static final double MIN_PAGA_ORARIA = 20.00;
    static final double MAX_PAGA_ORARIA = 500.00;
    static final int NUMERO_GIORNI_SETTIMANA = 7;
    static final int NUMERO_ORE_GIORNATA = 24;
    static final int NUMERO_ORE_MIN_FULLTIME_SETTIMANALI = 40;
    static final int NUMERO_ORE_MAX_FULLTIME_SETTIMANALI = 48;
    static final int NUMERO_GIORNILAVORATIVI_MAX = 5;
    static final int ORE_CONTINUE = 4;
    static final int DURATA_MAX_TURNO = 12;
    static final LocalTime ORA_INIZIO_ATTIVITA = LocalTime.parse("05:00");
    static final LocalTime ORA_FINE_ATTIVITA = LocalTime.parse("22:00");

    static int[] assegnaCategoriaManuale(int numeroDipendenti, int numeroCategorieDipendenti) {
        int[] mat_categoriaDipendenti = new int[numeroDipendenti];
        for (int d=0; d<numeroDipendenti; d++) {
            mat_categoriaDipendenti[d] = InputDati.leggiIntero("Inserisci la categoria del dipendente " + String.valueOf(d) + ": ",1,numeroCategorieDipendenti);
        }
        return mat_categoriaDipendenti;
    }

    static int[] assegnaCategoriaCasuale(int numeroDipendenti, int numeroCategorieDipendenti) {
        int[] mat_categoriaDipendenti = new int[numeroDipendenti];
        for (int d=0; d<numeroDipendenti; d++) {
            mat_categoriaDipendenti[d] = EstrazioniCasuali.estraiIntero(1, numeroCategorieDipendenti);
        }
        return mat_categoriaDipendenti;
    }

    static int[] assegnaPartFullTimeManuale(int numeroDipendenti) {
        int[] mat_isFulltime = new int[numeroDipendenti];
        for (int d=0; d<numeroDipendenti; d++) {
            mat_isFulltime[d] = InputDati.leggiIntero("Inserisci 0 se il dipendente " + d + " è part-time, 1 se è full-time: ", 0, 1);
        }
        return mat_isFulltime;
    }

    static int[] assegnaPartFullTimeCasuale(int numeroDipendenti) {
        int[] mat_isFulltime = new int[numeroDipendenti];
        for (int d=0; d<numeroDipendenti; d++) {
            mat_isFulltime[d] = EstrazioniCasuali.estraiIntero(0, 1);
        }
        return mat_isFulltime;
    }

    static Frazionamento sceltaNumeroIntervalli() {
        int scelta;
        Frazionamento f = new Frazionamento();
        MyMenu menuNumeroIntervalli = new MyMenu(TITOLO_SCELTA_INTERVALLI, VOCI_SCELTA_INTERVALLI);
        do {
            scelta = menuNumeroIntervalli.scegli();
            sceltaIntervalli(f, scelta);
        } while (scelta < 1 || scelta > 3);
        return f;
    }

    static Frazionamento sceltaIntervalli(Frazionamento f, int scelta) {
        switch (scelta) {
            case 1:
                f.setNumeroIntervalli(96);
                f.setFattoreMoltiplicativo(4);
                break;
            case 2:
                f.setNumeroIntervalli(48);
                f.setFattoreMoltiplicativo(2);
                break;
            default:
                f.setNumeroIntervalli(24);
                f.setFattoreMoltiplicativo(1);
        }
        return f;
    }

    static ArrayList<String> estraiListaDaFile(File fileIstanza) {
        ArrayList<String> listaAttributi = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(fileIstanza);
            while (scanner.hasNextLine()) {
                listaAttributi.add(scanner.nextLine());
            }
            OutputDati.stampaMessaggio("Ho finito di leggere il file");
            scanner.close();
        } catch (Exception e) {
        }
        return listaAttributi;
    }

    public static double[] calcolaPagheOrariePerIntervallo(double[] array_pagheOrarieNormalizzato, int fattoreMoltiplicativo) {
        double[] array_pagheOrariePerIntervallo = new double[array_pagheOrarieNormalizzato.length];
        for (int i = 0; i < array_pagheOrarieNormalizzato.length; i++) {
            //sovrascrivo ogni elemento, tanto non mi serve piu
            array_pagheOrariePerIntervallo[i] = array_pagheOrarieNormalizzato[i] / fattoreMoltiplicativo;
        }
        return array_pagheOrariePerIntervallo;
    }

    public static String assegnaNomeManuale() {
        String nome = InputDati.leggiStringaNonVuota("Inserisci il nome che vuoi assegnare all'istanza: ");
        return nome;
    }

    public static String assegnaNomeCasuale(int numeroIstanzeGiaEsistenti) {
        //non è un nome casuale, ma incrementale
        String nome = "istanza_" + String.valueOf(numeroIstanzeGiaEsistenti);
        return nome;
    }

    public static int assegnaNumeroDipendentiCasuale() {
        return EstrazioniCasuali.estraiIntero(GeneratoreDatiIstanze.MIN_NUMERO_DIPENDENTI, GeneratoreDatiIstanze.MAX_NUMERO_DIPENDENTI);
    }

    public static Frazionamento assegnaFrazionamentoCasuale() {
        Frazionamento frazionamentoGiornata = new Frazionamento();
        sceltaIntervalli(frazionamentoGiornata, EstrazioniCasuali.estraiIntero(1,3));
        return frazionamentoGiornata;
    }


    public static int assegnaNumeroCategorieDipendentiCasuale() {
        return EstrazioniCasuali.estraiIntero(GeneratoreDatiIstanze.MIN_NUMERO_CATEGORIE, GeneratoreDatiIstanze.MAX_NUMERO_CATEGORIE);
    }

    public static double[] assegnaPagheCasuale(int numeroCategorieDipendenti) {
        double[] array_pagheOrarieNormalizzato = new double[numeroCategorieDipendenti];
        for (int i = 0; i < numeroCategorieDipendenti; i++) {
            array_pagheOrarieNormalizzato[i] = EstrazioniCasuali.estraiDecimale(GeneratoreDatiIstanze.MIN_PAGA_ORARIA, GeneratoreDatiIstanze.MAX_PAGA_ORARIA);
        }
        return array_pagheOrarieNormalizzato;
    }

    public static int assegnaNumeroDipendentiManuale() {
        int numeroDipendenti = InputDati.leggiInteroPositivo("Inserisci il numero di dipendenti: ");
        return numeroDipendenti;
    }

    public static int assegnaNumeroCategorieDipendentiManuale() {
        int numeroCategorieDipendenti = InputDati.leggiIntero("Inserisci il numero di categorie di dipendenti (ad es. direzione, amministrazione, tecnico, ...): ");
        return numeroCategorieDipendenti;
    }

    public static double[] assegnaPagheManuale(int numeroCategorieDipendenti) {
        double[] array_pagheOrarieNormalizzato = new double[numeroCategorieDipendenti];
        for (int i = 0; i < numeroCategorieDipendenti; i++) {
            array_pagheOrarieNormalizzato[i] = InputDati.leggiDouble("Inserisci la paga oraria per la categoria " + String.valueOf(i+1) + ": € ");
        }
        return array_pagheOrarieNormalizzato;
    }

    public static int[] assegnaParametriTipici() {
        int numeroParametriTipici = 8;
        int[] parametriTipici;
        OutputDati.stampaMessaggio("Valori di default:\n" +
                "numero di giorni della settimana:\t" + NUMERO_GIORNI_SETTIMANA + "\n" +
                "numero di ore della giornata:\t" + NUMERO_ORE_GIORNATA + "\n" +
                "percentuale part time:\t " + PERCENTUALE_PARTTIME + "\n" +
                "numero di ore minime settimanali in part-time:\t" + (NUMERO_ORE_MIN_FULLTIME_SETTIMANALI * PERCENTUALE_PARTTIME / 100) + "\n" +
                "numero di ore minime settimanali in full-time:\t" + NUMERO_ORE_MIN_FULLTIME_SETTIMANALI + "\n" +
                "numero di ore massime settimanali in part-time:\t" + (NUMERO_ORE_MAX_FULLTIME_SETTIMANALI * PERCENTUALE_PARTTIME / 100) + "\n" +
                "numero di ore massime settimanali in full-time:\t" + NUMERO_ORE_MAX_FULLTIME_SETTIMANALI + "\n" +
                "numero di giorni lavorativi massimi in 1 settimana:\t" + NUMERO_GIORNILAVORATIVI_MAX + "\n" +
                "numero di ore minime continuative:\t" + ORE_CONTINUE + "\n" +
                "numero di ore di durata massima di un turno:\t" + DURATA_MAX_TURNO);
        boolean isManuale = InputDati.yesOrNo("Vuoi impostare dei valori diversi da quelli di default per i parametri sopra?");
        if (isManuale) {
            parametriTipici = assegnaParametriTipiciManuale(numeroParametriTipici);
        } else {
            parametriTipici = assegnaParametriTipiciDefault(numeroParametriTipici);
        }

        return parametriTipici;
    }

    private static int[] assegnaParametriTipiciManuale(int numeroParametriTipici) {
        int[] parametriTipici = new int[numeroParametriTipici];
        OutputDati.stampaMessaggio("Valori nuovi:");
        parametriTipici[0] = InputDati.leggiIntero("numero di giorni della settimana: ");
        parametriTipici[1] = InputDati.leggiIntero("numero di ore della giornata: ");
        parametriTipici[2] = InputDati.leggiIntero("percentuale part-time (ad es. 50, 75): ");
        parametriTipici[3] = InputDati.leggiIntero("numero di ore settimanali minime in full-time: ");
        parametriTipici[4] = InputDati.leggiIntero("numero di ore settimanali massime in full-time: ");
        parametriTipici[5] = InputDati.leggiIntero("numero di giorni lavorativi massimi in 1 settimana: ");
        parametriTipici[6] = InputDati.leggiIntero("numero di ore minime continuative: ");
        parametriTipici[7] = InputDati.leggiIntero("numero di ore di durata massima di un turno: ");

        return parametriTipici;
    }

    public static int[] assegnaParametriTipiciDefault(int numeroParametriTipici) {
        int[] parametriTipici = new int[numeroParametriTipici];
        parametriTipici[0] = NUMERO_GIORNI_SETTIMANA;
        parametriTipici[1] = NUMERO_ORE_GIORNATA;
        parametriTipici[2] = PERCENTUALE_PARTTIME;
        parametriTipici[3] = NUMERO_ORE_MIN_FULLTIME_SETTIMANALI;
        parametriTipici[4] = NUMERO_ORE_MAX_FULLTIME_SETTIMANALI;
        parametriTipici[5] = NUMERO_GIORNILAVORATIVI_MAX;
        parametriTipici[6] = ORE_CONTINUE;
        parametriTipici[7] = DURATA_MAX_TURNO;

        return parametriTipici;
    }

    public static LocalTime[] assegnaOrarioAttivita() {
        int numeroOrari = 2;
        LocalTime[] orarioAttivita = new LocalTime[numeroOrari];
        boolean hasOrarioAttivita = InputDati.yesOrNo("L'azienda ha una fascia oraria di attività (s) oppure è attiva 24 ore su 24 (n)?");

        if (hasOrarioAttivita) {

            OutputDati.stampaMessaggio("Valori di default:\n" +
                    "ora di inizio attività dell'azienda:\t" + ORA_INIZIO_ATTIVITA.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
                    "ora di fine attività dell'azienda:\t" + ORA_FINE_ATTIVITA.format(DateTimeFormatter.ofPattern("HH:mm")));
            boolean isManuale = InputDati.yesOrNo("Vuoi impostare dei valori diversi da quelli di default per i parametri sopra?");
            if (isManuale) {
                orarioAttivita = assegnaOrarioAttivitaManuale(numeroOrari);
            } else {
                orarioAttivita = assegnaOrarioAttivitaDefault(numeroOrari);
            }
        }

        return orarioAttivita;
    }

    private static LocalTime[] assegnaOrarioAttivitaManuale(int numeroOrari) {
        LocalTime[] orarioAttivita = new LocalTime[numeroOrari];
        OutputDati.stampaMessaggio("Valori nuovi (usare il formato HH:mm e un orario consono al frazionamento di giornata scelto):");

        do {
            try {
                String string_oraInizio = InputDati.leggiStringaNonVuota("ora di inizio attività dell'azienda: ");
                String string_oraFine = InputDati.leggiStringaNonVuota("ora di fine attività dell'azienda: ");
                orarioAttivita[0] = LocalTime.parse(string_oraInizio);
                orarioAttivita[1] = LocalTime.parse(string_oraFine);
            } catch (Exception e) {
                OutputDati.stampaErrore("Il formato inserito non è accettato");
            }
        } while (orarioAttivita[0] == null || orarioAttivita[1] == null);

        return orarioAttivita;
    }

    public static LocalTime[] assegnaOrarioAttivitaDefault(int numeroOrari) {
        LocalTime[] orarioAttivita = new LocalTime[numeroOrari];
        orarioAttivita[0] = ORA_INIZIO_ATTIVITA;
        orarioAttivita[1] = ORA_FINE_ATTIVITA;

        return orarioAttivita;
    }

    public static QuantitaMinima inserisciNuovaQuantitaMinima(int numeroCategorie, int numeroGiorni) {
        int numeroCategoria = InputDati.leggiIntero("Inserisci la categoria per cui vuoi impostare una quantità minima di personale: (i numeri vanno da 1 a " + (numeroCategorie) + "): ", 1, numeroCategorie);
        LocalTime[] orarioQuantitaMinima = new LocalTime[2];
        OutputDati.stampaMessaggio("Usa il formato HH:mm e un orario consono al frazionamento di giornata scelto):");

        do {
            try {
                String string_oraInizio = InputDati.leggiStringaNonVuota("ora di inizio della fascia in cui si desidera una quantità minima di personale della categoria " + numeroCategoria +": ");
                String string_oraFine = InputDati.leggiStringaNonVuota("ora di fine della fascia in cui si desidera una quantità minima di personale della categoria " + numeroCategoria +": ");
                orarioQuantitaMinima[0] = LocalTime.parse(string_oraInizio);
                orarioQuantitaMinima[1] = LocalTime.parse(string_oraFine);
            } catch (Exception e) {
                OutputDati.stampaErrore("Il formato inserito non è accettato");
            }
        } while (orarioQuantitaMinima[0] == null || orarioQuantitaMinima[1] == null);

        int quantitaMinimaPersonale = InputDati.leggiIntero("Inserisci la quantità minima di personale che si desidera nella fascia oraria inserita: ");

        boolean isGiornoSbagliato;
        int[] listaGiorni;
        do {
            isGiornoSbagliato = false;
            String string_listaGiorni = InputDati.leggiStringaNonVuota("Inserisci, separati da una virgola, i numeri dei giorni in cui si richiede la quantità minima: (i numeri vanno da 0 a " + (numeroGiorni - 1) + "): ");
            listaGiorni = LettoreIstanzaDaFile.estraiElementiInt(string_listaGiorni);

            for (int j : listaGiorni) {
                if (j < 0 || j >= numeroGiorni) {
                    isGiornoSbagliato = true;
                }

                if (isGiornoSbagliato) {
                    OutputDati.stampaErrore("I numeri dei giorni inseriti non sono tra 0 e " + (numeroGiorni - 1));
                }
            }

        } while (isGiornoSbagliato);
        QuantitaMinima quantitaMinima = new QuantitaMinima(orarioQuantitaMinima[0], orarioQuantitaMinima[1], numeroCategoria, quantitaMinimaPersonale, listaGiorni);

        return quantitaMinima;
    }

    public static int[] assegnOreUomoMinimeManuale(int numeroDipendenti, int numeroGiorni) {
        int[] array_oreUomoMinime = new int[numeroGiorni];

        for (int g = 0; g < numeroGiorni; g++) {
            array_oreUomoMinime[g] = InputDati.leggiInteroNonNegativo("Inserisci il numero di ore minime che devono essere svolte nella giornata " + g + ": ");
        }

        return array_oreUomoMinime;
    }

    public static int[] assegnOreUomoMinimeCasuale(int numeroDipendenti, int numeroGiorni, int numeroOreContinue) {
        int[] array_oreUomoMinime = new int[numeroGiorni];

        for (int g = 0; g < numeroGiorni; g++) {
            int percentuale = EstrazioniCasuali.estraiIntero(0,20);
            int piuOrMeno;

            do {
                piuOrMeno = EstrazioniCasuali.estraiIntero(-1, 1);
            } while(piuOrMeno == 0);

            array_oreUomoMinime[g] = numeroDipendenti * numeroOreContinue + (piuOrMeno * (percentuale * numeroDipendenti * numeroOreContinue / 100));
        }

        return array_oreUomoMinime;
    }
}
