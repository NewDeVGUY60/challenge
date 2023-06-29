
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;

public class challenge {

    public static void main(String[] args) {

        challenge.setResponse(null);
        challenge.getPassword();

    }

    public static void getPassword() {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://shallenge.onrender.com/challenges"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(res -> {

                    HashMap<String, String> resp = new HashMap<>();
                    resp.put("id", res.substring(7, 43));
                    resp.put("hash", res.substring(53, 117));
                    resp.put("salt", res.substring(127, 159));

                    challenge.setResponse(resp);
                })
                .join();

        challenge.searchPassword(getResponse());

    };

    public static void searchPassword(HashMap<String, String> resp) {
        String myHash = resp.get("hash");
        String mySalt = resp.get("salt");

        byte[] mySaltToByte = mySalt.getBytes();

        String password = "" ;
        String hashedPassword = hashPassword(password, mySaltToByte);
        
        for (int a = 0; a < 26; a++) {
            System.out.println(password);
            System.out.println(hashedPassword);
            for (int b = 0; b < 26; b++) {
                for (int c = 0; c < 26; c++) {
                    for (int d = 0; d < 26; d++) {
                        for (int e = 0; e < 26; e++) {
                            for (int f = 0; f < 26; f++) {
                                password = "" + (char) ('a' + a) + (char) ('a' + b) + (char) ('a' + c) +
                                        (char) ('a' + d) + (char) ('a' + e) + (char) ('a' + f);
                                hashedPassword = hashPassword(password, mySaltToByte);

                                if (hashedPassword.equals(myHash)) {
                                    System.out.println(password);
                                    challenge.testPassword(password, getResponse().get("id")); // Mot de passe trouvé
                                }
                            }
                        }
                    }
                }
            }
        }

        challenge.setResponse(null);
        System.out.println("password not fund");
    }

    public static Boolean testPassword(String password, String id) {

        return false;
    }

    public static String hashPassword(String passwordToTest, byte[] salt) {

        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] digest = md.digest(passwordToTest.getBytes(StandardCharsets.UTF_8));
          

                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    String hex = String.format("%02x", digest[i]);
                    hexString.append(hex);
                }

                return hexString.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        return null;
    }

    private static HashMap<String, String> response;

    public static HashMap<String, String> getResponse() {

        System.out.println(challenge.response);
        return challenge.response;

    }

    public static void setResponse(HashMap<String, String> response) {
        challenge.response = response;
    }


}