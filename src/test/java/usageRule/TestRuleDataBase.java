package usageRule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.RuleDataBase;
import util.RuleResource;

public class TestRuleDataBase {
    protected static final Logger log = LoggerFactory.getLogger(TestRuleDataBase.class);


    public static RuleResource rr = new RuleResource("usageRule/TestRuleDataBase");

    private static String[] targetTables = new String[]{"ppap"};
    public static RuleDataBase rd = new RuleDataBase(targetTables);

    @ClassRule
    public static RuleChain rc = RuleChain.outerRule(rr).around(rd);

    @Test
    public void test() throws Exception {
        InsertData id = new InsertData();
        id.ppap();

        rd.assertDatas("result");
    }

    private class InsertData {
        public void ppap() throws ClassNotFoundException {
            Class.forName("org.sqlite.JDBC");
            Connection connection = null;
            Statement statement = null;
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:sqlite/locals.sqlite3");
                statement = connection.createStatement();
                statement.setQueryTimeout(30);

                String sql = "INSERT INTO ppap VALUES ('pen pineapple apple pen.')";
                statement.execute(sql);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
