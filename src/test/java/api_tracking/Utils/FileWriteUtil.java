package api_tracking.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.json.simple.JSONObject;

public class FileWriteUtil {

    @SuppressWarnings("unchecked")
    public static void writeApiData(String fileName, Map<String, Map<String, Object>> apiData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(apiData);

        try (FileWriter file = new FileWriter(fileName)) {
            file.write(jsonObject.toJSONString());
            file.flush();
            System.out.println("API data written to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
