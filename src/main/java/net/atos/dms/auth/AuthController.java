package net.atos.dms.auth;

import net.atos.dms.security.JwtUtil;
import net.atos.dms.user.User;
import net.atos.dms.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class LoginRequest {
        public String username; // can be email
        public String email;    // alias
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public long exp;

        public LoginResponse(String token, long exp) {
            this.token = token;
            this.exp = exp;
        }
    }

    public static class RegisterRequest {
        public String email;
        public String password;
        public String nid; // optional
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.email == null || req.email.isBlank() || req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password are required"));
        }

        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.status(409).body(Map.of("error", "user already exists"));
        }

        var u = new User();
        u.setEmail(req.email);
        u.setPassword(passwordEncoder.encode(req.password));
        u.setNid(req.nid == null ? ("nid_" + Instant.now().toEpochMilli()) : req.nid);
        userRepository.save(u);

        return ResponseEntity.ok(Map.of("status", "created", "email", req.email));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // accept either username or email field
        String username = (req == null) ? null : (req.username == null || req.username.isBlank() ? req.email : req.username);
        String password = (req == null) ? null : req.password;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username (or email) and password are required"));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // build extra claims (example: nid if available on principal)
            Map<String, Object> extra = new HashMap<>();
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails ud = (UserDetails) principal;
                extra.put("sub", ud.getUsername());
                try {
                    var m = ud.getClass().getMethod("getNid");
                    Object nid = m.invoke(ud);
                    if (nid != null) extra.put("nid", nid.toString());
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ignored) {
                }
            } else if (principal != null) {
                extra.put("sub", principal.toString());
            }

            String token = jwtUtil.generate(username, extra);
            long expMillis = System.currentTimeMillis() + jwtUtilExpirationMillisFallback();
            long expSecs = expMillis / 1000L;

            return ResponseEntity.ok(new LoginResponse(token, expSecs));
        } catch (Exception ex) {
            // do not leak internal details; return 401
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }
    }

    // We can't access JwtUtil.expirationMillis (private) — compute from property fallback for response
    private long jwtUtilExpirationMillisFallback() {
        try {
            String s = System.getProperty("dms.jwt.exp-minutes");
            if (s == null) {
                // try environment / spring property via System.getenv
                s = System.getenv("DMS_JWT_EXP_MINUTES");
            }
            long minutes = (s == null || s.isBlank()) ? 1440L : Long.parseLong(s);
            return minutes * 60L * 1000L;
        } catch (Exception ex) {
            return 1440L * 60L * 1000L;
        }
    }
}
