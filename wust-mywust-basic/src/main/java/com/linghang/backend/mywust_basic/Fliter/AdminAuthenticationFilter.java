package com.linghang.backend.mywust_basic.Fliter;
import com.linghang.backend.mywust_basic.Service.ManagerService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import cn.wustlinghang.mywust.common.security.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AdminAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    ManagerService managerService;
    private final JwtUtils jwtUtils;
    @Autowired
    TokenService tokenService;
    public AdminAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // 管理员专属接口路径（仅这些路径会被当前拦截器处理）
    private static final String[] ADMIN_PATHS = {
            "/auth/**",          // 管理员认证接口
            "/admin/common/**",  // 公共管理接口（图片等）
            "/operationLog/**",  // 通知/日志管理
            "/LogController/**",
            "/system/monitor/**",
            "/service/**",
            "/UnderGraduateStudent/addOccupation",    // 教室占用管理
            "/UnderGraduateStudent/listOccupations",
            "/UnderGraduateStudent/deleteOccupation",
            "/UnderGraduateStudent/addSchoolClassroom", // 基础教室管理
            "/UnderGraduateStudent/listSchoolClassrooms",
            "/UnderGraduateStudent/deleteSchoolClassroom",
            "/UnderGraduateStudent/listBuildings"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 关键1：放行 OPTIONS 预检请求（不进行管理员权限验证）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            request.setAttribute("adminAuthenticated", Boolean.TRUE);
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();

        // 仅处理管理员接口
        if (!isAdminPath(request, requestPath)) {
            request.setAttribute("adminAuthenticated", Boolean.TRUE);
            filterChain.doFilter(request, response);
            return;
        }
        // 管理员接口的完整验证逻辑（认证+权限）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "请先登录");
            return;
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            sendErrorResponse(response, 401, "登陆凭证错误");
            return;
        }
        String username = tokenService.getAdminUsername(token);
        if (username != null) {
            // 添加声明 已过滤
            request.setAttribute("adminAuthenticated", Boolean.TRUE);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(token, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            // 如果 Token 校验通过但不是管理员
            sendErrorResponse(response, 401, "权限不足：该功能仅限管理员访问");
        }
    }

    // 判断是否为管理员接口路径
    private boolean isAdminPath(HttpServletRequest request, String path) {
        if (path.equals("/auth/login")) {
            return false;
        }
        if (path.equals("/admin/common/upload")) {
            return false;
        }
        if (path.equals("/admin/common/getCarousels") || path.equals("/admin/common/getCalendar")) {
            return false;
        }
        
        // 对于通知列表接口，要求必须登录（学生或管理员均可）
        if (path.startsWith("/operationLog/list/publishedBut") || path.startsWith("/operationLog/publishedBut")) {
            // 检查 SecurityContextHolder，看 JwtAuthenticationFilter 是否已经填充了认证信息
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                return false; // 已登录，放行
            }
            return true; // 未登录，作为管理员路径拦截，会返回 401
        }

        if (path.equals("/admin/common/getPictureDetail")) {
            return false;
        }
        for (String adminPath : ADMIN_PATHS) {
            if (path.startsWith(adminPath.replace("**", ""))) {
                return true;
            }
        }
        return false;
    }

    // 关键2：错误响应中添加 CORS 头，确保浏览器能接收跨域错误信息
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        // 添加 CORS 头（允许前端 origin 访问）
        response.setHeader("Access-Control-Allow-Origin", "http://localhost"); // 替换为你的前端 origin
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        // 设置响应内容
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
        writer.flush();
        writer.close();
    }
}