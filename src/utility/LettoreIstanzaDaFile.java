package utility;

import domain.Frazionamento;
import domain.IstanzaProblema;

import java.time.LocalTime;
import java.util.ArrayList;

public class LettoreIstanzaDaFile {
    public static IstanzaProblema leggiIstanzaDaLista(ArrayList<String> listaAttributi) {
        String nomeIstanza = listaAttributi.get(0);
        int numeroDipendenti = Integer.parseInt(listaAttributi.get(1));
        Frazionamento frazionamento = sceltaFrazionamento(Integer.parseInt(listaAttributi.get(2)));
        int numeroCategorieDipendenti = Integer.parseInt(listaAttributi.get(3));
        double[] array_pagheOrarieNormalizzato = estraiElementiDouble(listaAttributi.get(4));
        int[] array_isFullTime = estraiElementiInt(listaAttributi.get(5));
        int[] array_categoriaDipendente = estraiElementiInt(listaAttributi.get(6));
        int[] array_parametriTipici = estraiElementiInt(listaAttributi.get(7));
        LocalTime[] array_attivita = estraiElementiLocalTime(listaAttributi.get(8));
        int[] array_oreUomoMinime = estraiElementiInt(listaAttributi.get(9));
        return new IstanzaProblema(nomeIstanza, numeroDipendenti, frazionamento, numeroCategorieDipendenti, array_pagheOrarieNormalizzato, array_isFullTime, array_categoriaDipendente, array_parametriTipici, array_attivita, array_oreUomoMinime);
    }

    public static int[] estraiElementiInt(String csvStringIsFullTime) {
        String[] vettoreStringIsFullTime = csvStringIsFullTime.split(",");
        int[] vettoreIntIsFullTime = new int[vettoreStringIsFullTime.length];
        for (int i = 0; i < vettoreStringIsFullTime.length; i++) {
            vettoreIntIsFullTime[i] = Integer.parseInt(vettoreStringIsFullTime[i]);
        }
        return vettoreIntIsFullTime;
    }

    static double[] estraiElementiDouble(String csvStringPaghe) {
        String[] vettoreStringPaghe = csvStringPaghe.split(",");
        double[] vettoreDoublePaghe = new double[vettoreStringPaghe.length];
        for (int i = 0; i < vettoreStringPaghe.length; i++) {
            vettoreDoublePaghe[i] = Double.parseDouble(vettoreStringPaghe[i]);
        }
        return vettoreDoublePaghe;
    }

    static LocalTime[] estraiElementiLocalTime(String csvOrari) {
        int numeroOrari = 2;
        LocalTime[] vettoreLocaltimeOrari = new LocalTime[numeroOrari];
        if (csvOrari.equals("--")) {
            vettoreLocaltimeOrari[0] = null;
            vettoreLocaltimeOrari[1] = null;
        } else {

            String[] vettoreStringOrari = csvOrari.split(",");
            for (int i = 0; i < vettoreStringOrari.length; i++) {
                vettoreLocaltimeOrari[i] = LocalTime.parse(vettoreStringOrari[i]);
            }
        }

        return vettoreLocaltimeOrari;
    }

    static Frazionamento sceltaFrazionamento(int numeroIntervalli) {
        Frazionamento f = new Frazionamento();
        switch (numeroIntervalli) {
            case 96:
                f.setNumeroIntervalli(96);
                f.setFattoreMoltiplicativo(4);
                break;
            case 48:
                f.setNumeroIntervalli(48);
                f.setFattoreMoltiplicativo(2);
                break;
            default:
                f.setNumeroIntervalli(24);
                f.setFattoreMoltiplicativo(1);
        }
        return f;
    }
}
