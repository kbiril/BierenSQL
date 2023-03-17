package be.vdab;

import be.vdab.repositories.BrouwerRepository;

import java.sql.SQLException;
import java.util.TreeSet;


import static be.vdab.util.SetVanBrouwerId.addIds;

public class Main {
    public static void main(String[] args) {

        TreeSet<Integer> setIds = addIds();

        if ( !setIds.isEmpty()) {

            BrouwerRepository repository = new BrouwerRepository();
            try {
                TreeSet<Integer> result = repository.makeOmzetEmty(setIds);
                if( !result.isEmpty() ) {
                    System.out.println("De volgende IDs zijn niet geldig:");
                    result.forEach(System.out::println);
                } else {
                    System.out.println("Alle ingegeven IDs waren geldig, de omzet is nu null");
                }
            } catch (IllegalArgumentException ex) {
                System.err.println(ex.getMessage());
            } catch (SQLException ex) {
                ex.printStackTrace(System.err);
            }
        } else {
            System.out.println("Geen IDs werden ingegeven!");
        }
    }
}