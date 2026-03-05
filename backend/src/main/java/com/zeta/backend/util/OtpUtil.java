package com.zeta.backend.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpUtil {

    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Value("${app.otp.validity-seconds}")
    private long otpValiditySeconds;

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpCache.put(email, new OtpEntry(otp, Instant.now()));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpEntry entry = otpCache.get(email);
        if (entry == null) return false;

        // Check expiry
        if (Instant.now().isAfter(entry.getCreatedAt().plusSeconds(otpValiditySeconds))) {
            otpCache.remove(email);
            return false;
        }

        boolean valid = entry.getOtp().equals(otp);
        if (valid) otpCache.remove(email); // remove after successful validation
        return valid;
    }

    private static class OtpEntry {
        private final String otp;
        private final Instant createdAt;

        public OtpEntry(String otp, Instant createdAt) {
            this.otp = otp;
            this.createdAt = createdAt;
        }

        public String getOtp() { return otp; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
