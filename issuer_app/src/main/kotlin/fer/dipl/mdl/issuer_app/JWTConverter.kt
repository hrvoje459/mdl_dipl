package fer.dipl.mdl.issuer_app

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import java.util.stream.Stream


@Component
class JwtConverter() : Converter<Jwt?, AbstractAuthenticationToken?> {
    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities: Collection<GrantedAuthority> = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt)!!.stream(),
            extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet())

        //return JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt))
        return OAuth2AuthenticationToken(DefaultOAuth2User(authorities, jwt.claims, "preferred_username"), authorities, getPrincipalClaimName(jwt))

    }

    private fun getPrincipalClaimName(jwt: Jwt): String {
        var claimName = JwtClaimNames.SUB
        return jwt.getClaim(claimName)
    }

    private fun extractResourceRoles(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess: Map<String, Collection<String>> = jwt.getClaim("realm_access")
        var resourceRoles: Collection<String> = listOf()

        resourceRoles = realmAccess.get("roles")!!
        val group_roles :Collection<String> = jwt.getClaim("groups")
        resourceRoles = resourceRoles.plus(group_roles)

        return resourceRoles.distinct().stream()
            .map { role: String ->
                SimpleGrantedAuthority(
                    "ROLE_$role"
                )
            }
            .collect(Collectors.toSet())
    }
}