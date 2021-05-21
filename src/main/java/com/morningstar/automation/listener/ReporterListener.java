package com.morningstar.automation.listener;

import com.morningstar.automation.base.core.report.DefaultReportSolution;
import com.morningstar.automation.base.core.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.morningstar.automation.base.core.utils.Util;
import org.apache.commons.io.FileUtils;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.reporters.XMLReporter;
import org.testng.xml.XmlSuite;

public class ReporterListener extends XMLReporter {
    static final Logger logger = Logger.getLogger(com.morningstar.automation.base.core.listener.ReporterListener.class);
    Date startDate;
    Date endDate;

    public ReporterListener() {
    }

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        super.generateReport(xmlSuites, suites, outputDirectory);
        this.CopyMappingFileToOutput(outputDirectory);
        Iterator var4 = suites.iterator();

        while(var4.hasNext()) {
            ISuite suite = (ISuite)var4.next();
            this.rescheduleDateBySuite(suite);
            DefaultReportSolution solution = new DefaultReportSolution(suite, this.startDate, this.endDate);
            logger.info("generate report");
            solution.generateReport();
            logger.info("backup report");
//            solution.backupReport();
            logger.info("send report");
            solution.sendEmail(solution.getContent());
        }

    }

    void rescheduleDateBySuite(ISuite suite) {
        Date minStartDate = new Date();
        Date maxEndDate = null;
        Map<String, ISuiteResult> results = suite.getResults();
        Iterator var5 = results.entrySet().iterator();

        while(true) {
            Date startDate;
            Date endDate;
            do {
                if (!var5.hasNext()) {
                    if (maxEndDate == null) {
                        maxEndDate = minStartDate;
                    }

                    this.startDate = minStartDate;
                    this.endDate = maxEndDate;
                    return;
                }

                Entry<String, ISuiteResult> result = (Entry)var5.next();
                ITestContext testContext = ((ISuiteResult)result.getValue()).getTestContext();
                startDate = testContext.getStartDate();
                endDate = testContext.getEndDate();
                if (minStartDate.after(startDate)) {
                    minStartDate = startDate;
                }
            } while(maxEndDate != null && !maxEndDate.before(endDate));

            maxEndDate = endDate != null ? endDate : startDate;
        }
    }

    void CopyMappingFileToOutput(String outputDirectory) {
        String sourceFile = Util.getClassPath() + "testData";
        logger.debug("Copy Mapping File To Output from:" + sourceFile);
        String target = outputDirectory;
        File file = new File(sourceFile);
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList == null) {
                logger.debug("Copy Mapping File To Output: File list is null");
            }

            File[] var6 = fileList;
            int var7 = fileList.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                File f = var6[var8];
                String fileName = f.getName();
                if (fileName.endsWith(".csv")) {
                    try {
                        FileUtils.copyFileToDirectory(f, new File(target));
                    } catch (IOException var12) {
                        logger.error("Copy MappingFile To Output error:" + var12.getMessage());
                    }
                }
            }
        }

    }
}

