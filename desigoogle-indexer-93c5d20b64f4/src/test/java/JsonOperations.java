//import org.json.JSONObject;
//import org.junit.Test;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class JsonOperations {
//    @Test
//    public void TestReadJson() {
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader("inp/file1"));
//            StringBuilder stringBuilder = new StringBuilder();
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line);
//                stringBuilder.append(System.lineSeparator());
//            }
//            String finalLine = stringBuilder.toString();
//            JSONObject jsonObject = new JSONObject(finalLine);
//            String docId = jsonObject.getString("docId");
//            String docHash = jsonObject.getString("docHash");
//            String docContent = jsonObject.getString("docContent");
//
//            assertTrue(jsonObject.has("docId"));
//            assertTrue(jsonObject.has("docHash"));
//            assertTrue(jsonObject.has("docContent"));
//            assertTrue(docId.equals("123"));
//            assertTrue(docHash.equals("abc"));
//            assertTrue(docContent.equals("hello hello good"));
//
//        } catch (FileNotFoundException fnfe) {
//            assertFalse("File Not Found", true);
//        } catch (IOException ioe) {
//            assertFalse("IOException occurred", true);
//        }
//    }
//}
