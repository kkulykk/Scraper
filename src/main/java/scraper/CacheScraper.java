package scrapper;

import lombok.SneakyThrows;
import scraper.DefaultScraper;
import scraper.Home;
import scraper.Scraper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CacheScraper implements Scraper {
    private Scraper scraper = new DefaultScraper();

    @Override @SneakyThrows
    public Home scrape(String url) {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:db.sqlite");
        Statement statement = connection.createStatement();

        String query = String.format("select count(*) as count from homes where url='%s'", url);
        ResultSet rs = statement.executeQuery(query);

        if (rs.getInt("count") > 0) {
            query = String.format("select * from homes where url='%s'", url);
            rs = statement.executeQuery(query);
            return new Home(rs.getInt("price"), rs.getDouble("beds"),
                    rs.getDouble("baths"), rs.getDouble("garages") );
        } else {
            Home home = scraper.scrape(url);
            String insert = String.format("insert into homes(url, price, beds, baths, garage) :('%s', '%d', '%f', '%f', '%f')",
                    url, home.getPrice(), home.getBeds(), home.getBath(), home.getGarage());
            statement.executeUpdate(insert);
            return  home;
        }
    }
}