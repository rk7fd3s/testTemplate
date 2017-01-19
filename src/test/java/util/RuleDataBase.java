/**
 *
 */
package util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * dbUnit関連のRuleクラス<br>
 * <br>
 * application.properties like this..
 * <pre>
 * dbUnit.enable=true
 *
 * datasource.driver-class-name=com.mysql.jdbc.Driver
 * datasource.url=jdbc:mysql://localhost:3306/hoge?useUnicode=true&amp;characterEncoding=UTF-8
 * datasource.username=hoge_user
 * datasource.password=hoge_pw
 * </pre>
 *
 * @author r.kinoshita
 *
 */
public class RuleDataBase extends ExternalResource {
    protected static final Logger log = LoggerFactory.getLogger(RuleDataBase.class);

    /** DBコネクション **/
    protected Connection conn;

    /** バックアップすべきテーブル名の配列 **/
    private String[] targetTables;

    /** INSERT時や照合時に除外するテーブル-カラム情報 **/
    protected Map<String, String[]> excludedColumns = new HashMap<String, String[]>() {
        {
            /**
             * テーブル毎に除外するカラムを羅列する
             * Ex. put("hoge_table", new String[] { "hoge_column" });
            **/
        }
    };

    /** バックアップファイル **/
    private File backupFile;

    @SuppressWarnings("unused")
    private RuleDataBase() {
    }

    /**
     * コンストラクタ
     *
     * @param targetTables バックアップすべきテーブル名の配列 ※テストで書き換えるテーブルの名前
     */
    public RuleDataBase(String[] targetTables) {
        this.targetTables = targetTables;
    }

    /**
     * コンストラクタ
     *
     * @param targetTables バックアップすべきテーブル名の配列 ※テストで書き換えるテーブルの名前
     * @param excludedColumns 除外するテーブル-カラム情報 デフォルトの設定に追加される
     */
    public RuleDataBase(String[] targetTables, Map<String, String[]> excludedColumns) {
        this.targetTables = targetTables;

        for (Entry<String, String[]> entry : excludedColumns.entrySet()) {
            this.excludedColumns.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * テストの開始前の処理
     *
     * <ul>
     * <li>DBへの接続
     * <li>指定したテーブルのバックアップ
     * <li>pre_dataがあれば、その内容でデータの書き換え
     * </ul>
     */
    @Override
    protected void before() {
        if (RuleResource.isDbUnitTest()) {
            // DB接続
            try {
                log.debug("Try to connect db server.");
                conn = getConnection();
                log.debug("Connect db success.");
            } catch (Exception e) {
                log.debug("Connect db fail.");
                e.getStackTrace();
                fail();
            }

            // テスト対象テーブルのバックアップ
            try {
                backUpTables();
            } catch (Exception e) {
                log.debug("table backup fail.");
                e.printStackTrace();
                fail();
            }

            // テストクラス用データリソース投入
            // Ex. ./src/test/resources/data/[テストクラス名]/pre_data.xml
            try {
                cleanInsertData("pre_data");
            } catch (Exception e) {
                log.debug("datafile insert fail.");
                e.printStackTrace();
                fail();
            }
        }
    }

    /**
     * テスト終了時の処理
     *
     * <ul>
     * <li>バックアップされたテーブルデータの復元
     * <li>DB接続の切断
     * </ul>
     */
    @Override
    protected void after() {
        if (RuleResource.isDbUnitTest()) {
            // テーブルリストア
            try {
                restoreTables();
            } catch (Exception e) {
                log.debug("Table restore fail.");
                e.printStackTrace();
            }

            // DB切断
            if (conn != null) {
                try {
                    conn.close();
                    log.debug("Disconnect db success.");
                } catch (SQLException e) {
                    log.debug("Disconnect db fail.");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * DBへの接続
     *
     * @return
     * @throws Exception
     */
    private Connection getConnection() throws Exception {
        Properties configuration = RuleResource.getConfiguration();

        Class.forName(configuration.getProperty("datasource.driver-class-name"));
        Connection connection = DriverManager.getConnection(
                configuration.getProperty("datasource.url"),
                configuration.getProperty("datasource.username"),
                configuration.getProperty("datasource.password"));

        return connection;
    }

    /**
     * テスト対象となるテーブル内容をテスト前にファイルに保存します
     *
     * @throws Exception
     */
    private void backUpTables() throws Exception {
        if (conn != null && targetTables != null && targetTables.length != 0) {
            try {
                QueryDataSet partialDataSet = new QueryDataSet(new DatabaseConnection(conn));
                for (String tableName : targetTables) {
                    partialDataSet.addTable(tableName);
                }
                backupFile = new File(RuleResource.getDataResourcePath(),
                        RuleResource.getEnvironmentName() + "_backup.xml");

                // ファイル格納フォルダがなければ作成する
                if (!backupFile.getParentFile().exists()) {
                    backupFile.getParentFile().mkdirs();
                }

                FlatXmlDataSet.write(partialDataSet, new FileOutputStream(backupFile));

                log.debug("table backup success.");
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * バックアップファイルを用いてテーブル内容をテスト前の状態に戻します
     *
     * @throws Exception
     */
    private void restoreTables() throws Exception {
        if (conn != null && targetTables != null && targetTables.length != 0 && backupFile != null) {
            try {
                FlatXmlProducer xml = new FlatXmlProducer(new InputSource(new FileInputStream(backupFile)));
                IDataSet dataSet = new FlatXmlDataSet(xml);
                DatabaseOperation.CLEAN_INSERT.execute((new DatabaseConnection(conn)), dataSet);

                log.debug("Table restore success.");
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * リソースファイルとして対応する拡張子の配列<br>
     * 最後の""はCSVファイル用
     */
    protected static final String[] SUFFIXS = new String[] { ".xml", "" };

    /**
     * データリソース名＋SUFFIXS　で存在するファイルがあれば、その拡張子を返却します
     *
     * @param resourceName データリソース名
     * @return
     */
    private String getResourceSuffix(String resourceName) {
        File dataFile = null;
        for (String suffix : SUFFIXS) {
            dataFile = new File(RuleResource.getDataResourcePath(), resourceName + suffix);
            if (dataFile.exists()) {
                return suffix;
            }
        }

        return null;
    }

    /**
     * データリソースからIDataSetオブジェクトを生成します
     *
     * @param resourceName データリソース名
     * @param excludedColumns 除外するテーブル-カラム情報
     * @return IDataSetオブジェクト
     * @throws FileNotFoundException
     * @throws DataSetException
     */
    private IDataSet createDataSetFromFile(String resourceName, Map<String, String[]> excludedColumns)
            throws FileNotFoundException, DataSetException {
        String suffix = getResourceSuffix(resourceName);
        if (suffix == null) {
            log.debug(RuleResource.getDataResourcePath() + resourceName + ".xxx is not found.");
            return null;
        }

        File dataFile = new File(RuleResource.getDataResourcePath(), resourceName + suffix);

        IDataSet dataSet = null;
        try {
            // 拡張子ごとに処理わけ
            switch (suffix) {
            case ".xml":
                FlatXmlProducer xml = new FlatXmlProducer(new InputSource(new FileInputStream(dataFile)));
                dataSet = new FlatXmlDataSet(xml);
                break;
            default: // CSV
                dataSet = new CsvDataSet(dataFile);
                break;

            }

            // 除外カラムフィルター
            if (excludedColumns == null) {
                excludedColumns = this.excludedColumns;
            }
            dataSet = filterDataSet(dataSet, excludedColumns);

            log.debug("get data from " + dataFile.getPath());
        } catch (FileNotFoundException | DataSetException e) {
            throw e;
        }

        return dataSet;
    }

    /**
     * DBへのデータ投入<br>
     *
     * データリソースの内容でDBにデータ投入を行います。<br>
     * その際、データリソースに記載されたテーブルの中身は一旦削除されます。<br>
     * <b>データリソースに記載のテーブルは、テスト開始前のバックアップ対象テーブルに必ず含めてください。</b>
     *
     * @param resourceName データリソース名
     * @throws Exception
     */
    public void cleanInsertData(String resourceName) throws Exception {
        cleanInsertData(resourceName, null);
    }

    /**
     * DBへのデータ投入<br>
     *
     * データリソースの内容でDBにデータ投入を行います。<br>
     * その際、データリソースに記載されたテーブルの中身は一旦削除されます。<br>
     * <b>データリソースに記載のテーブルは、テスト開始前のバックアップ対象テーブルに必ず含めてください。</b>
     *
     * @param resourceName データリソース名
     * @param excludedColumns 除外するテーブル-カラム情報
     * @throws Exception
     */
    public void cleanInsertData(String resourceName, Map<String, String[]> excludedColumns) throws Exception {
        if (conn != null) {
            IDataSet dataSet = createDataSetFromFile(resourceName, excludedColumns);

            // 実際にデータ投入するトコロ
            if (dataSet != null) {
                log.debug("Insert data from " + resourceName);
                try {
                    DatabaseOperation.CLEAN_INSERT.execute((new DatabaseConnection(conn)), dataSet);
                } catch (DatabaseUnitException | SQLException e) {
                    // ココで失敗した場合、DBを元の状態に戻す
                    after();
                    throw e;
                }

                log.debug("datafile insert success.");
            }
        }
    }

    /**
     * 現状のDBのデータセットを生成します
     *
     * @param excludedColumns 除外するテーブル-カラム情報
     * @return
     * @throws SQLException
     * @throws DatabaseUnitException
     */
    private IDataSet createCurrentDataSet(Map<String, String[]> excludedColumns)
            throws SQLException, DatabaseUnitException {
        IDatabaseConnection connection = new DatabaseConnection(conn);
        IDataSet dataSet = connection.createDataSet();

        // 除外カラムフィルター
        if (excludedColumns == null) {
            excludedColumns = this.excludedColumns;
        }
        dataSet = filterDataSet(dataSet, excludedColumns);

        return dataSet;
    }

    /**
     * データリソースの内容とDBの値を比較します
     *
     * @param resourceName データリソース名
     * @throws Exception
     */
    public void assertDatas(String resourceName) throws Exception {
        assertDatas(resourceName, null, null);
    }

    /**
     * データリソースの内容とDBの値を比較します
     *
     * @param resourceName データリソース名
     * @param excludedColumns 除外するテーブル-カラム情報
     * @throws Exception
     */
    public void assertDatas(String resourceName, Map<String, String[]> excludedColumns) throws Exception {
        assertDatas(resourceName, null, excludedColumns);
    }

    /**
     * データリソースの内容とDBの値を比較します
     *
     * @param resourceName データリソース名
     * @param targetTables 検証対象テーブル名配列。nullの場合はデータファイルにある全てのテーブルが対象
     * @throws Exception
     */
    public void assertDatas(String resourceName, String[] targetTables) throws Exception {
        assertDatas(resourceName, targetTables, null);
    }

    /**
     * データリソースの内容とDBの値を比較します
     *
     * @param resourceName データリソース名
     * @param targetTables 検証対象テーブル名配列。nullの場合はデータファイルにある全てのテーブルが対象
     * @param excludedColumns 除外するテーブル-カラム情報
     * @throws Exception
     */
    public void assertDatas(String resourceName, String[] targetTables, Map<String, String[]> excludedColumns)
            throws Exception {
        if (conn != null) {
            IDataSet expectedDataSet = createDataSetFromFile(resourceName, excludedColumns);

            if (targetTables == null) {
                targetTables = expectedDataSet.getTableNames();
            }

            for (String tableName : targetTables) {
                ITable expectedTable = expectedDataSet.getTable(tableName);
                ITable actualTable = createCurrentDataSet(excludedColumns).getTable(tableName);

                Assertion.assertEquals(expectedTable, actualTable);
            }
        }
    }

    /**
     * 除外カラムフィルター<br>
     *
     * @param src データセット
     * @param excludedColumns 除外するテーブル-カラム情報
     * @return
     * @throws DataSetException
     */
    private static IDataSet filterDataSet(IDataSet src, Map<String, String[]> excludedColumns)
            throws DataSetException {
        if (excludedColumns == null) {
            return src;
        }

        ArrayList<ITable> tables = new ArrayList<ITable>(
                src.getTableNames().length);

        for (String tableName : src.getTableNames()) {

            if (excludedColumns.containsKey(tableName)) {
                ITable filteredTable = DefaultColumnFilter
                        .excludedColumnsTable(
                                src.getTable(tableName),
                                excludedColumns.get(tableName));

                tables.add(filteredTable);
            } else {
                tables.add(src.getTable(tableName));
            }
        }

        return new DefaultDataSet(tables.toArray(new ITable[0]),
                src.isCaseSensitiveTableNames());
    }

}
