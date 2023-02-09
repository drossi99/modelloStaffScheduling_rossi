package domain;

import gurobi.*;
import utility.InputDati;
import view.OutputDati;

import java.time.LocalTime;
import java.util.ArrayList;

public class Risolutore {

    public Risolutore() {
    }

    public Modello aggiungiVincoli(IstanzaProblema istanza) throws GRBException {
        Modello modelloDaIstanza = GeneratoreModello.generaModelloDaIstanza(istanza);
        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        modelloDaIstanza.setModelloGRB(aggiungiVincolo1(istanza, modelloDaIstanza));        //almeno le ore minime settimanali
        modelloDaIstanza.setModelloGRB(aggiungiVincolo2(istanza, modelloDaIstanza));        //non piu delle ore massime settimanali
        modelloDaIstanza.setModelloGRB(aggiungiVincolo3(istanza, modelloDaIstanza));        //giorno lavorativi massimi
        modelloDaIstanza.setModelloGRB(aggiungiVincolo4(istanza, modelloDaIstanza));        //pone y=1 se lavora durante quel giorno
        modelloDaIstanza.setModelloGRB(aggiungiVincolo5(istanza, modelloDaIstanza));        //variabili compatte
        modelloDaIstanza.setModelloGRB(aggiungiVincolo6(istanza, modelloDaIstanza));        //durata massima del turno

        if (istanza.getOraInizioAttivita() != null && istanza.getOraFineAttivita() != null) {
            modelloDaIstanza.setModelloGRB(aggiungiVincolo7(istanza, modelloDaIstanza));    //inizio attivita
            modelloDaIstanza.setModelloGRB(aggiungiVincolo8(istanza, modelloDaIstanza));    //fine attivita
        }
        modelloDaIstanza.setModelloGRB(aggiungiVincolo9(istanza, modelloDaIstanza));        //pausa minima tra turni
        modelloDaIstanza.setModelloGRB(aggiungiVincolo10(istanza, modelloDaIstanza));       //ore uomo minime rg

        return modelloDaIstanza;
    }

    private GRBModel aggiungiVincolo1(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (1): almeno ore minime settimanali
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();
        for (int d = 0; d< istanza.getNumeroDipendenti(); d++) {
            GRBLinExpr sommatoriaIntervalliDuranteLaSettimana = new GRBLinExpr();
            int numeroOreMinimeDelDipendente = istanza.getNumeroOreMinParttime() * (1 - istanza.getArray_isFullTime()[d]) + istanza.getNumeroOreMinFulltime() * istanza.getArray_isFullTime()[d];
            int intervalliMinimiSettimanali = numeroOreMinimeDelDipendente * fattoreMoltiplicativo;

            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                for (int t = 0; t < numeroIntervalli; t++) {
                    sommatoriaIntervalliDuranteLaSettimana.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
            }
            GRBConstr vincoloOreMinimeSettimanali = modelloGRB.addConstr(sommatoriaIntervalliDuranteLaSettimana, GRB.GREATER_EQUAL, intervalliMinimiSettimanali, "vincoloOreMinime_dip" + d);
        }
        return modelloGRB;
    }



    private GRBModel aggiungiVincolo2(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (2): non piu di ore massime settimanali
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();
        for (int d = 0; d< istanza.getNumeroDipendenti(); d++) {
            GRBLinExpr sommatoriaIntervalliDuranteLaSettimana = new GRBLinExpr();
            int numeroOreMassimeDelDipendente = istanza.getNumeroOreMaxParttime() * (1 - istanza.getArray_isFullTime()[d]) + istanza.getNumeroOreMaxFulltime() * istanza.getArray_isFullTime()[d];
            int intervalliMassimiSettimanali = numeroOreMassimeDelDipendente * fattoreMoltiplicativo;
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                for (int t = 0; t < numeroIntervalli; t++) {
                    sommatoriaIntervalliDuranteLaSettimana.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
            }
            GRBConstr vincoloOreMassimeSettimanali = modelloGRB.addConstr(sommatoriaIntervalliDuranteLaSettimana, GRB.LESS_EQUAL, intervalliMassimiSettimanali, "vincoloOreMassime_dip" + d);
        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo3(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (3): un dipendente non puo' lavorare piu giorni di quelli consentiti durante una settimana
        int numeroGiorniMassimi = istanza.getNumeroGiorniLavorativiMax();
        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
            GRBLinExpr sommatoriaGiorniInCuidLavora = new GRBLinExpr();
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                sommatoriaGiorniInCuidLavora.addTerm(1, modelloDaIstanza.getVettoreY()[g][d]);
            }
            GRBConstr vincoloGiorniMassimi = modelloGRB.addConstr(sommatoriaGiorniInCuidLavora, GRB.LESS_EQUAL, numeroGiorniMassimi, "vincoloGiorniMassimi_" + d);

        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo4(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (4): pone a 1 la variabile y_{g,d} se il dipendente d lavora almeno durante un intervallo del giorno g
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        int bigM = istanza.getDurataMassimaTurno() * fattoreMoltiplicativo;
        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                GRBLinExpr sommatoriaIntervalliMenoYbigM = new GRBLinExpr();
                for (int t = 0; t < numeroIntervalli; t++) {
                    sommatoriaIntervalliMenoYbigM.addTerm(-1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
                sommatoriaIntervalliMenoYbigM.addTerm(bigM, modelloDaIstanza.getVettoreY()[g][d]);
                GRBConstr vincoloVarYbigM = modelloGRB.addConstr(sommatoriaIntervalliMenoYbigM, GRB.GREATER_EQUAL, 0, "vincoloVarYbigM_giorno" + g + "_dip" + d);
            }
        }
        return modelloGRB;
    }


    private GRBModel aggiungiVincolo5(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (5); vincolo sulla durata minima di un turno
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int numeroGiorni = istanza.getNumeroGiorniSettimana();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        int oreContinuative = istanza.getOreMinimeContinuative() * fattoreMoltiplicativo;

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
            for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
                for (int t = 0; t < numeroIntervalli; t++) {
                    GRBLinExpr sommatoriaVariabili = new GRBLinExpr();
                    switch (g) {
                        case 0:
                            switch (t) {
                                case 0:
                                    for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                        sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                    }
                                    sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                    break;
                                default:
                                    if (t + oreContinuative < numeroIntervalli) {
                                        for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                            sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                        }
                                        sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                        sommatoriaVariabili.addTerm(oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                    } else {
                                        int intervalliRimanenti = -(t - numeroIntervalli);
                                        for (int t_turno = t; t_turno < numeroIntervalli; t_turno++) {
                                            sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                        }
                                        for (int t_turno = 0; t_turno < intervalliRimanenti; t_turno++) {
                                            sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g + 1][d][t_turno]);
                                        }
                                        sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                        sommatoriaVariabili.addTerm(+oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                    }
                            }
                            break;
                        default:
                            if (g < numeroGiorni - 1) {
                                switch (t) {
                                    case 0:
                                        for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                            sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                        }
                                        sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                        sommatoriaVariabili.addTerm(oreContinuative, modelloDaIstanza.getVettoreX()[g - 1][d][numeroIntervalli - 1]);
                                        break;
                                    default:
                                        if (t + oreContinuative < numeroIntervalli) {
                                            for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                                sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                            }
                                            sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                            sommatoriaVariabili.addTerm(oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                        } else {
                                            int intervalliRimanenti = -(t - numeroIntervalli);
                                            for (int t_turno = t; t_turno < numeroIntervalli; t_turno++) {
                                                sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                            }
                                            for (int t_turno = 0; t_turno < intervalliRimanenti; t_turno++) {
                                                sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g + 1][d][t_turno]);
                                            }
                                            sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                            sommatoriaVariabili.addTerm(+oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                        }
                                }
                            } else {
                                switch (t) {
                                    case 0:
                                        for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                            sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                        }
                                        sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                        sommatoriaVariabili.addTerm(oreContinuative, modelloDaIstanza.getVettoreX()[g - 1][d][numeroIntervalli - 1]);
                                        break;
                                    default:
                                        if (t + oreContinuative < numeroIntervalli) {
                                            for (int t_turno = t; t_turno < t + oreContinuative; t_turno++) {
                                                sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                            }
                                            sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                            sommatoriaVariabili.addTerm(oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                        } else {
                                            int intervalliRimanenti = -(t - numeroIntervalli);
                                            for (int t_turno = t; t_turno < numeroIntervalli; t_turno++) {
                                                sommatoriaVariabili.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t_turno]);
                                            }

                                            sommatoriaVariabili.addTerm(-oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t]);
                                            sommatoriaVariabili.addTerm(+oreContinuative, modelloDaIstanza.getVettoreX()[g][d][t - 1]);
                                        }
                                }
                            }
                    }
                            GRBConstr vincoloOreMinimeContinuative = modelloGRB.addConstr(sommatoriaVariabili, GRB.GREATER_EQUAL, 0, "vincoloOreMinimeContinuative_giorno" + g + "_dip" + d + "_int" + t);
                }
            }
        }

        return modelloGRB;
    }

    private GRBModel aggiungiVincolo6(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (6): il turno non puo avere una durata superiore alla durata massima
        int intervalliDurataMassima = istanza.getDurataMassimaTurno() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                GRBLinExpr sommatoriaIntervalliTurno = new GRBLinExpr();
                for (int t = 0; t < numeroIntervalli; t++) {
                    sommatoriaIntervalliTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
                GRBConstr vincoloDurataMassima = modelloGRB.addConstr(sommatoriaIntervalliTurno, GRB.LESS_EQUAL, intervalliDurataMassima, "vincoloDurataMassima_giorno" + g + "_dip" + d);
            }
        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo7(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (7): il turno non puo iniziare prima dell'orario di inizio attivita dell'azienda
        int intervalloInizioAttivita = istanza.getOraInizioAttivita().getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + istanza.getOraInizioAttivita().getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                GRBLinExpr sommatoriaIntervalliPrimaInizioAttivita = new GRBLinExpr();
                for (int t = 0; t < intervalloInizioAttivita; t++) {
                    sommatoriaIntervalliPrimaInizioAttivita.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
                GRBConstr vincoloInizioAttivita = modelloGRB.addConstr(sommatoriaIntervalliPrimaInizioAttivita, GRB.EQUAL, 0, "vincoloInizioAttivita_giorno" + g + "_dip" + d);
            }
        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo8(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (8): il turno non puo finire prima dell'orario di fine attivita dell'azienda
        int intervalloFineAttivita = istanza.getOraFineAttivita().getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + istanza.getOraFineAttivita().getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();

        for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
            for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
                GRBLinExpr sommatoriaIntervalliDopoFineAttivita = new GRBLinExpr();
                for (int t = intervalloFineAttivita; t < istanza.getNumeroOreGiornata() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo(); t++) {
                    sommatoriaIntervalliDopoFineAttivita.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
                GRBConstr vincoloInizioAttivita = modelloGRB.addConstr(sommatoriaIntervalliDopoFineAttivita, GRB.EQUAL, 0, "vincoloFineAttivita_giorno_" + g + "dip_" + d);
            }
        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo9(IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        //vincolo (9); vincolo sulla riposo minimo di 11 ore dopo ogni turno
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();
        int numeroOreRiposoMinimo = 11;
        int intervalliPausaMinima = numeroOreRiposoMinimo * fattoreMoltiplicativo;

        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();


        for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
            for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
                for (int t = 0; t < numeroIntervalli; t++) {
                    if (t - intervalliPausaMinima < 0) {
                        if (g > 0) {
                            for (int p = 2; p < intervalliPausaMinima + 1; p++) {
                                if (t - p < 0) {
                                    GRBLinExpr sommatoriaTurno = new GRBLinExpr();
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g - 1][d][numeroIntervalli - p]);
                                    sommatoriaTurno.addTerm(-1, modelloDaIstanza.getVettoreX()[g - 1][d][numeroIntervalli - p + 1]);
                                    GRBConstr vincoloPausaMinimaTraTurni = modelloGRB.addConstr(sommatoriaTurno, GRB.LESS_EQUAL, 1, "vincoloPausaMinimaTraTurni_giorno" + g + "_dip" + d + "_int" + t + "_p" + p) ;

                                } else {
                                    GRBLinExpr sommatoriaTurno = new GRBLinExpr();
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t - p]);
                                    sommatoriaTurno.addTerm(-1, modelloDaIstanza.getVettoreX()[g][d][t - p + 1]);
                                    GRBConstr vincoloPausaMinimaTraTurni = modelloGRB.addConstr(sommatoriaTurno, GRB.LESS_EQUAL, 1, "vincoloPausaMinimaTraTurni_giorno" + g + "_dip" + d + "_int" + t + "_p" + p) ;

                                }
                            }
                        } else {
                            for (int p = 2; p < intervalliPausaMinima + 1; p++) {
                                if (t - p >= 0) {
                                    GRBLinExpr sommatoriaTurno = new GRBLinExpr();
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                                    sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t - p]);
                                    sommatoriaTurno.addTerm(-1, modelloDaIstanza.getVettoreX()[g][d][t - p + 1]);
                                    GRBConstr vincoloPausaMinimaTraTurni = modelloGRB.addConstr(sommatoriaTurno, GRB.LESS_EQUAL, 1, "vincoloPausaMinimaTraTurni_giorno" + g + "_dip" + d + "_int" + t + "_p" + p) ;
                                }
                            }
                        }
                    } else {
                        for (int p = 2; p < intervalliPausaMinima + 1; p++) {
                            GRBLinExpr sommatoriaTurno = new GRBLinExpr();
                            sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                            sommatoriaTurno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t - p]);
                            sommatoriaTurno.addTerm(-1, modelloDaIstanza.getVettoreX()[g][d][t - p + 1]);
                            GRBConstr vincoloPausaMinimaTraTurni = modelloGRB.addConstr(sommatoriaTurno, GRB.LESS_EQUAL, 1, "vincoloPausaMinimaTraTurni_giorno" + g + "_dip" + d + "_int" + t + "_p" + p) ;
                        }
                    }
                }
            }
        }
        return modelloGRB;
    }

    private GRBModel aggiungiVincolo10(IstanzaProblema istanza, Modello modelloDaIstanza) throws  GRBException {
        //vincolo (10): almeno ore uomo giornaliere rg
        GRBModel modelloGRB = modelloDaIstanza.getModelloGRB();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();
        int fattoreMoltiplicativo = istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo();

        for (int g = 0; g < istanza.getNumeroGiorniSettimana(); g++) {
            GRBLinExpr sommatoriaIntervalliUomoDuranteGiorno = new GRBLinExpr();
            int numeroOreUomoMinime = istanza.getArray_oreUomoMinime()[g];
            int intervalliUomoMinimiGiornalieri = numeroOreUomoMinime * fattoreMoltiplicativo;

            for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
                for (int t = 0; t < numeroIntervalli; t++) {
                    sommatoriaIntervalliUomoDuranteGiorno.addTerm(1, modelloDaIstanza.getVettoreX()[g][d][t]);
                }
            }
            GRBConstr vincoloOreUomoAlGiorno = modelloGRB.addConstr(sommatoriaIntervalliUomoDuranteGiorno, GRB.GREATER_EQUAL, intervalliUomoMinimiGiornalieri, "vincoloOreUomoMinime_giorno" + g);
        }

        return modelloGRB;
    }

    public void ottimizzaModello(Modello modelloConVincoli, IstanzaProblema istanza) throws GRBException {
        GRBModel modelloGRB = modelloConVincoli.getModelloGRB();

        OutputDati.stampaMessaggio("Si sta preparando il modello: potrebbe volerci un po' di tempo...");

        modelloGRB.presolve();
        modelloGRB.optimize();
        modelloConVincoli.impostaVettoriXY(modelloGRB.getVars(), istanza.getNumeroGiorniSettimana(), istanza.getNumeroDipendenti(), istanza.getFrazionamentoGiornata().getNumeroIntervalli());

        OutputDati.presentaRisultati(modelloConVincoli, istanza, modelloGRB);
        modelloGRB.write(istanza.getNome() + ".lp");
    }

    public static GRBModel aggiungiVincoloQuantitaminimapersonale(GRBModel modelloGRB, IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        boolean sceltaInserimentoQuantita;
        ArrayList<QuantitaMinima> listaQuantitaMinime = new ArrayList<>();
        int numeroCategorie = istanza.getNumeroCategorieDipendenti();
        int numeroGiorni = istanza.getNumeroGiorniSettimana();
        do {
            listaQuantitaMinime.add(GeneratoreDatiIstanze.inserisciNuovaQuantitaMinima(numeroCategorie, numeroGiorni));
            sceltaInserimentoQuantita = InputDati.yesOrNo("Vuoi aggiungere un'altra quantità minima per il personale? ");
        } while (sceltaInserimentoQuantita);

        for (QuantitaMinima quantitaMinimaCorrente : listaQuantitaMinime) {
            int intervalloInizio = quantitaMinimaCorrente.getOraInizio().getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + quantitaMinimaCorrente.getOraInizio().getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());
            int intervalloFine = quantitaMinimaCorrente.getOraFine().getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + quantitaMinimaCorrente.getOraFine().getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());

            for (int t = intervalloInizio; t < intervalloFine; t++){
                for (int k = 0; k < quantitaMinimaCorrente.getGiorni().length; k++) {
                    GRBLinExpr sommatoriaPersonaleMinimo = new GRBLinExpr();
                    int giornoDaVincolare = quantitaMinimaCorrente.getGiorni()[k];
                    for (int d = 0; d < istanza.getNumeroDipendenti(); d++) {
                        if (istanza.getArray_categoriaDipendente()[d] == (quantitaMinimaCorrente.getCategoria())) {
                            sommatoriaPersonaleMinimo.addTerm(1, modelloDaIstanza.getVettoreX()[giornoDaVincolare][d][t]);
                        }
                    }
                    GRBConstr vincoloPersonaleMinimo = modelloGRB.addConstr(sommatoriaPersonaleMinimo, GRB.GREATER_EQUAL, quantitaMinimaCorrente.getPersonaleMinimo(), "vincoloPersonaleMinimo_giorno" + giornoDaVincolare + "_cat" + quantitaMinimaCorrente.getCategoria() + "_fasciaDa" + intervalloInizio + "a" + intervalloFine);
                }

            }
        }
        return modelloGRB;
    }

    public static GRBModel aggiungiVincoloPermesso(GRBModel modelloGRB, IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        int dipendentePermesso = utility.InputDati.leggiIntero("Inserisci il numero del dipendente per cui si vuole aggiungere il permesso (i numeri vanno da 0 a " + (istanza.getNumeroDipendenti()-1) + "): ", 0, istanza.getNumeroDipendenti()-1);
        int giornoPermesso = utility.InputDati.leggiIntero("Inserisci il numero del giorno per cui si vuole aggiungere il permesso (i numeri vanno da 0 a " + (istanza.getNumeroGiorniSettimana()-1) + "): ", 0, istanza.getNumeroGiorniSettimana()-1);
        boolean isPermessoGiornaliero = utility.InputDati.yesOrNo("Il permesso è giornaliero (s) o solo per qualche ora (n)?");

        if (isPermessoGiornaliero) {
            modelloDaIstanza.setModelloGRB(aggiungiVincoloPermessoGiornaliero(istanza, modelloGRB, modelloDaIstanza, dipendentePermesso, giornoPermesso));
        } else {
            modelloDaIstanza.setModelloGRB(aggiungiVincoloPermessoOre(istanza, modelloGRB, modelloDaIstanza, dipendentePermesso, giornoPermesso));
        }
        return modelloDaIstanza.getModelloGRB();
    }

    private static GRBModel aggiungiVincoloPermessoGiornaliero(IstanzaProblema istanza, GRBModel modelloGRB, Modello modelloDaIstanza, int dipendentePermesso, int giornoPermesso) throws GRBException {
        GRBLinExpr lhsVincoloPermessoGiornaliero = new GRBLinExpr();
        lhsVincoloPermessoGiornaliero.addTerm(1, modelloDaIstanza.getVettoreY()[giornoPermesso][dipendentePermesso]);
        GRBConstr vincoloInizioAttivita = modelloGRB.addConstr(lhsVincoloPermessoGiornaliero, GRB.EQUAL, 0, "vincoloPermessoGiornaliero" + giornoPermesso + "_dip" + dipendentePermesso);
        return modelloGRB;
    }

    private static GRBModel aggiungiVincoloPermessoOre(IstanzaProblema istanza, GRBModel modelloGRB, Modello modelloDaIstanza, int dipendentePermesso, int giornoPermesso) throws GRBException {
        LocalTime[] orarioPermesso = new LocalTime[2];
        OutputDati.stampaMessaggio("Inserisci la fascia oraria in cui concedere il permesso (usare il formato HH:mm e un orario consono al frazionamento di giornata scelto):");

        do {
            try {
                String string_oraInizio = InputDati.leggiStringaNonVuota("ora di inizio permesso: ");
                String string_oraFine = InputDati.leggiStringaNonVuota("ora di fine permesso dell'azienda: ");
                orarioPermesso[0] = LocalTime.parse(string_oraInizio);
                orarioPermesso[1] = LocalTime.parse(string_oraFine);
            } catch (Exception e) {
                OutputDati.stampaErrore("Il formato inserito non è accettato");
            }
        } while (orarioPermesso[0] == null || orarioPermesso[1] == null);

        int intervalloInizioPermesso = orarioPermesso[0].getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + orarioPermesso[0].getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());
        int intervalloFinePermesso = orarioPermesso[1].getHour() * istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo() + orarioPermesso[1].getMinute() / (60 / istanza.getFrazionamentoGiornata().getFattoreMoltiplicativo());

        GRBLinExpr sommatoriaPermessoOre = new GRBLinExpr();
        for (int t = intervalloInizioPermesso; t < intervalloFinePermesso; t++) {
            sommatoriaPermessoOre.addTerm(1, modelloDaIstanza.getVettoreX()[giornoPermesso][dipendentePermesso][t]);
        }
        GRBConstr vincoloPermessoOre = modelloGRB.addConstr(sommatoriaPermessoOre, GRB.EQUAL, 0, "vincoloPermessoOre" + giornoPermesso + "dip_" + dipendentePermesso);

        return modelloGRB;
    }

    public static GRBModel aggiungiVincoloDipendentiIncompatibili(GRBModel modelloGRB, IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        int numeroDipendenti = istanza.getNumeroDipendenti();
        int numeroGiorni = istanza.getNumeroGiorniSettimana();
        int numeroIntervalli = istanza.getFrazionamentoGiornata().getNumeroIntervalli();

        int dipendente1 = InputDati.leggiIntero("Inserisci il numero del primo dipendente: (i numeri vanno da 0 a " + (istanza.getNumeroDipendenti()-1) + "): ", 0, numeroDipendenti - 1);
        int dipendente2 = InputDati.leggiIntero("Inserisci il numero del secondo dipendente: (i numeri vanno da 0 a " + (istanza.getNumeroDipendenti()-1) + "): ", 0, numeroDipendenti - 1);

        for (int g = 0; g < numeroGiorni; g++) {
            for (int t = 0; t < numeroIntervalli; t++) {
                GRBLinExpr sommatoriaIncompatibili = new GRBLinExpr();
                sommatoriaIncompatibili.addTerm(1, modelloDaIstanza.getVettoreX()[g][dipendente1][t]);
                sommatoriaIncompatibili.addTerm(1, modelloDaIstanza.getVettoreX()[g][dipendente2][t]);
                GRBConstr vincoliDipendentiIncompatibili = modelloGRB.addConstr(sommatoriaIncompatibili, GRB.LESS_EQUAL, 1, "vincoliDipendentiIncompatibili_giorno" + g + "_dip1" + dipendente1 + "_dip2" + dipendente2 + "_int" + t);

            }
        }

        return modelloGRB;
    }
}
