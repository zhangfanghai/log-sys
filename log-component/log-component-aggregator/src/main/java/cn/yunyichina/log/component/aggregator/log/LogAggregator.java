package cn.yunyichina.log.component.aggregator.log;

import cn.yunyichina.log.common.exception.AggregatorException;
import cn.yunyichina.log.component.index.entity.ContextIndex;
import cn.yunyichina.log.component.index.entity.ContextInfo;
import cn.yunyichina.log.component.scanner.imp.LogScanner;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Leo
 * @Blog: http://blog.csdn.net/lc0817
 * @CreateTime: 2016/11/17 10:56
 * @Description:
 */
public class LogAggregator {

    private int beginIndex;
    private int endIndex;
    private List<File> logList;

    private final int DEFAULT_LOG_SIZE = 1024 * 1024 * 10;

    public static String aggregate(ContextInfo contextInfo, String logDir) {
        try {
            LogAggregator aggregator = new LogAggregator(contextInfo, logDir);
            return aggregator.aggregate();
        } catch (Exception e) {
            return "";
        }
    }

    protected LogAggregator(ContextInfo contextInfo, String logDir) throws AggregatorException {
        ContextIndex begin = contextInfo.getBegin();
        ContextIndex end = contextInfo.getEnd();
        if (begin == null || end == null) {
            throw new AggregatorException("日志聚合器无法聚合残缺的上下文.");
        }
        this.beginIndex = begin.getIndexOfLogFile();
        this.endIndex = end.getIndexOfLogFile();
        Map<String, File> logMap = LogScanner.scan(begin.getLogFile(), end.getLogFile(), logDir);
        logList = new ArrayList<>(logMap.values());
    }


    protected String aggregate() throws IOException {
        if (logList.size() == 1) {
            File log = logList.get(0);
            String logContent = Files.asCharSource(log, Charsets.UTF_8).read();
            return logContent.substring(beginIndex, endIndex);
        } else if (logList.size() > 1) {
            StringBuilder logBuilder = new StringBuilder(DEFAULT_LOG_SIZE);
            int logListSize = logList.size();

            String firstLogConetnt = Files.asCharSource(logList.get(0), Charsets.UTF_8).read();
            logBuilder.append(firstLogConetnt.substring(beginIndex));

            for (int i = 1; i < logListSize - 2; i++) {
                logBuilder.append(Files.asCharSource(logList.get(i), Charsets.UTF_8).read());
            }

            String lastLogConetnt = Files.asCharSource(logList.get(logListSize - 1), Charsets.UTF_8).read();
            logBuilder.append(lastLogConetnt.substring(0, endIndex));

            return logBuilder.toString();
        } else {
            return "";
        }
    }

}
