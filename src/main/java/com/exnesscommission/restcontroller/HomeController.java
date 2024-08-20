package com.exnesscommission.restcontroller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.exnesscommission.dto.AuthResponse;
import com.exnesscommission.dto.DataItem;
import com.exnesscommission.dto.LoginRequest;
import com.exnesscommission.dto.MailDto;
import com.exnesscommission.entity.ExnessCommission;
import com.exnesscommission.service.ExnessCommissionService;
import com.exnesscommission.service.MailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;

@RestController
public class HomeController {
	@Autowired
	ExnessCommissionService exCommissionService;
	
	@Autowired
	MailService mailService;
	
	@GetMapping("/test/{fromDate}/{toDate}/{type}")
	@CrossOrigin(origins = "*")
	public ResponseEntity<List<ExnessCommission>> test(@PathVariable("fromDate") String fromDate, @PathVariable("toDate") String toDate,
			@PathVariable("type") int type) throws JsonMappingException, JsonProcessingException {
		List<DataItem> results = new ArrayList<>();
		List<ExnessCommission> dataToDatabase = new ArrayList<>();
		
		long startTime = System.currentTimeMillis()/1000;
		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"

		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		try {
	        ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
	                AuthResponse.class);

	        if (responseEntity.getStatusCode().is2xxSuccessful()) {
	            AuthResponse authResponse = responseEntity.getBody();
	            String token = authResponse.getToken();

	            // Gọi API khác với token
	            String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + fromDate
	                    + "&reward_date_to=" + toDate + "&limit=1000";

	            HttpHeaders headersWithToken = new HttpHeaders();
	            headersWithToken.set("Authorization", "JWT " + token);

	            HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

	            ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET,
	                    requestWithToken, String.class);

	            String json = apiResponse.getBody();

	            ObjectMapper objectMapper = new ObjectMapper();
	            JsonNode rootNode = objectMapper.readTree(json);

	            if (rootNode.has("data")) {
	                JsonNode dataNode = rootNode.get("data");
	                if (dataNode.isArray()) {
	                    results = objectMapper.readValue(dataNode.toString(), new TypeReference<List<DataItem>>() {
	                    });
	                }
	            }

	            for (DataItem item : results) {
	                ExnessCommission itemToWrite = new ExnessCommission();
	                long unixTimestamp = convertToUnixTimestamp(item.getReward_date(), "GMT+7");
	                itemToWrite.setTransactionId(String.valueOf(item.getId()));
	                itemToWrite.setDate(unixTimestamp);
	                itemToWrite.setAmount(Double.parseDouble(item.getReward()));
	                itemToWrite.setCurrency(item.getCurrency());
	                itemToWrite.setClientAccount(String.valueOf(item.getClient_account()));
	                dataToDatabase.add(itemToWrite);
	            }
	        }
	    } catch (ResourceAccessException e) {
	        System.err.println("Connection failed: " + e.getMessage());
	    }
		
		if (dataToDatabase.size() > 0 && type == 1) exCommissionService.saveListExnessCommission(dataToDatabase);
		
		long endTime = System.currentTimeMillis()/1000;
		System.out.println(fromDate + " - " + toDate + " = " + results.size());
		System.out.println("Run time: " + (endTime-startTime) + "ms");
		
		return ResponseEntity.ok(dataToDatabase);
	}
	
	@PostMapping("/send-mail")
	@CrossOrigin(origins = "*")
	public ResponseEntity<String> sendMail(@RequestBody MailDto request) {
		StringBuilder result = new StringBuilder();
		try {
			mailService.send(request);
			
			result.append("Success");
		} catch (MessagingException mge) {
			// TODO Auto-generated catch block
			result.append(mge.getMessage());
		}
		return ResponseEntity.ok(result.toString());
	}
	
	public static long convertToUnixTimestamp(String dateString, String timeZoneId) {
        try {
            // Định dạng của chuỗi ngày
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Đặt múi giờ cho đối tượng DateFormat
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            
            // Chuyển đổi chuỗi ngày thành đối tượng Date
            Date date = dateFormat.parse(dateString);
            
            // Sử dụng Calendar để thiết lập giờ, phút và giây
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            
            // Lấy giá trị Unix timestamp
            long unixTimestamp = calendar.getTimeInMillis() / 1000; // Chia cho 1000 để chuyển từ mili giây sang giây
            return unixTimestamp + 86400;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Trả về giá trị âm để chỉ ra lỗi
        }
    }
}
