package org.telescope.javel.framework.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.server.Context;
import org.telescope.config.Keys;
import org.telescope.javel.framework.storage.database.DB;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.Key;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWT {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWT.class);
    private static String SECRET_KEY = Context.getConfig().getString(Keys.JWT_SECRET_KEY);
    
    public static String encode(String id, String issuer) {
        return getToken(id , issuer, null, -1);
    }
    
    public static String encode(String id, String issuer, String subject) {
        return getToken(id , issuer, subject, -1);
    }
    
    public static String encode(String id, String issuer, String subject, long ttlMillis) {
        return getToken(id , issuer, subject, ttlMillis);
    }
    
    public static Map<String, Object> decode(String token) {
    
        Claims claims = getClaims(token);
        if (claims == null) {return null;}
        String table = claims.getSubject();
        if (table == null) {table = getTable(claims.getIssuer());}
        Map<String, Object> user = DB.table(table).where("id", claims.getId()).where("token", token).first();
        if (user != null) {
            if (user.containsKey("disabled") && user.get("disabled").toString().equals("true")) {return null;}
            user.put("guard", claims.getIssuer());
            return user;
        }
        return null;
    }
    
    private static String getToken(String id, String issuer, String subject, long ttlMillis) {

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
        
        String token = builder.compact();
        if (subject == null) {
            updateDriver(id, getTable(issuer), token);
        } else {
            updateDriver(id, subject, token);
        }
        return token;
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
    
    private static void updateDriver(String id, String table, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token",  token);
        DB.table(table).where("id", id).update(data);
    }
    
    private static String getTable(String issuer) {
        return issuer + "s";
    }
}
