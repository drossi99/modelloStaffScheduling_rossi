package domain;

import gurobi.*;

public class GeneratoreModello {
    static IstanzaProblema istanza;

    public static Modello generaModelloDaIstanza(IstanzaProblema istanza) throws GRBException {
        GRBEnv env = new GRBEnv(istanza.getNome() + ".log");
        GRBModel modelGRB = new GRBModel(env);
        GRBVar[][][] vettoreX = dichiaraVariabiliX(modelGRB, istanza);
        GRBVar[][] vettoreY = dichiaraVariabiliY(modelGRB, istanza);
        dichiaraFunzioneObiettivo(istanza, modelGRB, vettoreX);

        return new Modello(modelGRB, vettoreX, vettoreY);
    }

    private static void dichiaraFunzioneObiettivo(IstanzaProblema istanza, GRBModel model, GRBVar[][][] vettoreX) throws GRBException {
        GRBLinExpr funObjExpr = new GRBLinExpr();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int[] array_categoriaDipendente = istanza.getArray_categoriaDipendente();
        for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
            for (int d=0; d<istanza.getNumeroDipendenti(); d++) {
                for (int t=0; t < numeroIntervalli; t++) {
                    funObjExpr.addTerm(istanza.getArray_pagheOrariePerIntervallo()[array_categoriaDipendente[d]-1], vettoreX[g][d][t]); //metto -1 perche le categorie sono 1,2, 3, mentre l'arrayCategorieDipendente parte da 0,1,2
                }
            }
        }
        model.setObjective(funObjExpr, GRB.MINIMIZE);
    }

    private static GRBVar[][][] dichiaraVariabiliX(GRBModel modelGRB, IstanzaProblema istanza) throws GRBException {
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        GRBVar[][][] vettoreX = new GRBVar[istanza.getNumeroGiorniSettimana()][istanza.getNumeroDipendenti()][istanza.getNumeroOreGiornata() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo()];
        for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
            for (int d=0; d<istanza.getNumeroDipendenti(); d++) {
                for (int t=0; t < numeroIntervalli; t++) {
                    GRBVar nuovaVar_x = modelGRB.addVar(0, 1, 0, GRB.BINARY, "x"+ g + "_" + d + "_" + t);
                    vettoreX[g][d][t] = nuovaVar_x;
                }
            }
        }
        return vettoreX;
    }

    private static GRBVar[][] dichiaraVariabiliY(GRBModel modelGRB, IstanzaProblema istanza) throws GRBException {
        int numeroGiorniSettimana = istanza.getNumeroGiorniSettimana();
        int numeroDipendenti = istanza.getNumeroDipendenti();
        GRBVar[][] vettoreY = new GRBVar[numeroGiorniSettimana][numeroDipendenti];
        for (int g = 0; g < numeroGiorniSettimana; g++) {
            for (int d=0; d < numeroDipendenti; d++) {
                    GRBVar nuovaVar_y = modelGRB.addVar(0, 1, 0, GRB.BINARY, "y"+ g + "_" + d);
                    vettoreY[g][d] = nuovaVar_y;
                }
            }
        return vettoreY;
    }
}
