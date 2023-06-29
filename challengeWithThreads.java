
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;

public class challengeWithThreads {

    public static void main(String[] args) {

        setResponse(null);
        getPassword();

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

                    setResponse(resp);
                })
                .join();

        searchPassword(getResponse());

    };

    public static void searchPassword(HashMap<String, String> resp) {
        String passwordHash = resp.get("hash").toString();
        String mySalt = resp.get("salt").toString();

        byte[] mySaltToByte = HexFormat.of().parseHex(mySalt);

        int NUM_THREADS = 8; // Nombre de threads souhaité

        Thread[] threads = new Thread[NUM_THREADS];

        for (int t = 0; t < NUM_THREADS; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                searchInRange(threadIndex, NUM_THREADS, passwordHash, mySaltToByte);
            });
            threads[t].start();
        }

        // Attendre la fin de tous les threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        setResponse(null);
        System.out.println("Password not found");
    }

    public static void searchInRange(int startIndex, int step, String passwordHash, byte[] salt) {

        for (int a = startIndex; a < 26; a += step) {
            System.out.println((char) ('a' + a));
            for (int b = 0; b < 26; b++) {
                for (int c = 0; c < 26; c++) {
                    for (int d = 0; d < 26; d++) {
                        for (int e = 0; e < 26; e++) {
                            for (int f = 0; f < 26; f++) {
                                String password = "" + (char) ('a' + a) + (char) ('a' + b) + (char) ('a' + c) +
                                        (char) ('a' + d) + (char) ('a' + e) + (char) ('a' + f);

                                if (hashPassword(password, salt).equals(passwordHash)) {
                                    setPasswordFund(true);
                                    System.out.println(password);
                                    testPassword(password, getResponse().get("id")); // Mot de passe trouvé
                                    break;
                                } else if (getPasswordFund()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }

    }

    public static String hashPassword(String passwordToTest, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] passwordBytes = passwordToTest.getBytes();

            byte[] merge = new byte[salt.length + passwordBytes.length];
            System.arraycopy(salt, 0, merge, 0, salt.length);
            System.arraycopy(passwordBytes, 0, merge, salt.length, passwordBytes.length);

            byte[] passwordHashed = md.digest(merge);

            StringBuilder hexString = new StringBuilder();
            for (byte b : passwordHashed) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Void testPassword(String password, String id) {

        System.out.println("\"" + password + "\"");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://shallenge.onrender.com/challenges/" + id + "/answer"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("\"" + password + "\""))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(res -> {

                    System.out.println(res);
                })
                .join();

    }

    private static HashMap<String, String> response;
    private volatile static Boolean passwordFund = false;

    public static Boolean getPasswordFund() {
        return passwordFund;
    }

    public static void setPasswordFund(Boolean passwordFund) {
        challengeWithThreads.passwordFund = passwordFund;
    }

    public static HashMap<String, String> getResponse() {

        System.out.println(response);
        return response;

    }

    public static void setResponse(HashMap<String, String> resp) {
        response = resp;
    }

}