package be.vdab.repositories;

import be.vdab.domain.Brouwer;
import be.vdab.dto.BrouwerNaamQtyBier;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BrouwerRepository extends AbstractRepository{

    public BigDecimal getAvgOmzet() throws SQLException {
        String sql = """
                    select avg(omzet) as averageOmzet
                    from brouwers
                    """;
        try (Connection connection = super.getConnection();
            var statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getBigDecimal("averageOmzet");
        }
    }

    private Brouwer toBrouwer (ResultSet resultSet) throws SQLException {
        return new Brouwer(resultSet.getInt("id"), resultSet.getString("naam"),
                resultSet.getString("adres"), resultSet.getInt("postcode"),
                resultSet.getString("gemeente"), resultSet.getBigDecimal("omzet"));
    }

    public List<Brouwer> getBrouwersOmzetMoreAvg () throws SQLException {
        ArrayList<Brouwer> brouwers = new ArrayList<>();
        String sql = """
                    select id, naam, adres, postcode, gemeente, omzet
                    from brouwers 
                    where omzet > (select avg(omzet) from brouwers)
                    """;
        try (Connection connection = super.getConnection();
            var statement = connection.prepareStatement(sql))
        {
            for (var result = statement.executeQuery(); result.next(); ) {
                brouwers.add(toBrouwer(result));
            }
        }
        return brouwers;
    }

    public List<Brouwer> findBrouwersOmzetBetween(BigDecimal omzet1, BigDecimal omzet2) throws SQLException{
        ArrayList<Brouwer> brouwers = new ArrayList<>();
        String sql = """
                        select id, naam, adres, postcode, gemeente, omzet
                        from brouwers
                        where omzet between ? and ?
                        order by omzet, id
                     """;
        try (Connection connection = super.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            statement.setBigDecimal(1, omzet1);
            statement.setBigDecimal(2, omzet2);
            for (ResultSet result = statement.executeQuery(); result.next(); ) {
                brouwers.add(toBrouwer(result));
            }
            return brouwers;
        }
    }

    public List<Brouwer> findOmzetBetween (int minOmzet, int maxOmzet) throws SQLException {
        ArrayList<Brouwer> brouwers = new ArrayList<>();
        try (Connection connection = super.getConnection();
             var statement = connection.prepareCall("{call BrouwersOmzetTussen(?,?)}"))
        {
            statement.setInt(1, minOmzet);
            statement.setInt(2, maxOmzet);

            for (ResultSet result = statement.executeQuery(); result.next(); )
            {
                brouwers.add(toBrouwer(result));
            }
            return brouwers;
        }
    }

    public void bierenOvername() throws SQLException {
        String sqlDoor2de = """
                            update bieren
                            set brouwerId = 2
                            where alcohol >= 8.5 and brouwerId = 1
                            """;
        String sqlDoor3de = """
                            update bieren
                            set brouwerId = 3
                            where alcohol < 8.5 and brouwerId = 1
                            """;
        String sqlDelete1ste = """
                               delete
                               from brouwers
                               where id = 1
                               """;

        try (
                Connection connection = super.getConnection();
                var statementOvername2de = connection.prepareStatement(sqlDoor2de);
                var statementOvername3de = connection.prepareStatement(sqlDoor3de);
                var statementDelete1ste = connection.prepareStatement(sqlDelete1ste)
        )
        {
            connection.setAutoCommit(false);
            statementOvername2de.executeUpdate();
            statementOvername3de.executeUpdate();
            statementDelete1ste.executeUpdate();
            connection.commit();
        }
    }

    public Optional<Brouwer> findBrouwerById(long id) throws SQLException {
        String sql = """
                        select id, naam, adres, postcode, gemeente, omzet
                        from brouwers
                        where id = ?;
                     """;
        try (Connection connection = super.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();

            return result.next() ? Optional.of(toBrouwer(result)) : Optional.empty();
        }
    }

    public List<BrouwerNaamQtyBier> qtyBiersByBrouwer() throws SQLException {
        ArrayList<BrouwerNaamQtyBier> list = new ArrayList<>();
        String sql = """
                        select br.naam as brouwerNaam, count(b.id) as aantalBieren
                        from bieren b
                        inner join brouwers br
                        on b.brouwerId = br.id
                        group by br.naam
                        order by br.naam
                     """;

        try (Connection connection = super.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            for (ResultSet result = statement.executeQuery(); result.next(); ) {
                list.add(new BrouwerNaamQtyBier(result.getString("brouwerNaam"),
                                                result.getInt("aantalBieren")));
            }
            connection.commit();
        } return list;
    }

    public TreeSet<Integer> makeOmzetEmty (TreeSet<Integer> setIds) throws SQLException {
        TreeSet<Integer> set = setIds;
        String sql = """
                     update brouwers
                     set omzet = null
                     where id in (
                     """
                     + "?,".repeat(setIds.size() - 1)
                     + "?)";

        try (Connection connection = super.getConnection();
             var  statement = connection.prepareStatement(sql))
        {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            int index = 1;
            for (int id : setIds) {
                statement.setInt(index++, id);
            }

            int aantalAangepasteRecords = statement.executeUpdate();
            if (aantalAangepasteRecords == setIds.size()) {
                connection.commit();
            }

            String sqlNotExistBrouwers = """
                                           select id
                                           from brouwers
                                           where id in (
                                           """
                                           + "?,".repeat(setIds.size() - 1)
                                           + "?)";

            try (var statementSelect = connection.prepareStatement(sqlNotExistBrouwers))
            {
                int i = 1;
                for(int id : setIds) {
                    statementSelect.setInt(i++, id);
                }

                for (ResultSet result = statementSelect.executeQuery(); result.next(); ) {
                    set.remove(result.getInt("id"));
                }
                connection.commit();

            }
        }
        return set;
    }
}
