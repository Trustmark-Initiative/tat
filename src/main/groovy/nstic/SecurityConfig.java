package nstic;

import assessment.tool.UserService;
import edu.gatech.gtri.trustmark.grails.OidcLoginCustomizer;
import nstic.util.AssessmentToolProperties;

import nstic.web.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import static org.gtri.fj.data.Option.somes;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.scope}")
    private String scope;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("keycloak")
                .clientId(clientId)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope(scope)
                .authorizationUri(issuerUri + "/protocol/openid-connect/auth")
                .tokenUri(issuerUri + "/protocol/openid-connect/token")
                .userInfoUri(issuerUri + "/protocol/openid-connect/userinfo")
                .userNameAttributeName("preferred_username")
                .jwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                .clientName("Keycloak")
                .build();

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    UserService userService() {
        return new UserService();
    }

    private static final String ROLES_CLAIM = "roles";

    @Bean
    @Order(3)
    SecurityFilterChain oidcFilterChain(
            final HttpSecurity httpSecurity)
            throws Exception {

        httpSecurity
            .csrf().disable()
            .authorizeHttpRequests(authorize -> authorize
                .regexMatchers("/").permitAll()
                .regexMatchers("/assets/.*").permitAll()
                .regexMatchers("/public/documents").permitAll()
                .antMatchers("/publicApi/findDocs/**").permitAll()
                .antMatchers("/publicApi/trustmarks/**").permitAll()
                .anyRequest().authenticated())

            .oauth2Login(new OidcLoginCustomizer(defaultOAuth2User -> {
                userService().insertOrUpdateHelper(
                        defaultOAuth2User.getName(),
                        (String)defaultOAuth2User.getAttributes().get("family_name"),
                        (String)defaultOAuth2User.getAttributes().get("given_name"),
                        (String)defaultOAuth2User.getAttributes().get("email"),
                        somes(org.gtri.fj.data.List.iterableList(
                                defaultOAuth2User.getAuthorities()).map(
                                        GrantedAuthority::getAuthority).map(
                                                Role::fromValueOption)));
            }, "/", "/oauth2/authorize-client"))
            .logout()
            .logoutUrl("/logout")
            .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID");

        return httpSecurity.build();
    }


    @Bean
    @Order(2)
    SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {

        boolean enableApiProtection = AssessmentToolProperties.getIsApiClientAuthorizationRequired();

        if (enableApiProtection) {
            http
                    .antMatcher("/public/**")
                    .authorizeHttpRequests(authorize -> {
                        authorize
                                .mvcMatchers(HttpMethod.GET, "/public/documents/**").permitAll()
                                .mvcMatchers(HttpMethod.GET, "/publicApi/findDocs/**").permitAll()
                                .mvcMatchers(HttpMethod.GET, "/public/status").permitAll()
                                .anyRequest().hasAuthority("SCOPE_profile");
                    })
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt()); // This enables the actual validation of JWTs
        } else {
            http
                    .antMatcher("/public/**")
                    .authorizeHttpRequests(authorize -> {
                        authorize
                                .anyRequest().permitAll();
                    });
        }

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {

        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(
                        this.clientRegistrationRepository());

        String redirectUrl = AssessmentToolProperties.getBaseUrl();

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(redirectUrl);

        return oidcLogoutSuccessHandler;
    }

    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;

                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                    if (userInfo.hasClaim(ROLES_CLAIM)) {
                         var roles = (Collection<String>) userInfo.getClaim(ROLES_CLAIM);
                        mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
                    }
                } else if (authority instanceof OAuth2UserAuthority) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                    if (userAttributes.containsKey(ROLES_CLAIM)) {
                        var roles = (Collection<String>) userAttributes.get(ROLES_CLAIM);
                        mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
                    }
                }
            });

            return mappedAuthorities;
        };
    }

    Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    @Bean
    public HttpFirewall allowUrlEncodedPercentHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }
}
