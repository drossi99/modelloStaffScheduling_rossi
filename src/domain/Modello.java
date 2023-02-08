package domain;

import gurobi.*;
import view.OutputDati;

public class Modello {
    private GRBModel modelloGRB;
    private GRBVar[][][] vettoreX;
    private GRBVar[][] vettoreY;

    public String valoriVettoreXToString() throws GRBException {

        StringBuilder valoriVettoreX = new StringBuilder("I valori delle variabili vettoreX[giorno][dipendente][intervallo] sono:\n");
        try {
            for (int g = 0; g < vettoreX.length; g++) {
                for (int d = 0; d < vettoreX[g].length; d++) {
                    for (int t = 0; t < vettoreX[g][d].length; t++) {
                        valoriVettoreX.append(vettoreX[g][d][t].get(GRB.StringAttr.VarName)).append(": ").append(vettoreX[g][d][t].get(GRB.DoubleAttr.X)).append("  ");
                    }
                    valoriVettoreX.append("\n");
                }
                valoriVettoreX.append("\n");
            }
        } catch (GRBException grbException) {
            OutputDati.stampaErrore("Non e' stato possibile ottenere i valori delle variabili X");
        }
        return valoriVettoreX.toString();
    }

    public String valoriVettoreYToString() throws GRBException {
        StringBuilder valoriVettoreY = new StringBuilder("I valori delle variabili vettoreY[giorno][dipendente] sono:\n");

        try {
            for (int d = 0; d < vettoreY[0].length; d++) {
                for (int g = 0; g < vettoreY.length; g++) {
                    valoriVettoreY.append(vettoreY[g][d].get(GRB.StringAttr.VarName)).append(": ").append(vettoreY[g][d].get(GRB.DoubleAttr.X)).append("  ");
                    valoriVettoreY.append("\n");
                }
                valoriVettoreY.append("\n");
            }
        } catch (GRBException grbException) {
            OutputDati.stampaErrore("Non e' stato possibile ottenere i valori delle variabili Y");
        }
        return valoriVettoreY.toString();
    }

    public Modello(GRBModel modelloGRB, GRBVar[][][] vettoreX, GRBVar[][] vettoreY) {
        this.modelloGRB = modelloGRB;
        this.vettoreX = vettoreX;
        this.vettoreY = vettoreY;
    }

    public GRBModel getModelloGRB() {
        return modelloGRB;
    }

    public void setModelloGRB(GRBModel modelloGRB) {
        this.modelloGRB = modelloGRB;
    }

    public GRBVar[][][] getVettoreX() {
        return vettoreX;
    }

    public void setVettoreX(GRBVar[][][] vettoreX) {
        this.vettoreX = vettoreX;
    }

    public GRBVar[][] getVettoreY() {
        return vettoreY;
    }

    public void setVettoreY(GRBVar[][] vettoreY) {
        this.vettoreY = vettoreY;
    }

    public void impostaVettoriXY(GRBVar[] vars, int numGiorni, int numDipendenti, int numIntervalli) {
        int numeroVariabiliX = numGiorni * numDipendenti * numIntervalli;
        System.out.println("Il modello ha " + vars.length + " variabili");
        int i = 0;
        for (int g = 0; g < numGiorni; g++) {
            for (int d = 0; d< numDipendenti; d++) {
                for (int t = 0; t < numIntervalli; t++) {
                    this.vettoreX[g][d][t] = vars[i];
                    i = i + 1;
                }
            }
        }

        for (int g = 0; g < numGiorni; g++) {
            for (int d = 0; d< numDipendenti; d++) {
                this.vettoreY[g][d] = vars[i];
                    i = i + 1;
            }
        }
    }
}
