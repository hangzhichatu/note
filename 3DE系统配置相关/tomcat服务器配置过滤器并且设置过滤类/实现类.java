import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthFilter implements Filter {
    private Map<String, String> userCredentials = new HashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 读取初始化参数
        for (String paramName : filterConfig.getInitParameterNames()) {
            String paramValue = filterConfig.getInitParameter(paramName);
            userCredentials.put(paramName, paramValue);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String username = httpRequest.getParameter("username");
        String password = httpRequest.getParameter("password");

        if (username == null || password == null || !userCredentials.containsKey(username) ||
                !userCredentials.get(username).equals(password)) {
            // 认证失败，返回401 Unauthorized
            response.getWriter().write("Unauthorized");
            return;
        }

        // 认证成功，继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理资源
    }
}
