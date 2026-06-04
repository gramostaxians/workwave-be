package com.hr.workwave.services;

import com.hr.workwave.model.User;
import com.hr.workwave.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = usersRepository.findByEmail(email);

        // New user: not in DB yet but has a valid Azure JWT.
        // Return a default EMPLOYEE role so the request is not blocked.
        // The /api/users/{email} endpoint will auto-create them on the same first request.
        if (user == null) {

            //TODO
            // Setting what role to what user
            // we should consult the VESI /persons api
            // for time being we will just assign EMPLOYEE role to all new users
            return new org.springframework.security.core.userdetails.User(
                    email,
                    "",
                    List.of(new SimpleGrantedAuthority("EMPLOYEE"))
            );
        }

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "",
                authorities
        );
    }
}
