import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "admin123";
        
        // Generate a fresh hash
        String freshHash = encoder.encode(password);
        System.out.println("Fresh BCrypt Hash: " + freshHash);
        System.out.println("Hash length: " + freshHash.length());
        
        // Test the existing hash from database
        String existingHash = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfP5fqnQq1dP0m6";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("Existing hash matches: " + matchesExisting);
        
        // Test the fresh hash
        boolean matchesFresh = encoder.matches(password, freshHash);
        System.out.println("Fresh hash matches: " + matchesFresh);
    }
}