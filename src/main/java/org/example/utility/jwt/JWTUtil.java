package org.example.utility.jwt;

import com.christ.utility.lib.vault.VaultUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String KEY_ID = "ID";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ROLE = "Role";
    public static final String KEY_DATA = "Data";
    private static final String Secret;
    private static final long ExpirationTime;
    private static final long RefreshExpirationTime;
    static {
//        Secret = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG,"security.password.encoder.secret").getValue();
        Secret = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG,"security.token.password.encoder.secret").getValue();
//        Secret = "NHYDUWHKFUI@EJNJNSCHUSNW29NCUNWMMDWMDOMODWDI@JD";
        ExpirationTime = Long.parseLong(VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG,"security.token.expiration.seconds").getValue());
        RefreshExpirationTime = Long.parseLong(VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG,"security.refresh.token.expiration.seconds").getValue());
    }

    public JWTUtil() {
    }

    public static Claims getClaims(String token) {
//        return (Claims) Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    public static String getIDFromToken(String token) {
        return (String)getClaims(token).get("ID", String.class);
    }

    public static String getNameFromToken(String token) {
        return (String)getClaims(token).get("Name", String.class);
    }

    public static String getRoleFromToken(String token) {
        return (String)getClaims(token).get("Role", String.class);
    }

    public static <T> T getDataFromToken(String token) {
        return (new Gson()).fromJson((String)getClaims(token).get("Data", String.class), (new TypeToken<T>() {
        }).getType());
    }

    public static JwtUser getUserFromToken(String token) {
        return (new Gson()).fromJson(getClaims(token).get("Data", String.class), JwtUser.class);
    }

    public static Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    public static String generateToken(JwtUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("ID", user.id);
        claims.put("Name", user.name);
        claims.put("Role", user.role);
        claims.put("Data", (new Gson()).toJson(user));
        return generateToken(claims, user.id);
    }

    public static Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private static Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private static SecretKey getKey() {
        SecretKey key = null;
        try {
            byte[] apiKeySecretBytes = Base64.getEncoder().encodeToString(Secret.getBytes()).getBytes();
//            key = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS512.getJcaName());
            key = new SecretKeySpec(apiKeySecretBytes, "HmacSHA512");
        } catch (Exception var2) {
        }
        return key;
    }

    private static String generateToken(Map<String, Object> claims, String username) {
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + ExpirationTime * 1000L);
//        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(createdDate).setExpiration(expirationDate).compressWith(CompressionCodecs.DEFLATE).signWith(SignatureAlgorithm.HS512, getKey()).compact();
        return Jwts.builder().claims(claims).subject(username).issuedAt(createdDate).expiration(expirationDate).compressWith(Jwts.ZIP.DEF).signWith(getKey(), Jwts.SIG.HS512).compact();
    }

    public static String generateRefreshToken(JwtUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("ID", user.id);
        claims.put("Name", user.name);
        claims.put("Role", user.role);
        claims.put("Data", (new Gson()).toJson(user));
        return generateRefreshToken(claims, user.id);
    }

    private static String generateRefreshToken(Map<String, Object> claims, String username) {
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + RefreshExpirationTime * 1000L);
//        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(createdDate).setExpiration(expirationDate).compressWith(CompressionCodecs.DEFLATE).signWith(SignatureAlgorithm.HS512, getKey()).compact();
        return Jwts.builder().claims(claims).subject(username).issuedAt(createdDate).expiration(expirationDate).compressWith(Jwts.ZIP.DEF).signWith(getKey(), Jwts.SIG.HS512).compact();
    }
}
