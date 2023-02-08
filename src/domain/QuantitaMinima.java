package domain;

import java.time.LocalTime;

public class QuantitaMinima {
    LocalTime oraInizio;
    LocalTime oraFine;
    int categoria;
    int personaleMinimo;
    int[] giorni;

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public int getCategoria() {
        return categoria;
    }

    public int getPersonaleMinimo() {
        return personaleMinimo;
    }

    public int[] getGiorni() {
        return giorni;
    }

    public QuantitaMinima(LocalTime oraInizio, LocalTime oraFine, int categoria, int personaleMinimo, int[] giorni) {
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.categoria = categoria;
        this.personaleMinimo = personaleMinimo;
        this.giorni = giorni;
    }
}
