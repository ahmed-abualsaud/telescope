package org.traccar.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Map;
import java.util.Date;
import org.traccar.database.DB;
import org.traccar.config.Keys;
import org.traccar.Context;
import io.jsonwebtoken.*;


public class JWT {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWT.class);
    private static String SECRET_KEY = Context.getConfig().getString(Keys.JWT_SECRET_KEY);

    public static String encodeJWT(String id, String issuer, String subject, long ttlMillis) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
    
    public static Map<String, Object> decodeJWT(String token) {
    
        Claims claims = getClaims(token);
        if (claims == null) {return null;}
        Map<String, Object> user = DB.table(getTable(claims.getSubject()))
                .where("token", token).first();
        if (user != null) {
            user.put("guard", claims.getSubject());
            return user;
        }
        return null;
    }
    
    private static Claims getClaims(String jwt) {
        Claims claims = null;
        try {
             claims = Jwts.parser()
                     .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                     .parseClaimsJws(jwt).getBody();
        } catch (JwtException e) {
            LOGGER.error("Untrusted token: ", e);
        }
        return claims;
    }
    
    private static String getTable(String guard) {
        return "tc_" + guard + "s";
    }
}
