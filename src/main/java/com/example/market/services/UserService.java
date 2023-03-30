package com.example.market.services;

import com.example.market.models.User;
import com.example.market.models.enums.Role;
import com.example.market.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private EmailSenderService emailSenderService;

    public boolean createUser(User user){
        String email = user.getEmail();
        if(userRepository.findByEmail(email) != null) return false;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.ROLE_USER);
        user.setActivationCode(UUID.randomUUID().toString());
        log.info("Saving new User with email: {}", email);
        userRepository.save(user);
        if(!email.isEmpty()){
            String message = String.format(
                    "Hello, %s! \n" +
                            "Please, visit next link: http://localhost:8080/activate/%s",
                    user.getName(),
                    user.getActivationCode()
            );
            emailSenderService.send(user.getEmail(), "Activation", message);
        }
        return true;
    }

    public List<User> list(){
        return userRepository.findAll();
    }

    public void banUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null){
            if (user.isActive()) {
                user.setActive(false);
                log.info("Ban user with id {}; email {}", user.getId(), user.getEmail());
            }else {
                user.setActive(true);
                log.info("Unban user with id {}; email {}", user.getId(), user.getEmail());
            }
        }
        userRepository.save(user);
    }

    public void changeUserRoles(User user, Map<String, String> form) {
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        user.getRoles().clear();
        for(String key : form.keySet()){
            if(roles.contains(key))
                user.getRoles().add(Role.valueOf(key));
        }
        userRepository.save(user);
    }

    public User getUserByPrincipal(Principal principal) {
        if (principal == null) return new User();
        return userRepository.findByEmail(principal.getName());
    }

    public boolean activateUser(String code) {
        User user = userRepository.findByActivationCode(code);

        if(user == null)
            return false;
        user.setActivationCode(null);
        user.setActive(true);
        userRepository.save(user);
        return true;
    }
}
