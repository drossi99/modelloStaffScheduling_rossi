package domain;

import view.OutputDati;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IstanzaProblema {
    private String nome;
    private int numeroDipendenti;
    private Frazionamento frazionamentoGiornata;
    private int numeroCategorieDipendenti;
    private double[] array_pagheOrarieNormalizzato;
    private double[] array_pagheOrariePerIntervallo;
    private int[] array_isFullTime;
    private int[] array_categoriaDipendente;
    private int numeroGiorniSettimana;
    private int numeroOreGiornata;
    private int percentualeParttime;
    private int numeroOreMinParttime;
    private int numeroOreMinFulltime;
    private int numeroOreMaxParttime;
    private int numeroOreMaxFulltime;
    private int numeroGiorniLavorativiMax;
    private int oreMinimeContinuative;
    private int durataMassimaTurno;
    private LocalTime oraInizioAttivita;
    private LocalTime oraFineAttivita;
    private int[] array_oreUomoMinime;

    public int getOreMinimeContinuative() {
        return oreMinimeContinuative;
    }

    public int getDurataMassimaTurno() {
        return durataMassimaTurno;
    }

    public IstanzaProblema(String nomeIstanza, int numeroDipendenti, Frazionamento frazionamentoGiornata, int numeroCategorieDipendenti, double[] array_pagheOrarieNormalizzato, int[] array_isFullTime, int[] array_categoriaDipendente, int[] parametriTipici, LocalTime[] orarioAttivita, int[] array_oreUomoMinime) {
        this.nome = nomeIstanza;
        this.numeroDipendenti = numeroDipendenti;
        this.frazionamentoGiornata = frazionamentoGiornata;
        this.numeroCategorieDipendenti = numeroCategorieDipendenti;
        this.array_pagheOrarieNormalizzato = array_pagheOrarieNormalizzato;
        this.array_pagheOrariePerIntervallo = GeneratoreDatiIstanze.calcolaPagheOrariePerIntervallo(array_pagheOrarieNormalizzato, frazionamentoGiornata.getFattoreMoltiplicativo());
        this.array_isFullTime = array_isFullTime;
        this.array_categoriaDipendente = array_categoriaDipendente;
        this.numeroGiorniSettimana = parametriTipici[0];
        this.numeroOreGiornata = parametriTipici[1];
        this.percentualeParttime = parametriTipici[2];
        this.numeroOreMinFulltime = parametriTipici[3];
        this.numeroOreMaxFulltime = parametriTipici[4];
        this.numeroOreMinParttime = percentualeParttime * numeroOreMinFulltime / 100;
        this.numeroOreMaxParttime = percentualeParttime * numeroOreMaxFulltime / 100;
        this.numeroGiorniLavorativiMax = parametriTipici[5];
        this.oreMinimeContinuative = parametriTipici[6];
        this.durataMassimaTurno = parametriTipici[7];
        this.oraInizioAttivita = orarioAttivita[0];
        this.oraFineAttivita = orarioAttivita[1];
        this.array_oreUomoMinime = array_oreUomoMinime;
    }

    public String toText() {
        StringBuilder text = new StringBuilder("******\nL'istanza presente è composta come segue:\n");
        text.append("\n\tnome: ").append(this.nome);
        text.append("\n\tnumero di dipendenti: ").append(this.numeroDipendenti);
        text.append("\n\tnumero di intervalli in 1 giornata: ").append(this.frazionamentoGiornata.getNumeroIntervalli());
        text.append("\n\tnumero di categorie dipendenti: ").append(this.numeroCategorieDipendenti);
        text.append("\n\tvettore paghe orarie di ogni categoria:");
        for (int i = 0; i < this.numeroCategorieDipendenti; i++) {
            text.append("\n\t\tcategoria: ").append(i).append(" -> ").append(this.array_pagheOrarieNormalizzato[i]);
        }
        text.append("\n\tvettore paghe per intervallo di ogni categoria:");
        for (int i = 0; i < this.numeroCategorieDipendenti; i++) {
            text.append("\n\t\tcategoria: ").append(i).append(" -> ").append(this.array_pagheOrariePerIntervallo[i]);
        }
        text.append("\n\tvettore che indica se un dipendente sia part (0) o full (1) time:");
        for (int i = 0; i < this.numeroDipendenti; i++) {
            text.append("\n\t\tdipendente: ").append(i).append(" -> ").append(this.array_isFullTime[i]);
        }
        text.append("\n\tvettore che indica a che categoria appartenga un dipendente:");
        for (int i = 0; i < this.numeroDipendenti; i++) {
            text.append("\n\t\tdipendente: ").append(i).append(" -> ").append(this.array_categoriaDipendente[i]);
        }
        text.append("\n\tnumero di giorni della settimana: ").append(this.numeroGiorniSettimana);
        text.append("\n\tnumero di ore della giornata: ").append(this.numeroOreGiornata);
        text.append("\n\tpercentuale part-time: ").append(this.percentualeParttime);
        text.append("\n\tnumero di ore minime per part-time: ").append(this.numeroOreMinParttime);
        text.append("\n\tnumero di ore minime per full-time: ").append(this.numeroOreMinFulltime);
        text.append("\n\tnumero di ore massime per part-time: ").append(this.numeroOreMaxParttime);
        text.append("\n\tnumero di ore massime per full-time: ").append(this.numeroOreMaxFulltime);
        text.append("\n\tnumero di giorni lavorativi massimi in 1 settimana: ").append(this.numeroGiorniLavorativiMax);
        text.append("\nore minime continuative di un turno: ").append(this.oreMinimeContinuative);
        text.append("\ndurata massima di un turno: ").append(this.durataMassimaTurno);
        text.append("\norario di attività:");
        if (this.getOraInizioAttivita() != null && this.getOraFineAttivita() != null) {
            text.append("\n\tora inizio attività: ").append(this.oraInizioAttivita.format(DateTimeFormatter.ofPattern("HH:mm")));
            text.append("\n\tora fine attività: ").append(this.oraFineAttivita.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            text.append("\n\tattiva 24 ore su 24");
        }

        text.append("\nvettore ore uomo minime durante le giornate:");
        for (int i = 0; i < this.numeroGiorniSettimana; i++) {
            text.append("\n\t\tgiorno: ").append(i).append(" -> ").append(this.array_oreUomoMinime[i]);
        }
        text.append("\n*******");
        return text.toString();
    }

    public File toFile(String nomeFile) {
        File fileIstanza = new File(nomeFile);
        try {
            if (fileIstanza.createNewFile()) {
                OutputDati.stampaMessaggio("Il file " + fileIstanza.getName() + " è stato creato correttamente");
            } else {
                OutputDati.stampaErrore("Il file " + fileIstanza.getName() + " esiste già: verrà sovrascritto");
            }

            FileWriter fileIstanzaWriter = new FileWriter(fileIstanza);
            fileIstanzaWriter.write(this.toTextAttributiPerLinea());
            fileIstanzaWriter.close();

            OutputDati.stampaMessaggio("Il file " + fileIstanza.getName() + " è stato scritto correttamente");
        } catch (IOException e) {
            OutputDati.stampaErrore("Non è stato possibile creare il file");
            e.printStackTrace();
        }
        return fileIstanza;
    }

    private String toTextAttributiPerLinea() {
        StringBuilder text = new StringBuilder();
        text.append(this.nome).append("\n");
        text.append(this.numeroDipendenti).append("\n");
        text.append(this.frazionamentoGiornata.getNumeroIntervalli()).append("\n");
        text.append(this.numeroCategorieDipendenti).append("\n");
        for (int i = 0; i < this.numeroCategorieDipendenti-1; i++) {
            text.append(this.array_pagheOrarieNormalizzato[i]).append(",");
        }
        text.append(this.array_pagheOrarieNormalizzato[this.numeroCategorieDipendenti - 1]).append("\n"); //in modo da avere paga1,paga2,paga3\n

        for (int i = 0; i < this.numeroDipendenti-1; i++) {
            text.append(this.array_isFullTime[i]).append(",");
        }
        text.append(this.array_isFullTime[this.numeroDipendenti - 1]).append("\n");
        for (int i = 0; i < this.numeroDipendenti-1; i++) {
            text.append(this.array_categoriaDipendente[i]).append(",");
        }
        text.append(this.array_categoriaDipendente[this.numeroDipendenti - 1]).append("\n");

        //valori tipici
        text.append(this.numeroGiorniSettimana).append(",");
        text.append(this.numeroOreGiornata).append(",");
        text.append(this.percentualeParttime).append(",");
        text.append(this.numeroOreMinFulltime).append(",");
        text.append(this.numeroOreMaxFulltime).append(",");
        text.append(this.numeroGiorniLavorativiMax).append(",");
        text.append(this.oreMinimeContinuative).append(",");
        text.append(this.durataMassimaTurno).append("\n");

        if (this.getOraInizioAttivita() != null && this.getOraFineAttivita() != null) {
            text.append(this.oraInizioAttivita.format(DateTimeFormatter.ofPattern("HH:mm"))).append(",");
            text.append(this.oraFineAttivita.format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
        } else {
            text.append("--\n");
        }
        for (int i = 0; i < this.numeroGiorniSettimana - 1; i++) {
            text.append(this.array_oreUomoMinime[i]).append(",");
        }
        text.append(this.array_oreUomoMinime[this.numeroGiorniSettimana - 1]);
        return text.toString();
    }

    public void controllaNomeDoppio(List<IstanzaProblema> listaIstanze) {
        boolean hasNomeDoppio = this.richiediNuovoNomeSeDoppio(listaIstanze);

        if (hasNomeDoppio) {
            do {
                this.setNome(GeneratoreDatiIstanze.assegnaNomeManuale());
                hasNomeDoppio = this.richiediNuovoNomeSeDoppio(listaIstanze);
            } while (hasNomeDoppio);

        }
    }

    public boolean richiediNuovoNomeSeDoppio(List<IstanzaProblema> listaIstanze) {
        String nomeDaControllare = this.nome;
        boolean nomeIsDoppio = false;
        for (IstanzaProblema istanzaDaLista : listaIstanze) {
            if (nomeDaControllare.equals(istanzaDaLista.getNome())) {
                OutputDati.stampaErrore("Il nome " + nomeDaControllare + " dell'istanza che si vuole salvare è già usato: immetterne uno nuovo");
                nomeIsDoppio = true;
            }
        }
        return nomeIsDoppio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNumeroDipendenti() {
        return numeroDipendenti;
    }

    public Frazionamento getFrazionamentoGiornata() {
        return frazionamentoGiornata;
    }

    public int getNumeroCategorieDipendenti() {
        return numeroCategorieDipendenti;
    }

    public double[] getArray_pagheOrariePerIntervallo() {
        return array_pagheOrariePerIntervallo;
    }

    public int[] getArray_isFullTime() {
        return array_isFullTime;
    }

    public int[] getArray_categoriaDipendente() {
        return array_categoriaDipendente;
    }

    public int getNumeroGiorniSettimana() {
        return numeroGiorniSettimana;
    }

    public int getNumeroOreGiornata() {
        return numeroOreGiornata;
    }

    public int getNumeroOreMinParttime() {
        return numeroOreMinParttime;
    }

    public int getNumeroOreMinFulltime() {
        return numeroOreMinFulltime;
    }

    public int getNumeroOreMaxParttime() {
        return numeroOreMaxParttime;
    }

    public int getNumeroOreMaxFulltime() {
        return numeroOreMaxFulltime;
    }

    public int getNumeroGiorniLavorativiMax() {
        return numeroGiorniLavorativiMax;
    }

    public int getPercentualeParttime() {
        return percentualeParttime;
    }

    public LocalTime getOraInizioAttivita() {
        return oraInizioAttivita;
    }

    public LocalTime getOraFineAttivita() {
        return oraFineAttivita;
    }

    public int[] getArray_oreUomoMinime() {
        return array_oreUomoMinime;
    }
}
