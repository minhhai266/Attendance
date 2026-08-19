package com.attendenceSystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class FirewallConfig {
    @Bean
    public HttpFirewall strictHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedPeriod(false);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        firewall.setAllowUrlEncodedSlash(false);
        return firewall;
    }

}
