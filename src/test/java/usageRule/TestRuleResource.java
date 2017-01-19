package usageRule;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.RuleResource;

public class TestRuleResource {
    protected static final Logger log = LoggerFactory.getLogger(TestRuleResource.class);


    public static RuleResource rr = new RuleResource("usageRule/TestRuleResource");

    @ClassRule
    public static RuleChain rc = RuleChain.outerRule(rr);

    @SuppressWarnings("static-access")
    @Test
    public void test() {
        log.debug("EnvironmentName  :" + rr.getEnvironmentName());
        log.debug("DataResourcePath :" + rr.getDataResourcePath());
        log.debug("CapturePath      :" + rr.getCapturePath());
        log.debug("Configuration    :" + rr.getConfiguration().toString());
    }
}
