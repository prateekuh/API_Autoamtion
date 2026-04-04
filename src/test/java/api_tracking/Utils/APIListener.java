package api_tracking.Utils;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v142.network.Network;
import org.openqa.selenium.devtools.v142.network.model.Request;
import org.openqa.selenium.devtools.v142.network.model.Response;

import java.util.*;

/**
 * APIListener captures requests/responses via CDP (DevTools),
 * computes response time and keeps section-wise lists for reporting.
 */
public class APIListener {

    // Temporary store keyed by requestId (so duplicate URLs are handled)
    private final Map<String, Map<String, Object>> apiDataByRequestId = new LinkedHashMap<>();

    // Final section-wise captured list (Section -> List of maps)
    private final Map<String, List<Map<String, Object>>> sectionWiseData = new LinkedHashMap<>();

    private String currentSection = "";

    public void startSection(String sectionName) {
        currentSection = sectionName;
        System.out.println("\n============================== ===== " + sectionName + " API STARTED ===== ==============================");
    }

    /**
     * endSection prints summary for the section (200 vs non-200)
     * but DOES NOT remove sectionWiseData (so final Excel can be generated)
     */
    public void endSection(String sectionName) {
        printSectionSummary(sectionName);

        System.out.println("\n============================== ===== " + sectionName + " API ENDED ===== ==============================\n");

        // Clear temporary map (old request entries) to free memory
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

        // REQUEST listener
        devTools.addListener(Network.requestWillBeSent(), requestEvent -> {
            Request req = requestEvent.getRequest();
            String requestId = requestEvent.getRequestId().toString();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("Section", currentSection);
            data.put("Method", req.getMethod());
            data.put("URL", req.getUrl());
            data.put("RequestTime", System.currentTimeMillis());

            // store by request id
            apiDataByRequestId.put(requestId, data);

            System.out.println("[REQUEST] " + req.getMethod() + " → " + req.getUrl());
        });

        // RESPONSE listener
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            Response resp = responseReceived.getResponse();
            String requestId = responseReceived.getRequestId().toString();
            String url = resp.getUrl();
            int status = resp.getStatus();

            // Print log (same format you already have)
            System.out.println("[RESPONSE] " + status + " ← " + url);

            // fetch request entry
            Map<String, Object> requestEntry = apiDataByRequestId.getOrDefault(requestId, new LinkedHashMap<>());

            // If no RequestTime available, still compute with best effort
            long reqTime = requestEntry.get("RequestTime") instanceof Long ? (Long) requestEntry.get("RequestTime") : System.currentTimeMillis();

            long responseTime = System.currentTimeMillis() - reqTime;

            // build final map
            Map<String, Object> finalEntry = new LinkedHashMap<>();
            // Section - prefer requestEntry section if available, otherwise currentSection
            String section = requestEntry.getOrDefault("Section", currentSection).toString();
            finalEntry.put("Section", section);
            finalEntry.put("Method", requestEntry.getOrDefault("Method", "UNKNOWN"));
            finalEntry.put("URL", url);
            finalEntry.put("Status", status);
            finalEntry.put("ResponseTimeMs", responseTime);

            // store in sectionWiseData
            sectionWiseData.computeIfAbsent(section, s -> new ArrayList<>()).add(finalEntry);

            // also replace/put into apiDataByRequestId for JSON dump if needed
            apiDataByRequestId.put(requestId, finalEntry);
        });
    }

    /**
     * Print the section summary showing 200 vs non-200 grouped (as requested)
     */
    public void printSectionSummary(String sectionName) {

        List<String> success200 = new ArrayList<>();
        List<String> failedOthers = new ArrayList<>();

        List<Map<String, Object>> entries = sectionWiseData.getOrDefault(sectionName, Collections.emptyList());

        for (Map<String, Object> entry : entries) {
            Object statusObj = entry.get("Status");
            Object urlObj = entry.get("URL");

            if (statusObj == null || urlObj == null) continue;

            int status = (statusObj instanceof Integer) ? (Integer) statusObj : Integer.parseInt(statusObj.toString());
            String url = urlObj.toString();

            // Extract endpoint only for console summary (remove protocol + host)
            String endpoint = url.replaceAll("https?://[^/]+", "");
            if (endpoint.trim().isEmpty()) endpoint = "/";

            if (status == 200) {
                success200.add(endpoint);
            } else {
                failedOthers.add(endpoint + " - " + status);
            }
        }

        System.out.println("=============== SUCCESS (200) APIs ===============");
        if (success200.isEmpty()) {
            System.out.println("(None)");
        } else {
            success200.forEach(System.out::println);
        }

        System.out.println("\n================ FAILED APIs =====================");
        if (failedOthers.isEmpty()) {
            System.out.println("(None)");
        } else {
            failedOthers.forEach(System.out::println);
        }
    }

    /**
     * Expose the collected section-wise data for reporting utilities
     *
     * Format:
     * Map<SectionName, List<Map<"Section","Method","URL","Status","ResponseTimeMs">>>
     */
    public Map<String, List<Map<String, Object>>> getSectionWiseData() {
        return sectionWiseData;
    }

    /**
     * Optional: export raw captured data as JSON-style map (requestId -> entry)
     */
    public Map<String, Map<String, Object>> getApiCapturedData() {
        return new LinkedHashMap<>(apiDataByRequestId);
    }
}
