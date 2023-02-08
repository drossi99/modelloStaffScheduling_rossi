package view;

import domain.*;
import gurobi.GRBException;
import gurobi.GRBModel;
import utility.MyMenu;

public class GestioneMenu {

    private static final String TITOLO_SCELTA_ISTANZA = "Seleziona come vuoi aggiungere una nuova istanza";
    private static final String[] VOCI_SCELTA_ISTANZA = {"Inserimento manuale", "Generazione casuale", "Lettura da file"};
    private static final String TITOLO_AGGIUNTA_OR_SIMULAZIONE = "Seleziona l'azione desiderata";
    private static final String[] VOCI_AGGIUNTA_OR_SIMULAZIONE = {"Aggiunta di un'istanza", "Ottimizzazione di un'istanza già esistente"};
    private static final String TITOLO_AGGIUNGI_VINCOLO_PARTICOLARE = "Seleziona che tipo di vincolo particolare vuoi aggiungere";
    private static final String[] VOCI_AGGIUNGI_VINCOLO_PARTICOLARE = {"Quantità minima di personale", "Permesso", "Incompatibilità tra dipendenti"};


    public Sessione aggiuntaOrOttimizzazione() throws Exception {
        Sessione sessioneCorrente = new Sessione();
        int scelta;

        MyMenu menuAggiuntaOrOttimizzazione = new MyMenu(TITOLO_AGGIUNTA_OR_SIMULAZIONE, VOCI_AGGIUNTA_OR_SIMULAZIONE);
        do {
            scelta = menuAggiuntaOrOttimizzazione.scegli();
            sceltaAggiuntaOrSimulazione(scelta, sessioneCorrente);
        } while (scelta != 0);
        return sessioneCorrente;
    }

    private void sceltaAggiuntaOrSimulazione(int scelta, Sessione sessioneCorrente) throws Exception {
        switch (scelta) {
            case 1:
                popolazioneListaIstanze(sessioneCorrente);
                break;
            case 2:
                if (sessioneCorrente.getListaIstanze().isEmpty()) {
                    OutputDati.stampaErrore("Non e' presente ancora alcuna istanza: aggiungine una per iniziare");
                } else
                    sessioneCorrente.selezionaIstanzaPerModello();

                break;
            default:
                return;
        }
    }

    public Sessione popolazioneListaIstanze(Sessione sessioneCorrente) {
        int scelta;
        MyMenu menuCreazioneIstanza = new MyMenu(TITOLO_SCELTA_ISTANZA, VOCI_SCELTA_ISTANZA);
        do {
            scelta = menuCreazioneIstanza.scegli();
            sceltaIstanza(scelta, sessioneCorrente);
        } while (scelta != 0);
        return sessioneCorrente;
    }

    private void sceltaIstanza(int scelta, Sessione sessione) {
        try {
            IstanzaProblema nuovaIstanza = null;
            switch (scelta) {
                case 1:
                    nuovaIstanza = GeneratoreIstanze.generazioneIstanzaGuidata();
                    break;
                case 2:
                    nuovaIstanza = GeneratoreIstanze.generazioneIstanzaCasuale(sessione.getListaIstanze().size());
                    break;
                case 3:
                    try {
                        nuovaIstanza = GeneratoreIstanze.generazioneIstanzaDaFile();
                    } catch (Exception e) {
                        OutputDati.stampaErrore("C'è stato un errore nella lettura del file");
                    }
                    break;
                default:
                    return;
            }
            nuovaIstanza.controllaNomeDoppio(sessione.getListaIstanze());
            nuovaIstanza.toFile(nuovaIstanza.getNome() + ".txt");
            sessione.getListaIstanze().add(nuovaIstanza);
            OutputDati.stampaMessaggio("È stata salvata la seguente istanza:\n" + nuovaIstanza.toText());
        } catch (Exception e) {
            OutputDati.stampaErrore("C'è stato un errore nella generazione dell'istanza");
        }
    }

    public GRBModel aggiungiVincoloParticolare(GRBModel modelloGrb, IstanzaProblema istanzaProblema, Modello modelloDaIstanza) throws GRBException {
        int scelta;

        MyMenu menuAggiuntaOrOttimizzazione = new MyMenu(TITOLO_AGGIUNGI_VINCOLO_PARTICOLARE, VOCI_AGGIUNGI_VINCOLO_PARTICOLARE);
        do {
            scelta = menuAggiuntaOrOttimizzazione.scegli();
            modelloGrb = sceltaVincoloParticolare(scelta, modelloGrb, istanzaProblema, modelloDaIstanza);
        } while (scelta != 0);
        return modelloGrb;
    }

    private GRBModel sceltaVincoloParticolare(int scelta, GRBModel modelloGRB, IstanzaProblema istanza, Modello modelloDaIstanza) throws GRBException {
        switch (scelta) {
            case 1:
                modelloGRB = Risolutore.aggiungiVincoloQuantitaminimapersonale(modelloGRB, istanza, modelloDaIstanza);
                break;
            case 2:
                modelloGRB = Risolutore.aggiungiVincoloPermesso(modelloGRB, istanza, modelloDaIstanza);
                break;
            case 3:
                modelloGRB = Risolutore.aggiungiVincoloDipendentiIncompatibili(modelloGRB, istanza, modelloDaIstanza);
            default:

        }
        return modelloGRB;
    }
}
