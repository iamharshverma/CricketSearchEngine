package hello;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonPropertyOrder({
        "responseHeader",
        "response"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyResponse {

    @JsonProperty("responseHeader")
    public ResponseHeader responseHeader;
    @JsonProperty("response")
    public Response response;

}
