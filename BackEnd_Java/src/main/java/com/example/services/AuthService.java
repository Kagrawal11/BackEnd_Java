package com.example.services;

import com.example.dto.CustomerDTO;
import com.example.dto.CustomerIdDTO;
import com.example.dto.LoginDTO;
import com.example.dto.ResetPasswordDTO;
import com.example.entities.CustomerMaster;

import com.example.enums.AuthProvider;

import com.example.enums.CustomerRole;
import com.example.mapper.CustomerMapper;
import com.example.model.CustomerModel;
import com.example.repositories.CustomerRepository;
import com.example.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerMapper mapper;

    @Autowired
    private EmailService emailService;

    // REGISTER (LOCAL)
    public CustomerModel register(CustomerDTO dto) {
        logger.info("Attempting to register user with email: {}", dto.getEmail());

        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already exists", dto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        CustomerMaster entity = new CustomerMaster();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setCustomerRole(CustomerRole.CUSTOMER);
        entity.setAuthProvider(AuthProvider.LOCAL);
        entity.setProfileCompleted(false);

        CustomerMaster saved = repository.save(entity);
        logger.info("User registered successfully: {}", saved.getEmail());

        CustomerModel model = mapper.toModel(saved);
        model.setPassword(null); // 🔒 never expose

        return model;
    }

    // LOGIN (LOCAL)
    public String login(LoginDTO dto) {

        CustomerMaster user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please login using " + user.getAuthProvider());
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getCustomerRole().name());
    }

    public void sendResetToken(String email) {

        CustomerMaster customerMaster = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = jwtUtil.generateResetToken(email);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        emailService.sendSimpleEmail(
                email,
                "Reset Your Password",
                "Click the link below to reset your password:\n\n" +
                        resetLink +
                        "\n\nThis link is valid for 15 minutes.");
    }

    public void resetPassword(ResetPasswordDTO dto) {

        if (!jwtUtil.isResetTokenValid(dto.getToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
        }

        String email = jwtUtil.extractUsername(dto.getToken());

        CustomerMaster user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        repository.save(user);

        emailService.sendSimpleEmail(
                user.getEmail(),
                "Password Updated",
                "Your password has been successfully updated.");
    }

    public CustomerModel getCustomerProfile(String email) {
        CustomerMaster customer = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerModel model = mapper.toModel(customer);
        model.setPassword(null); // 🔒 never expose password

        return model;
    }

    // update profile
    public CustomerModel updateCustomerProfile(String email, CustomerDTO dto) {

        CustomerMaster customer = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // updatable fields
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setProfileCompleted(true);

        // password optional
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        CustomerMaster saved = repository.save(customer);

        CustomerModel model = mapper.toModel(saved);
        model.setPassword(null); // never return password

        return model;
    }

    public CustomerIdDTO getCustomerIdByEmail(String email) {

        CustomerMaster customer = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        return new CustomerIdDTO(customer.getId());
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        CustomerMaster customer = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid old password");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        repository.save(customer);
    }

    public String googleLogin(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String firstName = (String) payload.get("given_name");
                String lastName = (String) payload.get("family_name");

                CustomerMaster user = repository.findByEmail(email)
                        .orElseGet(() -> {
                            CustomerMaster u = new CustomerMaster();
                            u.setEmail(email);
                            u.setFirstName(firstName);
                            u.setLastName(lastName);
                            u.setCustomerRole(CustomerRole.CUSTOMER);
                            u.setAuthProvider(AuthProvider.GOOGLE);
                            u.setProfileCompleted(false);
                            return repository.save(u);
                        });

                return jwtUtil.generateToken(user.getEmail(), user.getCustomerRole().name());
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid ID token.");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying Google token", e);
            throw new RuntimeException("Google Sign-In failed");
        }
    }

}
