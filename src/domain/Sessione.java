package domain;

import gurobi.GRBModel;
import view.GestioneMenu;
import utility.InputDati;
import utility.MyMenu;
import view.OutputDati;

import java.util.ArrayList;

public class Sessione {
    public static final String TITOLO_SCELTA_ISTANZA = "Inserisci il numero dell'istanza da cui creare il modello:";
    private ArrayList<IstanzaProblema> listaIstanze = new ArrayList<>();

    public ArrayList<IstanzaProblema> getListaIstanze() {
        return listaIstanze;
    }

    public void selezionaIstanzaPerModello() throws Exception {
        int scelta;
        String[] listaNomiIstanze = this.getListaNomiIstanze();
        MyMenu selezionaIstanza = new MyMenu(TITOLO_SCELTA_ISTANZA, listaNomiIstanze);
        do {
            scelta = selezionaIstanza.scegli();
            sceltaRisolutore(scelta);
        } while (scelta != 0);
    }

    private void sceltaRisolutore(int scelta) throws Exception {
        switch (scelta) {
        case 0:
            return;
        default:
            IstanzaProblema istanzaSelezionata = this.getListaIstanze().get(scelta-1);
            Risolutore risolutore = new Risolutore();
            Modello modelloConVincoli = risolutore.aggiungiVincoli(istanzaSelezionata);
            GRBModel modelloGRB = modelloConVincoli.getModelloGRB();
            risolutore.ottimizzaModello(modelloConVincoli, istanzaSelezionata);

            boolean continua;
            do {
                GestioneMenu menuAggiuntaVincoloParticolare = new GestioneMenu();
                modelloConVincoli.setModelloGRB(menuAggiuntaVincoloParticolare.aggiungiVincoloParticolare(modelloGRB, istanzaSelezionata, modelloConVincoli));
                risolutore.ottimizzaModello(modelloConVincoli, istanzaSelezionata);

                continua = InputDati.yesOrNo("Vuoi aggiungere altri vincoli?");
            } while (continua);
        }
    }

    private String[] getListaNomiIstanze() {
        String[] listaNomi = new String[this.listaIstanze.size()];
        for (int i = 0; i < this.getListaIstanze().size(); i++) {
            listaNomi[i] = this.getListaIstanze().get(i).getNome();
        }
        return listaNomi;
    }
}
