
package fer.dipl.mdl.issuer_app

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
@EnableWebSecurity
class SecurityConfig {
    private val GROUPS: String = "groups"
    private val REALM_ACCESS_CLAIM: String = "realm_access"
    private val ROLES_CLAIM: String = "roles"

    @Autowired
    private var keycloakLogoutHandler: KeycloakLogoutHandler? = null

    fun SecurityConfig(keycloakLogoutHandler: KeycloakLogoutHandler?) {
        this.keycloakLogoutHandler = keycloakLogoutHandler
    }

    @Autowired
    private val jwtConverter: JwtConverter? = null


    private val log = LoggerFactory.getLogger(this.javaClass)


    @Bean
    @Throws(Exception::class)
    fun resourceServerFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests(Customizer { auth ->
            auth
                .requestMatchers(AntPathRequestMatcher("/drivers*"))
                .hasRole("driver")
                //.permitAll()
                .requestMatchers(AntPathRequestMatcher("/"))
                .permitAll()
                .anyRequest()
                .authenticated()
        })
        /*http.oauth2ResourceServer { oauth2: OAuth2ResourceServerConfigurer<HttpSecurity?> ->
            oauth2
                .jwt(Customizer.withDefaults())
        }*/
        http.oauth2ResourceServer { oauth2 -> oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtConverter) } }


        http.sessionManagement{session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
        http.cors {}
        http.csrf { csrf -> csrf.disable()}

        http.headers { headers  -> headers.frameOptions{fro -> fro.disable()}}
        http.formLogin { frlo -> frlo.disable()}
        http.httpBasic { basic -> basic.disable()}



        /*http.oauth2Login{oauth -> oauth.}
            .logout { logout: LogoutConfigurer<HttpSecurity?> ->
                logout.addLogoutHandler(
                    keycloakLogoutHandler
                ).logoutSuccessUrl("/")
            }
*/
        /*http.oauth2Login(Customizer.withDefaults())
            .logout { logout: LogoutConfigurer<HttpSecurity?> ->
                logout.addLogoutHandler(
                    keycloakLogoutHandler
                ).logoutSuccessUrl("/")
            }*/
        return http.build()
    }


    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.setAllowedOriginPatterns(listOf("*"))
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }

/*
    @Bean
    fun userAuthoritiesMapperForKeycloak(): GrantedAuthoritiesMapper {
        return GrantedAuthoritiesMapper { authorities: Collection<GrantedAuthority> ->
            val mappedAuthorities: MutableSet<GrantedAuthority> = HashSet()
            val authority = authorities.iterator().next()
            val isOidc = authority is OidcUserAuthority

            log.info("JAN ROCKER: " + authorities)
            println("JAN ROCKER: " + authorities)

            if (isOidc) {
                val oidcUserAuthority = authority as OidcUserAuthority
                val userInfo = oidcUserAuthority.userInfo

                // Tokens can be configured to return roles under
                // Groups or REALM ACCESS hence have to check both
                if (userInfo.hasClaim(REALM_ACCESS_CLAIM)) {
                    val realmAccess =
                        userInfo.getClaimAsMap(REALM_ACCESS_CLAIM)
                    val roles =
                        realmAccess[ROLES_CLAIM] as Collection<String>?
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
                } else if (userInfo.hasClaim(GROUPS)) {
                    val roles = userInfo.getClaim<Any>(
                        GROUPS
                    ) as Collection<String>
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
                }
            } else {
                val oauth2UserAuthority = authority as OAuth2UserAuthority
                val userAttributes = oauth2UserAuthority.attributes

                if (userAttributes.containsKey(REALM_ACCESS_CLAIM)) {
                    val realmAccess = userAttributes[REALM_ACCESS_CLAIM] as Map<String, Any>?
                    val roles =
                        realmAccess!![ROLES_CLAIM] as Collection<String>?
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles))
                }
            }
            mappedAuthorities
        }
    }*/

    /*fun generateAuthoritiesFromClaim(roles: Collection<String>?): Collection<GrantedAuthority> {
        return roles!!.stream().map { role: String ->
            SimpleGrantedAuthority(
                "ROLE_$role"
            )
        }.collect(
            Collectors.toList()
        )
    }*/


}