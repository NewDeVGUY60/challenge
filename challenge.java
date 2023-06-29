
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;

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
        String passwordHash = resp.get("hash");
        String mySalt = resp.get("salt");

        byte[] mySaltToByte = HexFormat.of().parseHex(mySalt);

        String password = "";
        String hashedPassword = "";

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

                                if (hashedPassword.equals(passwordHash)) {
                                    System.out.println(password);
                                    testPassword(password, getResponse().get("id")); // Mot de passe trouvé
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

    public static Void testPassword(String password, String id){

        System.out.println("\""+password+"\"");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://shallenge.onrender.com/challenges/"+id+"/answer"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("\""+password+"\""))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(res -> {

                    System.out.println(res);
                })
                .join();

    }

    public static String hashPassword(String passwordToTest, byte[] salt) {


        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] passwordBytes = passwordToTest.getBytes();
            byte[] digest = md.digest(passwordBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = String.format("%02x", b);
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