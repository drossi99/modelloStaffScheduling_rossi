package view;

import domain.IstanzaProblema;
import domain.Modello;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OutputDati {
    public static void stampaMessaggio(String s) {
        System.out.println(s);
        System.out.flush();
    }

    public static void stampaErrore(String s) {
        System.err.println(s);
        System.err.flush();
    }

    public static void presentaRisultati(Modello modelloConVincoli, IstanzaProblema istanza, GRBModel modelloGRB) throws GRBException {
        if (modelloConVincoli.getModelloGRB().get(GRB.IntAttr.Status) == 3) {//il modello e' impossibile
            stampaMessaggio("Il modello inserito è impossibile");
        } else {
            stampaMessaggio(produciOutputModelloOttimizzatoFeasible(modelloConVincoli, istanza));
            stampaMessaggio(produciOutputModelloOttimizzatoFeasibleOrari(modelloConVincoli, istanza, modelloGRB));
            scriviFileOutput(modelloConVincoli, istanza, modelloGRB);
        }
    }

    private static void scriviFileOutput(Modello modelloConVincoli, IstanzaProblema istanza, GRBModel modelloGRB) {
        File fileIstanza = new File(istanza.getNome() + "_soluzione.txt");
        try {
            if (fileIstanza.createNewFile()) {
                OutputDati.stampaMessaggio("Il file " + fileIstanza.getName() + " è stato creato correttamente");
            } else {
                OutputDati.stampaErrore("Il file " + fileIstanza.getName() + " esiste già: verrà sovrascritto");
            }

            FileWriter fileIstanzaWriter = new FileWriter(fileIstanza);
            fileIstanzaWriter.write(produciOutputModelloOttimizzatoFeasibleOrari(modelloConVincoli, istanza, modelloGRB));
            fileIstanzaWriter.close();

            OutputDati.stampaMessaggio("Il file " + fileIstanza.getName() + " è stato scritto correttamente");
        } catch (IOException e) {
            OutputDati.stampaErrore("Non è stato possibile creare il file");
            e.printStackTrace();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    private static String produciOutputModelloOttimizzatoFeasibleOrari(Modello modelloConVincoli, IstanzaProblema istanza, GRBModel modelloGRB) throws GRBException {
        int numeroGiorniSettimana = istanza.getNumeroGiorniSettimana();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        GRBVar[][][] vettoreX = modelloConVincoli.getVettoreX();
        GRBVar[][] vettoreY = modelloConVincoli.getVettoreY();

        StringBuilder stringBuilderOutput = new StringBuilder();
        for (int d = 0; d < vettoreX[0].length; d++) {
                StringBuilder strDipendente = new StringBuilder();
                strDipendente.append("*****");
                strDipendente.append(getOrariDipendente(vettoreX, vettoreY, d, numeroGiorniSettimana, numeroIntervalli, fattoreMoltiplicativo));

                stringBuilderOutput.append(strDipendente);
        }

        stringBuilderOutput.append("Il costo totale per il datore di lavoro è di € " + modelloGRB.get(GRB.DoubleAttr.ObjVal));
        stringBuilderOutput.append("\nIl modello ha " + modelloGRB.get(GRB.IntAttr.NumVars) + " variabili");
        stringBuilderOutput.append("\nIl modello ha " + modelloGRB.get(GRB.IntAttr.NumConstrs) + " vincoli");
        stringBuilderOutput.append("\nIl modello ci ha messo " + modelloGRB.get(GRB.DoubleAttr.Runtime) + " secondi");
        

        return stringBuilderOutput.toString();
    }

    private static String getOrariDipendente(GRBVar[][][] vettoreX, GRBVar[][] vettoreY, int d, int numeroGiorni, int numeroIntervalli, int fattoreMoltiplicativo) throws GRBException {
        ArrayList<Integer> listaVariabiliLineare = new ArrayList<>();
        ArrayList<Integer> listaIntervalliInizio = new ArrayList<>();
        ArrayList<Integer> listaGiorniInizio = new ArrayList<>();
        ArrayList<Integer> listaIntervalliFine = new ArrayList<>();
        ArrayList<Integer> listaGiorniFine = new ArrayList<>();

        StringBuilder strDipendente = new StringBuilder();
        strDipendente.append("*****");

        for (int g = 0; g < numeroGiorni; g++) {
            for (int t = 0; t < numeroIntervalli; t++) {
                listaVariabiliLineare.add((int) vettoreX[g][d][t].get(GRB.DoubleAttr.X));
            }
        }

        for (int i = 0; i < listaVariabiliLineare.size(); i++) {
            if (i == 0) {
                if (listaVariabiliLineare.get(i) == 1) {
                    listaIntervalliInizio.add(i);
                    listaGiorniInizio.add(i / numeroIntervalli);
                }
            } else {
                if (listaVariabiliLineare.get(i) == 1 && listaVariabiliLineare.get(i - 1) == 0) {
                    listaIntervalliInizio.add(i);
                    listaGiorniInizio.add(i / numeroIntervalli);
                }
            }
        }

        for (int i = 0; i < listaVariabiliLineare.size(); i++) {
            if (i == listaVariabiliLineare.size() - 1) {
                if (listaVariabiliLineare.get(i) == 1) {
                    listaIntervalliFine.add(i);
                    listaGiorniFine.add(numeroGiorni - 1);
                }
            } else {
                if (listaVariabiliLineare.get(i) == 1 && listaVariabiliLineare.get(i + 1) == 0) {
                    listaIntervalliFine.add(i);
                    listaGiorniFine.add(i / numeroIntervalli);
                }
            }
        }

        ArrayList<Integer> listaGiorniNonLavorativi = new ArrayList<>();
        for (int i = 0; i < numeroGiorni; i++) {
            listaGiorniNonLavorativi.add(i);
        }

        listaGiorniNonLavorativi.removeIf(listaGiorniInizio::contains);
        listaGiorniNonLavorativi.removeIf(listaGiorniFine::contains);

        strDipendente.append("\nIl dipendente " + d + " avrà questi orari:");
        strDipendente.append("\n\tnon lavora nei giorni: ");
        for (int j = 0; j < listaGiorniNonLavorativi.size() - 1; j++) {
            strDipendente.append(listaGiorniNonLavorativi.get(j)).append(", ");
        }
        strDipendente.append(listaGiorniNonLavorativi.get(listaGiorniNonLavorativi.size() - 1));

        for (int j = 0; j < listaIntervalliInizio.size(); j++) {
            int oraInizio = (listaIntervalliInizio.get(j) % numeroIntervalli) / fattoreMoltiplicativo;
            int minutiInizio = ((listaIntervalliInizio.get(j) % numeroIntervalli) % fattoreMoltiplicativo) * 60 / fattoreMoltiplicativo ;
            int oraFine = ((listaIntervalliFine.get(j) % numeroIntervalli )/ fattoreMoltiplicativo) + 1;//+1 serve perché l'intervallo 5 inizia sì alle 5, ma termina alle 6
            int minutiFine = ((listaIntervalliFine.get(j) % numeroIntervalli) % fattoreMoltiplicativo) * 60 / fattoreMoltiplicativo;
            strDipendente.append("\n\til giorno " + (listaGiorniInizio.get(j)) + ": dalle ");
            strDipendente.append(oraInizio);
            strDipendente.append(":");
            strDipendente.append(minutiInizio == 0 ? "00" : minutiInizio);
            strDipendente.append(" alle ");
            strDipendente.append(oraFine == 24 ? "00" : oraFine);
            strDipendente.append(":");
            strDipendente.append(minutiFine == 0 ? "00" : minutiFine);
            if (listaGiorniInizio.get(j) != listaGiorniFine.get(j) || oraFine == 24) {
                strDipendente.append(" del giorno seguente");
            }
        }

        strDipendente.append("\n");

        return strDipendente.toString();
    }

    private static ArrayList<Integer> getIntervalliFine(GRBVar[] vettoreX) throws GRBException {
        ArrayList<Integer> listaIntervalliFine = new ArrayList<>();

        for (int t = 1; t < vettoreX.length; t++) {
            if (vettoreX[t].get(GRB.DoubleAttr.X) == 0 && vettoreX[t-1].get(GRB.DoubleAttr.X) == 1) {
                listaIntervalliFine.add(t);
            }
        }

        return listaIntervalliFine;
    }

    private static String getIntervalloInizio(GRBVar[] intervalli, int tInizio, int numeroIntervalli, int fattoreMoltiplicativo) throws GRBException {
        String stringOrario = "";
        for (int t = tInizio; t < numeroIntervalli; t++) {
            if (t == 0) {
                if (intervalli[t].get(GRB.DoubleAttr.X) == 1) {
                    return "Dalle " + daIntervalloToStringaOrario(t, fattoreMoltiplicativo);
                }
            } else {
                if (intervalli[t].get(GRB.DoubleAttr.X) == 1 && intervalli[t - 1].get(GRB.DoubleAttr.X) == 0) {
                    return "Dalle " + daIntervalloToStringaOrario(t, fattoreMoltiplicativo);
                }
            }
        }
        return stringOrario;
    }

    private static String getIntervalloFine(GRBVar[] intervalli, int tInizio, int numeroIntervalli, int fattoreMoltiplicativo) throws GRBException {
        String stringOrario = "";
        for (int t = tInizio; t < numeroIntervalli; t++) {
            if (tInizio != 0) {
                if (intervalli[t].get(GRB.DoubleAttr.X) == 0 && intervalli[t-1].get(GRB.DoubleAttr.X) == 1) {
                    return "alle " + daIntervalloToStringaOrario(t, fattoreMoltiplicativo);
                }
            }
        }
        return stringOrario;
    }

    private static String daIntervalloToStringaOrario(int t, int fattoreMoltiplicativo) {
        String oraString = "";
        String minutiString = "";
        int minuti;
        int ora = t / (24 * fattoreMoltiplicativo);

        oraString += Integer.toString(ora);
        switch (fattoreMoltiplicativo) {
            case (1):
                minuti = (t % fattoreMoltiplicativo) * 60;
                break;
            case (2):
                minuti = (t % fattoreMoltiplicativo) * 30;
                break;
            default:
                minuti = (t % fattoreMoltiplicativo) * 15;
        }

        if (minuti == 0) {
            minutiString += "00";
        } else {
            minutiString = Integer.toString(minuti);
        }

        return oraString + ":" + minutiString;
    }

    private static String produciOutputModelloOttimizzatoFeasible(Modello modelloConVincoli, IstanzaProblema istanza) throws GRBException {
        int numeroGiorniSettimana = istanza.getNumeroGiorniSettimana();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        GRBVar[][][] vettoreX = modelloConVincoli.getVettoreX();
        GRBVar[][] vettoreY = modelloConVincoli.getVettoreY();

        OutputDati.stampaMessaggio(modelloConVincoli.valoriVettoreXToString());
        OutputDati.stampaMessaggio(modelloConVincoli.valoriVettoreYToString());

        StringBuilder stringBuilderOutput = new StringBuilder();

        for (int d = 0; d < vettoreX[0].length; d++) {
            StringBuilder strDipendente = new StringBuilder();

            int sommaGiorniLavorativiInUnaSettimana = 0;
            int sommaIntervalliLavorativi = 0;
            for (int g = 0; g < vettoreX.length; g++) {
                sommaGiorniLavorativiInUnaSettimana += vettoreY[g][d].get(GRB.DoubleAttr.X);
            }
            strDipendente.append("\n******\nIl dipendente ").append(d).append(" lavora ").append(sommaGiorniLavorativiInUnaSettimana).append(" giorni in questa settimana");

            for (int g = 0; g < numeroGiorniSettimana; g++) {
                strDipendente.append("\nIl dipendente ").append(d).append(" il giorno ").append(g).append(" avra' questi turni\n\t[");

                for (int t = 0; t < numeroIntervalli; t++) {
                    strDipendente.append((int) vettoreX[g][d][t].get(GRB.DoubleAttr.X)).append("\t");
                    sommaIntervalliLavorativi += vettoreX[g][d][t].get(GRB.DoubleAttr.X);
                }
                strDipendente.append("]");

            }
            strDipendente.append("\nIl dipendente " + d + " lavora " + (sommaIntervalliLavorativi / fattoreMoltiplicativo) + " ore");
            stringBuilderOutput.append(strDipendente.toString());
        }

        return stringBuilderOutput.toString();
    }
}
