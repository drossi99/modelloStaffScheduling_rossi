package domain;

import view.GestioneMenu;

public class Main {
    public static void main(String[] args) throws Exception {
        Sessione sessioneCorrente = new Sessione();
        GestioneMenu menuIniziale = new GestioneMenu();
        sessioneCorrente = menuIniziale.aggiuntaOrOttimizzazione();
    }
}
