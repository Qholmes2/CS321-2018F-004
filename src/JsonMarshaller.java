import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonMarshaller {
	public static final JsonMarshaller MARSHALLER = new JsonMarshaller();
	private ObjectMapper jsonMapper;

	public JsonMarshaller() {
		this.jsonMapper = new ObjectMapper();
		this.jsonMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		this.jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.jsonMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		this.jsonMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
	}

	public <T> T unmarshal(String obj, Class<T> type) throws Exception {
		return this.unmarshal(obj.getBytes(), type);
	}

	public <T> T unmarshalFile(String fileName, Class<T> type) throws Exception {
		return this.unmarshal(Files.readAllBytes(Paths.get(fileName)), type);
	}

	public <T> T unmarshal(byte[] obj, Class<T> type) throws Exception {
		return jsonMapper.readValue(obj, type);
	}

	public String marshal(Object o) throws Exception {
		return jsonMapper.writeValueAsString(o);
	}

	public String marshalIndent(Object o) throws Exception {
		return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
	}

}
