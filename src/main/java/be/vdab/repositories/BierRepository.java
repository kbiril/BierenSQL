package be.vdab.repositories;

import be.vdab.domain.Bier;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BierRepository extends AbstractRepository{
    public int deleteBierNoAlcohol() throws SQLException {
        String sql = """
                delete bieren
                from bieren
                where alcohol is null
                """;
        try (Connection connection = super.getConnection();
        var statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    public List<Bier> findBierByMonth(int month) throws SQLException {
        ArrayList<Bier> biers = new ArrayList<>();
        String sql = """
                     select id, naam, brouwerId, soortId, alcohol, sinds 
                     from bieren 
                     where {fn month(sinds)} = ? 
                     order by naam;
                     """;
        try (Connection connection = super.getConnection();
             var statement = connection.prepareStatement(sql))
        {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setInt(1, month);

            for (ResultSet result = statement.executeQuery(); result.next(); ) {
                biers.add(toBier(result));
            }
            connection.commit();
            return biers;
        }
    }

    public List<String> findBierPerSoort(String soort) throws SQLException {
        ArrayList<String> list = new ArrayList<>();

        String sql = """
                      select b.naam 
                      from bieren b 
                      inner join soorten s 
                      on s.id = b.soortId 
                      where s.naam = ?
                     """;

        try (Connection connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setString(1, soort);

            for (ResultSet result = statement.executeQuery(); result.next(); ) {
                list.add(result.getString("naam"));
            }
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Soort niet gevonden!");
            }
            connection.commit();
            return list;
        }
    }

    public Bier toBier(ResultSet result) throws SQLException {
        return new Bier(result.getInt("id"), result.getString("naam"),
                        result.getInt("brouwerId"), result.getInt("soortId"),
                        result.getFloat("alcohol"), result.getObject("sinds", LocalDate.class));
    }
}
