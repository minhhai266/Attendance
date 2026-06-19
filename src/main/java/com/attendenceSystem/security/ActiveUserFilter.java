// package com.attendenceSystem.security;

// import java.io.IOException;

// import org.springframework.security.authentication.AnonymousAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import com.attendenceSystem.constant.Routes;
// import com.attendenceSystem.entity.User;
// import com.attendenceSystem.repository.UserRepository;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class ActiveUserFilter extends OncePerRequestFilter {

//     private final UserRepository userRepository;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain)
//             throws ServletException, IOException {

//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//         if (authentication != null
//                 && authentication.isAuthenticated()
//                 && !(authentication instanceof AnonymousAuthenticationToken)) {

//             String username = authentication.getName();

//             User user = userRepository.findByUsername(username).orElse(null);

//             if (user == null || !user.isActive()) {
//                 SecurityContextHolder.clearContext();
//                 HttpSession session = request.getSession(false);
//                 if (session != null) {
//                     session.invalidate();
//                 }
//                 response.sendRedirect(Routes.USER + "/login");
//                 return;
//             }
//         }

//         filterChain.doFilter(request, response);
//     }

//     @Override
//     protected boolean shouldNotFilter(HttpServletRequest request) {
//         String uri = request.getRequestURI();

//         return uri.startsWith("/css/")
//                 || uri.startsWith("/js/")
//                 || uri.startsWith("/images/");
//     }
// }