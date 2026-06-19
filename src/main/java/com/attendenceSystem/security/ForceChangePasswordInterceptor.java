// package com.attendenceSystem.security;

// import org.springframework.stereotype.Component;
// import org.springframework.web.servlet.HandlerInterceptor;

// import com.attendenceSystem.constant.Routes;
// import com.attendenceSystem.entity.User;
// import com.attendenceSystem.repository.UserRepository;
// import com.attendenceSystem.util.SecurityUtil;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class ForceChangePasswordInterceptor implements HandlerInterceptor {
//         private final UserRepository userRepository;
        
//         @Override
//         public boolean preHandle(
//                         HttpServletRequest request,
//                         HttpServletResponse response,
//                         Object handler)
//                         throws Exception {
//                 String username = SecurityUtil.getCurrentUserName();
//                 if (username == null) {
//                         return true;
//                 }
//                 User user = userRepository.findUserByUsername(username).orElse(null);
//                 if (user == null) {
//                         return true;
//                 }
//                 String uri = request.getRequestURI();
//                 if (user.isMustChangePassword() && !uri.startsWith(Routes.USER + "/change-password")) {
//                         response.sendRedirect(Routes.USER + "/change-password");
//                         return false;
//                 }
//                 return true;
//         }
// }