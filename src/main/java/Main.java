import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {

        URL url = new URL("http://studyplanet.kr/home/data/get.do");


        while (true) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            JSONObject object = new JSONObject();
            object.put("weekday", 4);
            object.put("memberEmail", "dydtn3510@naver.com");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            bw.write(object.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = br.readLine();
            System.out.println(line);
            Thread.sleep(10L);
        }

    }

    private static class Person implements Comparable<Person> {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(Person o) {
            return 0;
        }
    }
}
