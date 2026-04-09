package api_tracking.Utils;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v143.network.Network;
import org.openqa.selenium.devtools.v143.network.model.Request;
import org.openqa.selenium.devtools.v143.network.model.Response;

import java.util.*;

public class APIListener {

    private final Map<String, Map<String, Object>> apiDataByRequestId = new LinkedHashMap<>();
    private final Map<String, List<Map<String, Object>>> sectionWiseData = new LinkedHashMap<>();

    private String currentSection = "";

    public void startSection(String sectionName) {
        currentSection = sectionName;
        System.out.println("\n========== " + sectionName + " START ==========");
    }

    public void endSection(String sectionName) {
        printSectionSummary(sectionName);
        System.out.println("\n========== " + sectionName + " END ==========\n");
        apiDataByRequestId.clear();
    }

    public void enableNetworkTracking(DevTools devTools) {

    	devTools.send(Network.enable(
    	        Optional.empty(),
    	        Optional.empty(),
    	        Optional.empty(),
    	        Optional.empty(),
    	        Optional.empty()
    	));

        devTools.addListener(Network.requestWillBeSent(), requestEvent -> {

            Request req = requestEvent.getRequest();
            String requestId = requestEvent.getRequestId().toString();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("Section", currentSection);
            data.put("Method", req.getMethod());
            data.put("URL", req.getUrl());
            data.put("RequestTime", System.currentTimeMillis());

            apiDataByRequestId.put(requestId, data);

            System.out.println("[REQUEST] " + req.getMethod() + " → " + req.getUrl());
        });

        devTools.addListener(Network.responseReceived(), responseEvent -> {

            Response resp = responseEvent.getResponse();
            String requestId = responseEvent.getRequestId().toString();

            int status = resp.getStatus().intValue();
            String url = resp.getUrl();

            System.out.println("[RESPONSE] " + status + " ← " + url);

            Map<String, Object> requestEntry =
                    apiDataByRequestId.getOrDefault(requestId, new LinkedHashMap<>());

            long reqTime = requestEntry.get("RequestTime") instanceof Long
                    ? (Long) requestEntry.get("RequestTime")
                    : System.currentTimeMillis();

            long responseTime = System.currentTimeMillis() - reqTime;

            Map<String, Object> finalEntry = new LinkedHashMap<>();

            String section = requestEntry.getOrDefault("Section", currentSection).toString();

            finalEntry.put("Section", section);
            finalEntry.put("Method", requestEntry.getOrDefault("Method", "UNKNOWN"));
            finalEntry.put("URL", url);
            finalEntry.put("Status", status);
            finalEntry.put("ResponseTimeMs", responseTime);

            sectionWiseData
                    .computeIfAbsent(section, s -> new ArrayList<>())
                    .add(finalEntry);

            apiDataByRequestId.put(requestId, finalEntry);
        });
    }

    public void printSectionSummary(String sectionName) {

        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        List<Map<String, Object>> entries =
                sectionWiseData.getOrDefault(sectionName, Collections.emptyList());

        for (Map<String, Object> entry : entries) {

            int status = Integer.parseInt(entry.get("Status").toString());
            String url = entry.get("URL").toString();

            String endpoint = url.replaceAll("https?://[^/]+", "");
            if (endpoint.isEmpty()) endpoint = "/";

            if (status == 200) success.add(endpoint);
            else failed.add(endpoint + " - " + status);
        }

        System.out.println("SUCCESS APIs:");
        success.forEach(System.out::println);

        System.out.println("\nFAILED APIs:");
        failed.forEach(System.out::println);
    }

    public Map<String, List<Map<String, Object>>> getSectionWiseData() {
        return sectionWiseData;
    }

    public Map<String, Map<String, Object>> getApiCapturedData() {
        return new LinkedHashMap<>(apiDataByRequestId);
    }
}