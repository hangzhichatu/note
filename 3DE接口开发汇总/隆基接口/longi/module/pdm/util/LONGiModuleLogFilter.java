package longi.module.pdm.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LONGiModuleLogFilter extends Filter<ILoggingEvent>{
	@Override
    public FilterReply decide(ILoggingEvent event) {

        // 判断日志名是否包含monitor
        if (event.getLoggerName().contains("module_interface")) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;

    }
}
