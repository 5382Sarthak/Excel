package com.sarthak.demo.controller;

import com.sarthak.demo.model.User;
import com.sarthak.demo.repository.UserRepository;
import com.sarthak.demo.service.EmailService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String username,
            @RequestParam String password) {

        if (userRepo.findByUsername(username) != null) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("USER_EXISTS");
        }

        User user = new User();

        user.setUsername(username);

        user.setPassword(
                encoder.encode(password)
        );

        userRepo.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("REGISTERED");
    }


    // =========================================================
    // LOGIN
    // STEP 1 → CHECK USERNAME/PASSWORD
    // STEP 2 → GENERATE OTP
    // STEP 3 → SEND OTP EMAIL
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        System.out.println(
                "Login attempt for username: " + username
        );

        User user = userRepo.findByUsername(username);


        // -----------------------------------------------------
        // USER NOT FOUND
        // -----------------------------------------------------

        if (user == null) {

            System.out.println(
                    "User NOT FOUND in MongoDB"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("INVALID_CREDENTIALS");
        }


        // -----------------------------------------------------
        // CHECK PASSWORD
        // -----------------------------------------------------

        boolean passwordMatches =
                encoder.matches(
                        password,
                        user.getPassword()
                );

        System.out.println(
                "User found in MongoDB"
        );

        System.out.println(
                "Password match: " + passwordMatches
        );


        if (!passwordMatches) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("INVALID_CREDENTIALS");
        }


        // -----------------------------------------------------
        // GENERATE 6-DIGIT OTP
        // -----------------------------------------------------

        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );


        // -----------------------------------------------------
        // STORE OTP IN SESSION
        // -----------------------------------------------------

        session.setAttribute(
                "otp",
                otp
        );

        session.setAttribute(
                "tempUser",
                username
        );

        session.setAttribute(
                "otpTime",
                System.currentTimeMillis()
        );


        // -----------------------------------------------------
        // SEND OTP TO EMAIL
        // -----------------------------------------------------

        try {

            emailService.sendOtp(
                    username,
                    otp
            );

        } catch (Exception e) {

            e.printStackTrace();

            // Remove temporary authentication data
            session.removeAttribute("otp");
            session.removeAttribute("tempUser");
            session.removeAttribute("otpTime");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OTP_SEND_FAILED");
        }


        System.out.println(
                "OTP generated and sent successfully."
        );


        // -----------------------------------------------------
        // SUCCESS
        // -----------------------------------------------------

        return ResponseEntity
                .ok("OTP_SENT");
    }


    // =========================================================
    // VERIFY OTP
    // =========================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestParam String otp,
            HttpSession session) {

        String sessionOtp =
                (String) session.getAttribute("otp");

        String tempUser =
                (String) session.getAttribute("tempUser");

        Long otpTime =
                (Long) session.getAttribute("otpTime");


        // -----------------------------------------------------
        // NO OTP IN SESSION
        // -----------------------------------------------------

        if (sessionOtp == null ||
            tempUser == null ||
            otpTime == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("NO_OTP");
        }


        // -----------------------------------------------------
        // CHECK OTP EXPIRATION
        // 5 MINUTES
        // -----------------------------------------------------

        long currentTime =
                System.currentTimeMillis();

        if (currentTime - otpTime > 300000) {

            session.removeAttribute("otp");
            session.removeAttribute("tempUser");
            session.removeAttribute("otpTime");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("OTP_EXPIRED");
        }


        // -----------------------------------------------------
        // CHECK OTP
        // -----------------------------------------------------

        if (!sessionOtp.equals(otp)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("INVALID_OTP");
        }


        // -----------------------------------------------------
        // OTP CORRECT
        // USER IS NOW LOGGED IN
        // -----------------------------------------------------

        session.setAttribute(
                "user",
                tempUser
        );


        // Remove temporary OTP information

        session.removeAttribute("otp");

        session.removeAttribute("tempUser");

        session.removeAttribute("otpTime");


        System.out.println(
                "OTP verified successfully for: "
                + tempUser
        );


        return ResponseEntity
                .ok("SUCCESS");
    }


    // =========================================================
    // CHECK SESSION
    // =========================================================

    @GetMapping("/check-session")
    public ResponseEntity<Boolean> checkSession(
            HttpSession session) {

        boolean loggedIn =
                session.getAttribute("user") != null;

        return ResponseEntity.ok(loggedIn);
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @GetMapping("/logout")
    public ResponseEntity<String> logout(
            HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                "LOGGED_OUT"
        );
    }
}