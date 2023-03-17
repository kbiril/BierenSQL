package be.vdab.dto;

public record BrouwerNaamQtyBier(String brouwerNaam, int qtyBier) {
    @Override
    public String toString() {
        return "Brouwer Naam: " + brouwerNaam
                + " - Aantal Bieren: " + qtyBier;
    }
}
