package be.vdab.util;

import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class SetVanBrouwerId {

    public static TreeSet<Integer> addIds() {
        TreeSet<Integer> setIds = new TreeSet<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Geef IDs van Brouwers; 0 - te stoppen:");

        int id;

        while((id = scanner.nextInt()) != 0) {
            if (!setIds.add(id)) {
                throw new IllegalArgumentException("Dit nummer werd al getypt!");
            }

            if (id < 0) {
                throw new IllegalArgumentException("Het nummer moet groter dan 0!");
            }
        }

        return setIds;
    }
}
