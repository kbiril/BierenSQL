package be.vdab.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Bier {
    private final int id;
    private final String naam;
    private final int brouwerId;
    private final int soortId;
    private final float alcohol;
    private final LocalDate sinds;

    private static final Locale BELGIE = new Locale("nl", "BE");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MMMM/yyyy", BELGIE);

    public Bier(int id, String naam, int brouwerId, int soortId, float alcohol, LocalDate sinds) {
        this.id = id;
        this.naam = naam;
        this.brouwerId = brouwerId;
        this.soortId = soortId;
        this.alcohol = alcohol;
        this.sinds = sinds;
    }

    @Override
    public String toString() {
        return "Bier naam: " + naam + ", Soort ID: " + soortId + ", Sinds: " + sinds.format(FORMATTER);
    }
}
