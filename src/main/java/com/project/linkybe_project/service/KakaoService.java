package com.project.linkybe_project.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

@Service
public class KakaoService {

    public String getEmail(String accessToken) {
        try {
            URL url = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            JSONObject json = new JSONObject(response.toString());

            JSONObject kakaoAccount = json.getJSONObject("kakao_account");

            // ⚠️ 이메일 제공 안할 수도 있음
            if (!kakaoAccount.has("email")) {
                throw new RuntimeException("이메일 제공 동의 필요");
            }

            return kakaoAccount.getString("email");

        } catch (Exception e) {
            throw new RuntimeException("카카오 인증 실패");
        }
    }
}