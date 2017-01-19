/**
 *
 */
package util;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 設定ファイル読み込み<br>
 * <br>
 * application.properties like this..
 *
 * <pre>
 * environment.name=local
 * dbUnit.enable=true
 * capture.enable=true
 * </pre>
 *
 * @author r.kinoshita
 *
 */
public class RuleResource extends ExternalResource {
    protected static final Logger log = LoggerFactory.getLogger(RuleResource.class);

    /** データリソースの在り処の基本パス **/
    protected static final String BASE_PATH = "./src/test/resources/data/";

    /** エビデンス保存場所の基本パス **/
    protected static final String EVIDENCE_PATH = "./evidence/";

    /** 各テストのデータリソースディレクトリのパス **/
    private static String dataResourcePath = "";

    /** 各テストのエビデンス保存ディレクトリパス **/
    private static String capturePath = "";

    /** 設定ファイル内容格納オブジェクト **/
    private static Properties configuration = new Properties();

    /** テスト対象環境名 **/
    private static String environmentName = "local";

    /** dbUnit有効無効 **/
    private static boolean dbUnitTest = false;

    /** キャプチャ有効無効 **/
    private static boolean capture = false;

    @SuppressWarnings("unused")
    private RuleResource() {}

    /**
     * コンストラクタ
     *
     * @param testName テスト名
     */
    public RuleResource(String testName) {
        dataResourcePath = BASE_PATH + testName + "/";
        capturePath = EVIDENCE_PATH + testName + "/";
    }

    /**
     * テストの開始前の処理
     *
     * <ul>
     * <li>プロパティファイルの読み込みとオブジェクトに保持
     * <li>テスト対象環境名など、基礎情報の取得
     * </ul>
     *
     */
    @Override
    protected void before() {
        try {
            configuration.load(RuleResource.class.getResourceAsStream("../application.properties"));
            environmentName = configuration.getProperty("environment.name");
            dbUnitTest = BooleanUtils.toBoolean(configuration.getProperty("dbUnit.enable"));
            capture = BooleanUtils.toBoolean(configuration.getProperty("capture.enable"));

            log.debug("application.properties load complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 各テストのデータリソースディレクトリのパス
     */
    public static String getDataResourcePath() {
        return dataResourcePath;
    }

    /**
     * @return 各テストのエビデンス保存ディレクトリパス
     */
    public static String getCapturePath() {
        return capturePath;
    }

    /**
     * @return 設定
     */
    public static Properties getConfiguration() {
        return configuration;
    }

    /**
     * @return テスト対象環境名
     */
    public static String getEnvironmentName() {
        return environmentName;
    }

    /**
     * @return dbUnitの有効無効
     */
    public static boolean isDbUnitTest() {
        return dbUnitTest;
    }

    /**
     * @return キャプチャの有効無効
     */
    public static boolean isCapture() {
        return capture;
    }
}
