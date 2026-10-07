package com.sarthak.demo.controller;

import com.sarthak.demo.model.User;
import com.sarthak.demo.repository.UserRepository;
import com.sarthak.demo.service.EmailService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;



    // ✅ REGISTER
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password) {

        if (userRepo.findByUsername(username) != null) {
            return "USER_EXISTS";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));

        userRepo.save(user);
        return "REGISTERED";
    }

    // ✅ LOGIN → STEP 1 (Generate OTP + Email)
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {

        User user = userRepo.findByUsername(username);

        // 🔍 DEBUG START
        System.out.println("Entered username: " + username);
        System.out.println("Entered password: " + password);

        if (user != null) {
            System.out.println("Stored hash: " + user.getPassword());
            System.out.println("Match result: " + encoder.matches(password, user.getPassword()));
        } else {
            System.out.println("User NOT FOUND in DB");
        }
        // 🔍 DEBUG END

        if (user != null && encoder.matches(password, user.getPassword())) {

            String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

            session.setAttribute("otp", otp);
            session.setAttribute("tempUser", username);
            session.setAttribute("otpTime", System.currentTimeMillis());

            emailService.sendOtp(username, otp);

            return "OTP_SENT";
        }

        return "FAIL";
    }
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
    // ✅ VERIFY OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, HttpSession session) {

        String sessionOtp = (String) session.getAttribute("otp");
        String tempUser = (String) session.getAttribute("tempUser");
        Long otpTime = (Long) session.getAttribute("otpTime");

        if (sessionOtp == null || tempUser == null || otpTime == null) {
            return "NO_OTP";
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - otpTime > 300000) {
            session.removeAttribute("otp");
            return "OTP_EXPIRED";
        }

        if (sessionOtp.equals(otp)) {

            session.setAttribute("user", tempUser);

            session.removeAttribute("otp");
            session.removeAttribute("tempUser");
            session.removeAttribute("otpTime");

            return "SUCCESS";
        }

        return "INVALID_OTP";
    }

    @GetMapping("/check-session")
    public boolean checkSession(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "LOGGED OUT";
    }
}